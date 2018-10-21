package com.app.bitcoinvault.util;

import com.app.bitcoinvault.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import controller.SDKControl;

/**
 * Created by vvdntech on 23/11/17.
 */


@ReportsCrashes(
        mailTo = "amit.goyal@vvdntech.in",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)
public class BitcoinApplication extends SDKControl {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
