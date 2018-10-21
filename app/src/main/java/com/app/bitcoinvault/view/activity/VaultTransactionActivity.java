package com.app.bitcoinvault.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.app.bitcoinvault.R;
import com.app.bitcoinvault.adaptor.WalletTransactionListAdaptor;
import com.app.bitcoinvault.bean.TransactionBean;
import com.app.bitcoinvault.bean.WalletTransactionBean;
import com.app.bitcoinvault.database.CRUD_OperationOfDB;
import com.app.bitcoinvault.database.IDBConstant;
import com.app.bitcoinvault.databinding.ActivityVaultTransactionBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bitmanagers.BitVaultWalletManager;
import commons.SecureSDKException;
import iclasses.TransactionHistoryCallback;
import iclasses.VaultDetailsCallback;
import iclasses.WalletArrayCallback;
import model.VaultDetails;
import model.WalletDetails;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;

public class VaultTransactionActivity extends AppCompatActivity implements IDBConstant, View.OnClickListener, IAppConstant, VaultDetailsCallback, TransactionHistoryCallback, WalletArrayCallback {

    private final String TAG = this.getClass().getSimpleName();
    private ActivityVaultTransactionBinding activityVaultTransactionBinding;
    private List<WalletDetails> mWalletList = new ArrayList<>();
    private Double CURRENT_USD = 0.0;
    private VaultDetails mVaultDetails;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private WalletTransactionListAdaptor adaptor;
    private List<WalletTransactionBean> walletTransactionBeanArrayList = new ArrayList<>();
    private List<WalletTransactionBean> tempWalletTransactionBeanArrayList = new ArrayList<>();

    private int SEND_BITCOIN = 101;
    private int SEND_BITCOIN_RESULT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityVaultTransactionBinding = DataBindingUtil.setContentView(this, R.layout.activity_vault_transaction);
        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();
        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        setAdaptor();

        setData();

        setListner();

        setTypeFace();

        requestWallet();

