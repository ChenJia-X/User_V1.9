package com.example.foolishfan.user_v10;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static com.example.foolishfan.user_v10.Utils.showToast;


public class NewMeasure extends AppCompatActivity {

    //Bluetooth
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private LeDeviceListAdapter leDeviceListAdapter;
    private BluetoothLeService.BluetoothServiceBinder bluetoothServiceBinder;
    private AlertDialog dialog;
    private static final int REQUEST_ENABLE_BT = 1;

    //Permission
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 101;

    private BroadcastReceiver baseGattReceiver = new BaseGattReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    leDeviceListAdapter.addDevice(device);
                    leDeviceListAdapter.notifyDataSetChanged();
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast(context, "蓝牙扫描关闭");
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                BluetoothLeService.connectionState = BluetoothAdapter.STATE_CONNECTED;
                showToast(context, "蓝牙连接成功");
                cancelDiscovery();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothServiceBinder = (BluetoothLeService.BluetoothServiceBinder) service;
            bluetoothServiceBinder.setBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothServiceBinder.setBound(false);
            bluetoothServiceBinder = null;
        }
    };

    private Context context = NewMeasure.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_measure);
        Button bluetoothConnect = (Button) findViewById(R.id.lianjie);
        Button scanQRCode = (Button) findViewById(R.id.scan_qrcode);
        Button measure = (Button) findViewById(R.id.measure);
        Button calculate = (Button) findViewById(R.id.calculate);
        //给button按钮设置一个点击事件
        bluetoothConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BluetoothLeService.class);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                prepareForScan();
            }
        });
        scanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openScanQRCode = new Intent(context, ScanQRCodeActivity.class);
                startActivity(openScanQRCode);
            }
        });

        registerReceiver(baseGattReceiver,
                BaseGattReceiver.getIntentFilter(BaseGattReceiver.COMMUNICATE_TYPE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothServiceBinder != null && bluetoothServiceBinder.isBound()) {
            unbindService(serviceConnection);
        }
        unregisterReceiver(baseGattReceiver);
    }


    /**
     * ------------------------------获取权限----------------------------------------------------
     */

    /**
     * 检测Bluetooth 和 Location的开关是否打开；
     * 检测是否需要运行时权限；
     */
    private void prepareForScan() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a mScanList asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            showDialogForEnable(context, R.string.dialog_goto_settings_bluetooth,
                    BluetoothAdapter.ACTION_REQUEST_ENABLE, REQUEST_ENABLE_BT);
            return;//必须加这一句，否则下一个判断会覆盖这个判断，会先出现下一个的dialog窗口。
        }
        //Android6.0以上需要动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                showScanListDialog();
            } else {
                askForRunTimePermission();
            }
        } else {
            showScanListDialog();
        }
    }

    /**
     * 打开对应action的页面
     *
     * @param context
     * @param message
     * @param action
     * @param requestCode
     */
    private void showDialogForEnable(Context context, int message, final String action, final int requestCode) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //进入设置页面开启action
                        Intent enableLocate = new Intent(action);
                        startActivityForResult(enableLocate, requestCode);
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 运行时权限
     */
    private void askForRunTimePermission() {
        ActivityCompat.requestPermissions(NewMeasure.this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    prepareForScan();
                } else {
                    Toast.makeText(context, "打开失败，请手动打开蓝牙开关", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareForScan();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        //用户勾选不再询问按钮
                        // 解释原因，并且引导用户至设置页手动授权
                        new AlertDialog.Builder(this)
                                .setMessage("需要开启权限，否则无法使用。请到权限管理页面手动给予权限")
                                .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //引导用户至设置页手动授权
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //引导用户手动授权，权限请求失败
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        //引导用户手动授权，权限请求失败
                                    }
                                })
                                .show();
                    } else {
                        prepareForScan();
                    }
                }
            }
        }
    }



    /**
     * -------------------------------扫描-------------------------------------------------------
     */

    /**
     * 显示蓝牙搜索列表
     */
    private void showScanListDialog() {
        View scanView = getLayoutInflater().inflate(R.layout.dialog_scan_list,
                (ViewGroup) findViewById(R.id.dialog_scan_list));
        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_scan_title)
                .setView(scanView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelDiscovery();
                    }
                })
                .setCancelable(true)
                .show();
        ListView mScanLV = (ListView) scanView.findViewById(R.id.scan_lv);
        // Initializes list view adapter.
        leDeviceListAdapter = new LeDeviceListAdapter(context, R.layout.scan_lv_item);
        mScanLV.setAdapter(leDeviceListAdapter);
        mScanLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothServiceBinder.connect(leDeviceListAdapter.getDevice(position));
            }
        });

        //在执行设备发现之前，有必要查询已配对的设备集，以了解所需的设备是否处于已知状态。
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                leDeviceListAdapter.addDevice(device);
            }
            leDeviceListAdapter.notifyDataSetChanged();
        }

        scanClassicDevice();
    }

    /**
     * Adapter for holding devices found through scanning.
     */
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private Context mContext;
        private int mResourceId;

        LeDeviceListAdapter(Context context, int resourceId) {
            super();
            mLeDevices = new ArrayList<>();
            mContext = context;
            mResourceId = resourceId;
        }

        void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(mResourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            //set data for views
            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }

        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    }

    private void scanClassicDevice() {
        // If we're already discovering, stop it
        cancelDiscovery();
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    private void cancelDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }
}
