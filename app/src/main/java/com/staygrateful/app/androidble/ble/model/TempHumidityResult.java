package com.staygrateful.app.androidble.ble.model;

import com.staygrateful.app.androidble.ble.ConnectionState;

public class TempHumidityResult {
    final float temperature;
    final float humidity;
    final ConnectionState connectionState;

    public TempHumidityResult(float temperature, float humidity, ConnectionState connectionState) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.connectionState = connectionState;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
}