        getVault();

    }

    /**
     * Method is used to set Adapter
     */
    private void setAdaptor() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(VaultTransactionActivity.this);
        activityVaultTransactionBinding.walletRecycleView.setLayoutManager(layoutManager);
        adaptor = new WalletTransactionListAdaptor(tempWalletTransactionBeanArrayList, VaultTransactionActivity.this);
        activityVaultTransactionBinding.walletRecycleView.setAdapter(adaptor);

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
            mBitVaultWalletManager.getWallets(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Method is used to set Spinner Data
     */
    private void setData() {

        Utils.setBackgroundImage(this, activityVaultTransactionBinding.headerLayout.headerLinear, VAULT);
        activityVaultTransactionBinding.bitcoinSymbol.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityVaultTransactionBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_white, generateAddressList());
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        activityVaultTransactionBinding.headerLayout.sendSpinner.setVisibility(View.VISIBLE);
        activityVaultTransactionBinding.headerLayout.sendSpinnerLay.setVisibility(View.VISIBLE);
        activityVaultTransactionBinding.headerLayout.sendSpinner.setAdapter(dataAdapter);
        activityVaultTransactionBinding.headerLayout.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                tempWalletTransactionBeanArrayList.clear();
                if (item.equals("All")) {
                    tempWalletTransactionBeanArrayList.addAll(walletTransactionBeanArrayList);

                } else if (item.equals("Sent")) {
                    for (int i = 0; i < walletTransactionBeanArrayList.size(); i++) {
                        if (walletTransactionBeanArrayList.get(i).getType().equalsIgnoreCase("Sent"))
                            tempWalletTransactionBeanArrayList.add(walletTransactionBeanArrayList.get(i));
                    }

                } else if (item.equals("Received")) {
                    for (int i = 0; i < walletTransactionBeanArrayList.size(); i++) {
                        if (walletTransactionBeanArrayList.get(i).getType().equalsIgnoreCase("Received"))
                            tempWalletTransactionBeanArrayList.add(walletTransactionBeanArrayList.get(i));
                    }
                }
                if (adaptor != null)
                    adaptor.notifyDataSetChanged();
                if (tempWalletTransactionBeanArrayList.size() > 0)
                    activityVaultTransactionBinding.noRecord.setVisibility(View.GONE);
                else
                    activityVaultTransactionBinding.noRecord.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(VaultTransactionActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {

        activityVaultTransactionBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityVaultTransactionBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityVaultTransactionBinding.headerLayout.optionMenu.setText("c");
    }

    /**
     * Method used to get list from string-arrays
     *
     * @return
     */

    private List<String> generateAddressList() {
        List<String> Lines = Arrays.asList(getResources().getStringArray(R.array.spinnerList));
        return Lines;
    }


    /**
     * Method is used to set Listner
     */

    private void setListner() {
        activityVaultTransactionBinding.bottomSend.setOnClickListener(this);
        activityVaultTransactionBinding.headerLayout.backArrow.setOnClickListener(this);
        activityVaultTransactionBinding.headerLayout.optionMenu.setOnClickListener(this);
    }


    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.bottomSend:
                Intent sendvaultIntent = new Intent(this, SendBitcoinVaultActivity.class);
                startActivityForResult(sendvaultIntent, SEND_BITCOIN);
                break;

            case R.id.backArrow:
                onBackPressed();
                break;

            case R.id.optionMenu:
                Intent vaultIntent = new Intent(VaultTransactionActivity.this, WalletHomeActivity.class);
                startActivity(vaultIntent);
                break;
        }
    }


    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        this.mVaultDetails = mVaultDetails;
        if (mVaultDetails != null) {

            activityVaultTransactionBinding.headerLayout.walletName.setText(mVaultDetails.getVAULT_NAME());
            if (mVaultDetails.getVAULT_LAST_UPDATE_BALANCE() != null && !mVaultDetails.getVAULT_LAST_UPDATE_BALANCE().equals("")) {
                double valueInDolllar = Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * CURRENT_USD;
                activityVaultTransactionBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE())));
                activityVaultTransactionBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(valueInDolllar));
            }
            if (Utils.isNetworkConnected(this))
                getUnspentCount();
            else
                Utils.showSnakbar(activityVaultTransactionBinding.linear, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);

        }

    }

    /**
     * this method is used to get transaction history from SDK
     */
    public void getUnspentCount() {
        if (mBitVaultWalletManager != null) {
            try {
                activityVaultTransactionBinding.progressBar.setVisibility(View.VISIBLE);
                mBitVaultWalletManager.getVaultTransactionsHistory(this);

            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void transactionHistorySuccess(JSONObject mHistoryResponse, String wallet_address) {
        TransactionBean transactionBean = new Gson().fromJson(mHistoryResponse.toString(), TransactionBean.class);
        try {
            if (transactionBean != null && transactionBean.getItems() != null) {

                parseValue(transactionBean.getItems(), wallet_address);
                if (adaptor != null)
                    adaptor.notifyDataSetChanged();
                activityVaultTransactionBinding.headerLayout.sendSpinner.setSelection(0);
                if (tempWalletTransactionBeanArrayList.size() > 0)
                    activityVaultTransactionBinding.noRecord.setVisibility(View.GONE);
                else
                    activityVaultTransactionBinding.noRecord.setVisibility(View.VISIBLE);

            }

            activityVaultTransactionBinding.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void allWalletsTransactionHistory(JSONObject mHistoryResponse, ArrayList<String> mWalletsList) {

    }

    @Override
    public void transactionHistoryFailed(VolleyError mVolleyError) {

    }

    /**
     * this method is used to prares data and distinguish b/w send and recieve transaction
     *
     * @param listData
     * @param address
     */
    private void parseValue(List<TransactionBean.Tx> listData, String address) {
        this.walletTransactionBeanArrayList.clear();
        this.tempWalletTransactionBeanArrayList.clear();
        Map<String, String> desc = getDescFromDB();
        for (int k = 0; k < listData.size(); k++) {

            WalletTransactionBean walletTransactionBean = new WalletTransactionBean();
            TransactionBean.Tx data = listData.get(k);
            // type is used for distinguish b/w send and recieve transaction
            String type = "Received";
            Double amount = 0.0;
            if (data != null) {
                ArrayList<TransactionBean.Vin> vIn = data.getVin();
                //Vout contain the list of all in transaction
                if (vIn != null && vIn.size() > 0) {
                    for (int i = 0; i < vIn.size(); i++) {
                        if (vIn.get(i).getAddr().equals(address))
                            type = "Sent";
                    }
                }
                walletTransactionBean.setType(type);
                //Vout contain the list of all out transaction
                ArrayList<TransactionBean.Vout> vOut = data.getVout();
                if (vOut != null && vOut.size() > 0) {

                    if (type.equals("Received")) {
                        if (mWalletList != null && mWalletList.size() > 0) {
                            for (int i = 0; i < mWalletList.size(); i++) {
                                if (mWalletList.get(i).getmKeyPair().address.equals(vIn.get(0).getAddr())) {
                                    walletTransactionBean.setName("Address - " + mWalletList.get(i).getWALLET_NAME());
                                    break;
                                }
                            }
                        } else
                            walletTransactionBean.setName("Address - " + vIn.get(0).getAddr());

                        for (int i = 0; i < vOut.size(); i++) {
                            if (vOut.get(i).getScriptPubKey().getAddresses().get(0).equals(address)) {
                                amount = Double.parseDouble(vOut.get(i).getValue());
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < vOut.size(); i++) {
                            if (!vOut.get(i).getScriptPubKey().getAddresses().get(0).equals(address)) {
                                amount += Double.parseDouble(vOut.get(i).getValue());

                            }
                        }
                        amount += data.getFees();
                        if (mWalletList != null && mWalletList.size() > 0) {
                            for (int i = 0; i < mWalletList.size(); i++) {
                                if (mWalletList.get(i).getmKeyPair().address.equals(vOut.get(0).getScriptPubKey().getAddresses().get(0))) {
                                    walletTransactionBean.setName("Address - " + mWalletList.get(i).getWALLET_NAME());
                                    break;
                                }
                            }
                        } else
                            walletTransactionBean.setName("Address - " + vOut.get(0).getScriptPubKey().getAddresses().get(0));

                        if (desc != null && desc.size() > 0) {
                            Iterator myVeryOwnIterator = desc.keySet().iterator();
                            while (myVeryOwnIterator.hasNext()) {
                                String txid = (String) myVeryOwnIterator.next();
                                if (data.getTxid().equals(txid)) {
                                    String value = desc.get(txid);

                                    walletTransactionBean.setDescription("Description - " + value);
                                    break;
                                }
                            }
                        }

                    }
                }

            }


            /**
             * Decimal format is used to show decimal value upto some precision
             */

            walletTransactionBean.setBitcoin(Utils.convertDecimalFormatPattern(amount));
            assert data != null;

            walletTransactionBean.setDate(getDate((data.getTime() * 1000L) + ""));


            walletTransactionBean.setTxId("TXID - " + data.getTxid());

            if (data.getConfirmations() > 0)
                walletTransactionBean.setStatus("Confirmed");
            else
                walletTransactionBean.setStatus("Pending");

            walletTransactionBean.setVisibility(View.GONE);

            this.walletTransactionBeanArrayList.add(walletTransactionBean);
            this.tempWalletTransactionBeanArrayList.add(walletTransactionBean);
        }
    }

    /**
     * get desc from database to show desc
     *
     * @return map of desc
     */

    private Map<String, String> getDescFromDB() {
        Map<String, String> desc = null;

        CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);
        Cursor mCursor = dbOperation.fetchData(TABLE_NAME, null, IDBConstant.WALLET_ID + "=?", new String[]{VAULT}, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            desc = new HashMap<>();
            mCursor.moveToFirst();
            do {
                desc.put(mCursor.getString(mCursor.getColumnIndex(TXD_ID)), mCursor.getString(mCursor.getColumnIndex(MESSAGE)));
            } while ((mCursor.moveToNext()));
        }
        return desc;
    }

    /**
     * this method convert timestamp to mm/dd/yyyy format
     *
     * @param timeStampStr timestamp as a input
     * @return return date in mm/dd/yyyy format
     */
    private String getDate(String timeStampStr) {
        try {
            DateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  HH:mm");
            Date netDate = (new Date(Long.parseLong(timeStampStr)));
            return sdf.format(netDate);
        } catch (Exception ignored) {
            return "";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_BITCOIN && resultCode == SEND_BITCOIN_RESULT && mBitVaultWalletManager != null) {

            getVault();
        }
    }


    @Override
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null) {
                if (mRequestedWallets.size() > 0) {
                    mWalletList.clear();
                    mWalletList.addAll(mRequestedWallets);
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
            double value = intent.getDoubleExtra(currency, 0.0);
            if (activityVaultTransactionBinding != null && mVaultDetails != null)
                activityVaultTransactionBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(mVaultDetails.getVAULT_LAST_UPDATE_BALANCE()) * value)));

        }
    };
}