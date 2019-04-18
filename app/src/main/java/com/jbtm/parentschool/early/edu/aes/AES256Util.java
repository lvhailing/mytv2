package com.jbtm.parentschool.early.edu.aes;

import android.util.Base64;
import android.util.Log;

/**
 * Created by lvhailing on 2018/12/18.
 */

public class AES256Util {
    private static String aesKey = "PptUvr9TL7TC4qVtIJH9h3lpIVaC0OK7";

    //金榜给的
    public static String getAes256(String json) {
        try {
            Log.i("aaa", "result: " + json);
            return Security.encrypt(json);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("aaa", "getAes256 Exception: " + e.getMessage());
            return "";
        }
    }

    //从网上找的 加密有点问题
    public static String getAes256Str(String json) {
        try {
            byte[] after_encrypt = AES256.AES_cbc_encrypt(
                    json.getBytes(),
                    aesKey.getBytes(),
                    "1111111111111111".getBytes());

            String encodeResult = new String(Base64.encode(after_encrypt, Base64.DEFAULT));

            Log.i("aaa", "result: " + json);
            Log.i("aaa", "after_encrypt: " + new String(after_encrypt));
            Log.i("aaa", "encodeResult: " + encodeResult);
            return encodeResult;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
