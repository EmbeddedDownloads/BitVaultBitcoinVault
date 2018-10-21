package com.app.bitcoinvault.util;


/**
 * this class used for make application appConstant
 */
public interface IAppConstant {


    String WALLET_ID = "wallet_id";

    String WALLET = "wallet";
    String VAULT = "vault";


    String RECIEVER_ADDRESS = "reciever_add";
    String AMOUNT_TO_SEND = "amount_to_send";
    String DESC = "desc";
    String SEND_FROM = "send_from";
    int SEND_WALLET_TO_WALLET = 1;
    int SEND_WALLET_TO_VAULT = 2;
    int SEND_SINGAL_WALLET_EMPTY = 3;
    int SEND_ALL_WALLET_EMPTY = 4;
    int SEND_VAULT_TO_WALLET = 5;
    int SEND_WALLET_TO_WALLET_OTHER_APP = 6;



    String FROM_NOTIFICATION = "notification";
    String NOTIFICATION_RECIEVER = "notification_reciever";


    String MOBILE = "Mobile";
    String HOME = "Home";
    String WORK = "Work";
    String MAIN = "Main";
    String CUSTOM = "Custom";
    String CONVERSION_ACTION_FILTER = "conversion_action";
    String currency = "currency";
    int WALLET_BALANCE_UPDATE_INITIAL_DELAY = 1000;
    int WALLET_BALANCE_TIMER = 10000;

    String TRID = "transId";
    String STATUS="status";

    int wallet_type=0;

}
