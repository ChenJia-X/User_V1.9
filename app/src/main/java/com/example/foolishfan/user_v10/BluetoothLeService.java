
package com.example.foolishfan.user_v10;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

/*public class BluetoothLeService extends AppCompatActivity {
    private Switch aSwitch;
    private Button buttonVisible,buttonSearch,buttonExit;
    private ListView listView;

    private Handler handler;

    private BluetoothAdapter bluetoothAdapter;

    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private UUID uuid;

    private List<String> listDevice = new ArrayList<>();
    private ArrayAdapter<String> adapterDevice;

    private static BluetoothSocket socket;
    public static BluetoothSocket bluetoothSocket;

    private static AcceptThread acceptThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lanya);
        findViewById();
        OnClick();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        InitBlueTooth();
    }
    private void OnClick() {
        //Switch开关
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        return;
                    } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        bluetoothAdapter.enable();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                            Toast.makeText(BluetoothLeService.this, "蓝牙已打开", Toast.LENGTH_SHORT).show();
                        }
                        *//***********修改修改**********//*
                        try {
                            acceptThread = new AcceptThread();
                            handler = new Handler() {
                                public void handle(Message message) {
                                    switch (message.what) {
                                        case 0:
                                            acceptThread.start();
                                    }

                                }

                            };
                        } catch (Exception e) {
                            Toast.makeText(BluetoothLeService.this, "服务监听出错", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else if (isChecked = false) {
                    bluetoothAdapter.disable();
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF)
                        Toast.makeText(BluetoothLeService.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //可见性Button
        buttonVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        });
        //搜索设备
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("本机蓝牙地址" + bluetoothAdapter.getAddress());
                listDevice.clear();
                bluetoothAdapter.startDiscovery();//启动搜索其他蓝牙设备
            }
        });
        //退出Button
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothSocket != null)
                {
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    BluetoothLeService.this.finish();
                }
            }
        });
    }

    *//************************************************************************************************************//*
    private void InitBlueTooth() {
        Toast.makeText(BluetoothLeService.this,"正在初始化软件，请稍等...",Toast.LENGTH_SHORT).show();

        *//*if(bluetoothAdapter == null)
        {
            Toast.makeText(MainActivity.this,"您的蓝牙不可用，请查验您的手机是否具有此功能",Toast.LENGTH_LONG).show();
            return;
        }
        else
        {*//*
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF)
        {
            aSwitch.setChecked(false);
            bluetoothAdapter.enable();
            if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            {
                Toast.makeText(BluetoothLeService.this,"成功帮您打开蓝牙",Toast.LENGTH_LONG).show();
            }else
            {
                Toast.makeText(BluetoothLeService.this,"打开蓝牙失败，请手动打开您的蓝牙",Toast.LENGTH_LONG).show();
            }


        }else if(bluetoothAdapter.getState() == bluetoothAdapter.STATE_ON)
        {
            acceptThread = new AcceptThread();

            handler = new Handler()
            {
                public void handleMessage(Message msg)
                {
                    switch (msg.what)
                    {
                        case 0:
                            acceptThread.start();
                            break;
                    }

                }

            };

        }
        //注册Receiver蓝牙设备的相关结果
        //IntentFilter结构化的描述意图值匹配。IntentFilter可以匹配操作、类别和数据(通过其类型、计划和/或路径)的意图。它还包括一个“优先级”值用于订单多个匹配过滤器。
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//广播操作：指示远程设备上的键状态的改变
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//广播操作：指示本地适配器的蓝牙扫描模式已经改变。
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//广播操作：当地蓝牙适配器的状态已更改。
        registerReceiver(serachDevices,intentFilter);
        // }
    }

    *//**************************************************************************************************************//*
    private void findViewById() {
        aSwitch = (Switch)findViewById(R.id.switchOpenAndClose);
        buttonVisible = (Button)findViewById(R.id.buttonVisible);
        buttonSearch = (Button)findViewById(R.id.buttonSeraveDevice);
        buttonExit = (Button)findViewById(R.id.buttonExit);
        listView = (ListView)findViewById(R.id.listViewShow);
        adapterDevice = new ArrayAdapter<String>(BluetoothLeService.this,android.R.layout.simple_list_item_1,listDevice);
        listView.setAdapter(adapterDevice);
        listView.setOnItemClickListener(new ItemClickEvent());//设置ListView 的单击监听
    }

    *//*************************************************************************************************************//*
    class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;//蓝牙服务套接口
        public AcceptThread()
        {
            BluetoothServerSocket BTServerSocket = null;
            try {
                Method listenMethod = bluetoothAdapter.getClass().getMethod("listenUsingRfcommOn",new  Class[]{int.class});
                BTServerSocket = (BluetoothServerSocket)listenMethod.invoke(bluetoothAdapter,Integer.valueOf(1));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            serverSocket = BTServerSocket;
        }
        public void run()
        {
            while (true)
            {
                try {
                    socket = serverSocket.accept();
                    Log.e("MainActivity","socket = serverSocket.accept();错误");
                } catch (IOException e) {
                    break;
                }
                if(socket != null)
                {
                    manageConnectedSocket();
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);

        }

        public void cancel()
        {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothLeService","注销错误");
            }
        }
    }

    *//*************************************************************************************************************//*
    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String str = listDevice.get(position);
            String[] values = str.split("\\|");
            String address = values[1];
            Log.i("address",values[1]);
            uuid = UUID.fromString(SPP_UUID);
            Log.i("uuid",uuid.toString());

            BluetoothDevice BtDev = bluetoothAdapter.getRemoteDevice(address);//getRemoteDevice获取BluetoothDevice对象从给定的蓝牙硬件地址
            Log.i("获取的蓝牙硬件对象","   "+BtDev);
            Method method;
            try {
                method = BtDev.getClass().getMethod("createRfcommSocket",new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(BtDev,Integer.valueOf(1));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            bluetoothAdapter.cancelDiscovery();//对蓝牙进行信号注销
            try {
                bluetoothSocket.connect();//蓝牙配对
                Toast.makeText(BluetoothLeService.this,"连接成功进入测量界面",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setClass(BluetoothLeService.this, NewMeasure.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(BluetoothLeService.this,"连接失败",Toast.LENGTH_SHORT).show();
            }

        }
    }

    *//**
 * 基类代码,将收到发送的意图sendBroadcast()。
 *//*
    private BroadcastReceiver serachDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            Object[] listName = bundle.keySet().toArray();

            //显示收到的消息及细节
            for(int i = 0;i < listName.length;i++)
            {
                String keyName = listName[i].toString();
                Log.i(keyName,String.valueOf(bundle.get(keyName)));
            }
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice BTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String str = BTDevice.getName()+"|"+BTDevice.getAddress();
                if(listDevice.indexOf(str) == -1)//indexOf检索字符串，如果为null返回-1
                {
                    listDevice.add(str);
                }
                adapterDevice.notifyDataSetChanged();//每次改变刷新一下自己
            }

        }
    };
    *//*************************************************************************************************************//*
    @Override
    protected void onDestroy() {
        this.unregisterReceiver(serachDevices);
        super.onDestroy();
        android.os.Process.killProcess(Process.myPid());//杀死Pid
        acceptThread.cancel();
        acceptThread.destroy();
    }

    */

