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
import android.view.View;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.databinding.ActivityConfirmBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import iclasses.TransactionFeesCalculator;
import iclasses.VaultDetailsCallback;
import iclasses.WalletCallback;
import model.VaultDetails;
import model.WalletDetails;
import valle.btc.BTCUtils;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;
import static com.app.bitcoinvault.util.Utils.TRANSACTION_FEE;

/**
 * this activity is used to show confirm screen
 */
public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener, IAppConstant,
        WalletCallback, VaultDetailsCallback, TransactionFeesCalculator {

    private final String TAG = this.getClass().getSimpleName();
    private ActivityConfirmBinding activityConfirmBinding;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private Double CURRENT_USD = 0.0;
    int sendFrom;
    String amount;
    boolean isFeeFatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityConfirmBinding = DataBindingUtil.setContentView(this, R.layout.activity_confirm);
        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();
        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        init();

        setTypeFace();

        setListner();

        getDataFromIntent();

    }

    /**
     * Method is used to setListners on the required views
     */

    private void setListner() {
        activityConfirmBinding.confirmButton.setOnClickListener(this);
        activityConfirmBinding.headerLayout.backArrow.setOnClickListener(this);
    }

    /**
     * This method will set type for the required textviews.
     */

    private void setTypeFace() {
        activityConfirmBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityConfirmBinding.bitcoinIconTextView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityConfirmBinding.amtBitcoinIconTextView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityConfirmBinding.feeBitcoinIconTextView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityConfirmBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);
        activityConfirmBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityConfirmBinding.headerLayout.optionMenu.setOnClickListener(this);
    }

    /**
     * Method is used to initialize variables or maintaining  the visibilty of the views
     */

    private void init() {

        activityConfirmBinding.headerLayout.sendSpinner.setVisibility(View.GONE);
    }

    /**
     * This Method is used to get Data from intent and setting the views
     */

    private void getDataFromIntent() {

        if (getIntent() != null && getIntent().getExtras() != null) {

            TRANSACTION_FEE = "0";

            String reciever_add = "";
            amount = "";
            Bundle bundle = getIntent().getExtras();
            sendFrom = bundle.getInt(SEND_FROM);
            reciever_add = bundle.getString(RECIEVER_ADDRESS);

            amount = bundle.getString(AMOUNT_TO_SEND);
            activityConfirmBinding.VaultTextView.setText(reciever_add);

            switch (sendFrom) {
                case SEND_WALLET_TO_WALLET:
                case SEND_WALLET_TO_VAULT: {
                    activityConfirmBinding.headerLayout.optionMenu.setText("d");
                    String selected_wallet_id = bundle.getString(IAppConstant.WALLET_ID);
                    try {
                        mBitVaultWalletManager.getWallet(Integer.parseInt(selected_wallet_id), wallet_type, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }
                    Utils.setBackgroundImage(this, activityConfirmBinding.mainLinear, WALLET);

                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern((Double.parseDouble(amount) + Double.parseDouble(TRANSACTION_FEE))));
                    activityConfirmBinding.progressBar1.setVisibility(View.VISIBLE);
                    try {
                        mBitVaultWalletManager.calculateTransactionFees(Integer.parseInt(selected_wallet_id), reciever_add, BTCUtils.parseValue(amount), 0, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case SEND_SINGAL_WALLET_EMPTY: {
                    activityConfirmBinding.headerLayout.optionMenu.setText("d");
                    String selected_wallet_id = bundle.getString(IAppConstant.WALLET_ID);
                    try {
                        mBitVaultWalletManager.getWallet(Integer.parseInt(selected_wallet_id), wallet_type, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }
                    Utils.setBackgroundImage(this, activityConfirmBinding.mainLinear, WALLET);

                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern((Double.parseDouble(amount) + Double.parseDouble(TRANSACTION_FEE))));


                    activityConfirmBinding.progressBar1.setVisibility(View.VISIBLE);
                    try {
                        mBitVaultWalletManager.calculateTransactionFees(Integer.parseInt(selected_wallet_id), reciever_add, BTCUtils.parseValue(amount), 0, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case SEND_VAULT_TO_WALLET: {
                    activityConfirmBinding.headerLayout.optionMenu.setText("c");
                    getVault();
                    Utils.setBackgroundImage(this, activityConfirmBinding.mainLinear, VAULT);


                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern((Double.parseDouble(amount) + Double.parseDouble(TRANSACTION_FEE))));

                    String selected_wallet_id = bundle.getString(IAppConstant.WALLET_ID);

                    try {
                        mBitVaultWalletManager.calculateVaultToWalletFees(reciever_add, BTCUtils.parseValue(amount), this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    break;
                }
                case SEND_WALLET_TO_WALLET_OTHER_APP: {
                    activityConfirmBinding.headerLayout.optionMenu.setVisibility(View.GONE);
                    String selected_wallet_id = bundle.getString(IAppConstant.WALLET_ID);
                    try {
                        mBitVaultWalletManager.getWallet(Integer.parseInt(selected_wallet_id), wallet_type, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }
                    Utils.setBackgroundImage(this, activityConfirmBinding.mainLinear, WALLET);

                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern((Double.parseDouble(amount) + Double.parseDouble(TRANSACTION_FEE))));
                    activityConfirmBinding.progressBar1.setVisibility(View.VISIBLE);
                    try {
                        mBitVaultWalletManager.calculateTransactionFees(Integer.parseInt(selected_wallet_id), reciever_add, BTCUtils.parseValue(amount), 0, this);
                    } catch (SecureSDKException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case SEND_ALL_WALLET_EMPTY: {
                    activityConfirmBinding.headerLayout.optionMenu.setText("d");
                    Utils.setBackgroundImage(this, activityConfirmBinding.mainLinear, WALLET);

                    activityConfirmBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(amount) * CURRENT_USD));
                    activityConfirmBinding.headerLayout.walletName.setText(reciever_add + getString(R.string.invoice_hy));

                    double amountAfterDeduction = Double.parseDouble(amount) - Double.parseDouble(TRANSACTION_FEE);

                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amountAfterDeduction + "")));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(amount);
                    activityConfirmBinding.progressBar1.setVisibility(View.VISIBLE);
                    mBitVaultWalletManager.calculateEmptyWalletToVaultFees(0, this);

                    break;
                }
            }

        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmButton: {

                try {
                    double walletBal = Double.parseDouble(activityConfirmBinding.headerLayout.amount.getText().toString());
                    double sendAmount = Double.parseDouble(activityConfirmBinding.totalAmtInBitcoin.getText().toString());
                    if (isFeeFatch) {
                        if (sendAmount <= walletBal) {
                            if (Utils.isNetworkConnected(this)) {
                                if (getIntent() != null && getIntent().getExtras() != null) {
                                    Intent sendIntent = new Intent(ConfirmActivity.this, FingerprintAuthenticationActivity.class);
                                    sendIntent.putExtras(getIntent().getExtras());
                                    startActivity(sendIntent);
                                }
                            } else {
                                Utils.showSnakbar(activityConfirmBinding.mainLinear, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);
                            }
                        } else {
                            Utils.showSnakbar(activityConfirmBinding.mainLinear, getString(R.string.insuficient_bal), Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        Utils.showSnakbar(activityConfirmBinding.mainLinear, getString(R.string.tryagain), Snackbar.LENGTH_SHORT);
                    }
                } catch (Exception e) {

                }
                break;
            }

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
    public void getWallet(WalletDetails walletDetails) {
        if (walletDetails != null) {
            activityConfirmBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
            activityConfirmBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
            activityConfirmBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME() + getString(R.string.invoice_hy));
        }
    }

    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        if (mVaultDetails != null) {
            activityConfirmBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE())));
            activityConfirmBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * CURRENT_USD));
            activityConfirmBinding.headerLayout.walletName.setText(mVaultDetails.getVAULT_NAME() + getString(R.string.invoice_hy));
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
            if (activityConfirmBinding != null && !activityConfirmBinding.headerLayout.amount.getText().toString().equals(""))
                activityConfirmBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(activityConfirmBinding.headerLayout.amount.getText().toString()) * value)));

        }
    };

    @Override
    public void transactionFees(long fees) {
        if (fees > 0) {
            isFeeFatch = true;
            double feeInDouble = (double) fees / 100000000;

            String fee = Utils.convertDecimalFormatPattern(feeInDouble);
            TRANSACTION_FEE = fee;
            activityConfirmBinding.progressBar1.setVisibility(View.GONE);


            switch (sendFrom) {
                case SEND_SINGAL_WALLET_EMPTY:
                case SEND_ALL_WALLET_EMPTY: {

                    double amountAfterDeduction = Double.parseDouble(amount) - Double.parseDouble(TRANSACTION_FEE);

                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amountAfterDeduction + "")));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));

                    break;
                }
                default: {
                    activityConfirmBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(amount)));
                    activityConfirmBinding.amtFeeInBitcoin.setText(TRANSACTION_FEE);
                    activityConfirmBinding.totalAmtInBitcoin.setText(Utils.convertDecimalFormatPattern((Double.parseDouble(amount) + Double.parseDouble(TRANSACTION_FEE))));
                    break;
                }

            }
        }
    }

    @Override
    public void transacionFeesCalculatorFailed(String error) {

    }
}
