package com.app.bitcoinvault.view.activity;

import android.content.Intent;
import android.os.Bundle;

import com.app.bitcoinvault.util.IAppConstant;
import com.embedded.wallet.BitVaultActivity;

import bitmanagers.BitVaultWalletManager;
import iclasses.UserAuthenticationCallback;

/**
 * this is authentication activity is loading very first time and after transaticon confirm
 */
public class FingerprintAuthenticationActivity extends BitVaultActivity implements UserAuthenticationCallback, IAppConstant {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION) && BitVaultWalletManager.getInstance().getUserValid()) {
            Intent i = new Intent(FingerprintAuthenticationActivity.this, HomeActivity.class);
            i.putExtra(FROM_NOTIFICATION, true);
            i.putExtra(NOTIFICATION_RECIEVER, getIntent().getStringExtra(NOTIFICATION_RECIEVER));
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateUser(this, this);
    }

    @Override
    public void onAuthenticationSuccess() {

        if (getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION) && BitVaultWalletManager.getInstance().getUserValid()) {
            Intent i = new Intent(FingerprintAuthenticationActivity.this, HomeActivity.class);
            i.putExtra(FROM_NOTIFICATION, true);
            i.putExtra(NOTIFICATION_RECIEVER, getIntent().getStringExtra(NOTIFICATION_RECIEVER));
            startActivity(i);
            finish();
        } else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(SEND_FROM)) {
            Intent sendIntent = new Intent(FingerprintAuthenticationActivity.this, SucessActivity.class);
            sendIntent.putExtras(getIntent().getExtras());
            startActivity(sendIntent);
            finish();
        } else if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(RECIEVER_ADDRESS)&& getIntent().getExtras().containsKey(AMOUNT_TO_SEND)) {
            Intent sendIntent = new Intent(FingerprintAuthenticationActivity.this, SendBitcoinThirdPartyActivity.class);
            sendIntent.putExtras(getIntent().getExtras());
            startActivity(sendIntent);
            finish();
        } else {
            Intent i = new Intent(FingerprintAuthenticationActivity.this, SplashActivity.class);
            startActivity(i);
            finish();
        }
    }

}



