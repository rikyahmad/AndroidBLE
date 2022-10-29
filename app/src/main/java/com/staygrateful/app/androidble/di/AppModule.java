package com.staygrateful.app.androidble.di;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.staygrateful.app.androidble.ble.*;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static BluetoothAdapter provideBluetoothAdapter(@ApplicationContext Context context) {
        final BluetoothManager manager = (BluetoothManager)
                context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter();
    }


    @Provides
    @Singleton
    public static TemperatureAndHumidityReceiveManager provideTempHumidityReceiveManager(
            @ApplicationContext Context context,
            BluetoothAdapter bluetoothAdapter
    ) {
        return new TemperatureAndHumidityBLEReceiveManager(bluetoothAdapter, context);
    }
}
