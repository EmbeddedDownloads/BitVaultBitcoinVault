package com.app.bitcoinvault.adaptor;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.bean.WalletTransactionBean;
import com.app.bitcoinvault.database.IDBConstant;

import java.util.List;

/**
 * this class is used to set the adopter of transaction(send/recieve)
 */

public class WalletTransactionListAdaptor extends RecyclerView.Adapter<WalletTransactionListAdaptor.TransactionHolder> implements IDBConstant {


    private final List<WalletTransactionBean> transactionBeanList;
    private final Activity activity;


    /**
     * @param data     list of transaction (send/recireve)
     * @param activity activity from where this adaptor is called
     */
    public WalletTransactionListAdaptor(List<WalletTransactionBean> data, Activity activity) {
        transactionBeanList = data;
        this.activity = activity;
    }


    @Override
    public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wallet_tranasaction_list_item, parent, false);
        return new TransactionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TransactionHolder holder, final int position) {
        final WalletTransactionBean data = transactionBeanList.get(position);

        if (data.getType().equals("Received")) {
            holder.typeTextView.setTextColor(activity.getResources().getColor(R.color.red));
        } else
            holder.typeTextView.setTextColor(activity.getResources().getColor(R.color.green));

        if (data.getDescription() != null && !data.getDescription().equals("")) {
            holder.descTextView.setText(data.getDescription());
            holder.descTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descTextView.setVisibility(View.GONE);
        }

        if (data.getVisibility() == View.VISIBLE) {
            holder.relLayout.setVisibility(View.VISIBLE);
        } else {
            holder.relLayout.setVisibility(View.GONE);
        }

        holder.typeTextView.setText(data.getType());

        holder.amountTextView.setText(data.getBitcoin());

        holder.dateTextView.setText(data.getDate());
        holder.statusTextView.setText(data.getStatus());

        holder.transIdTextView.setText(data.getTxId());
        holder.nameTextView.setText(data.getName());

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data.getVisibility() == View.VISIBLE) {
                    data.setVisibility(View.GONE);
                    holder.relLayout.setVisibility(View.GONE);
                } else {
                    data.setVisibility(View.VISIBLE);
                    holder.relLayout.setVisibility(View.VISIBLE);
                }


            }
        });

    }


    @Override
    public int getItemCount() {
        return transactionBeanList.size();
    }

    public class TransactionHolder extends RecyclerView.ViewHolder {
        public final TextView typeTextView;
        public final TextView amountTextView;
        public final TextView dateTextView;
        public final TextView statusTextView;
        public final TextView descTextView;
        public final LinearLayout parentLayout;
        public final RelativeLayout relLayout;
        public final TextView nameTextView;
        public final TextView transIdTextView;

        public TransactionHolder(View view) {
            super(view);
            typeTextView = (TextView) view.findViewById(R.id.typeTextView);
            amountTextView = (TextView) view.findViewById(R.id.amountTextView);
            dateTextView = (TextView) view.findViewById(R.id.dateTextView);
            statusTextView = (TextView) view.findViewById(R.id.statusTextView);
            descTextView = (TextView) view.findViewById(R.id.descriptTextView);
            parentLayout = (LinearLayout) view.findViewById(R.id.parentLayout);
            relLayout = (RelativeLayout) view.findViewById(R.id.relLayout);
            nameTextView = (TextView) view.findViewById(R.id.nameTextView);
            transIdTextView = (TextView) view.findViewById(R.id.transIdTextView);
        }
    }


}