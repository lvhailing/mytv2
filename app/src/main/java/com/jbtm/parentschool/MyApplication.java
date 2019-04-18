package com.jbtm.parentschool;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

/**
 * Created by lvhailing on 2018/12/14.
 */

public class MyApplication extends Application {
    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //腾讯bugly
        initBugly();

        //小米统计
        initMiStat();
    }

    //腾讯bugly
    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "024cdf058a", false);
    }

    //小米统计
    private void initMiStat() {
        MiStatInterface.initialize(this, "2882303761517926759", "5781792648759", "mi");
    }
}
