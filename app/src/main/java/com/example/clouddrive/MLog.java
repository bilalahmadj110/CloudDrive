package com.example.clouddrive;

import android.util.Log;

public class MLog {

    public static void log(String str) {
        Log.i("cloud_drive_flask", str);
    }

    public static void log(int str) {
        log(str + "");
    }

    public static void log(boolean str) {
        log(str + "");
    }

    public static void log(char str) {
        log(str + "");
    }
}
