package com.app.bitcoinvault.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * This class contain all the CRUD (CREATE ,READ , UPDATE , DELETE) Operations
 * to be performed on the Database
 *
 */


public class CRUD_OperationOfDB {

    private Context mContext;

    public CRUD_OperationOfDB(
            Context mContext) {
        this.mContext = mContext;
    }


    // ===============  CREATE OPERATION ========================

    /**
     * Method to insert values in the DB (CREATE OPERATION)
     *
     * @param tableName -- Name of the table
     * @param values    -- Content Values
     * @return -- Primary id generated
     */

    public long InsertIntoDB(String tableName, ContentValues values) {

        return SqlLiteDB.getInstance(mContext).getWritableDatabase().insert(tableName, null, values);
    }


    // ===============  READ OPERATION ========================

    /**
     * Method to get data from DB having following criterias (READ Operation)
     *
     * @param tableName      -- Nama of the table
     * @param projection     -- Column to return
     * @param selection      --  The columns for the WHERE clause
     * @param selectionArgus -- The values for the WHERE clause
     * @param sortOrder      --   Sorting Order
     * @return -- Cursor (Contains all results )
     */
    public Cursor fetchData(String tableName, String[] projection, String selection, String[] selectionArgus, String sortOrder) {

        return SqlLiteDB.getInstance(mContext).getReadableDatabase().query(tableName, projection, selection, selectionArgus, null, null, sortOrder);
    }

    // ===============  UPDATE OPERATION ========================

    /**
     * Method to update values in DB
     *
     * @param tableName     -- tableName
     * @param selection     -- column name as selection
     * @param selectionArgs -- column values as selection
     * @param values        -- Content values
     * @return -- No of rows updates
     */
    public int UpdateDB(String tableName, String selection, String[] selectionArgs, ContentValues values) {

        return SqlLiteDB.getInstance(mContext).getReadableDatabase().update(tableName, values, selection, selectionArgs);
    }


    // ===============  DELETE OPERATION ========================

    /**
     * Method to delete entry from Database
     *
     * @param tableName     --  table Name
     * @param selection     --  column name as selection
     * @param selectionArgs --  column values as selection
     * @return -- No of rows deleted
     */
    public int deleteFromDB(String tableName, String selection, String[] selectionArgs) {

        return SqlLiteDB.getInstance(mContext).getWritableDatabase().delete(tableName, selection, selectionArgs);

    }


}

