package com.app.bitcoinvault.adaptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bitcoinvault.R;
import com.app.bitcoinvault.util.FontManager;
import com.app.bitcoinvault.util.IAppConstant;
import com.app.bitcoinvault.util.Utils;

import java.util.List;

import model.WalletDetails;


/**
 * this class is used to set the adaptor of wallet
 */

public class WalletListAdaptor extends RecyclerView.Adapter<WalletListAdaptor.WalletHolder> implements IAppConstant {

    private final String TAG = getClass().getSimpleName();
    private final List<WalletDetails> walletBeanClassList;
    private final Context mContext;

    /**
     * @param walletBeanClassList list of all wallet
     * @param mContext            context from where this adaptor is called
     */
    public WalletListAdaptor(List<WalletDetails> walletBeanClassList, Context mContext) {
        this.walletBeanClassList = walletBeanClassList;
        this.mContext = mContext;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_list_item, parent, false);
        WalletHolder holder = new WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, int position) {
        if (walletBeanClassList != null) {
            final WalletDetails walletDetails = walletBeanClassList.get(position);

            holder.walletNameTextView.setText(walletDetails.getWALLET_NAME());
            holder.walletAvailableCoinTextView.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(walletDetails.getWALLET_LAST_UPDATE_BALANCE())));

            if (walletDetails.getWALLET_ICON() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(walletDetails.getWALLET_ICON(), 0, walletDetails.getWALLET_ICON().length);
                BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), bitmap);
                holder.logoImageView.setBackground(ob);
            } else {
                holder.logoImageView.setBackgroundResource(R.drawable.wallet_logo);
            }
        }

    }

    @Override
    public int getItemCount() {
        if (walletBeanClassList != null) {
            return walletBeanClassList.size();
        }
        return 0;
    }

    /**
     * this is Holder class used recycler view
     */
    public class WalletHolder extends RecyclerView.ViewHolder {
        public final ImageView logoImageView;
        public final TextView walletNameTextView;
        public final TextView bitcoinlogoTextView;
        public final TextView walletAvailableCoinTextView;


        public WalletHolder(View view) {
            super(view);
            logoImageView = (ImageView) view.findViewById(R.id.logoImageView);
            walletNameTextView = (TextView) view.findViewById(R.id.walletNameTextView);
            bitcoinlogoTextView = (TextView) view.findViewById(R.id.bitcoinlogoTextView);
            walletAvailableCoinTextView = (TextView) view.findViewById(R.id.walletAvailableCoinTextView);

            walletNameTextView.setTypeface(FontManager.getTypeface(mContext, FontManager.ROBOTOLIGHT));
            bitcoinlogoTextView.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));

        }
    }
}

