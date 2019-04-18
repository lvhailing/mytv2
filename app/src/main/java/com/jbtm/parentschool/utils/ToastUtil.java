package com.jbtm.parentschool.utils;

import android.content.Context;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jbtm.parentschool.MyApplication;
import com.jbtm.parentschool.R;

public class ToastUtil {

    private static Toast mToast;
    private static Toast mCustomToast;
    private static Context mContext = MyApplication.instance;

    public static void showToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), message,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static void showToast(Context context, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public static void showToast(Context context, String message, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), message,
                    duration);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static void showToast(Context context, int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), resId,
                    duration);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public static void showToast(String message) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if (mToast == null) {
                mToast = Toast.makeText(mContext.getApplicationContext(), message,
                        Toast.LENGTH_SHORT);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }

    public static void showToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext.getApplicationContext(), resId,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public static void showToast(String message, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext.getApplicationContext(), message,
                    duration);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static void showToast(int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext.getApplicationContext(), resId,
                    duration);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }

    public static void showCustom(String text) {
        if (mCustomToast == null) {
            mCustomToast = new Toast(mContext);
        }
        final View toastLayout = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_toast, null);
        final TextView toastTextView = toastLayout.findViewById(R.id.tv_toast_text);
        toastTextView.setText(text);
        mCustomToast.setGravity(Gravity.CENTER, 0, 0);
        mCustomToast.setView(toastLayout);
        mCustomToast.setDuration(Toast.LENGTH_SHORT);
        mCustomToast.show();
    }

}
