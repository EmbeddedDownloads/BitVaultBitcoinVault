package com.app.bitcoinvault.util;

import android.util.Log;

/**
 * Logger class for log
 */

public class Logger {

    public static boolean ishowLog = false;

    /**
     * Method to show logs verbose
     *
     * @param tag   -- tag value
     * @param value -- value
     */
    public static void Logv(String tag, String value) {
        if (ishowLog)
            Log.v(tag, value);
    }


    /**
     * Method to show logs debug
     *
     * @param tag   -- tag value
     * @param value -- value
     */
    public static void Logd(String tag, String value) {
        if (ishowLog)
            Log.d(tag, value);
    }


    /**
     * Method to show logs error
     *
     * @param tag   -- tag value
     * @param value -- value
     */
    public static void Loge(String tag, String value) {
        if (ishowLog)
            Log.e(tag, value);
    }

}
