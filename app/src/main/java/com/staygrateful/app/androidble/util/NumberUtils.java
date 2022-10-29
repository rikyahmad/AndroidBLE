package com.staygrateful.app.androidble.util;

public class NumberUtils {

    public static Float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int parseInt(byte value) {
        return (int) value;
    }
}
