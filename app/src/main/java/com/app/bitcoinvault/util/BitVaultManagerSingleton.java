package com.app.bitcoinvault.util;

import bitmanagers.BitVaultWalletManager;

/**
 * Created by admin on 23-05-2017.
 */

public class BitVaultManagerSingleton {

    public static BitVaultWalletManager mBitVaultManager = null;

    public static BitVaultWalletManager getInstance() {
        if (mBitVaultManager == null) {
            mBitVaultManager = BitVaultWalletManager.getWalletInstance();
        }
        return mBitVaultManager;
    }

}
