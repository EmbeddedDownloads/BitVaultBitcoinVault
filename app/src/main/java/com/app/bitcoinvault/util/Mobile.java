package com.app.bitcoinvault.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by admin on 01-09-2017.
 */

public class Mobile implements Parcelable {
    public static final Creator<Mobile> CREATOR = new Creator<Mobile>() {
        @Override
        public Mobile createFromParcel(Parcel in) {
            return new Mobile(in);
        }

        @Override
        public Mobile[] newArray(int size) {
            return new Mobile[size];
        }
    };

    private String number;
    private String numberType;

    public Mobile() {

    }

    public Mobile(String number, String numberType) {
        this.number = number;
        this.numberType = numberType;
    }

    private Mobile(Parcel in) {
        number = in.readString();
        numberType = in.readString();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(number);
        parcel.writeString(numberType);
    }

    public static int emptyCounter(ArrayList<Mobile> mobileList){
        int counter=0;
        for(int i=0;i<mobileList.size();i++){
            if(mobileList.get(i).getNumber()==null||mobileList.get(i).getNumber().length()==0){
                counter++;
            }
        }
        return counter;
    }
}