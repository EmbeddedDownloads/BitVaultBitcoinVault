package com.app.bitcoinvault.database;

/**
 * Class will contains all fields to be related to database
 */


public interface IDBConstant {


    String TABLE_NAME = "description";
    String MESSAGE = "message";
    String TXD_ID = "transaction_id";
    String WALLET_ID = "wallet_id";

    int DATABASE_VERSION = 1;
    String DATABASE_NAME = "BitcoinVaultApp.db";
    String TEXT_TYPE = " TEXT";
    String INTEGER_TYPE = " INTEGER";
    String COMMA_SEP = ",";

}
