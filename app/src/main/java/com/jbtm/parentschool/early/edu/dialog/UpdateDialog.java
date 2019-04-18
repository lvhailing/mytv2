package com.jbtm.parentschool.early.edu.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jbtm.parentschool.R;

public class UpdateDialog extends Dialog {
    private Context context;
    private MyClickListener listener;
    private String title;
    private boolean isForce;

    public interface MyClickListener {
        void sure();

        void cancel();
    }

    public UpdateDialog(Context context, String title, boolean isForce) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
        this.title = title;
        this.isForce = isForce;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_update, null);
        setContentView(view);

        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_sure = view.findViewById(R.id.tv_sure);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);

        tv_sure.setOnClickListener(new clickListener());
        tv_cancel.setOnClickListener(new clickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.65);
        lp.height = (int) (d.heightPixels * 0.55);
        dialogWindow.setAttributes(lp);

        //默认退出按钮获得焦点 注意：动态获取焦点会走OnFocusChangeListener，xml里获取焦点不会
        tv_sure.requestFocus();

        if (isForce) {
            tv_cancel.setVisibility(View.GONE);
            setCanceledOnTouchOutside(false);
        }
        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
        }
    }

    public void setOnMyClickListener(MyClickListener myClickListener) {
        this.listener = myClickListener;
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.tv_sure:  //更新
                    if (listener != null) {
                        listener.sure();
                    }
                    break;
                case R.id.tv_cancel:    //不更新
                    if (listener != null) {
                        listener.cancel();
                    }
                    break;
            }
        }
    }
}
