package com.jbtm.parentschool.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.PayActivityXiaoMi;
import com.jbtm.parentschool.models.PayModel;


public class BuyKaView extends LinearLayout {
    private TextView tv_valid_time;
    private TextView tv_ka;
    private TextView tv_buy_again;
    private Context mContext;

    public BuyKaView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BuyKaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.view_buy_ka, this);

        tv_ka = view.findViewById(R.id.tv_ka);
        tv_valid_time = view.findViewById(R.id.tv_valid_time);
        tv_buy_again = view.findViewById(R.id.tv_buy_again);

        tv_buy_again.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PayActivityXiaoMi.startActivity(mContext);
            }
        });

        listenFocus(tv_buy_again);
    }

    public void setData(PayModel payModel) {
        tv_ka.setText(payModel.valid_time);
        tv_valid_time.setText(payModel.name);
    }

    public void setKaInfo(PayModel kaInfo) {
        tv_ka.setText(kaInfo.name);
        tv_valid_time.setText(kaInfo.valid_time);
    }

    private void listenFocus(TextView view) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValue)
                            .scaleY(Constants.scaleValue)
                            .setDuration(Constants.scaleTime)
                            .start();
                } else {
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .scaleY(1)
                            .start();
                }
            }
        });
    }
}
