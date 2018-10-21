package com.app.bitcoinvault.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.adaptor.WalletListAdaptor;
import com.app.bitcoinvault.appinterface.ClickListener;
import com.app.bitcoinvault.database.IDBConstant;
import com.app.bitcoinvault.databinding.ActivityWalletHomeBinding;
import com.app.bitcoinvault.databinding.EditPopupBinding;
import com.app.bitcoinvault.databinding.WalletEmptyDialogBinding;
import com.app.bitcoinvault.util.AppPreferences;
import com.app.bitcoinvault.util.BitVaultManagerSingleton;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.RecyclerTouchListener;
import com.app.bitcoinvault.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bitmanagers.BitVaultWalletManager;
import iclasses.WalletArrayCallback;
import model.WalletDetails;
import utils.SDKUtils;

import static android.view.View.GONE;


/**
 * this wallet  activity
 */
public class WalletHomeActivity extends AppCompatActivity implements View.OnClickListener, IAppConstant, WalletArrayCallback {

    private final String TAG = this.getClass().getSimpleName();
    private static final int GALLERY_PIC_REQUEST = 500;
    private static final int CROP_PIC = 2;
    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 100;
    private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 200;
    private final int MY_PERMISSIONS_CAMERA = 300;
    private final int CAMERA_PIC_REQUEST = 400;

