package com.app.bitcoinvault.bean;

/**
 * Created by admin on 30-08-2017.
 */

public class ContactsModel {

    private String mName;
    private String mReceiverAddress;
    private String mImage;

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmReceiverAddress() {
        return mReceiverAddress;
    }

    public void setmReceiverAddress(String mReceiverAddress) {
        this.mReceiverAddress = mReceiverAddress;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    public ContactsModel(String mName, String mReceiverAddress, String mImage) {
        this.mName = mName;
        this.mReceiverAddress = mReceiverAddress;
        this.mImage = mImage;
    }

    public ContactsModel() {
    }
}