package com.jbtm.parentschool.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.models.PayModel;

/**
 * 无订购信息 或 无观看记录
 */
public class BuyNothingView extends LinearLayout {
    private Context mContext;
    private ImageView iv_arrow;
    private TextView tv_valid_time;

    public BuyNothingView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BuyNothingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.view_buy_nothing, this);

        iv_arrow = view.findViewById(R.id.iv_arrow);
        tv_valid_time = view.findViewById(R.id.tv_valid_time);
    }

    public void setType(int type) {
        if (type == 1) {
            //是没订购信息
            tv_valid_time.setText("您还没有订购，请到VIP订购进行办理");
            iv_arrow.setVisibility(VISIBLE);
        } else {
            //是没观看记录
            tv_valid_time.setText("观看后的视频会在此出现");
            iv_arrow.setVisibility(INVISIBLE);
        }
    }
}
