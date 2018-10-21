package com.app.bitcoinvault.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.app.bitcoinvault.R;
import com.app.bitcoinvault.database.IDBConstant;
import com.app.bitcoinvault.databinding.ActivitySendBitcoinThirdPartyBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import currencyconvertor.CurrencyConvertor;
import iclasses.CurrencyConvertorCallback;
import iclasses.WalletArrayCallback;
import model.WalletDetails;


public class SendBitcoinThirdPartyActivity extends AppCompatActivity implements IDBConstant, CurrencyConvertorCallback, WalletArrayCallback, View.OnClickListener, IAppConstant {

    private ActivitySendBitcoinThirdPartyBinding activitySendBitcoinThirdPartyBinding;
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<String> addressList = new ArrayList<>();
    private List<WalletDetails> mWalletList = new ArrayList<>();
    private Double CURRENT_USD = 0.0;
    private WalletDetails walletDetails = null;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private ArrayAdapter<String> dataAdapter = null;
    private String selected_wallet_id = null;
    private Timer mTimer;
    private CurrencyConvertor currencyConvertor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BitVaultWalletManager.getInstance().getUserValid()) {
            Intent sendIntent = new Intent(SendBitcoinThirdPartyActivity.this, FingerprintAuthenticationActivity.class);
            sendIntent.putExtras(getIntent().getExtras());
            startActivity(sendIntent);
            finish();
        } else {

            activitySendBitcoinThirdPartyBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_bitcoin_third_party);


            CURRENT_USD = AppPreferences.getInstance(this).getCurrency();

            mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();


            init();

            getDataFromIntent();

            setTypeFace();

            currencyConvertor = new CurrencyConvertor(this);
            checkWalletBalance();
        }

    }


    @Override
    public void USDtoBTC(String btc) {

    }

    @Override
    public void BTCtoUSD(String usd) {
        AppPreferences.getInstance(SendBitcoinThirdPartyActivity.this).setCurrency(usd);

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


    /**
     * Method used to set data in views
     */
    private void requestWallet() {
        try {
            mBitVaultWalletManager.getWallets(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method is used to initialize variables or maintaining  the visibilty of the views
     */

    private void init() {

        Utils.setBackgroundImage(this, activitySendBitcoinThirdPartyBinding.mainLinear, WALLET);
        activitySendBitcoinThirdPartyBinding.headerLayout.backArrow.setVisibility(View.GONE);
        activitySendBitcoinThirdPartyBinding.headerLayout.optionMenu.setVisibility(View.GONE);
        activitySendBitcoinThirdPartyBinding.headerLayout.sendSpinner.setVisibility(View.GONE);

        setSpinner();

        setListner();


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


    /**
     * Method is used the spinner
     */

    private void setSpinner() {
        dataAdapter = new ArrayAdapter<String>(this, R.layout.wallet_spinner_item, addressList);
        dataAdapter.setDropDownViewResource(R.layout.wallet_spinner_item);
        activitySendBitcoinThirdPartyBinding.sendSpinner.setAdapter(dataAdapter);
        activitySendBitcoinThirdPartyBinding.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String item = adapterView.getItemAtPosition(position).toString();
                if (mWalletList != null && item != null && !item.equals("")) {
                    updateHeaderAndWallet(mWalletList.get(position));
                    selected_wallet_id = mWalletList.get(position).getWALLET_ID();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    /**
     * This Method is used to get Data from intent and setting the views
     */

    private void getDataFromIntent() {
        Intent intent = getIntent();

        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            String reciever_add = bundle.getString(RECIEVER_ADDRESS);
            String amount = bundle.getString(AMOUNT_TO_SEND);
            activitySendBitcoinThirdPartyBinding.recieverAddrerssTextView.setText(reciever_add);
            activitySendBitcoinThirdPartyBinding.amtInBitcoin.setText(amount);
            try {
                if (!amount.equals("")) {
                    double value = Double.parseDouble(amount);
                    double total = CURRENT_USD * value;
                    activitySendBitcoinThirdPartyBinding.amtInDoller.setText(Utils.convertDecimalFormatPattern(total));
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

            requestWallet();
        }

    }

    /**
     * update header
     *
     * @param wallet wallet object to update
     */
    private void updateHeaderAndWallet(WalletDetails wallet) {
        activitySendBitcoinThirdPartyBinding.headerLayout.walletName.setText(wallet.getWALLET_NAME() + getString(R.string.send_hy));
        activitySendBitcoinThirdPartyBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(wallet.getWALLET_LAST_UPDATE_BALANCE())));
        walletDetails = wallet;
        activitySendBitcoinThirdPartyBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
    }

    /**
     * Method is used to setListners on the required views
     */
    private void setListner() {
        activitySendBitcoinThirdPartyBinding.sendBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendBtn: {
                if (checkTransactionIsValid()) {
                    if (Utils.isNetworkConnected(this)) {
                        Intent sendIntent = new Intent(SendBitcoinThirdPartyActivity.this, ConfirmActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(RECIEVER_ADDRESS, activitySendBitcoinThirdPartyBinding.recieverAddrerssTextView.getText().toString().trim());
                        bundle.putInt(SEND_FROM, SEND_WALLET_TO_WALLET_OTHER_APP);

                        bundle.putString(IDBConstant.WALLET_ID, walletDetails.getWALLET_ID());
                        bundle.putString(AMOUNT_TO_SEND, activitySendBitcoinThirdPartyBinding.amtInBitcoin.getText().toString().trim());
                        sendIntent.putExtras(bundle);
                        startActivity(sendIntent);
                    } else {
                        Utils.showSnakbar(activitySendBitcoinThirdPartyBinding.sendBitcoinLay, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);
                    }
                }
                break;
            }

        }
    }

    /**
     * this method is used to refersh view after send transaction is conf
     *
     * @param i
     */
    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        Intent intent = new Intent();
        if (i != null && i.getExtras() != null) {
            intent.putExtras(i.getExtras());


            final Intent intent1 = new Intent();
            intent1.setAction("com.thirdparty.payment.bitcoinvault");
            intent1.putExtras(i.getExtras());
            intent1.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//            intent1.setComponent(new ComponentName("com.bitvault.appstore", "com.bitvault.appstore.receivers.TestReciever"));
            sendBroadcast(intent1);


        }
        setResult(RESULT_OK, intent);
        finish();
    }


    /**
     * method to check that send amount is valid or not and send amount is not greater then available amount
     *
     * @return
     */
    private boolean checkTransactionIsValid() {
        try {
            String amt = activitySendBitcoinThirdPartyBinding.amtInBitcoin.getText().toString();
            if (Utils.convertDecimalFormatPatternDouble(Double.parseDouble(amt)) > Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())) {
                Utils.showSnakbar(activitySendBitcoinThirdPartyBinding.sendBitcoinLay, getString(R.string.insuficient_bal), Snackbar.LENGTH_SHORT);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {
        activitySendBitcoinThirdPartyBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activitySendBitcoinThirdPartyBinding.bitcoinIconTextView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
    }


    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null) {
                if (mRequestedWallets.size() > 0) {
                    int selectedWalletPostion = 0;
                    mWalletList.clear();
                    addressList.clear();
                    if (selected_wallet_id == null)
                        selected_wallet_id = mRequestedWallets.get(0).getWALLET_ID();
                    for (int i = 0; i < mRequestedWallets.size(); i++) {
                        mWalletList.add(mRequestedWallets.get(i));
                        addressList.add(mRequestedWallets.get(i).getWALLET_NAME());

                        if (mWalletList.get(i).getWALLET_ID().equals(selected_wallet_id)) {
                            selectedWalletPostion = i;
                            walletDetails = mWalletList.get(i);
                            if (walletDetails != null) {
                                activitySendBitcoinThirdPartyBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME() + getString(R.string.send_hy));
                                activitySendBitcoinThirdPartyBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
                                activitySendBitcoinThirdPartyBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
                            }
                        }
                    }
                    if (dataAdapter != null) {
                        dataAdapter.notifyDataSetChanged();
                        activitySendBitcoinThirdPartyBinding.sendSpinner.setSelection(selectedWalletPostion);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            CURRENT_USD = intent.getDoubleExtra(currency, 0.0);
            if (activitySendBitcoinThirdPartyBinding != null && walletDetails != null)
                activitySendBitcoinThirdPartyBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD)));

        }
    };

}
