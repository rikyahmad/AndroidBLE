package com.staygrateful.app.androidble.ble.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.staygrateful.app.androidble.util.StringUtils;
import com.staygrateful.app.androidble.util.Transform;

import java.util.ArrayList;
import java.util.List;

public class BleUtils {

    public static String CCCD_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static void printGattTable(BluetoothGatt gatt) {
        final List<BluetoothGattService> services = gatt.getServices();
        if (services.isEmpty()) {
            Log.d("BluetoothGatt", "No service and characteristic available, call discoverServices() first?");
            return;
        }
        for (BluetoothGattService service : services) {

            String characteristicsTable = StringUtils.joinToString(
                    service.getCharacteristics(),
                    "\n|--",
                    "|--",
                    "",
                    data -> {
                        String description = data.getUuid() + " : " + printProperties(data);
                        if (!data.getDescriptors().isEmpty()) {
                            description += "\n" + StringUtils.joinToString(
                                    data.getDescriptors(),
                                    "\n|------",
                                    "|------",
                                    "",
                                    descriptor -> descriptor.getUuid() + " : " + printProperties(descriptor)
                            );
                        }
                        return description;
                    }
            );
            Log.d("BluetoothGatt","Service " +
                    service.getUuid() +
                    "\nCharacteristics:" +
                    "\n" + characteristicsTable);
        }
    }

    public static String printProperties(BluetoothGattCharacteristic gattChar) {
        final List<String> result = new ArrayList<>();
        if (isReadable(gattChar)) {
            result.add("READABLE");
        }
        if (isWritable(gattChar)) {
            result.add("WRITABLE");
        }
        if (isWritableWithoutResponse(gattChar)) {
            result.add("WRITABLE WITHOUT RESPONSE");
        }
        if (isIndicatable(gattChar)) {
            result.add("INDICATABLE");
        }
        if (isNotifiable(gattChar)) {
            result.add("NOTIFIABLE");
        }
        if (result.isEmpty()) {
            result.add("EMPTY");
        }
        return String.join(", ", result);
    }

    public static String printProperties(BluetoothGattDescriptor descriptor) {
        final List<String> result = new ArrayList<>();
        if (isReadable(descriptor)) {
            result.add("READABLE");
        }
        if (isWritable(descriptor)) {
            result.add("WRITABLE");
        }
        if (result.isEmpty()) {
            result.add("EMPTY");
        }
        return String.join(", ", result);
    }

    public static Boolean isReadable(BluetoothGattDescriptor descriptor) {
        return containsPermission(descriptor, BluetoothGattCharacteristic.PROPERTY_READ);
    }

    public static Boolean isWritable(BluetoothGattDescriptor descriptor) {
        return containsPermission(descriptor, BluetoothGattCharacteristic.PROPERTY_WRITE);
    }

    public static Boolean isReadable(BluetoothGattCharacteristic gattChar) {
        return containsProperty(gattChar, BluetoothGattCharacteristic.PROPERTY_READ);
    }

    public static Boolean isWritable(BluetoothGattCharacteristic gattChar) {
        return containsProperty(gattChar, BluetoothGattCharacteristic.PROPERTY_WRITE);
    }

    public static Boolean isWritableWithoutResponse(BluetoothGattCharacteristic gattChar) {
        return containsProperty(gattChar, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
    }

    public static Boolean isIndicatable(BluetoothGattCharacteristic gattChar) {
        return containsProperty(gattChar, BluetoothGattCharacteristic.PROPERTY_INDICATE);
    }

    public static Boolean isNotifiable(BluetoothGattCharacteristic gattChar) {
        return containsProperty(gattChar, BluetoothGattCharacteristic.PROPERTY_NOTIFY);
    }

    public static Boolean containsProperty(BluetoothGattCharacteristic gattChar, int property) {
        return gattChar.getProperties() != 0 && property != 0;
    }

    public static Boolean containsPermission(BluetoothGattDescriptor descriptor, int permission) {
        return descriptor.getPermissions() != 0 && permission != 0;
    }
}
