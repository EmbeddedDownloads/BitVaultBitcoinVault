package com.app.bitcoinvault.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.bean.ContactsModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all the utils method, used in complete application.
 */

public class Utils implements IAppConstant {


    public static String TRANSACTION_FEE = "0";

    /**
     * This method will setBackground Image on the basis of whether activity belongs to
     * Wallet or Vault
     *
     * @param mContext - Context from where method is called
     * @param mView    - View on which image to be set.
     * @param from     - Either request coming from Wallet or Vault.
     */
    public static void setBackgroundImage(Context mContext, View mView, String from) {
        mView.setBackground(from.equals(VAULT) ? mContext.getResources().getDrawable(R.mipmap.vault_background)
                : mContext.getResources().getDrawable(R.mipmap.wallet_background));
    }


    /**
     * Creates a DecimalFormat using the given pattern and the symbols
     * for the default locale. This is a convenient way to obtain a
     * DecimalFormat when internationalization is not the main concern.
     *
     * @param value the value what we want to change
     * @return formatted string value
     */
    public static String convertDecimalFormatPattern(Double value) {
        DecimalFormat df = new DecimalFormat("#0.########");
//        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value);
    }


    /**
     * Creates a DecimalFormat using the given pattern and the symbols
     * for the default locale. This is a convenient way to obtain a
     * DecimalFormat when internationalization is not the main concern.
     *
     * @param value the value what we want to change
     * @return formatted string value
     */
    public static String convertDecimalFormatPatternHeader(Double value) {
        DecimalFormat df = new DecimalFormat("#0.##");
//        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value);
    }


    /**
     * Creates a DecimalFormat using the given pattern and the symbols
     * for the default locale. This is a convenient way to obtain a
     * DecimalFormat when internationalization is not the main concern.
     *
     * @param value the value what we want to change
     * @return formatted string value
     */
    public static Double convertDecimalFormatPatternDouble(Double value) {
        DecimalFormat df = new DecimalFormat("#0.########");
//        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(value));
    }

    /**
     * Method is used to allocate run time permission
     * *
     * * @param mContext
     * -- context
     * * @param permission      -
     * -- permission reuired
     * * @param callbackConstant -- int constant for permission
     */
    public static void allocateRunTimePermissions(Activity mContext, String[] permission, int[] callbackConstant) {
        for (int i = 0; i < permission.length; i++) {
            if (ContextCompat.checkSelfPermission(mContext, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mContext, new String[]{permission[i]}, callbackConstant[i]);
            }
        }
    }

