package com.jbtm.parentschool.early.edu.network;


import android.content.Context;
import android.text.TextUtils;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.activity.PersonalInformationActivity;
import com.jbtm.parentschool.early.edu.network.model.ResultModel;
import com.jbtm.parentschool.utils.ToastUtil;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 基础ObserverAdapter
 */
public abstract class MyObserverAdapter<T extends ResultModel> implements Observer<T> {
    public static final String TAG = MyObserverAdapter.class.getSimpleName();
    private Context context;

    public MyObserverAdapter() {
    }

    public MyObserverAdapter(Context context) {
        this.context = context;
    }

    @Override
    public void onSubscribe(Disposable d) {
        onMySubscribe(d);
    }

    @Override
    public void onNext(T result) {
        if (result.code == Constants.SUCCESS) {
            onMySuccess(result);
            return;
        }
        if (result.code == Constants.LOGIN_INVALID) {
            if (context != null) {
                PersonalInformationActivity.startActivity(context, 1);
            }
            ToastUtil.showToast("登陆过期，请重新登录");
            return;
        }
        if (!TextUtils.isEmpty(result.msg)) {
            //状态码不为0
            ToastUtil.showToast(result.msg);
        } else {
            //状态码不为0，error_msg为空，则异常提示
            ToastUtil.showToast(Constants.ERRO_NETWORK_MSG);
        }
        //最终通知各观察者此异常
        onError(new IllegalArgumentException("error code: " + result.code));
    }

    @Override
    public void onError(Throwable e) {
        onMyError(e);
    }

    @Override
    public void onComplete() {
        onMyComplete();
    }


    public void onMySubscribe(Disposable d) {

    }

    public void onMySuccess(T result) {

    }

    public void onMyError(Throwable e) {

    }

    public void onMyComplete() {

    }

}
