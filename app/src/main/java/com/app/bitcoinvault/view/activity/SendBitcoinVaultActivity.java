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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.database.IDBConstant;
import com.app.bitcoinvault.databinding.ActivitySendBitcoinVaultBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import java.util.ArrayList;
import java.util.List;

import bitmanagers.BitVaultWalletManager;
import iclasses.VaultDetailsCallback;
import iclasses.WalletArrayCallback;
import model.VaultDetails;
import model.WalletDetails;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;

/**
 * this vault send bitcoin activity
 */
public class SendBitcoinVaultActivity extends AppCompatActivity implements View.OnClickListener, IAppConstant, VaultDetailsCallback, WalletArrayCallback {

    private ActivitySendBitcoinVaultBinding mActivitySendBitcoins;
    private ArrayList<String> addressList;
    private String mSelectedWalletName = "";
    private String mSelectedWalletid = "";
    private final String TAG = this.getClass().getSimpleName();
    private Double CURRENT_USD = 0.0;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private List<WalletDetails> mWalletList = new ArrayList<>();
    private VaultDetails mVaultDetails = null;
    private boolean isTransactionDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySendBitcoins = DataBindingUtil.setContentView(this, R.layout.activity_send_bitcoin_vault);

        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        Utils.setBackgroundImage(this, mActivitySendBitcoins.mainLinear, VAULT);

        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();

        getVault();

        requestWallet();

        init();

        setTypeFace();

        setListner();

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
     * this method is used to refersh view after send transaction is conf
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getVault();
        mActivitySendBitcoins.amtInBitcoin.setText("");
        mActivitySendBitcoins.descEditText.setText("");
        mActivitySendBitcoins.amtInDoller.setText("");
        isTransactionDone = true;
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

    @Override
    public void onBackPressed() {
        if (isTransactionDone) {
            Intent intent = new Intent();
            setResult(102, intent);
            finish();
        } else
            super.onBackPressed();
    }


    /**
     * Method is used to initialize variables or maintaining  the visibilty of the views
     */

    private void init() {
        mActivitySendBitcoins.headerLayout.optionMenu.setVisibility(View.VISIBLE);
        mActivitySendBitcoins.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        mActivitySendBitcoins.headerLayout.optionMenu.setText("c");
        mActivitySendBitcoins.headerLayout.optionMenu.setOnClickListener(this);
        mActivitySendBitcoins.headerLayout.sendSpinner.setVisibility(View.GONE);
        mActivitySendBitcoins.activitySendBitcoin.setOnClickListener(this);

        mActivitySendBitcoins.mainLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideSoftKeyboard(SendBitcoinVaultActivity.this);
            }
        });
    }

    /**
     * Method is used to set spinner
     */

    private void setSpinner() {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_black, addressList);
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item_left);
        mActivitySendBitcoins.sendSpinner.setAdapter(dataAdapter);
        mActivitySendBitcoins.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedWalletName = parent.getItemAtPosition(position).toString();
                mSelectedWalletid = mWalletList.get(position).getWALLET_ID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Method is used to setListners on the required views
     */
    private void setListner() {
        mActivitySendBitcoins.headerLayout.backArrow.setOnClickListener(this);
        mActivitySendBitcoins.buttonlayout.setOnClickListener(this);
        mActivitySendBitcoins.amtInBitcoin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (getCurrentFocus() == mActivitySendBitcoins.amtInBitcoin && charSequence != null && !charSequence.toString().equals("")) {
                        double value = Double.parseDouble(charSequence.toString());
                        double total = CURRENT_USD * value;
                        mActivitySendBitcoins.amtInDoller.setText(Utils.convertDecimalFormatPattern(total));
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getCurrentFocus() == mActivitySendBitcoins.amtInBitcoin && editable != null && editable.toString().equals("")) {
                    mActivitySendBitcoins.amtInDoller.setText("");
                }

            }
        });

        mActivitySendBitcoins.amtInDoller.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (getCurrentFocus() == mActivitySendBitcoins.amtInDoller && charSequence != null && !charSequence.toString().equals("")) {
                        double value = Double.parseDouble(charSequence.toString());
                        double total = (1 / CURRENT_USD) * value;
                        mActivitySendBitcoins.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(total));
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getCurrentFocus() == mActivitySendBitcoins.amtInDoller && editable != null && editable.toString().equals("")) {
                    mActivitySendBitcoins.amtInBitcoin.setText("");
                }

            }
        });
    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {

        mActivitySendBitcoins.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.backArrow:
                onBackPressed();
                break;
            case R.id.optionMenu:
                Intent vaultIntent = new Intent(SendBitcoinVaultActivity.this, WalletHomeActivity.class);
                startActivity(vaultIntent);
                break;
            case R.id.buttonlayout:
                if (checkTransactionIsValid()) {
                    if (Utils.isNetworkConnected(this)) {
                        Intent sendIntent = new Intent(SendBitcoinVaultActivity.this, ConfirmActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(RECIEVER_ADDRESS, mSelectedWalletName); //Reciever Wallet Address
                        bundle.putString(IDBConstant.WALLET_ID, mSelectedWalletid);//  Reciever Wallet ID
                        bundle.putInt(SEND_FROM, SEND_VAULT_TO_WALLET);
                        bundle.putString(AMOUNT_TO_SEND, mActivitySendBitcoins.amtInBitcoin.getText().toString().trim());
                        bundle.putString(DESC, mActivitySendBitcoins.descEditText.getText().toString().trim());
                        sendIntent.putExtras(bundle);
                        startActivity(sendIntent);
                    } else {
                        Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);
                    }
                }
                break;
            case R.id.activity_send_bitcoin:
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                break;
        }

    }

    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        this.mVaultDetails = mVaultDetails;
        if (mVaultDetails != null) {

            mActivitySendBitcoins.headerLayout.walletName.setText(mVaultDetails.getVAULT_NAME() + getString(R.string.send_hy));
            if (mVaultDetails.getVAULT_LAST_UPDATE_BALANCE() != null && !mVaultDetails.getVAULT_LAST_UPDATE_BALANCE().equals("")) {
                double valueInDolllar = Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * CURRENT_USD;
                mActivitySendBitcoins.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE())));
                mActivitySendBitcoins.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(valueInDolllar));
            }
        }
    }

    /**
     * method to check that send amount is valid or not and send amount is not greater then available amount
     *
     * @return
     */
    private boolean checkTransactionIsValid() {
        try {
            String amt = mActivitySendBitcoins.amtInBitcoin.getText().toString();
            if (amt.equals("")) {
                Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.please_enter_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (amt.equals(".")) {
                Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Double.parseDouble(amt) <= 0) {
                Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Double.parseDouble(Utils.convertDecimalFormatPattern(Double.parseDouble(amt))) <= 0) {
                Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Utils.convertDecimalFormatPatternDouble(Double.parseDouble(amt) ) > Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE())) {
                Utils.showSnakbar(mActivitySendBitcoins.activitySendBitcoin, getString(R.string.insuficient_bal), Snackbar.LENGTH_SHORT);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null) {
                if (mRequestedWallets.size() > 0) {
                    mWalletList.clear();
                    addressList = new ArrayList<>();
                    for (int i = 0; i < mRequestedWallets.size(); i++) {
                        mWalletList.add(mRequestedWallets.get(i));
                        addressList.add(mRequestedWallets.get(i).getWALLET_NAME());
                    }
                    setSpinner();
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
            if (mActivitySendBitcoins != null && mVaultDetails != null)
                mActivitySendBitcoins.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * CURRENT_USD)));

        }
    };
}