    /**
     * Method used to getAllPermission which are already given     *
     * * @param mContext
     * -- context     * @param grantedPermission -- list of permissions
     * * @return -- list of granted permissions
     */
    public static List getAllPermisiions(Context mContext, List grantedPermission) {
        try {
            if (mContext != null && grantedPermission != null) {
                PackageInfo mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
                for (int i = 0; i < mPackageInfo.requestedPermissions.length; i++) {
                    if ((mPackageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        grantedPermission.add(mPackageInfo.requestedPermissions[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grantedPermission;
    }


    /**
     * Method used to check whether device is connected with internet or not
     *
     * @param mContext -- context of the application
     * @return -- either network is connected or not
     */
    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method to snackbar in the application
     *
     * @param mView    -- parent layout
     * @param message  -- message to be displayed
     * @param duration -- durattion of the snackbar
     */
    public static void showSnakbar(View mView, String message, int duration) {
        if (mView != null) {
            Snackbar.make(mView, message, duration).show();
        }
    }

    /**
     * this method is used to hide keyboard
     *
     * @param activity which we have to hide keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


    /**
     * This method is used to get contact from URI
     *
     * @param context
     * @return List of contact
     */
    public static ArrayList<ContactsModel> readPhoneContacts(final Activity context) {

        allocateRunTimePermissions(context, new String[]{Manifest.permission.READ_CONTACTS}, new int[]{100});

        final ArrayList<ContactsModel> mContactList = new ArrayList<>();

        context.runOnUiThread(new Runnable() {
            public void run() {
                getMeContact(context);
            }
        });



        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");


        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String image_uri = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                ArrayList<PublicKey> keyArrayList = getKeyList(context, id, contactName);
                if (keyArrayList != null && keyArrayList.size() > 0) {
                    for (int k = 0; k < keyArrayList.size(); k++) {
                        ContactsModel mContact = new ContactsModel();
                        mContact.setmName(contactName);
                        mContact.setmImage(image_uri);
                        mContact.setmReceiverAddress(keyArrayList.get(k).getPublicKey());
                        mContactList.add(mContact);
                    }
                }
            }
            cursor.close();
        }
        return mContactList;
    }

    /**
     * This method is used get myself contact
     * @param context Object of context
     */
    private static void getMeContact(final Context context) {

        final ArrayList<ContactsModel> contactsModel = new ArrayList<>();
        new ProfileLoader((AppCompatActivity) context, new OnProfileLoadListener() {
            @Override
            public void onProfileLoadComplete(Cursor cursor) {
                Log.e("Cursor", "Cursor" + cursor);

                String name = "", uri = "";
                ArrayList<String> address = new ArrayList<>();

                while (cursor.moveToNext()) {
                    String mimeType = cursor.getString(cursor.getColumnIndex("mimetype"));
                    if (mimeType.endsWith("name")) {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    } else if (mimeType.endsWith("photo")) {
                        uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    } else if (mimeType.endsWith("sip_address")) {
                        address.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS)));
                    }
                }
                if (address.size() > 0 && !name.equals("")) {
                    for (int i = 0; i < address.size(); i++)
                        contactsModel.add(new ContactsModel(name, address.get(i), uri));

                    if (contactsModel != null && contactsModel.size() > 0) {
                        AppPreferences.getInstance(context).setMEContact(contactsModel);
                    }

                }
            }
        });
    }

    // This method is used to find key on the baisi of name and id
    public static ArrayList<PublicKey> getKeyList(Context context, String contactId, String name) {
        ArrayList<PublicKey> keyList = new ArrayList<>();
        Cursor postal = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.SipAddress.CONTACT_ID + " = "
                        + contactId, null, null);

        if (postal != null) {
            List<Mobile> list = getMobileList(context, contactId);
            while (postal.moveToNext()) {
                int keyType = postal.getInt(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                String publicKey = postal.getString(postal.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                switch (keyType) {
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK:
                    case ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER:
                        break;
                    default:
                        if (publicKey != null && publicKey.length() > 0) {
                            publicKey = publicKey.trim();
                            if (!publicKey.equalsIgnoreCase(name.trim()) && !name.contains(publicKey)
                                    && !publicKey.equals("")) {
                                boolean isExist = false;
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getNumber().equalsIgnoreCase(publicKey)) {
                                        isExist = true;
                                        break;
                                    }
                                }
                                if (!isExist) {
                                    keyList.add(new PublicKey(publicKey));
                                }
                            }
                        }
                        break;
                }
            }
            postal.close();
        }
        return keyList;
    }

    /**
     * This method is used to get mobile list of particular contct id
     * @param context object of context
     * @param contactId id , by which mobile list find
     * @return
     */
    public static ArrayList<Mobile> getMobileList(Context context, String contactId) {

        Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null);
        ArrayList<Mobile> mobileList = new ArrayList<>();

        assert pCursor != null;
        while (pCursor.moveToNext()) {
            int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            phoneNo = phoneNo.replaceAll(" ", "");
            phoneNo = phoneNo.replaceAll("-", "");
            switch (phoneType) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, MOBILE));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, HOME));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, WORK));
                    }
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, MAIN));
                    }
                    break;
                default:
                    if (!isNumberExist(mobileList, phoneNo)) {
                        mobileList.add(new Mobile(phoneNo, CUSTOM));
                    }
                    break;
            }
        }
        pCursor.close();
        return mobileList;
    }

    /**
     * Check number is phone or not
     * @param list list of mobile
     * @param phone By which list is search
     * @return
     */
    private static boolean isNumberExist(List<Mobile> list, String phone) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().equalsIgnoreCase(phone) || list.get(i).getNumber().endsWith(phone)) {
                return true;
            }
        }
        return false;
    }
}

