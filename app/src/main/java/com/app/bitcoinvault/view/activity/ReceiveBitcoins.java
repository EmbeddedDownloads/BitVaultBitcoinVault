package com.app.bitcoinvault.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.databinding.ReceiveBitcoinsBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import iclasses.WalletCallback;
import model.WalletDetails;
import qrcode.QRCodeManager;


/**
 * This class is for receive bitcoins.
 */

public class ReceiveBitcoins extends AppCompatActivity implements View.OnClickListener, IAppConstant, WalletCallback {

    private ReceiveBitcoinsBinding mReceiveBitcoinsBinding;
    private WalletDetails walletDetails = null;
    private Double CURRENT_USD = 0.0;
    private final String TAG = this.getClass().getSimpleName();
    private String selected_wallet_id = "";
    private BitVaultWalletManager mBitVaultWalletManager = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiveBitcoinsBinding = DataBindingUtil.setContentView(this, R.layout.receive_bitcoins);

        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();
        Utils.setBackgroundImage(this, mReceiveBitcoinsBinding.mainLinear, WALLET);

        getDataFromIntent();

        setTypeFace();

        setListner();

    }

    /**
     * Method is used to setListners on the required views
     */

    private void setListner() {

        mReceiveBitcoinsBinding.headerLayout.backArrow.setOnClickListener(this);
        mReceiveBitcoinsBinding.headerLayout.optionMenu.setText("d");
        mReceiveBitcoinsBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);
        mReceiveBitcoinsBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        mReceiveBitcoinsBinding.headerLayout.optionMenu.setOnClickListener(this);
        mReceiveBitcoinsBinding.headerLayout.sendSpinner.setVisibility(View.GONE);

    }

    /**
     * This Method is used to get Data from intent and setting the views
     */

    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getExtras() != null) {

            selected_wallet_id = getIntent().getStringExtra(IAppConstant.WALLET_ID);
            try {
                mBitVaultWalletManager.getWallet(Integer.parseInt(selected_wallet_id),wallet_type, this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {

        mReceiveBitcoinsBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.backArrow:
                onBackPressed();
                break;
            case R.id.optionMenu:
                Intent vaultIntent = new Intent(this, VaultTransactionActivity.class);
                startActivity(vaultIntent);
                break;

        }
    }


    @Override
    public void getWallet(WalletDetails mWalletDetails) {
        if (mWalletDetails != null) {
            walletDetails = mWalletDetails;
            mReceiveBitcoinsBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
            mReceiveBitcoinsBinding.address.setText(walletDetails.getmKeyPair().address);
            mReceiveBitcoinsBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME() + getString(R.string.receive_hy));

            Bitmap bitmap = new QRCodeManager().showQRCodePopupForAddress(walletDetails.getmKeyPair().address);
            if (bitmap != null) {
                mReceiveBitcoinsBinding.qrImage.setImageBitmap(bitmap);

            }
            mReceiveBitcoinsBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(CONVERSION_ACTION_FILTER));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            double value = intent.getDoubleExtra(currency, 0.0);
            if (mReceiveBitcoinsBinding != null && walletDetails != null)
                mReceiveBitcoinsBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * value)));

        }
    };
}
