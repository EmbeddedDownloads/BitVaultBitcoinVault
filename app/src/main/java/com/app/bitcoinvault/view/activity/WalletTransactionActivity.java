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
import android.util.Log;
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
import com.app.bitcoinvault.databinding.ActivityWalletTransactionBinding;
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
import iclasses.WalletCallback;
import model.VaultDetails;
import model.WalletDetails;

import static bitmanagers.BitVaultWalletManager.getWalletInstance;


/**
 * this wallet transaction activity
 */
public class WalletTransactionActivity extends AppCompatActivity implements View.OnClickListener, WalletCallback, VaultDetailsCallback, TransactionHistoryCallback, IAppConstant, IDBConstant {

    private final String TAG = this.getClass().getSimpleName();
    private ActivityWalletTransactionBinding activityWalletTransactionBinding;
    private WalletDetails walletDetails = null;
    private WalletTransactionListAdaptor adaptor;
    private List<WalletTransactionBean> walletTransactionBeanArrayList = new ArrayList<>();
    private List<WalletTransactionBean> tempWalletTransactionBeanArrayList = new ArrayList<>();
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private Double CURRENT_USD = 0.0;
    private String selected_wallet_id = "";
    private int SEND_BITCOIN = 101;
    private int SEND_BITCOIN_RESULT = 102;
    private VaultDetails mVaultDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWalletTransactionBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_transaction);
        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();
        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        getVault();

        setAdaptor();

        getDataFromIntent();

        setSpinnerData();

        setListner();

        setTypeFace();

    }

    /**
     * Method is used to set Adapter
     */
    private void setAdaptor() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(WalletTransactionActivity.this);
        activityWalletTransactionBinding.walletRecycleView.setLayoutManager(layoutManager);
        adaptor = new WalletTransactionListAdaptor(tempWalletTransactionBeanArrayList, WalletTransactionActivity.this);
        activityWalletTransactionBinding.walletRecycleView.setAdapter(adaptor);

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
     * Method is used to set Spinner Data
     */
    private void setSpinnerData() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_white, generateAddressList());
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        activityWalletTransactionBinding.headerLayout.sendSpinner.setVisibility(View.VISIBLE);
        activityWalletTransactionBinding.headerLayout.sendSpinnerLay.setVisibility(View.VISIBLE);
        activityWalletTransactionBinding.headerLayout.sendSpinner.setAdapter(dataAdapter);

        activityWalletTransactionBinding.headerLayout.sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                    activityWalletTransactionBinding.noRecord.setVisibility(View.GONE);
                else
                    activityWalletTransactionBinding.noRecord.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
     * Method is used to set data
     */
    private void setData() {
        if (walletDetails != null) {
            Utils.setBackgroundImage(this, activityWalletTransactionBinding.headerLayout.headerLinear, WALLET);
            activityWalletTransactionBinding.buttonlayout.setVisibility(View.VISIBLE);
            activityWalletTransactionBinding.recieveBitcoinSymbol.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
            activityWalletTransactionBinding.sendBitcoinSymbol.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
            activityWalletTransactionBinding.headerLayout.optionMenu.setText("d");
            activityWalletTransactionBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);
            activityWalletTransactionBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));
            activityWalletTransactionBinding.headerLayout.walletName.setText(walletDetails.getWALLET_NAME());
            activityWalletTransactionBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * CURRENT_USD));
            if (Utils.isNetworkConnected(this))
                getUnspentCount(Integer.parseInt(walletDetails.getWALLET_ID()));
            else
                Utils.showSnakbar(activityWalletTransactionBinding.linear, getString(R.string.intnetcheck), Snackbar.LENGTH_SHORT);

        }
    }

    /**
     * this method is used to get transaction history from SDK
     *
     * @param id wallet id
     */
    public void getUnspentCount(int id) {
        if (mBitVaultWalletManager != null) {
            try {
                activityWalletTransactionBinding.progressBar.setVisibility(View.VISIBLE);
                mBitVaultWalletManager.getTransactionsHistory(id, this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method is used to set Listner
     */

    private void setListner() {
        activityWalletTransactionBinding.receiveBtn.setOnClickListener(this);
        activityWalletTransactionBinding.sendBtn.setOnClickListener(this);
        activityWalletTransactionBinding.headerLayout.backArrow.setOnClickListener(this);
        activityWalletTransactionBinding.headerLayout.optionMenu.setOnClickListener(this);

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


    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.sendBtn:

                Intent sendIntent = new Intent(this, SendBitcoinActivity.class);
                sendIntent.putExtra(IAppConstant.WALLET_ID, walletDetails.getWALLET_ID());
                startActivityForResult(sendIntent, SEND_BITCOIN);

                break;

            case R.id.receiveBtn:

                Intent receiveIntent = new Intent(this, ReceiveBitcoins.class);
                receiveIntent.putExtra(IAppConstant.WALLET_ID, walletDetails.getWALLET_ID());
                startActivity(receiveIntent);
                break;

            case R.id.backArrow:
                onBackPressed();
                break;

            case R.id.optionMenu:
                Intent vaultIntent = new Intent(WalletTransactionActivity.this, VaultTransactionActivity.class);
                startActivity(vaultIntent);
                break;

        }
    }


    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {

        activityWalletTransactionBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityWalletTransactionBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

    }


    @Override
    public void transactionHistorySuccess(JSONObject mHistoryResponse, String wallet_address) {
        TransactionBean transactionBean = new Gson().fromJson(mHistoryResponse.toString(), TransactionBean.class);
        try {
            if (transactionBean != null && transactionBean.getItems() != null) {

                parseValue(transactionBean.getItems(), walletDetails.getmKeyPair().address);
                if (adaptor != null)
                    adaptor.notifyDataSetChanged();
                activityWalletTransactionBinding.headerLayout.sendSpinner.setSelection(0);
                if (tempWalletTransactionBeanArrayList.size() > 0)
                    activityWalletTransactionBinding.noRecord.setVisibility(View.GONE);
                else
                    activityWalletTransactionBinding.noRecord.setVisibility(View.VISIBLE);

            }

            activityWalletTransactionBinding.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void allWalletsTransactionHistory(JSONObject mHistoryResponse, ArrayList<String> mWalletsList) {

    }

    @Override
    public void transactionHistoryFailed(VolleyError mVolleyError) {
        Log.e("mHistoryResponse", "mHistoryResponse" + mVolleyError);
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

                        if (mVaultDetails.getmKeyPair().address.equals(vIn.get(0).getAddr()))
                            walletTransactionBean.setName("Address - " + mVaultDetails.getVAULT_NAME());
                        else
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
                        if (mVaultDetails.getmKeyPair().address.equals(vOut.get(0).getScriptPubKey().getAddresses().get(0)))
                            walletTransactionBean.setName("Address - " + mVaultDetails.getVAULT_NAME());
                        else
                            walletTransactionBean.setName("Address - " + vOut.get(0).getScriptPubKey().getAddresses().get(0));

                        if (desc != null && desc.size() > 0) {
                            Iterator myVeryOwnIterator = desc.keySet().iterator();
                            while (myVeryOwnIterator.hasNext()) {
                                String txid = (String) myVeryOwnIterator.next();
                                if (data.getTxid().equals(txid)) {
                                    String value = (String) desc.get(txid);

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


    private Map<String, String> getDescFromDB() {
        Map<String, String> desc = null;

        CRUD_OperationOfDB dbOperation = new CRUD_OperationOfDB(this);
        Cursor mCursor = dbOperation.fetchData(TABLE_NAME, null, IDBConstant.WALLET_ID + "=?", new String[]{walletDetails.getWALLET_ID()}, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            desc = new HashMap<>();
            mCursor.moveToFirst();
            do {
                desc.put(mCursor.getString(mCursor.getColumnIndex(TXD_ID)), mCursor.getString(mCursor.getColumnIndex(MESSAGE)));
            } while ((mCursor.moveToNext()));
        }
        return desc;
    }

    @Override
    public void getWallet(WalletDetails mWalletDetails) {
        if (mWalletDetails != null) {
            walletDetails = mWalletDetails;
            setData();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_BITCOIN && resultCode == SEND_BITCOIN_RESULT && !selected_wallet_id.equals("") && mBitVaultWalletManager != null) {
            try {
                mBitVaultWalletManager.getWallet(Integer.parseInt(selected_wallet_id),wallet_type, this);
            } catch (SecureSDKException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void vaultDetails(VaultDetails mVaultDetails) {
        if (mVaultDetails != null)
            this.mVaultDetails = mVaultDetails;
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
            if (activityWalletTransactionBinding != null && walletDetails != null)
                activityWalletTransactionBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE()) * value)));

        }
    };

}