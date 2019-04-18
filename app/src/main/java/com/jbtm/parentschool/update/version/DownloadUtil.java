package com.jbtm.parentschool.update.version;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadUtil {
    public static final String FILE_NAME = "jbtm";

    public static String getBaseDir(Context context) {
        String baseDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            baseDir = Environment.getExternalStorageDirectory() + "/" + FILE_NAME + "/";
        } else {
            baseDir = context.getCacheDir().getAbsolutePath() + "/" + FILE_NAME + "/";
        }
        File basedir = new File(baseDir);
        if (!basedir.exists()) {
            basedir.mkdirs();
        }
        return baseDir;
    }

    public static String getSecondDir(Context context, String dirname) {
        String basedir = getBaseDir(context);
        String dir = basedir + dirname;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return dir;
    }

    public static int[] getDeviceWH(Context context) {
        int[] wh = new int[2];
        int w = 0;
        int h = 0;
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        w = dm.widthPixels;
        h = dm.heightPixels;
        wh[0] = w;
        wh[1] = h;
        return wh;
    }
}
