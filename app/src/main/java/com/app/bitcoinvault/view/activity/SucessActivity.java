package com.app.bitcoinvault.view.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.database.CRUD_OperationOfDB;
import com.app.bitcoinvault.databinding.ActivitySucessBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import commons.SecureSDKException;
import iclasses.TransactionBuilder;
import iclasses.VaultDetailsCallback;
import iclasses.WalletCallback;
import model.VaultDetails;
import model.WalletDetails;
import valle.btc.BTCUtils;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;
import static com.app.bitcoinvault.database.IDBConstant.MESSAGE;
import static com.app.bitcoinvault.database.IDBConstant.TABLE_NAME;
import static com.app.bitcoinvault.database.IDBConstant.TXD_ID;
import static com.app.bitcoinvault.util.Utils.TRANSACTION_FEE;

/**
 * this wallet transaction confirm activity
 */
public class SucessActivity extends AppCompatActivity implements View.OnClickListener, IAppConstant, TransactionBuilder, WalletCallback, VaultDetailsCallback {

    private ActivitySucessBinding activitySucessBinding;
    private Double CURRENT_USD = 0.0;
    WalletDetails walletDetails = null;
    VaultDetails mVaultDetails = null;
    ProgressDialog dialog;
    private String sendAmount = "", recieverAdd = "", desc = "";
    int transactionType = 0;
    String selected_wallet_id = "";
    String trId = null;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySucessBinding = DataBindingUtil.setContentView(this, R.layout.activity_sucess);
        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();
        init();

        setListner();

        setTypeFace();

        getDataFromIntent();
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
     * Method is used to setListners on the required views
     */

    private void setListner() {

        activitySucessBinding.headerLayout.backArrow.setOnClickListener(this);
        activitySucessBinding.anotherTransactionButton.setOnClickListener(this);
        activitySucessBinding.finishButton.setOnClickListener(this);


    }