    private List<String> granted;
    private List<String> denied;
    private ActivityWalletHomeBinding activityWalletHomeBinding;
    private List<WalletDetails> mWalletList = new ArrayList<>();
    private WalletListAdaptor adaptor;
    private EditPopupBinding editPopupBinding;
    private Uri mImageUri, mCropImagedUri, gImageUri;
    private boolean isFromCamera = false;
    private byte[] mUpdateLogo = null;
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private double walletTotalAmount = 0.0;
    private Double CURRENT_USD = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWalletHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_home);

        mBitVaultWalletManager = BitVaultManagerSingleton.getInstance();

        CURRENT_USD = AppPreferences.getInstance(this).getCurrency();

        granted = new ArrayList<>();
        denied = new ArrayList<>();

        Utils.setBackgroundImage(this, activityWalletHomeBinding.mainLinear, WALLET);

        setTypeFace();

        setListner();

        setAdapter();

        if (getIntent() != null && getIntent().hasExtra(FROM_NOTIFICATION) && getIntent().hasExtra(NOTIFICATION_RECIEVER)) {
            Intent transactionIntent = new Intent(WalletHomeActivity.this, WalletTransactionActivity.class);
            transactionIntent.putExtra(WALLET_ID, getIntent().getStringExtra(NOTIFICATION_RECIEVER));
            startActivity(transactionIntent);
        }

    }


    /**
     * Method is used to set Adapter
     */
    private void setAdapter() {

        activityWalletHomeBinding.walletRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new WalletListAdaptor(mWalletList, WalletHomeActivity.this);

        activityWalletHomeBinding.walletRecycleView.setAdapter(adaptor);

        activityWalletHomeBinding.walletRecycleView.addOnItemTouchListener(new RecyclerTouchListener(this,
                activityWalletHomeBinding.walletRecycleView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Intent transactionIntent = new Intent(WalletHomeActivity.this, WalletTransactionActivity.class);
                transactionIntent.putExtra(WALLET_ID, mWalletList.get(position).getWALLET_ID());
                startActivity(transactionIntent);

            }

            @Override
            public void onLongClick(View view, int position) {
                showWalletEditDialog(view, position);
            }
        }));
    }


    /**
     * Method is used to set Listners
     */
    private void setListner() {

        activityWalletHomeBinding.headerLayout.backArrow.setOnClickListener(this);
        activityWalletHomeBinding.headerLayout.optionMenu.setOnClickListener(this);
        activityWalletHomeBinding.walletEmptyButton.setOnClickListener(this);

    }

    /**
     * This method will set type for the required textviews.
     */
    private void setTypeFace() {
        activityWalletHomeBinding.headerLayout.optionMenu.setText("d");
        activityWalletHomeBinding.headerLayout.optionMenu.setVisibility(View.VISIBLE);
        activityWalletHomeBinding.headerLayout.sendSpinner.setVisibility(GONE);

        activityWalletHomeBinding.headerLayout.backArrow.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityWalletHomeBinding.headerLayout.optionMenu.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityWalletHomeBinding.emptyVaultSymbol.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        activityWalletHomeBinding.txtEmpty.setTypeface(FontManager.getTypeface(this, FontManager.ROBOTOITALIC));
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {
            case R.id.backArrow:
                onBackPressed();
                break;

            case R.id.optionMenu:
                Intent vaultIntent = new Intent(WalletHomeActivity.this, VaultTransactionActivity.class);
                startActivity(vaultIntent);
                break;

            case R.id.walletEmptyButton:
                showConfirmDialog();
//                Utils.workInProgressAlert(this);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(WalletHomeActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    /**
     * Show Wallet edit popup
     *
     * @param view     wallet view
     * @param position wallet postion
     */
    private void showWalletEditDialog(final View view, final int position) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editPopupBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.edit_popup, null, false);
        dialog.setContentView(editPopupBinding.getRoot());

        editPopupBinding.walletEditText.setText(mWalletList.get(position).getWALLET_NAME());
        editPopupBinding.walletEditText.setSelection(editPopupBinding.walletEditText.getText().length());

        if (mWalletList.get(position).getWALLET_ICON() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mWalletList.get(position).getWALLET_ICON(), 0, mWalletList.get(position).getWALLET_ICON().length);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            editPopupBinding.logoImageView.setBackground(ob);
        } else {
            editPopupBinding.logoImageView.setBackgroundResource(R.drawable.wallet_logo);
        }

        editPopupBinding.camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.camera) {
                    try {
                        isFromCamera = true;
                        boolean isPermissionGrantedCamera = false;

                        if (granted != null && granted.size() > 0) {

                            if (granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    && granted.contains(Manifest.permission.CAMERA)) {

                                isPermissionGrantedCamera = true;

                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.TITLE, "temp");
                                mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                            }

                        }
                        if (!isPermissionGrantedCamera) {

                            if (granted != null && granted.size() > 0 && !granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                if (!ActivityCompat.shouldShowRequestPermissionRationale(WalletHomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                        denied.size() > 0 && denied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showPermissionsSettingsPage(getResources().getString(R.string.write_message));
                                } else {
                                    Utils.allocateRunTimePermissions(WalletHomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE});
                                }
                            }

                            if (granted != null && granted.size() > 0 && granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                if (!ActivityCompat.shouldShowRequestPermissionRationale(WalletHomeActivity.this, Manifest.permission.CAMERA) &&
                                        denied.size() > 0 && denied.contains(Manifest.permission.CAMERA)) {
                                    showPermissionsSettingsPage(getResources().getString(R.string.camera));
                                } else {
                                    Utils.allocateRunTimePermissions(WalletHomeActivity.this, new String[]{Manifest.permission.CAMERA}, new int[]{MY_PERMISSIONS_CAMERA});
                                }
                            }
                        }
                    } catch (ActivityNotFoundException anfe) {
                        Toast.makeText(getApplicationContext(), "This device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        editPopupBinding.galleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.galleryImage) {
                    try {
                        isFromCamera = false;
                        boolean isPermissionGrantedGallery = false;

                        if (granted != null && granted.size() > 0) {
                            if (granted.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    && granted.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                                isPermissionGrantedGallery = true;
                                gImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                                if (gImageUri != null) {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, gImageUri);
                                    galleryIntent.setType("image/*");
                                    startActivityForResult(galleryIntent, GALLERY_PIC_REQUEST);
                                }
                            }
                        }
                        if (!isPermissionGrantedGallery) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(WalletHomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                    denied.size() > 0 && denied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                showPermissionsSettingsPage(getResources().getString(R.string.write_message));
                            } else {
                                Utils.allocateRunTimePermissions(WalletHomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE});

                            }
                        }
                    } catch (Exception exception) {
                        Toast.makeText(WalletHomeActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        editPopupBinding.bitwalletIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = null;

                try {
                    Resources res = getResources();
                    Drawable drawable = res.getDrawable(R.drawable.wallet_logo);
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    mUpdateLogo = stream.toByteArray();
                    editPopupBinding.logoImageView.setBackgroundResource(R.drawable.wallet_logo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        editPopupBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBitVaultWalletManager != null && mWalletList != null && mWalletList.size() > 0) {
                    String name = editPopupBinding.walletEditText.getText().toString().trim();
                    boolean isUpdate = false;
                    if (!name.equalsIgnoreCase("")) {
                        mBitVaultWalletManager.updateWalletName(mWalletList.get(position).getWALLET_ID(), name);
                        mWalletList.get(position).setWALLET_NAME(name);
                        isUpdate = true;
                        AppPreferences.getInstance(WalletHomeActivity.this).setWalletNameByWalletAdd(mWalletList.get(position).getmKeyPair().address, mWalletList.get(position).getWALLET_NAME());
                    }
                    if (mUpdateLogo != null && mUpdateLogo.length > 0) {
                        mBitVaultWalletManager.updateWalletIcon(mWalletList.get(position).getWALLET_ID(), mUpdateLogo);
                        mWalletList.get(position).setWALLET_ICON(mUpdateLogo);
                        isUpdate = true;
                        mUpdateLogo = null;
                    }
                    if (isUpdate) {
                        if (adaptor != null)
                            adaptor.notifyDataSetChanged();

                    }
                }
                dialog.hide();
            }
        });
        editPopupBinding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestWallet();
        granted = Utils.getAllPermisiions(this, granted);
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
            if (activityWalletHomeBinding != null)
                activityWalletHomeBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((walletTotalAmount * value)));

        }
    };


    /**
     * Method used to showConfirm Dialog
     */

    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WalletEmptyDialogBinding walletEmptyDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.wallet_empty_dialog, null, false);
        dialog.setContentView(walletEmptyDialogBinding.getRoot());
        walletEmptyDialogBinding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                Intent sendIntent = new Intent(WalletHomeActivity.this, ConfirmActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(RECIEVER_ADDRESS,VAULT);
                bundle.putInt(SEND_FROM, SEND_ALL_WALLET_EMPTY);
                bundle.putString(IDBConstant.WALLET_ID, "");
                bundle.putString(AMOUNT_TO_SEND, walletTotalAmount + "");
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
    public void getWallets(ArrayList<WalletDetails> mRequestedWallets) {
        try {
            if (mRequestedWallets != null) {
                if (mRequestedWallets.size() > 0) {
                    mWalletList.clear();
                    mWalletList.addAll(mRequestedWallets);
                    updateWalletListAdaptor();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Method is used to update WalletList Adaptor
     */
    private void updateWalletListAdaptor() {
        try {
            if (mWalletList != null && mWalletList.size() > 0) {
                walletTotalAmount = 0;
                for (int i = 0; i < mWalletList.size(); i++) {
                    SDKUtils.showLog(TAG, "Wallet Address" + i + mWalletList.get(i).getmKeyPair().address);
                    walletTotalAmount += Double.parseDouble(mWalletList.get(i).getWALLET_LAST_UPDATE_BALANCE());
                }

                activityWalletHomeBinding.headerLayout.amountDollar.setText(Utils.convertDecimalFormatPatternHeader((walletTotalAmount * CURRENT_USD)));


                activityWalletHomeBinding.headerLayout.sendSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                activityWalletHomeBinding.headerLayout.amount.setText(Utils.convertDecimalFormatPattern(walletTotalAmount));

                if (adaptor != null)
                    adaptor.notifyDataSetChanged();
            }
        } catch (Exception ex) {

        }
    }

    /**
     * This method is used to crop image so that image does not pixlate and fit in wallet logo image view
     *
     * @param mFinalImageUri URI of image
     * @return
     */
    private boolean performCropImage(Uri mFinalImageUri) {
        try {
            if (mFinalImageUri != null) {
                //call the standard crop action intent (the user device may not support it)
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                //indicate image type and Uri
                cropIntent.setDataAndType(mFinalImageUri, "image/*");
                //set crop properties
                cropIntent.putExtra("crop", "true");
                //indicate aspect of desired crop
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("scale", true);
                //indicate output X and Y
                cropIntent.putExtra("outputX", 500);
                cropIntent.putExtra("outputY", 500);
                //retrieve data on return
                cropIntent.putExtra("return-data", false);

                File f = createNewFile("CROP_");
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    Log.e("io", ex.getMessage());
                }

                mCropImagedUri = Uri.fromFile(f);
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCropImagedUri);
                //start the activity - we handle returning in onActivityResult
                startActivityForResult(cropIntent, CROP_PIC);
                return true;
            }
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
        return false;
    }

    /**
     * create file temp file
     *
     * @param prefix set image prefix
     * @return
     */
    private File createNewFile(String prefix) {
        if (prefix == null || "".equalsIgnoreCase(prefix)) {
            prefix = "IMG_";
        }
        File newDirectory = new File(Environment.getExternalStorageDirectory() + "/mypics/");
        if (!newDirectory.exists()) {
            if (newDirectory.mkdir()) {
                Log.d(this.getClass().getName(), newDirectory.getAbsolutePath() + " directory created");
            }
        }
        File file = new File(newDirectory, (prefix + System.currentTimeMillis() + ".jpg"));
        if (file.exists()) {
            //this wont be executed
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * Method used to show snackbar to navigate to settings page for allowing permissions.
     */
    private void showPermissionsSettingsPage(String mMessage) {

        Snackbar.make(activityWalletHomeBinding.mainLinear, mMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .show();
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
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (isFromCamera) {
                        Utils.allocateRunTimePermissions(WalletHomeActivity.this, new String[]{Manifest.permission.CAMERA}, new int[]{MY_PERMISSIONS_CAMERA});
                    }

                } else {
                    denied.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                return;
            }


            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    denied.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                return;
            }


            case MY_PERMISSIONS_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    granted.add(Manifest.permission.CAMERA);
                } else {
                    denied.add(Manifest.permission.CAMERA);
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case CAMERA_PIC_REQUEST: {
                        Uri picUri;
                        if (data != null && data.getData() != null)
                            picUri = data.getData();
                        else
                            picUri = mImageUri;

                        performCropImage(picUri);
                        break;
                    }
                    case CROP_PIC: {
                        Uri imageUri;
                        Bitmap bitmap = null;
                        if (data != null && data.getData() != null)
                            imageUri = data.getData();
                        else
                            imageUri = mCropImagedUri;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            mUpdateLogo = stream.toByteArray();
                            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                            editPopupBinding.logoImageView.setBackground(ob);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case GALLERY_PIC_REQUEST: {
                        Uri targetUri = data.getData();
                        performCropImage(targetUri);
                        break;
                    }
                }
            }

        }
    }
}
