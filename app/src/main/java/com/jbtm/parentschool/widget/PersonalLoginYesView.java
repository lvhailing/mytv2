package com.jbtm.parentschool.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.PersonalInformationActivity;
import com.jbtm.parentschool.dialog.ExitAppDialog;
import com.jbtm.parentschool.dialog.ExitLoginDialog;
import com.jbtm.parentschool.models.CommonModel;
import com.jbtm.parentschool.models.CommonWrapper;
import com.jbtm.parentschool.models.PayModel;
import com.jbtm.parentschool.models.VersionModel;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.update.version.UpdateManager;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.SPUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.Util;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class PersonalLoginYesView extends RelativeLayout implements View.OnClickListener {
    private TextView tv_my_phone;
    private TextView tv_version;
    private Button btn_login_out;
    private Button btn_check_update;
    private Context mContext;

    public PersonalLoginYesView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public PersonalLoginYesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.view_login_yes, this);

        tv_version = view.findViewById(R.id.tv_version);
        tv_my_phone = view.findViewById(R.id.tv_my_phone);
        btn_login_out = view.findViewById(R.id.btn_login_out);
        btn_check_update = view.findViewById(R.id.btn_check_update);

        btn_login_out.setOnClickListener(this);
        btn_check_update.setOnClickListener(this);

        //手机号
        tv_my_phone.setText("我的手机号：" + SPUtil.getPhone());
        //最新版本
        tv_version.setText("已更新到：" + Util.getVersionName());

        btn_login_out.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int id = ((PersonalInformationActivity) mContext).findViewById(R.id.tv_menu_personal).getId();
                    btn_login_out.setNextFocusLeftId(id);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_out:    //退出登录
                showExitLoginDialog();
                break;
            case R.id.btn_check_update:    //版本更新
                checkUpdate();
                break;
        }
    }

    //检查更新
    private void checkUpdate() {
        Map<String, Object> params = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .checkVersion(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonWrapper>>(mContext) {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonWrapper> result) {
                        if (result.result != null && result.result.new_version != null) {
                            //弹出升级对话框
                            VersionModel versionModel = result.result.new_version;
                            new UpdateManager().checkVersion(mContext, versionModel);
                        } else {
                            //已是最新版本
                            ToastUtil.showCustom("已是最新版本");
                        }
                    }
                });
    }

    private void showExitLoginDialog() {
        final ExitLoginDialog dialog = new ExitLoginDialog(mContext);
        dialog.show();
        dialog.setOnMyClickListener(new ExitLoginDialog.MyClickListener() {
            @Override
            public void sure() {
                dialog.dismiss();
                ((PersonalInformationActivity) mContext).gotoLogin();
            }

            @Override
            public void cancel() {
                dialog.dismiss();
            }
        });
    }
}
