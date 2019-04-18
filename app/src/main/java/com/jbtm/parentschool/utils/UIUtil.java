package com.jbtm.parentschool.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.jbtm.parentschool.activity.CourseDetailActivity;

/**
 * Created by lvhailing on 2018/12/22.
 */

public class UIUtil {

    //防止被其他view z轴方向覆盖
    public static void bringToFront(View view) {
        //5.0以下系统会崩溃
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            view.bringToFront();
            view.setElevation(100f);   //防止被其他view z轴方向覆盖
        }
    }

    //防止z轴方向，覆盖其他view
    public static void bringToBackground(View view) {
        //5.0以下系统会崩溃
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            view.setElevation(0f);   //防止z轴方向，覆盖其他view
        }
    }

    //付款结果
    public static void setPayResultStyle(TextView tv, String str) {
        SpannableString ss = new SpannableString(str);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#32A9A0"));
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(1.3f);
        ss.setSpan(sizeSpan, 4, str.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(styleSpan, 4, str.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(colorSpan, 4, str.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setIncludeFontPadding(false);
        tv.setText(ss);
    }

    //获取屏幕实际宽度
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    //获取屏幕实际高度
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

}
