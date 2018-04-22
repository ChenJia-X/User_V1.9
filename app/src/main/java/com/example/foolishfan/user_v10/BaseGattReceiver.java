package com.example.foolishfan.user_v10;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import static com.example.foolishfan.user_v10.Utils.showToast;

public class BaseGattReceiver extends BroadcastReceiver {
    private static final String TAG = "BaseGattReceiver" + "--DEBUG";
    public static final int BASE_TYPE = 0;
    public static final int COMMUNICATE_TYPE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            BluetoothLeService.connectionState = BluetoothAdapter.STATE_CONNECTED;
            Log.d(TAG, "onReceive: connected");
            showToast(context, "Bluetooth connection is successful!");
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            BluetoothLeService.connectionState = BluetoothAdapter.STATE_DISCONNECTED;
            showToast(context, "Bluetooth connection failed!");
            Log.d(TAG, "onReceive: disconnected");
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            Log.d(TAG, "onReceive: discover services");
        } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
            Log.d(TAG, "onReceive: write callback data");
        }
    }

    public static IntentFilter getIntentFilter(int type) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        if (type == COMMUNICATE_TYPE) {
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        }
        return intentFilter;
    }
}
