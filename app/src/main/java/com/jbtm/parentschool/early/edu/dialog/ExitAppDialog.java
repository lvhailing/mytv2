package com.jbtm.parentschool.early.edu.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;

public class ExitAppDialog extends Dialog {
    private Context context;
    private MyClickListener listener;

    public interface MyClickListener {
        void moreTime();

        void exit();
    }

    public ExitAppDialog(Context context) {
        super(context, R.style.MyDialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_exit_app, null);
        setContentView(view);

        TextView tv_more_time = view.findViewById(R.id.tv_more_time);
        TextView tv_exit = view.findViewById(R.id.tv_exit);

        tv_more_time.setOnClickListener(new clickListener());
        tv_exit.setOnClickListener(new clickListener());

        listenFocus(tv_more_time);
        listenFocus(tv_exit);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.65);
        lp.height = (int) (d.heightPixels * 0.55);
        dialogWindow.setAttributes(lp);

        //默认退出按钮获得焦点 注意：动态获取焦点会走OnFocusChangeListener，xml里获取焦点不会
        tv_exit.requestFocus();
    }

    public void setOnMyClickListener(MyClickListener myClickListener) {
        this.listener = myClickListener;
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.tv_more_time:
                    listener.moreTime();
                    break;
                case R.id.tv_exit:
                    listener.exit();
                    break;
            }
        }
    }

    private void listenFocus(TextView view) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.green));
                    ((TextView) v).setTextColor(context.getResources().getColor(R.color.white));
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValue)
                            .setDuration(Constants.scaleTime)
                            .start();
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.dialog_exit_btn));
                    ((TextView) v).setTextColor(context.getResources().getColor(R.color.textColor));
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .start();
                }
            }
        });
    }
}
