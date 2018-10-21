package com.app.bitcoinvault.util;


import android.database.Cursor;

/**
 * Created by vvdntech on 24/11/17.
 */

public interface OnProfileLoadListener {
    void onProfileLoadComplete(Cursor cursor);
}

