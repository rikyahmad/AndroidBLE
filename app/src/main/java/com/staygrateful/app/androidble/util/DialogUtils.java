package com.staygrateful.app.androidble.util;

import android.content.Context;
import android.widget.Toast;

public class DialogUtils {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
