package com.app.bitcoinvault.bean;

import java.util.ArrayList;

/**
 * this class used for Transaction Bean
 */

public class TransactionBean {
    private int totalItems;

    public int getTotalItems() { return this.totalItems; }

    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    private int from;

    public int getFrom() { return this.from; }

    public void setFrom(int from) { this.from = from; }

    private int to;

    public int getTo() { return this.to; }

    public void setTo(int to) { this.to = to; }

    private ArrayList<Tx> items;

    public ArrayList<Tx> getItems() { return this.items; }

    public void setItems(ArrayList<Tx> items) { this.items = items; }

    public class ScriptSig {
        private String hex;
        private String asm;

        public String getHex() {
            return this.hex;
        }

        public void setHex(String hex) {
            this.hex = hex;
        }

        public String getAsm() {
            return this.asm;
        }

        public void setAsm(String asm) {
            this.asm = asm;
        }
    }

    public class Vin {
        private String txid;
        private int vout;
        private int n;
        private ScriptSig scriptSig;
        private String addr;
        private Long valueSat;
        private double value;

        public String getTxid() {
            return this.txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public int getVout() {
            return this.vout;
        }

        public void setVout(int vout) {
            this.vout = vout;
        }

        public int getN() {
            return this.n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public ScriptSig getScriptSig() {
            return this.scriptSig;
        }

        public void setScriptSig(ScriptSig scriptSig) {
            this.scriptSig = scriptSig;
        }

        public String getAddr() {
            return this.addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public Long getValueSat() {
            return this.valueSat;
        }

        public void setValueSat(Long valueSat) {
            this.valueSat = valueSat;
        }

        public double getValue() {
            return this.value;
        }

        public void setValue(double value) {
            this.value = value;
        }


    }

    public class ScriptPubKey {
        private String hex;
        private String asm;
        private ArrayList<String> addresses;
        private String type;

        public String getHex() {
            return this.hex;
        }

        public void setHex(String hex) {
            this.hex = hex;
        }

        public String getAsm() {
            return this.asm;
        }

        public void setAsm(String asm) {
            this.asm = asm;
        }

        public ArrayList<String> getAddresses() {
            return this.addresses;
        }

        public void setAddresses(ArrayList<String> addresses) {
            this.addresses = addresses;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public class Vout {
        private String value;
        private int n;
        private ScriptPubKey scriptPubKey;
        private String spentTxId;
        private Integer spentIndex;
        private Integer spentHeight;

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getN() {
            return this.n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public ScriptPubKey getScriptPubKey() {
            return this.scriptPubKey;
        }

        public void setScriptPubKey(ScriptPubKey scriptPubKey) {
            this.scriptPubKey = scriptPubKey;
        }

        public String getSpentTxId() {
            return this.spentTxId;
        }

        public void setSpentTxId(String spentTxId) {
            this.spentTxId = spentTxId;
        }

        public Integer getSpentIndex() {
            return this.spentIndex;
        }

        public void setSpentIndex(Integer spentIndex) {
            this.spentIndex = spentIndex;
        }

        public Integer getSpentHeight() {
            return this.spentHeight;
        }

        public void setSpentHeight(Integer spentHeight) {
            this.spentHeight = spentHeight;
        }
    }

    public class Tx {
        private String txid;
        private int version;
        private int locktime;
        private ArrayList<Vin> vin;
        private ArrayList<Vout> vout;
        private String blockhash;
        private int blockheight;
        private int confirmations;
        private Long time;
        private int blocktime;
        private double valueOut;
        private int size;
        private double valueIn;
        private double fees;

        public String getTxid() {
            return this.txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getLocktime() {
            return this.locktime;
        }

        public void setLocktime(int locktime) {
            this.locktime = locktime;
        }

        public ArrayList<Vin> getVin() {
            return this.vin;
        }

        public void setVin(ArrayList<Vin> vin) {
            this.vin = vin;
        }

        public ArrayList<Vout> getVout() {
            return this.vout;
        }

        public void setVout(ArrayList<Vout> vout) {
            this.vout = vout;
        }

        public String getBlockhash() {
            return this.blockhash;
        }

        public void setBlockhash(String blockhash) {
            this.blockhash = blockhash;
        }

        public int getBlockheight() {
            return this.blockheight;
        }

        public void setBlockheight(int blockheight) {
            this.blockheight = blockheight;
        }

        public int getConfirmations() {
            return this.confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public Long getTime() {
            return this.time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public int getBlocktime() {
            return this.blocktime;
        }

        public void setBlocktime(int blocktime) {
            this.blocktime = blocktime;
        }

        public double getValueOut() {
            return this.valueOut;
        }

        public void setValueOut(double valueOut) {
            this.valueOut = valueOut;
        }

        public int getSize() {
            return this.size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public double getValueIn() {
            return this.valueIn;
        }

        public void setValueIn(double valueIn) {
            this.valueIn = valueIn;
        }

        public double getFees() {
            return this.fees;
        }

        public void setFees(double fees) {
            this.fees = fees;
        }
    }


}
