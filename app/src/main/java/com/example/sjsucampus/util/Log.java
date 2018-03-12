package com.example.sjsucampus.util;

/**
 * Created by chitoo on 10/22/16.
 */

public class Log {
    private static final int HOST_CLASS_INDEX = 4;

    public static void v(Object... msg) {
        android.util.Log.v(Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                Util.getString(Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", Util.getString(msg)));
    }

    public static void i(Object... msg) {
        android.util.Log.i(Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                Util.getString(Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", Util.getString(msg)));
    }

    public static void d(Object... msg) {
        android.util.Log.d(Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                Util.getString(Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", Util.getString(msg)));
    }

    public static void e(Object... msg) {
        android.util.Log.e(Util.getClassNameByStackIndex(HOST_CLASS_INDEX),
                Util.getString(Util.getHostFunctionName(HOST_CLASS_INDEX),
                        "(): ", Util.getString(msg)));
    }
}
