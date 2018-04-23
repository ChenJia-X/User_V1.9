package com.example.foolishfan.user_v10;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.foolishfan.user_v10.Utils.showToast;


public class NewMeasure extends AppCompatActivity {

    //Bluetooth
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private LeDeviceListAdapter leDeviceListAdapter;
    private BluetoothLeService.BluetoothServiceBinder bluetoothServiceBinder;
    private static final int REQUEST_ENABLE_BT = 1;
    private boolean mScanning;
    private Handler handler = new Handler();
    //Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    //Permission
    private static final int REQUEST_LOCATION = 101;
    private static final int REQUEST_SETTINGS_LOCATION = 100;

    private LocalBroadcastManager localBroadcastManager;
    private BaseGattReceiver baseGattReceiver = new BaseGattReceiver();

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
                Intent intent1 = new Intent(NewMeasure.this, BluetoothLeService.class);
                startService(intent1);
                prepareForScan();

            }
        });
        scanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openScanQRCode = new Intent(NewMeasure.this, ScanQRCodeActivity.class);
                startActivity(openScanQRCode);
            }
        });

        Intent intent = new Intent(this, BluetoothLeService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(baseGattReceiver,
                BaseGattReceiver.getIntentFilter(BaseGattReceiver.COMMUNICATE_TYPE));

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothServiceBinder != null && bluetoothServiceBinder.isBound()) {
            unbindService(serviceConnection);
        }
        localBroadcastManager.unregisterReceiver(baseGattReceiver);
    }

    /**
     * ------------------------------获取权限----------------------------------------------------
     */

    /**
     * 检测Bluetooth 和 Location的开关是否打开；
     * 检测是否需要运行时权限；
     */
    private void prepareForScan() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(context, R.string.ble_not_supported);
            finish();
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a mScanList asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            showDialogForEnable(context, R.string.dialog_goto_settings_bluetooth,
                    BluetoothAdapter.ACTION_REQUEST_ENABLE, REQUEST_ENABLE_BT);
            return;//必须加这一句，否则下一个判断会覆盖这个判断，会先出现下一个的dialog窗口。
        }
        //判断Location的是否开启
        if (!isLocationOpen(context)) {
            showDialogForEnable(context, R.string.dialog_goto_settings_location,
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS, REQUEST_SETTINGS_LOCATION);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                //Android6.0需要动态申请权限
                askForBLEPermission();
            } else {
                showScanListDialog();
            }
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
     * 判断位置信息是否开启
     *
     * @param context
     * @return
     */
    private static boolean isLocationOpen(final Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetWorkProvider;
    }

    /**
     * 运行时权限
     */
    private void askForBLEPermission() {
        AndPermission.with(NewMeasure.this)
                .requestCode(REQUEST_LOCATION)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
                        //AndPermission.rationaleDialog(MainActivity.this,rationale).show();
                        new AlertDialog.Builder(context)
                                .setTitle("Tips")
                                .setMessage(R.string.dialog_permission_location)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rationale.resume();
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rationale.cancel();
                                    }
                                }).show();
                    }
                })
                .callback(listener)
                .start();
    }

    /**
     * AndPermission的Callback
     */
    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            if (requestCode == REQUEST_LOCATION) {
                showScanListDialog();
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            if (requestCode == REQUEST_LOCATION) {
                if (AndPermission.hasAlwaysDeniedPermission(NewMeasure.this, deniedPermissions)) {
                    AndPermission.defaultSettingDialog(NewMeasure.this, 400)
                            .setMessage(R.string.dialog_permission_goto_app_permissions)
                            .show();
                }
            }
        }
    };


    /**
     * -------------------------------扫描-------------------------------------------------------
     */

    /**
     * 显示蓝牙搜索列表
     */
    private void showScanListDialog() {
        View mScanList = getLayoutInflater().inflate(R.layout.dialog_scan_list,
                (ViewGroup) findViewById(R.id.dialog_scan_list));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_scan_title)
                .setView(mScanList)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
        ListView mScanLV = (ListView) mScanList.findViewById(R.id.scan_lv);
        // Initializes list view adapter.
        leDeviceListAdapter = new LeDeviceListAdapter(context, R.layout.scan_lv_item);
        mScanLV.setAdapter(leDeviceListAdapter);
        mScanLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothServiceBinder.connect(leDeviceListAdapter.getDevice(position));
                dialog.dismiss();
            }
        });
        scanLeDevice(true);
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

    /**
     * scan BLE devices
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * Device scan callback.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leDeviceListAdapter.addDevice(device);
                            leDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

}
