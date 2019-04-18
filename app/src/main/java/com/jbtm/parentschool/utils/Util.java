package com.jbtm.parentschool.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Base64;

import com.jbtm.parentschool.MyApplication;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvhailing on 2018/12/16.
 */

public class Util {

    public static boolean isPhoneNum(String phoneNum) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";
        // "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(phoneNum))
            return false;
        else
            return phoneNum.matches(telRegex);
    }

    public static int getVersionCode() {
        PackageInfo pInfo = null;
        try {
            pInfo = MyApplication.instance.getPackageManager().getPackageInfo(MyApplication.instance.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersionName() {
        PackageInfo pInfo = null;
        try {
            pInfo = MyApplication.instance.getPackageManager().getPackageInfo(MyApplication.instance.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String, Object> getVersionCodeAndName(Context context) {
        Map<String, Object> result = new HashMap<String, Object>();
        String version = "";
        int code = 0;
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
            code = packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put("versionName", version);
        result.put("versionCode", code);
        return result;
    }

    public static String getClockTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
//        int second = c.get(Calendar.SECOND);
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(":");
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
//        sb.append(":");
//        if (second < 10) {
//            sb.append("0");
//        }
//        sb.append(second);
        return sb.toString();
    }
}
