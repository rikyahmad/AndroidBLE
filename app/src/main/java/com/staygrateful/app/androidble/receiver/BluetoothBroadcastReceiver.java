package com.staygrateful.app.androidble.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    final EventState eventState;

    public BluetoothBroadcastReceiver(EventState eventState) {
        this.eventState = eventState;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if(action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            eventState.onBluetoothStateChanged(intent);
        }
    }

    public static void register(Context context, BluetoothBroadcastReceiver receiver) {
        final IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver, intentFilter);
    }

    public static void unregister(Context context, BluetoothBroadcastReceiver receiver) {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface EventState {
        void onBluetoothStateChanged(Intent intent);
    }
}
