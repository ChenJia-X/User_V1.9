
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static com.example.foolishfan.user_v10.Utils.showToast;

public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";
    private Context context = BluetoothLeService.this;

    public static int connectionState = STATE_DISCONNECTED;

    private List<Integer> mBuffer = new ArrayList<>();
    public ConnectedDeviceTask connectedDeviceTask;
    private static UpdateAngle updateAngle;


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

        public void setUpdateAngle(UpdateAngle updateAngle) {
            BluetoothLeService.this.updateAngle = updateAngle;
        }

        public void connect(BluetoothDevice device) {
            BluetoothLeService.this.connect(device);
        }

        public void write(String zhu) {
            double zhuDong = Double.parseDouble(zhu);
            if (isNearTarget(zhuDong, 0)) {
                ConnectedDeviceTask.command = "1";//12点
                ConnectedDeviceTask.write(new byte[]{Byte.parseByte(ConnectedDeviceTask.command)});
                return;
            } else if (isNearTarget(zhuDong, 90)) {
                ConnectedDeviceTask.command = "2";//3点
                ConnectedDeviceTask.write(new byte[]{Byte.parseByte(ConnectedDeviceTask.command)});
                return;
            } else if (isNearTarget(zhuDong, 180)) {
                ConnectedDeviceTask.command = "3";
                ConnectedDeviceTask.write(new byte[]{Byte.parseByte(ConnectedDeviceTask.command)});
                return;
            } else if (isNearTarget(zhuDong, -90)) {
                ConnectedDeviceTask.command = "4";
                ConnectedDeviceTask.write(new byte[]{Byte.parseByte(ConnectedDeviceTask.command)});
            } else {
                ((NewMeasure) updateAngle).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAngle.updateToast("角度不正确");
                    }
                });
            }
        }

        private boolean isNearTarget(double angle, double target) {
            return angle >= target - 5 && angle <= target + 5;
        }

        public double[] calculate() {
            return LaserAlignment.calculate();
        }

        public void cancel() {
            ConnectedDeviceTask.cancel();
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
    static class ConnectedDeviceTask extends AsyncTask<String, Integer, String> {
        private static BluetoothSocket mSocket;
        private InputStream mInStream;
        private static OutputStream mOutStream;
        private static boolean isContinue = true;
        static String command;

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
            //HC-06蓝牙的读数据方法
            BufferedReader reader = new BufferedReader(new InputStreamReader(mInStream));
            // Keep listening to the InputStream while connected
            while (isContinue) {
                try {
                    String string;
                    while ((string = reader.readLine()) != null) {
                        Log.d(TAG, "doInBackground: " + string);

                        TwoValue<Double, Double> twoValue = getValue(string);//解析数据

                        if (!string.contains("de")) {//先判断是否是偏差
                            if (string.contains("x")) {
                                //计算主动轴角度
                                //assert twoValue != null;
                                if (twoValue != null) {
                                    double angle = Math.atan2(twoValue.getSecond(), twoValue.getFirst()) * (180 / Math.PI);
                                    final String formatAngle = String.format("%.2f", angle);
                                    ((NewMeasure) updateAngle).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateAngle.updateZhuDong(formatAngle);
                                        }
                                    });
                                }
                            } else {
                                //计算从动轴角度
                                //assert twoValue != null;
                                if (twoValue != null) {
                                    double angle = Math.atan2(twoValue.getFirst(), twoValue.getSecond()) * (180 / Math.PI);
                                    final String formatAngle = String.format("%.2f", angle);
                                    ((NewMeasure) updateAngle).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateAngle.updateCongDong(formatAngle);
                                        }
                                    });
                                }
                            }
                        } else {
                            if (twoValue == null) {
                                return null;
                            }

                            LaserAlignment laserAlignment = new LaserAlignment();
                            if (ConnectedDeviceTask.command.equals("a")) {
                                laserAlignment.y12 = twoValue.getFirst();
                                laserAlignment.y_12 = twoValue.getSecond();
                            } else if (ConnectedDeviceTask.command.equals("b")) {
                                laserAlignment.y3 = twoValue.getFirst();
                                laserAlignment.y_3 = twoValue.getSecond();
                            } else if (ConnectedDeviceTask.command.equals("c")) {
                                laserAlignment.y6 = twoValue.getFirst();
                                laserAlignment.y_6 = twoValue.getSecond();
                            } else if (ConnectedDeviceTask.command.equals("d")) {
                                laserAlignment.y9 = twoValue.getFirst();
                                laserAlignment.y_9 = twoValue.getSecond();
                            }
                        }

                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
            return null;
        }

        private class TwoValue<T, U> {
            private T t;
            private U u;

            public TwoValue(T t, U u) {
                this.t = t;
                this.u = u;
            }

            public T getFirst() {
                return t;
            }

            public U getSecond() {
                return u;
            }
        }

        /**
         * 分别获取x、y的值
         *
         * @param string 格式为：x=2342.342,y=34342.123
         * @return 2个值
         */
        private TwoValue<Double, Double> getValue(String string) {
            Pattern splitTwoPart = Pattern.compile(",");
            String[] xy = splitTwoPart.split(string);//xy[0]为x部分;xy[1]为y部分

            if (xy.length != 2) {
                Log.d(TAG, "getValue1: " + Arrays.toString(xy));
                return null;
            }

            Pattern splitValue = Pattern.compile(":");
            String[] valueX = splitValue.split(xy[0]);//valueX[0]为x的值
            String[] valueY = splitValue.split(xy[1]);//valueX[0]为y的值

            if ((valueY.length != 2) || (valueX.length != 2)) {
                Log.d(TAG, "getValue2: " + Arrays.toString(valueX) + " //// " + Arrays.toString(valueY));
                return null;
            }

            Double x = Double.valueOf(valueX[1]);
            Double y = Double.valueOf(valueY[1]);
            return new TwoValue<>(x, y);
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
        public static void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public static void cancel() {
            isContinue = false;
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    static class LaserAlignment {
        protected static double a;
        protected static double b;
        protected static double c;

        protected static double y12;
        protected static double y_12;
        protected static double y3;
        protected static double y_3;
        protected static double y6;
        protected static double y_6;
        protected static double y9;
        protected static double y_9;

        protected static double[] calculate() {
            double xa = (y3 - y9) / 2;
            double ya = (y12 - y6) / 2;
            double xb = (y_3 - y_9) / 2;
            double yb = (y_12 - y_6) / 2;

            double ey = ya + ((c + b) * (yb - ya)) / c;
            double fy = ya + (a + b + c) * (yb - ya) / c;
            double ex = xa + ((c + b) * (xb - xa)) / c;
            double fx = xa + (a + b + c) * (xb - xa) / c;
            return new double[]{ey, fy, ex, fx};
        }
    }
}
