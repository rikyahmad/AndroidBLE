package com.staygrateful.app.androidble.util;

import android.Manifest;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static String[] permissions() {
        final List<String> results = new ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            results.add(Manifest.permission.BLUETOOTH_SCAN);
            results.add(Manifest.permission.BLUETOOTH_CONNECT);
            results.add(Manifest.permission.ACCESS_FINE_LOCATION);
            results.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else  {
            results.add(Manifest.permission.ACCESS_FINE_LOCATION);
            results.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return results.toArray(new String[results.size()-1]);
    }
}
