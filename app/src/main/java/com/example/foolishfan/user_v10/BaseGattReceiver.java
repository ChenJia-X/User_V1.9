package com.example.foolishfan.user_v10;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BaseGattReceiver extends BroadcastReceiver {
    private static final String TAG = "BaseGattReceiver" + "--DEBUG";
    public static final int BASE_TYPE = 0;
    public static final int COMMUNICATE_TYPE = 1;

    public static final String UPDATE_UI = "com.foolish.ui";

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public static IntentFilter getIntentFilter(int type) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(UPDATE_UI);
        if (type == COMMUNICATE_TYPE) {

        }
        return intentFilter;
    }
}
