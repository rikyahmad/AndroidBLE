package com.staygrateful.app.androidble.ble;

import com.staygrateful.app.androidble.ble.model.Resource;
import com.staygrateful.app.androidble.ble.model.TempHumidityResult;
import com.staygrateful.app.androidble.ble.util.TemperatureListener;

public interface TemperatureAndHumidityReceiveManager {

    void setTemperatureListener(TemperatureListener temperatureListener);

    void reconnect();

    void disconnect();

    void startReceiving();

    void closeConnection();
}
