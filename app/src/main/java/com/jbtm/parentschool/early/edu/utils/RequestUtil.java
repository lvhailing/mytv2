package com.jbtm.parentschool.early.edu.utils;

import com.google.gson.Gson;
import com.jbtm.parentschool.aes.Security;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvhailing on 2018/12/19.
 */

public class RequestUtil {

    //无业务参数的公共参数
    public static Map<String, Object> getBasicMapNoBusinessParams() {
        Map<String, Object> params = new HashMap<>();
        try {
            String data = Security.encrypt("");
            String md5 = Security.sign(data, Util.getVersionName());
            params.put("data", data);
            params.put("accept_sign", md5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    public static Map<String, Object> getBasicMap(Map<String, Object> params) {
        String json = new Gson().toJson(params);
        try {
            String data = Security.encrypt(json);
            String md5 = Security.sign(data, Util.getVersionName());
            params.put("data", data);
            params.put("accept_sign", md5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }
}
