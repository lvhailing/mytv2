package com.jbtm.parentschool.early.edu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.jbtm.parentschool.MyApplication;

public class SPUtil {
    private static SharedPreferences sp = null;
    private static final String SP_USER = "sp_user"; // sp文件名

    public static final String USER_NAME = "user_name";

    private static SharedPreferences getSp(Context context) {
        if (null == sp) {
            sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        }
        return sp;
    }

    public static boolean getBoolean(Context context, String key, boolean defVal) {
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, defVal);
    }

    public static void putBoolean(Context context, String key, boolean val) {
        SharedPreferences sp = getSp(context);
        Editor editor = sp.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getSp(context);
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = getSp(context);
        return sp.getString(key, "");
    }

    public static long getLong(Context context, String key) {
        SharedPreferences sp = getSp(context);
        return sp.getLong(key, 0);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp = getSp(context);
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = getSp(context);
        return sp.getInt(key, 0);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = getSp(context);
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setToken(String token) {
        getSp(MyApplication.instance).edit().putString("token", token).commit();
    }

    public static String getToken() {
        return getSp(MyApplication.instance).getString("token", "");
    }

    public static void setPhone(String token) {
        getSp(MyApplication.instance).edit().putString("phone", token).commit();
    }

    public static String getPhone() {
        return getSp(MyApplication.instance).getString("phone", "");
    }
}
