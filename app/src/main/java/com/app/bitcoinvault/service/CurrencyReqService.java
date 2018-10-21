package com.app.bitcoinvault.service;

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.VolleyError;
import com.app.bitcoinvault.util.AppPreferences;

import commons.SecureSDKException;
import currencyconvertor.CurrencyConvertor;
import iclasses.CurrencyConvertorCallback;
import utils.SDKUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CurrencyReqService extends IntentService {

    private final String TAG = this.getClass().getSimpleName();
    private CurrencyConvertor currencyConvertor = null;

    public CurrencyReqService() {
        super("CurrencyReqService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        currencyConvertor = new CurrencyConvertor(new CurrencyConvertorCallback() {
            @Override
            public void USDtoBTC(String btc) {
                SDKUtils.showErrorLog(TAG, btc);
            }

            @Override
            public void BTCtoUSD(String usd) {
                SDKUtils.showErrorLog(TAG, usd);
                AppPreferences.getInstance(CurrencyReqService.this).setCurrency(usd);
                stopSelf();
            }

            @Override
            public void ConversionFailed(VolleyError mError) {
                SDKUtils.showErrorLog(TAG, mError.getMessage());
                stopSelf();
            }
        });

        try {
            currencyConvertor.convertBTCtoUSD("1");
        } catch (SecureSDKException e) {
            e.printStackTrace();
        }
    }
}