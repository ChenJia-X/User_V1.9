
package com.example.foolishfan.user_v10;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static com.example.foolishfan.user_v10.Utils.showToast;

public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";
    private Context context = BluetoothLeService.this;

    public static int connectionState = STATE_DISCONNECTED;

    private List<Integer> mBuffer = new ArrayList<>();
    public ConnectedDeviceTask connectedDeviceTask;


    private BluetoothServiceBinder bluetoothServiceBinder = new BluetoothServiceBinder();

    class BluetoothServiceBinder extends Binder {
        private boolean isBound = false;

        public void setBound(boolean bound) {
            isBound = bound;
        }

        /**
         * Before call unbindService()，you must call this method to prevent
         * "java.lang.IllegalArgumentException: Service not registered"
         *
         * @return
         */
        public boolean isBound() {
            return isBound;
        }

        public void connect(BluetoothDevice device) {
            BluetoothLeService.this.connect(device);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return bluetoothServiceBinder;
    }



    /**
     * --------------------------------------以下为与硬件的蓝牙通讯-----------------------------------------
     */

    /**
     * 通过点击扫描列表中的蓝牙进行连接
     *
     * @param device
     */
    private void connect(BluetoothDevice device) {
        //第二个参数表示断了之后自动重连
        if (device != null) {
            ConnectDeviceTask connectDeviceTask = new ConnectDeviceTask();
            connectDeviceTask.setmDevice(device);
            connectDeviceTask.execute();
        }
    }

    /**
     *  连接HC-06
     */
    class ConnectDeviceTask extends AsyncTask<String, Integer, String> {
        private BluetoothSocket mSocket;
        private BluetoothDevice mDevice;
        private final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        private final UUID UUID_SPP = UUID.fromString(SPP_UUID);
        private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        void setmDevice(BluetoothDevice device) {
            this.mDevice = device;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = mDevice.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (IOException e) {
                Log.e(TAG, "BluetoothSocket connect failed!", e);
            }
            mSocket = tmp;
        }

        @Override
        protected String doInBackground(String... strings) {
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "unable to connect() socket", e);
                // Close the socket
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
            }
            connectedDeviceTask = new ConnectedDeviceTask(mSocket);
            connectedDeviceTask.execute();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showToast(context, "Socket连接成功");
            Intent intent = new Intent();
            intent.setAction(BaseGattReceiver.UPDATE_UI);
            sendBroadcast(intent);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对HC-06进行读写操作
     */
    class ConnectedDeviceTask extends AsyncTask<String, Integer, String> {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        ConnectedDeviceTask(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    //HC-06蓝牙的读数据方法
                    bytes = mInStream.read(buffer);
                    Log.d(TAG, new String(buffer));

                    synchronized (mBuffer) {
                        for (int i = 0; i < bytes; i++) {
                            mBuffer.add(buffer[i] & 0xFF);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */

        // HC-06蓝牙的写数据方法
        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
