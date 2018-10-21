package com.app.bitcoinvault.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.VolleyError;
import com.app.bitcoinvault.R;
import com.app.bitcoinvault.databinding.ActivityMainBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import commons.SecureSDKException;
import currencyconvertor.CurrencyConvertor;
import iclasses.CurrencyConvertorCallback;
import iclasses.VaultDetailsCallback;
import iclasses.WalletArrayCallback;
import model.VaultDetails;
import model.WalletDetails;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;

/**
 * this wallet dashboard activity
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener, IAppConstant, VaultDetailsCallback, WalletArrayCallback, CurrencyConvertorCallback {

    private final String TAG = getClass().getSimpleName();
    private ActivityMainBinding activityMainBinding = null;
    private CurrencyConvertor currencyConvertor = null;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_main);

        setListner();
        setTypeface();

        if (!AppPreferences.getInstance(this).getIsWalletSave() || getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION)) {
            getVault();
            requestWallet();
            AppPreferences.getInstance(this).setIsWalletSave(true);
        }

        currencyConvertor = new CurrencyConvertor(this);

        checkWalletBalance();
    }

    /***
     * This method is used to update the balance in the wallet
     *
     */
    private void checkWalletBalance() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    currencyConvertor.convertBTCtoUSD("1");
                } catch (SecureSDKException e) {
                    e.printStackTrace();
                }
            }
        }, WALLET_BALANCE_UPDATE_INITIAL_DELAY, WALLET_BALANCE_TIMER);
    }


    @Override
    public void USDtoBTC(String btc) {

    }

    @Override
    public void BTCtoUSD(String usd) {
        AppPreferences.getInstance(HomeActivity.this).setCurrency(usd);

        if (usd != null & !usd.equals("")) {
            try {
                Log.e(TAG, "Bitcoin To dollar   " + usd);
                Intent intent = new Intent(CONVERSION_ACTION_FILTER);
                intent.putExtra(currency, Double.parseDouble(usd));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } catch (Exception e) {

            }

        }
    }

    @Override
    public void ConversionFailed(VolleyError mError) {

    }

    /**
     * this method is used to get vault from the SDK
     */
    public void getVault() {
        try {
            if (BitVaultManagerSingleton.getInstance() != null) {
                getWalletInstance().getVault(this);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to set data in views
     */

    private void requestWallet() {
        try {
            if (BitVaultManagerSingleton.getInstance() != null) {
                BitVaultManagerSingleton.getInstance().getWallets(this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method is used to setListners on the required views
     */
    private void setListner() {
        activityMainBinding.cardViewVault.setOnClickListener(this);
        activityMainBinding.cardViewWallet.setOnClickListener(this);
    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeface() {
        activityMainBinding.bitvaultIcon.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityMainBinding.rightArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityMainBinding.rightArrowWallet.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityMainBinding.bitwalletIcon.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardViewVault: {

                Intent transactionIntent = new Intent(HomeActivity.this, VaultTransactionActivity.class);
                startActivity(transactionIntent);

                break;
            }
            case R.id.cardViewWallet: {
                Intent i = new Intent(HomeActivity.this, WalletHomeActivity.class);
                startActivity(i);
                break;
            }
        }
    }

    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
                for (int k = 0; k < mRequestedWallets.size(); k++) {

                    if (!AppPreferences.getInstance(this).getWalletAddIsExist(mRequestedWallets.get(k).getmKeyPair().address)) {
                        AppPreferences.getInstance(this).setWalletNameByWalletAdd(mRequestedWallets.get(k).getmKeyPair().address, mRequestedWallets.get(k).getWALLET_NAME());
                    }
                    //for redirect notification
                    if (getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION) && getIntent().getStringExtra(NOTIFICATION_RECIEVER).equalsIgnoreCase(mRequestedWallets.get(k).getmKeyPair().address)) {
                        Intent i = new Intent(HomeActivity.this, WalletHomeActivity.class);
                        i.putExtra(FROM_NOTIFICATION, true);
                        i.putExtra(NOTIFICATION_RECIEVER, mRequestedWallets.get(k).getWALLET_ID());
                        startActivity(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {

        if (!AppPreferences.getInstance(this).getWalletAddIsExist(mVaultDetails.getmKeyPair().address)) {
            AppPreferences.getInstance(this).setWalletNameByWalletAdd(mVaultDetails.getmKeyPair().address, mVaultDetails.getVAULT_NAME());
        }
        //for redirect notification
        if (mVaultDetails != null && getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION) && getIntent().getStringExtra(NOTIFICATION_RECIEVER).equalsIgnoreCase(mVaultDetails.getmKeyPair().address)) {
            Intent i = new Intent(HomeActivity.this, VaultTransactionActivity.class);
            i.putExtra(FROM_NOTIFICATION, true);
            i.putExtra(NOTIFICATION_RECIEVER, getIntent().getStringExtra(NOTIFICATION_RECIEVER));
            startActivity(i);
        }

    }
}
