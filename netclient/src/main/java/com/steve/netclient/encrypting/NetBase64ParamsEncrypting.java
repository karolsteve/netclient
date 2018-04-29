package com.steve.netclient.encrypting;

import android.util.Base64;

/**
 * Created by Steve Tchatchouang on 03/02/2018
 */

public class NetBase64ParamsEncrypting implements NetParamsEncrypting {
    @Override
    public byte[] encrypt(String params) {
        return Base64.encode(params.getBytes(),Base64.NO_WRAP);
    }
}
