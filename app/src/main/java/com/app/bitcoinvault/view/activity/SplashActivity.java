package com.app.bitcoinvault.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.bean.ContactsModel;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import java.util.ArrayList;

import utils.SDKUtils;

/**
 * this wallet splash activity
 */
public class SplashActivity extends AppCompatActivity implements IAppConstant {

    private final String TAG = this.getClass().getSimpleName();
    private TextView mVersioCode;
    private Handler handler;
    private Runnable runnable;
    private final long timer= 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            new FetchContacts().execute();
        }
        mVersioCode = (TextView) findViewById(R.id.version_code);
        try {
            String appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if (appVersion != null) {
                mVersioCode.setText(getResources().getString(R.string.version) + appVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        };
        handler.postDelayed(runnable, timer);
    }


    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }


    /**
     * Class is used to fetch contacts from the device
     */

    private class FetchContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<ContactsModel> mContactsModel = Utils.readPhoneContacts(SplashActivity.this);
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(SplashActivity.this).setContact(mContactsModel);
                SDKUtils.showLog("Contacts", mContactsModel.size() + " ");
            }
            return null;
        }
    }
}
