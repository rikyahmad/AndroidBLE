package com.staygrateful.app.androidble.ble.util;

import com.staygrateful.app.androidble.ble.model.Resource;
import com.staygrateful.app.androidble.ble.model.TempHumidityResult;

public interface TemperatureListener {
    void onEmit(Resource<TempHumidityResult> resource);
}
