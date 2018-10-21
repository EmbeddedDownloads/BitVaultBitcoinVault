package com.app.bitcoinvault.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * <p>
 * This singleton class create table in Sqlite DB
 */


public class SqlLiteDB extends SQLiteOpenHelper implements IDBConstant {

    // If you change the database schema, you must increment the database version.


    private static final String SQL_CREATE_ENTRIES =

            "CREATE TABLE " + TABLE_NAME + " (" +
                    TXD_ID + TEXT_TYPE + " PRIMARY KEY," +
                    MESSAGE + TEXT_TYPE + COMMA_SEP +
                    WALLET_ID + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static SqlLiteDB objSqlLiteDB;


    private SqlLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Through this we will create only pne instance of sqllite
    public static SqlLiteDB getInstance(Context mContext) {
        if (objSqlLiteDB == null) {
            objSqlLiteDB = new SqlLiteDB(mContext);
        }
        return objSqlLiteDB;
    }


    // This is Overrided method called once , it checks whther table exist in DB if not then create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * This is Overrided method called when any version of DB is changed
     *
     * @param db         -- db
     * @param oldVersion -- Old version of the DB
     * @param newVersion -- New Version of the DB
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        db.execSQL(SQL_DELETE_ENTRIES);
    }


}
