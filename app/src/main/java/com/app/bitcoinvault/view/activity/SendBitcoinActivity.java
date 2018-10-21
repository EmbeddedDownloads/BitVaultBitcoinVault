package com.app.bitcoinvault.view.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.adaptor.AutoCompleteAdapter;
import com.app.bitcoinvault.bean.ContactsModel;
import com.app.bitcoinvault.database.IDBConstant;
import com.app.bitcoinvault.databinding.ActivitySendBitcoinBinding;
import com.app.bitcoinvault.databinding.WalletEmptyDialogBinding;
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
import qrcode.ScanQRCode;
import utils.SDKUtils;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;

/**
 * this wallet send bitcoin activity
 */
public class SendBitcoinActivity extends AppCompatActivity implements IDBConstant, WalletArrayCallback, View.OnClickListener, IAppConstant, VaultDetailsCallback {

    private static final int REQUEST_SCAN_PRIVATE_KEY = 0;
    private static final int MY_PERMISSIONS_CONTACT = 101;
    private final String TAG = this.getClass().getSimpleName();
    private ActivitySendBitcoinBinding activitySendBitcoinBinding;
    private ArrayList<String> addressList = new ArrayList<>();
    private List<WalletDetails> mWalletList = new ArrayList<>();
    private Double CURRENT_USD = 0.0;
    private WalletDetails walletDetails = null;
    private VaultDetails mVaultDetails;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private ArrayAdapter<String> dataAdapter = null;
    private String selected_wallet_id;
    private boolean isTransactionDone = false;
    private boolean isTransTypeVault = false;
    private ArrayList<ContactsModel> mContactsModel = new ArrayList<>();
    private AutoCompleteAdapter autoCompleteAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySendBitcoinBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_bitcoin);

        if (ContextCompat.checkSelfPermission(SendBitcoinActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Utils.allocateRunTimePermissions(SendBitcoinActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, new int[]{MY_PERMISSIONS_CONTACT});
        }

        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();

        ArrayList<ContactsModel> meContact = AppPreferences.getInstance(this).getMEContact();
        if (meContact != null && meContact.size() > 0) {
            mContactsModel.addAll(meContact);
        }

        ArrayList<ContactsModel> contact = AppPreferences.getInstance(this).getContact();
        if (contact != null && contact.size() > 0) {
            mContactsModel.addAll(contact);
        }

        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        getVault();

        init();

        getDataFromIntent();

        setTypeFace();


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

        Utils.setBackgroundImage(this, activitySendBitcoinBinding.mainLinear, WALLET);

        activitySendBitcoinBinding.headerLayout.optionMenu.setText("d");
        activitySendBitcoinBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);
        activitySendBitcoinBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activitySendBitcoinBinding.headerLayout.optionMenu.setOnClickListener(this);
        activitySendBitcoinBinding.headerLayout.sendSpinner.setVisibility(View.GONE);

        activitySendBitcoinBinding.mainLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideSoftKeyboard(SendBitcoinActivity.this);
            }
        });

        setSpinner();

        setListner();


    }


    /**
     * this method is used to refersh view after send transaction is conf
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        requestWallet();
        activitySendBitcoinBinding.recieverAddrerssTextView.setText("");
        activitySendBitcoinBinding.amtInBitcoin.setText("");
        activitySendBitcoinBinding.descEditText.setText("");
        activitySendBitcoinBinding.amtInDoller.setText("");

        activitySendBitcoinBinding.recieverAddrerssTextView.requestFocus();
        isTransactionDone = true;
    }

    /**
     * Method is used the spinner
     */

    private void setSpinner() {
        dataAdapter = new ArrayAdapter<String>(this, R.layout.wallet_spinner_item, addressList);
        dataAdapter.setDropDownViewResource(R.layout.wallet_spinner_item);
        activitySendBitcoinBinding.sendSpinner.setAdapter(dataAdapter);
        activitySendBitcoinBinding.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        if (getIntent() != null && getIntent().getExtras() != null) {
            selected_wallet_id = getIntent().getStringExtra(IAppConstant.WALLET_ID);
            requestWallet();
        }

    }

    /**
     * update header
     *
     * @param wallet wallet object to update
     */
    private void updateHeaderAndWallet(WalletDetails wallet) {
        if (wallet != null) {
            activitySendBitcoinBinding.headerLayout.walletName.setText(wallet.getWALLET_NAME() + getString(R.string.send_hy));
            activitySendBitcoinBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(wallet.getWALLET_LAST_UPDATE_BALANCE())));
            walletDetails = wallet;
            double total = CURRENT_USD * Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE());
            activitySendBitcoinBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
        }
    }

    /**
     * Method is used to setListners on the required views
     */
    private void setListner() {
        activitySendBitcoinBinding.sendBtn.setOnClickListener(this);
        activitySendBitcoinBinding.qrScanView.setOnClickListener(this);
        activitySendBitcoinBinding.headerLayout.backArrow.setOnClickListener(this);
        activitySendBitcoinBinding.emptyWalletBtn.setOnClickListener(this);
        activitySendBitcoinBinding.sendtoVault.setOnClickListener(this);
        activitySendBitcoinBinding.amtInBitcoin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (getCurrentFocus() == activitySendBitcoinBinding.amtInBitcoin && charSequence != null && !charSequence.toString().equals("")) {
                        double value = Double.parseDouble(charSequence.toString());
                        double total = CURRENT_USD * value;
                        activitySendBitcoinBinding.amtInDoller.setText(Utils.convertDecimalFormatPattern(total));
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getCurrentFocus() == activitySendBitcoinBinding.amtInBitcoin && editable != null && editable.toString().equals("")) {
                    activitySendBitcoinBinding.amtInDoller.setText("");
                }

            }
        });

        activitySendBitcoinBinding.amtInDoller.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (getCurrentFocus() == activitySendBitcoinBinding.amtInDoller && charSequence != null && !charSequence.toString().equals("")) {
                        double value = Double.parseDouble(charSequence.toString());
                        double total = (1 / CURRENT_USD) * value;
                        activitySendBitcoinBinding.amtInBitcoin.setText(Utils.convertDecimalFormatPattern(total));
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (getCurrentFocus() == activitySendBitcoinBinding.amtInDoller && editable != null && editable.toString().equals("")) {
                    activitySendBitcoinBinding.amtInBitcoin.setText("");
                }

            }
        });
        activitySendBitcoinBinding.recieverAddrerssTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (isTransTypeVault && getCurrentFocus() == activitySendBitcoinBinding.recieverAddrerssTextView) {
                    isTransTypeVault = false;
                    if (before - count == 1) {
                        activitySendBitcoinBinding.recieverAddrerssTextView.setText("");

                    } else {
                        String str = charSequence.toString().replace(mVaultDetails.getVAULT_NAME(), "");
                        activitySendBitcoinBinding.recieverAddrerssTextView.setText(str);
                        activitySendBitcoinBinding.recieverAddrerssTextView.setSelection(activitySendBitcoinBinding.recieverAddrerssTextView.getText().length());
                    }
                }
                if (charSequence.toString().length() > 0 && getCurrentFocus() == activitySendBitcoinBinding.recieverAddrerssTextView) {
                    filterMatchedData();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Method used to match data from contacts
     */
    private void filterMatchedData() {

        if (mContactsModel != null && mContactsModel.size() > 0) {

            if (activitySendBitcoinBinding != null) {

                if (autoCompleteAdapter != null) {
                    activitySendBitcoinBinding.recieverAddrerssTextView.setAdapter(autoCompleteAdapter);
                    activitySendBitcoinBinding.recieverAddrerssTextView.setThreshold(1);
                } else {
                    autoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.spinner_autocomplete_layout, mContactsModel, mContactsModel);
                    activitySendBitcoinBinding.recieverAddrerssTextView.setAdapter(autoCompleteAdapter);
                    activitySendBitcoinBinding.recieverAddrerssTextView.setThreshold(0);


                }
            }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendBtn: {
                if (checkTransactionIsValid()) {
                    if (Utils.isNetworkConnected(this)) {
                        Intent sendIntent = new Intent(SendBitcoinActivity.this, ConfirmActivity.class);
                        Bundle bundle = new Bundle();
                        if (isTransTypeVault) {//Check transaction type is wallet to wallet or wallet to vault
                            bundle.putString(RECIEVER_ADDRESS, mVaultDetails.getVAULT_NAME());
                            bundle.putInt(SEND_FROM, SEND_WALLET_TO_VAULT);
                        } else {
                            bundle.putString(RECIEVER_ADDRESS, activitySendBitcoinBinding.recieverAddrerssTextView.getText().toString().trim());
                            bundle.putInt(SEND_FROM, SEND_WALLET_TO_WALLET);
                        }
                        bundle.putString(IDBConstant.WALLET_ID, walletDetails.getWALLET_ID());
                        bundle.putString(AMOUNT_TO_SEND, activitySendBitcoinBinding.amtInBitcoin.getText().toString().trim());
                        bundle.putString(DESC, activitySendBitcoinBinding.descEditText.getText().toString().trim());
                        sendIntent.putExtras(bundle);
                        startActivity(sendIntent);
                    } else {
                        Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);
                    }
                }
                break;
            }
            case R.id.sendtoVault: {
                isTransTypeVault = true;
                activitySendBitcoinBinding.recieverAddrerssTextView.setFocusable(false);
                if (mVaultDetails != null) {
                    activitySendBitcoinBinding.recieverAddrerssTextView.setText(mVaultDetails.getVAULT_NAME());
                }
                activitySendBitcoinBinding.recieverAddrerssTextView.setFocusableInTouchMode(true);
                activitySendBitcoinBinding.recieverAddrerssTextView.setFocusable(true);
                activitySendBitcoinBinding.recieverAddrerssTextView.setSelection(activitySendBitcoinBinding.recieverAddrerssTextView.getText().length());
                break;
            }
            case R.id.emptyWalletBtn: {
                if (checkEmptyToVault()) {
                    if (Utils.isNetworkConnected(this)) {
                        showConfirmDialog();
                    } else {
                        Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);
                    }
                }
                break;
            }
            case R.id.backArrow: {
                onBackPressed();
                break;
            }
            case R.id.qrScanView: {
                startActivityForResult(new Intent(this, ScanQRCode.class), REQUEST_SCAN_PRIVATE_KEY);
                break;
            }
            case R.id.optionMenu:
                Intent vaultIntent = new Intent(this, VaultTransactionActivity.class);
                startActivity(vaultIntent);
                break;

        }
    }

    /**
     * get vault from SDK
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
     * method to check that send amount is valid or not and send amount is not greater then available amount
     *
     * @return
     */
    private boolean checkTransactionIsValid() {
        try {
            String amt = activitySendBitcoinBinding.amtInBitcoin.getText().toString();
            if (activitySendBitcoinBinding.recieverAddrerssTextView.getText().toString().trim().equals("")) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.reciever_add), Snackbar.LENGTH_SHORT);
                return false;
            }
            if (activitySendBitcoinBinding.recieverAddrerssTextView.getText().toString().trim().length() < 34 && isTransTypeVault == false) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.Please_enter_valid_address), Snackbar.LENGTH_SHORT);
                return false;
            } else if (walletDetails.getmKeyPair().address.equals(activitySendBitcoinBinding.recieverAddrerssTextView.getText().toString())) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.please_enter_valid_address), Snackbar.LENGTH_SHORT);
                return false;
            } else if (amt.equals("")) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.please_enter_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (amt.equals(".")) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Double.parseDouble(amt) <= 0) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Double.parseDouble(Utils.convertDecimalFormatPattern(Double.parseDouble(amt))) <= 0) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.Please_enter_valid_amount), Snackbar.LENGTH_SHORT);
                return false;
            } else if (Utils.convertDecimalFormatPatternDouble(Double.parseDouble(amt)) > Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())) {
                Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.insuficient_bal), Snackbar.LENGTH_SHORT);
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * this method is used to validate when wallet is empty
     *
     * @return
     */
    private boolean checkEmptyToVault() {
        if (Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) <= 0) {
            Utils.showSnakbar(activitySendBitcoinBinding.sendBitcoinLay, getString(R.string.insuficient_bal), Snackbar.LENGTH_SHORT);
            return false;
        }
        return true;
    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {
        activitySendBitcoinBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activitySendBitcoinBinding.qrScanView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activitySendBitcoinBinding.bitcoinIconTextView.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
    }

    /**
     * this method is used to show conf dilaog for empty wallet
     */
    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WalletEmptyDialogBinding walletEmptyDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.wallet_empty_dialog, null, false);
        dialog.setContentView(walletEmptyDialogBinding.getRoot());
        String msg = getString(R.string.confirmation_msg) + Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())) + getString(R.string.btc_to) + walletDetails.getWALLET_NAME() + getString(R.string.to) + mVaultDetails.getVAULT_NAME() + getString(R.string.question_mark);
        walletEmptyDialogBinding.confMsg.setText(msg);
        walletEmptyDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();

                Intent sendIntent = new Intent(SendBitcoinActivity.this, ConfirmActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(RECIEVER_ADDRESS, mVaultDetails.getVAULT_NAME());
                bundle.putInt(SEND_FROM, SEND_SINGAL_WALLET_EMPTY);
                bundle.putString(IDBConstant.WALLET_ID, walletDetails.getWALLET_ID());
                double amt = Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE());
                bundle.putString(AMOUNT_TO_SEND, amt + "");
                bundle.putString(DESC, activitySendBitcoinBinding.descEditText.getText().toString().trim());
                sendIntent.putExtras(bundle);
                startActivity(sendIntent);

            }
        });
        walletEmptyDialogBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
        dialog.show();

    }


    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null) {
                if (mRequestedWallets.size() > 0) {
                    int selectedWalletPostion = 0;
                    mWalletList.clear();
                    addressList.clear();
                    for (int i = 0; i < mRequestedWallets.size(); i++) {
                        mWalletList.add(mRequestedWallets.get(i));
                        addressList.add(mRequestedWallets.get(i).getWALLET_NAME());

                        if (mWalletList.get(i).getWALLET_ID().equals(selected_wallet_id)) {
                            selectedWalletPostion = i;
                            walletDetails = mWalletList.get(i);
                            if (walletDetails != null) {
                                activitySendBitcoinBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME() + getString(R.string.send_hy));
                                activitySendBitcoinBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
                                activitySendBitcoinBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
                            }
                        }
                    }
                    if (dataAdapter != null) {
                        dataAdapter.notifyDataSetChanged();
                        activitySendBitcoinBinding.sendSpinner.setSelection(selectedWalletPostion);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String scannedResult = data.getStringExtra("data");
            scannedResult = scannedResult.replace("bitcoin:", "");
            activitySendBitcoinBinding.recieverAddrerssTextView.setText(scannedResult);
            activitySendBitcoinBinding.recieverAddrerssTextView.setSelection(scannedResult.length());
        }
    }


    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        if (mVaultDetails != null) {
            this.mVaultDetails = mVaultDetails;
        }
    }


    /**
     * This is a callback method which is call when user is click on contact list
     *
     * @param selectedContact object of selected contact
     */
    public void setSelectedContact(ContactsModel selectedContact) {
        Utils.hideSoftKeyboard(this);
        activitySendBitcoinBinding.recieverAddrerssTextView.dismissDropDown();
        activitySendBitcoinBinding.recieverAddrerssTextView.setFocusable(false);
        activitySendBitcoinBinding.recieverAddrerssTextView.setFocusableInTouchMode(false);
        activitySendBitcoinBinding.recieverAddrerssTextView.setText(selectedContact.getmReceiverAddress());
        activitySendBitcoinBinding.recieverAddrerssTextView.setFocusable(true);
        activitySendBitcoinBinding.recieverAddrerssTextView.setFocusableInTouchMode(true);
        activitySendBitcoinBinding.recieverAddrerssTextView.setSelection(activitySendBitcoinBinding.recieverAddrerssTextView.getText().length());
    }

    /**
     * Method to get callback of permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_CONTACT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FetchContacts().execute();
                    return;
                }
            }
        }

    }

    /**
     * Class is used to fetch contacts from the device
     */

    private class FetchContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) findViewById(R.id.progressBar1);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mContactsModel = Utils.readPhoneContacts(SendBitcoinActivity.this);
            if (mContactsModel != null && mContactsModel.size() > 0) {
                AppPreferences.getInstance(SendBitcoinActivity.this).setContact(mContactsModel);
                SDKUtils.showLog("Contacts", mContactsModel.size() + " ");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
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
            if (activitySendBitcoinBinding != null && walletDetails != null)
                activitySendBitcoinBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD)));

        }
    };

}