/*************************************************************************************************************//*
    private void manageConnectedSocket()
    {
        bluetoothSocket = socket;
        Intent newActivity = new Intent();
        newActivity.setClass(BluetoothLeService.this,NewMeasure.class);
        startActivity(newActivity);
    }

}*/

public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";

    //下面的包路径是GoogleSample中的，这些Action只是用来比对的，内容不影响功能，所以就不改了。
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE = "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    /**
     * SBP服务
     */

    public static String SBP_SERVICE = "0000FF00-0000-1000-8000-00805f9b34fb";
    public static String SBP_CHARACTERISTIC_WRITE = "0000FF01-0000-1000-8000-00805f9b34fb";
    public static String SBP_CHARACTERISTIC_NOTIFY = "0000FF02-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_SBP_SERVICE = UUID.fromString(SBP_SERVICE);
    public final static UUID UUID_SBP_CHARACTERISTIC_WRITE = UUID.fromString(SBP_CHARACTERISTIC_WRITE);
    public final static UUID UUID_SBP_CHARACTERISTIC__NOTIFY = UUID.fromString(SBP_CHARACTERISTIC_NOTIFY);

    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static int connectionState = STATE_DISCONNECTED;

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String bluetoothDeviceAddress;


    private LocalBinder localBinder = new LocalBinder();

    class LocalBinder extends Binder {
        BluetoothLeService getBluetoothService() {
            return BluetoothLeService.this;
        }

        void sendData(String sendString) {
            BluetoothLeService.this.sendData(sendString);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG, "onConnectionStateChange: Connected to GATT server.");
                Log.d(TAG, "Attempting to start service discovery: "
                        + bluetoothGatt.discoverServices());
            } else if (newState == STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                Log.w(TAG, "onConnectionStateChange: Disconnected from GATT server.");
                //showToast(context,"Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: success");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite: write ok");
                broadcastUpdate(ACTION_DATA_WRITE);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (UUID_SBP_CHARACTERISTIC__NOTIFY.equals(characteristic.getUuid())) {
                if (characteristic.getValue().length != 0) {
                    Log.d(TAG, "onCharacteristicChanged: rec= " + Arrays.toString(characteristic.getValue()));
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            } else {
                Log.w(TAG, "onCharacteristicChanged: ERROR");
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_SBP_CHARACTERISTIC__NOTIFY.equals(characteristic.getUuid())) {
            if (ACTION_DATA_AVAILABLE.equals(action)) {
                intent.putExtra(EXTRA_DATA, characteristic.getValue());
            }
        }
        sendBroadcast(intent);
    }

    /**
     * --------------------------------------以下为与硬件的蓝牙通讯-----------------------------------------
     */

    /**
     * 通过点击扫描列表中的蓝牙进行连接
     *
     * @param device
     */
    public void connect(BluetoothDevice device) {
        //第二个参数表示断了之后自动重连
        if (device != null) {
            bluetoothGatt = device.connectGatt(this, true, gattCallback);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * setCharacteristicNotification
     *
     * @param on
     */
    private void startReceive(boolean on) {
        BluetoothGattCharacteristic characteristic = bluetoothGatt
                .getService(BluetoothLeService.UUID_SBP_SERVICE)
                .getCharacteristic(BluetoothLeService.UUID_SBP_CHARACTERISTIC__NOTIFY);
        final int charaProp = characteristic.getProperties();
        final UUID uuid = characteristic.getUuid();
        Log.i("startReceive", "receiver uuid = " + uuid);

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            if (on) {
                setCharacteristicNotification(characteristic, true);
            } else {
                setCharacteristicNotification(characteristic, false);
            }
        }
    }

    /**
     * 在characteristic中写数据，其实就是发送数据
     *
     * @param characteristic characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "writeCharacteristic: write char === " + characteristic.getUuid()
                + " value===" + Arrays.toString(characteristic.getValue()));
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 发送命令
     *
     * @param sendString 命令
     */
    public void sendData(String sendString) {
        BluetoothGattCharacteristic characteristic = bluetoothGatt
                .getService(BluetoothLeService.UUID_SBP_SERVICE)
                .getCharacteristic(BluetoothLeService.UUID_SBP_CHARACTERISTIC_WRITE);
        final int charaProp = characteristic.getProperties();
        final UUID uuid = characteristic.getUuid();
        Log.d("sendData", "sender uuid = " + uuid);

        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            characteristic.setValue(sendString);
            characteristic.setWriteType(characteristic.getWriteType());
            Log.d("sendData", "write characteristic value " + sendString);
            startReceive(true);
            writeCharacteristic(characteristic);
        }
    }

}
