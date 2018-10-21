package com.app.bitcoinvault.adaptor;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.bean.ContactsModel;
import com.app.bitcoinvault.view.activity.SendBitcoinActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 30-08-2017.
 */

public class AutoCompleteAdapter extends ArrayAdapter implements Filterable {
    LayoutInflater flater;
    private List<ContactsModel> selectFilesAdapters;
    private List<ContactsModel> mFilteredSelectFileList;
    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = new FilterResults();
            ArrayList<ContactsModel> tempList = new ArrayList<ContactsModel>();

            if (mFilteredSelectFileList != null && mFilteredSelectFileList.size() > 0) {
                mFilteredSelectFileList.clear();
            }

            if (constraint != null && selectFilesAdapters != null) {

                for (int i = 0; i < selectFilesAdapters.size(); i++) {

                    ContactsModel mContact = selectFilesAdapters.get(i);
                    if (selectFilesAdapters != null && selectFilesAdapters.size() > 0) {
                        if (mContact.getmReceiverAddress() != null && !mContact.getmReceiverAddress().equalsIgnoreCase("")) {
                            if (mContact.getmName().toLowerCase().contains(constraint.toString().toLowerCase())
                                    || mContact.getmReceiverAddress().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                tempList.add(mContact);
                            }
                        } else {
                            if (mContact.getmName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                tempList.add(mContact);
                            }
                        }


                    }
                }
                filterResults.values = tempList;
                filterResults.count = tempList.size();

            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {

            if(results.values!=null) {
                mFilteredSelectFileList = (ArrayList<ContactsModel>) results.values;
            }else {
                mFilteredSelectFileList = new ArrayList<>();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
    private Context mContext;

    public AutoCompleteAdapter(Activity context, int resouceId, List<ContactsModel> list, List<ContactsModel> mlist) {
        super(context, resouceId, list);
        this.mContext = context;
        flater = context.getLayoutInflater();
        selectFilesAdapters = list;
        mFilteredSelectFileList = new ArrayList<>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ContactsModel rowItem = mFilteredSelectFileList.get(position);

        View rowview = flater.inflate(R.layout.spinner_autocomplete_layout, parent, false);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.userName);
        txtTitle.setText(rowItem.getmName());

        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.userReceiverAddress);
        mwalletBalance.setText(rowItem.getmReceiverAddress());

        ImageView mImageView = (ImageView) rowview.findViewById(R.id.userimage);
        if (rowItem.getmImage() != null) {

            mImageView.setImageURI(Uri.parse(rowItem.getmImage()));
        }


        RelativeLayout mRelativeMain = (RelativeLayout) rowview.findViewById(R.id.relative_main);

        mRelativeMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFilteredSelectFileList != null && mFilteredSelectFileList.size() > position) {
                    ((SendBitcoinActivity) mContext).setSelectedContact(mFilteredSelectFileList.get(position));
                }
            }
        });


        return rowview;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContactsModel rowItem = selectFilesAdapters.get(position);

        View rowview = flater.inflate(R.layout.spinner_autocomplete_layout, parent, false);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        txtTitle.setText(rowItem.getmName());

        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletName);
        mwalletBalance.setText(rowItem.getmReceiverAddress());

        return rowview;
    }

    @Override
    public int getCount() {
        return mFilteredSelectFileList.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return myFilter;
    }
}
