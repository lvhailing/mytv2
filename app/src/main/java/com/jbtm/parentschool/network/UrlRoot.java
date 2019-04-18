package com.jbtm.parentschool.network;


import com.jbtm.parentschool.BuildConfig;

public class UrlRoot {
    private final static int ENV_DEV = 0;
    private final static int ENV_PRODUCT = 1;

    // 主地址
    public static String ROOT_URL;

    static {
        switch (BuildConfig.my_environment) {
            case ENV_DEV:
                // 0 测试环境
                ROOT_URL = "http://test.v3wx.jzxt365.com/";
                break;
            case ENV_PRODUCT:
                // 1 正式环境
                ROOT_URL = "http://v3wx.jzxt365.com/";
                break;
            default:
                break;
        }
    }
}
