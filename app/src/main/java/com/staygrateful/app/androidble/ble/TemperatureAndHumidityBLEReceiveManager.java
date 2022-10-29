package com.staygrateful.app.androidble.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.staygrateful.app.androidble.ble.model.Resource;
import com.staygrateful.app.androidble.ble.model.TempHumidityResult;
import com.staygrateful.app.androidble.ble.util.BleUtils;
import com.staygrateful.app.androidble.ble.util.TemperatureListener;
import com.staygrateful.app.androidble.util.ListUtils;
import com.staygrateful.app.androidble.util.NumberUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

@SuppressLint("MissingPermission")
public class TemperatureAndHumidityBLEReceiveManager implements TemperatureAndHumidityReceiveManager {

    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    private static final String DEVICE_NAME = "Neocortex BLE";
    private static final String TEMP_HUMIDITY_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String TEMP_HUMIDITY_CHARACTERISTICS_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private static final int MAXIMUM_CONNECTION_ATTEMPTS = 5;

    private TemperatureListener temperatureListener;

    private BluetoothLeScanner _scanner;

    private BluetoothGatt gatt = null;

    private boolean isScanning = false;

    private int currentConnectionAttempt = 1;

    public synchronized BluetoothLeScanner bleScanner() {
        if (_scanner == null) {
            _scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        return _scanner;
    }

    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();

    @Inject
    public TemperatureAndHumidityBLEReceiveManager(BluetoothAdapter bluetoothAdapter, Context context) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;
    }

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        @RequiresApi(Build.VERSION_CODES.M)
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null) {
                return;
            }
            final String deviceName = result.getDevice().getName();
            if (deviceName != null && deviceName.equals(DEVICE_NAME)) {
                if (temperatureListener != null) {
                    temperatureListener.onEmit(new Resource.Loading<>("Connecting to device..."));
                }
                if (isScanning) {
                    result.getDevice().connectGatt(
                            context, false, gattCallback, BluetoothDevice.TRANSPORT_LE
                    );
                    isScanning = false;
                    bleScanner().stopScan(this);
                }
            }
        }
    };


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (temperatureListener != null) {
                        temperatureListener.onEmit(new Resource.Loading<>("Discovering Services..."));
                    }
                    gatt.discoverServices();
                    TemperatureAndHumidityBLEReceiveManager.this.gatt = gatt;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (temperatureListener != null) {
                        temperatureListener.onEmit(new Resource.Success<>(
                                new TempHumidityResult(0f, 0f, ConnectionState.Disconnected)));
                    }
                    gatt.close();
                }
            } else {
                gatt.close();
                currentConnectionAttempt += 1;
                if (temperatureListener != null) {
                    temperatureListener.onEmit(new Resource.Loading<>("Attempting to connect " +
                            + currentConnectionAttempt + "/" + MAXIMUM_CONNECTION_ATTEMPTS));
                }
                if (currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPTS) {
                    startReceiving();
                } else {
                    if (temperatureListener != null) {
                        temperatureListener.onEmit(new Resource.Error<>("Could not connect to ble device"));
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BleUtils.printGattTable(gatt);
            if (temperatureListener != null) {
                temperatureListener.onEmit(new Resource.Loading<>("Adjusting MTU space..."));
            }
            gatt.requestMtu(517);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            final BluetoothGattCharacteristic characteristic = findCharacteristics(
                    TEMP_HUMIDITY_SERVICE_UUID, TEMP_HUMIDITY_CHARACTERISTICS_UUID
            );
            if (characteristic == null) {
                if (temperatureListener != null) {
                    temperatureListener.onEmit(new Resource.Error<>("Could not find temp and humidity publisher"));
                }
                return;
            }
            enableNotification(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final UUID uuid = characteristic.getUuid();
            if (uuid == UUID.fromString(TEMP_HUMIDITY_CHARACTERISTICS_UUID)) {
                byte[] messageBytes = characteristic.getValue();
                String messageString = new String(messageBytes, StandardCharsets.UTF_8);
                Float temperature = NumberUtils.parseFloat(messageString);

                if (temperature != null) {
                    int humidity = NumberUtils.parseInt(characteristic.getValue()[2]);
                    Log.d("Hasil", messageString);
                    final TempHumidityResult tempHumidityResult = new TempHumidityResult(
                            temperature,
                            humidity,
                            ConnectionState.Connected
                    );
                    if (temperatureListener != null) {
                        temperatureListener.onEmit(new Resource.Success<>(tempHumidityResult));
                    }
                }
            }
        }
    };

    private void enableNotification(BluetoothGattCharacteristic characteristic) {
        UUID cccdUuid = UUID.fromString(BleUtils.CCCD_DESCRIPTOR_UUID);
        byte[] payload;

        if (BleUtils.isIndicatable(characteristic)) {
            payload = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else if (BleUtils.isNotifiable(characteristic)) {
            payload = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            return;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUuid);
        if (descriptor != null) {
            if (gatt == null) {
                return;
            }
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.d("BLEReceiveManager", "set characteristics notification failed");
                return;
            }
            writeDescription(descriptor, payload);
        }
    }

    private void writeDescription(BluetoothGattDescriptor descriptor, byte[] payload) {
        if (gatt == null) {
            throw new IllegalStateException("Not connected to a BLE device!");
        }
        boolean setResult = descriptor.setValue(payload);
        boolean writeResult = gatt.writeDescriptor(descriptor);
        if (!setResult && !writeResult) {
            throw new IllegalStateException("Not connected to a BLE device!");
        }
    }

    private BluetoothGattCharacteristic findCharacteristics(
            String serviceUUID, String characteristicsUUID
    ) {
        if (gatt == null) {
            return null;
        }
        final BluetoothGattService data = ListUtils.find(gatt.getServices(),
                service -> service.getUuid().toString().equals(serviceUUID));
        if (data != null) {
            return ListUtils.find(data.getCharacteristics(),
                    characteristics -> characteristics.getUuid().toString().equals(characteristicsUUID));
        }
        return null;
    }

    @Override
    public void setTemperatureListener(TemperatureListener temperatureListener) {
        this.temperatureListener = temperatureListener;
    }

    @Override
    public void reconnect() {
        if (gatt != null) {
            gatt.connect();
        }
    }

    @Override
    public void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    @Override
    public void startReceiving() {
        if (temperatureListener != null) {
            temperatureListener.onEmit(new Resource.Loading<>("Scanning Ble devices..."));
        }
        isScanning = true;
        bleScanner().startScan(null, scanSettings, scanCallback);
    }

    @Override
    public void closeConnection() {
        bleScanner().stopScan(scanCallback);
        BluetoothGattCharacteristic characteristic = findCharacteristics(
                TEMP_HUMIDITY_SERVICE_UUID, TEMP_HUMIDITY_CHARACTERISTICS_UUID
        );
        if(characteristic != null){
            disconnectCharacteristic(characteristic);
        }
        if (gatt != null) {
            gatt.close();
        }
    }

    private void disconnectCharacteristic(BluetoothGattCharacteristic characteristic){
        final UUID cccdUuid = UUID.fromString(BleUtils.CCCD_DESCRIPTOR_UUID);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUuid);
        if (descriptor != null) {
            if (gatt == null) {
                return;
            }
            if (!gatt.setCharacteristicNotification(characteristic, false)) {
                Log.d("TempHumidReceiveManager", "set characteristics notification failed");
                return;
            }
            writeDescription(descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
    }
}