    /**
     * Method is used to initialize variables or maintaining  the visibilty of the views
     */
    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        activitySucessBinding.headerLayout.optionMenu.setVisibility(View.INVISIBLE);
        activitySucessBinding.headerLayout.sendSpinner.setVisibility(View.GONE);
    }

    /**
     * This Method is used to get Data from intent and setting the views
     */
    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getExtras() != null) {

            Bundle bundle = getIntent().getExtras();

            selected_wallet_id = bundle.getString(IAppConstant.WALLET_ID);


            transactionType = bundle.getInt(SEND_FROM);
            sendAmount = bundle.getString(AMOUNT_TO_SEND);
            recieverAdd = bundle.getString(RECIEVER_ADDRESS);
            desc = bundle.getString(DESC);

            switch (transactionType) {
                case SEND_WALLET_TO_WALLET:
                case SEND_WALLET_TO_VAULT:
                case SEND_SINGAL_WALLET_EMPTY: {
                    Utils.setBackgroundImage(this, activitySucessBinding.mainLinear, WALLET);
                    try {
                        if (getWalletInstance() != null)
                            getWalletInstance().getWallet(Integer.parseInt(selected_wallet_id), wallet_type, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case SEND_VAULT_TO_WALLET: {
                    Utils.setBackgroundImage(this, activitySucessBinding.mainLinear, VAULT);
                    getVault();
                    break;
                }
                case SEND_WALLET_TO_WALLET_OTHER_APP: {
                    activitySucessBinding.headerLayout.backArrow.setVisibility(View.GONE);
                    activitySucessBinding.anotherTransactionButton.setVisibility(View.GONE);
                    activitySucessBinding.finishButton.setText(R.string.done);
                    Utils.setBackgroundImage(this, activitySucessBinding.mainLinear, WALLET);
                    try {
                        if (getWalletInstance() != null)
                            getWalletInstance().getWallet(Integer.parseInt(selected_wallet_id), wallet_type, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case SEND_ALL_WALLET_EMPTY: {
                    Utils.setBackgroundImage(this, activitySucessBinding.mainLinear, WALLET);

                    activitySucessBinding.anotherTransactionButton.setVisibility(View.GONE);
                    activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(sendAmount)));
                    activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(sendAmount) * CURRENT_USD));
                    activitySucessBinding.headerLayout.walletName.setText(recieverAdd + getString(R.string.invoice_hy));

                    dialog.show();

                    if (getWalletInstance() != null) {
                        try {
                            getWalletInstance().emptyAllWalletsToVault(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                }
            }
        }
    }

    /**
     * this m
     *
     * @param mWalletIdFromWhichDataToSend
     * @param outputAddress
     * @param amountToSend
     */
    public void createBitCoinTransaction(final int mWalletIdFromWhichDataToSend,
                                         final String outputAddress,
                                         final String amountToSend) {
        if (getWalletInstance() != null) {
            // From which wallet bitcoins to send
            // Recipient address
            // Amount to be send
            // Mining fee of the transaction
            getWalletInstance().sendBitCoins(mWalletIdFromWhichDataToSend, outputAddress, BTCUtils.parseValue(amountToSend),
                    BTCUtils.parseValue(TRANSACTION_FEE), this);
        }
    }

    public void transferWalletToVault(final int mWalletIdFromWhichDataToSend,
                                      final String amountToSend) {
        if (getWalletInstance() != null) {
            try {
                getWalletInstance().sendBitcoinsWallletToVault(mWalletIdFromWhichDataToSend, BTCUtils.parseValue(amountToSend),
                        BTCUtils.parseValue(TRANSACTION_FEE), this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }

    }

    public void emptySingalWallet(final int mWalletIdFromWhichDataToSend) {
        if (getWalletInstance() != null) {
            try {
                getWalletInstance().emptyWalletToVault(mWalletIdFromWhichDataToSend,
                        BTCUtils.parseValue(TRANSACTION_FEE), this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * this m
     *
     * @param mWalletIdFromWhichDataToSend
     * @param amountToSend
     */
    public void transferVaultToWallet(final int mWalletIdFromWhichDataToSend,
                                      final String amountToSend) {
        if (getWalletInstance() != null) {
            // From which wallet bitcoins to send
            // Recipient address
            // Amount to be send
            // Mining fee of the transaction
            try {
                getWalletInstance().sendBitcoinsVaultToWalllet(mWalletIdFromWhichDataToSend, BTCUtils.parseValue(amountToSend),
                        BTCUtils.parseValue(TRANSACTION_FEE), this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.finishButton: {

                switch (transactionType) {
                    case SEND_WALLET_TO_WALLET:
                    case SEND_WALLET_TO_VAULT:
                    case SEND_SINGAL_WALLET_EMPTY:
                    case SEND_ALL_WALLET_EMPTY: {
                        Intent i = new Intent(this, WalletHomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case SEND_VAULT_TO_WALLET: {
                        Intent i = new Intent(this, VaultTransactionActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case SEND_WALLET_TO_WALLET_OTHER_APP: {
                        Intent i = new Intent(this, SendBitcoinThirdPartyActivity.class);
                        Bundle bundle = new Bundle();
                        if (trId != null && !trId.equals("")) {
                            bundle.putString(TRID, trId);
                            bundle.putBoolean(STATUS, true);
                        } else {
                            bundle.putString(TRID, "");
                            bundle.putBoolean(STATUS, false);
                        }
                        i.putExtras(bundle);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                        break;
                    }
                }
                break;
            }

            case R.id.backArrow:
                onBackPressed();
                break;

            case R.id.anotherTransactionButton:

                switch (transactionType) {
                    case SEND_WALLET_TO_WALLET:
                    case SEND_WALLET_TO_VAULT:
                    case SEND_SINGAL_WALLET_EMPTY: {
                        Intent i = new Intent(this, SendBitcoinActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case SEND_VAULT_TO_WALLET: {
                        Intent i = new Intent(this, SendBitcoinVaultActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                        break;
                    }
                }

                break;
        }
    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {

        activitySucessBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

    }

    @Override
    public void onBackPressed() {

        switch (transactionType) {
            case SEND_WALLET_TO_WALLET:
            case SEND_WALLET_TO_VAULT:
            case SEND_SINGAL_WALLET_EMPTY:
            case SEND_ALL_WALLET_EMPTY: {
                Intent i = new Intent(this, WalletHomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
            case SEND_VAULT_TO_WALLET: {
                Intent i = new Intent(this, VaultTransactionActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
            case SEND_WALLET_TO_WALLET_OTHER_APP: {
                Intent i = new Intent(this, SendBitcoinThirdPartyActivity.class);
                Bundle bundle = new Bundle();
                if (trId != null && !trId.equals("")) {
                    bundle.putString(TRID, trId);
                    bundle.putBoolean(STATUS, true);
                } else {
                    bundle.putString(TRID, "");
                    bundle.putBoolean(STATUS, false);
                }
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
        }

    }

    @Override
    public void TransactionFailed(String error) {
        runOnUiThread(new Runnable() {
            public void run() {
                activitySucessBinding.tranTextView.setText(getString(R.string.your_transaction_has_been_failed));
            }
        });

        dialog.dismiss();
    }

    @Override
    public void RequestedTransaction(String mTransaction) {
        Log.e("Transaction", "Transaction" + mTransaction);
    }

    @Override
    public void TransactionId(String mTxId) {
        trId = mTxId;
        activitySucessBinding.tranTextView.setText(getString(R.string.your_transaction_has_been));
        activitySucessBinding.recieverAddrerssTextView.setVisibility(View.VISIBLE);
        activitySucessBinding.amountTextView.setVisibility(View.VISIBLE);
        activitySucessBinding.amountTitle.setVisibility(View.VISIBLE);
        activitySucessBinding.sendToTitle.setVisibility(View.VISIBLE);
        activitySucessBinding.recieverAddrerssTextView.setText(recieverAdd);
        activitySucessBinding.amountTextView.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(sendAmount)) + getString(R.string.btc));


        switch (transactionType) {
            case SEND_WALLET_TO_WALLET: {
                if (desc != null && !desc.equals(""))
                    insertDescInDB(desc, mTxId, walletDetails.getWALLET_ID());
                double amt = Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) - (Double.parseDouble(sendAmount) + Double.parseDouble(TRANSACTION_FEE));
                activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(amt));
                activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(amt * CURRENT_USD));
                break;
            }
            case SEND_WALLET_TO_VAULT: {
                if (desc != null && !desc.equals(""))
                    insertDescInDB(desc, mTxId, walletDetails.getWALLET_ID());
                double amt = Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) - (Double.parseDouble(sendAmount) + Double.parseDouble(TRANSACTION_FEE));
                activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(amt));
                activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(amt * CURRENT_USD));
                break;
            }
            case SEND_SINGAL_WALLET_EMPTY: {
                if (desc != null && !desc.equals(""))
                    insertDescInDB(desc, mTxId, walletDetails.getWALLET_ID());
                activitySucessBinding.headerLayout.amount.setText("0");
                activitySucessBinding.headerLayout.amountDollar.setText("0");
                break;
            }
            case SEND_VAULT_TO_WALLET: {
                if (desc != null && !desc.equals(""))
                    insertDescInDB(desc, mTxId, VAULT);

                double amt = Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) - (Double.parseDouble(sendAmount) + Double.parseDouble(TRANSACTION_FEE));
                activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(amt));
                activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(amt * CURRENT_USD));
                break;
            }
            case SEND_WALLET_TO_WALLET_OTHER_APP: {
                double amt = Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) - (Double.parseDouble(sendAmount) + Double.parseDouble(TRANSACTION_FEE));
                activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(amt));
                activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(amt * CURRENT_USD));
                break;
            }
            case SEND_ALL_WALLET_EMPTY: {
                activitySucessBinding.headerLayout.amount.setText("0");
                activitySucessBinding.headerLayout.amountDollar.setText("0");
                break;
            }
        }

        dialog.dismiss();
    }

    /**
     * Method is used to insert values in database
     */
    public long insertDescInDB(String desc, String txId, String w_id) {
        try {
            CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(MESSAGE, desc);
            mContentValues.put(TXD_ID, txId);
            mContentValues.put(WALLET_ID, w_id);
            return dbOperation.InsertIntoDB(TABLE_NAME, mContentValues);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }



    @Override
    public void getWallet(WalletDetails mWalletDetails) {
        if (mWalletDetails != null) {
            walletDetails = mWalletDetails;

            activitySucessBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME() + getString(R.string.invoice_hy));
            activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
            activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));

            try {
                dialog.show();
                switch (transactionType) {
                    case SEND_WALLET_TO_WALLET:
                    case SEND_WALLET_TO_WALLET_OTHER_APP: {
                        createBitCoinTransaction(Integer.parseInt(walletDetails.getWALLET_ID()), recieverAdd, sendAmount);
                        break;
                    }
                    case SEND_WALLET_TO_VAULT: {
                        transferWalletToVault(Integer.parseInt(walletDetails.getWALLET_ID()), sendAmount);
                        break;
                    }
                    case SEND_SINGAL_WALLET_EMPTY: {
                        emptySingalWallet(Integer.parseInt(walletDetails.getWALLET_ID()));
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        if (mVaultDetails != null) {
            this.mVaultDetails = mVaultDetails;
            activitySucessBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE())));
            activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * CURRENT_USD));
            activitySucessBinding.headerLayout.walletName.setText(mVaultDetails.getVAULT_NAME() + getString(R.string.invoice_hy));
            dialog.show();
            transferVaultToWallet(Integer.parseInt(selected_wallet_id), sendAmount);

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
            if (activitySucessBinding != null && walletDetails != null)
                activitySucessBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(activitySucessBinding.headerLayout.amount.getText().toString()) * value)));

        }
    };
}