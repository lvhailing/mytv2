package com.jbtm.parentschool.widget;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.PayActivityXiaoMi;
import com.jbtm.parentschool.models.PayModel;


public class PayTypeView extends LinearLayout {
    private LinearLayout ll_left;
    private TextView tv_money;
    private TextView tv_yuan;
    private TextView tv_origin_money;
    private LinearLayout ll_right;
    private TextView tv_ka;
    private LinearLayout ll_discount;
    private TextView tv_description;

    private int payType = 1;  //1包年，2包月，3单点
    private String price = "";    //当前套餐金额
    private Context mContext;

    private int deepColor;  //聚焦字体颜色
    private int normalColor;    //普通字体颜色
    private int textColorOrange;    //橘黄字体颜色
    private Drawable gradientYellow;

    public PayTypeView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public PayTypeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        setFocusable(true);
    }

    private void init() {
        this.removeAllViews();

        View view = View.inflate(mContext, R.layout.view_pay_type, this);

        ll_left = (LinearLayout) view.findViewById(R.id.ll_left);
        ll_right = (LinearLayout) view.findViewById(R.id.ll_right);

        tv_money = (TextView) view.findViewById(R.id.tv_money);
        tv_yuan = (TextView) view.findViewById(R.id.tv_yuan);
        tv_origin_money = (TextView) view.findViewById(R.id.tv_origin_money);
        tv_ka = (TextView) view.findViewById(R.id.tv_ka);
        ll_discount = (LinearLayout) view.findViewById(R.id.ll_discount);
        tv_description = (TextView) view.findViewById(R.id.tv_description);

        deepColor = mContext.getResources().getColor(R.color.textColorDeep);
        normalColor = mContext.getResources().getColor(R.color.textColor);
        textColorOrange = mContext.getResources().getColor(R.color.textColorOrange);
        gradientYellow = mContext.getResources().getDrawable(R.drawable.gradient_yellow);
    }

    public void setPayType(int type) {
        //1包年，2包月，3单点
        payType = type;
    }

    public int getPayType() {
        //1包年，2包月，3单点
        return payType;
    }

    public String getPrice() {
        //获取套餐金额
        return price;
    }

    public void setFocus(boolean hasFocus) {
        //背景处理
        if (hasFocus) {
            //聚焦
            ll_left.setBackground(gradientYellow);
            ll_right.setBackground(gradientYellow);
            if (payType != 3) {
                //非单点
                ll_discount.setBackground(mContext.getResources().getDrawable(R.drawable.bg_rect_discount));
            }
        } else {
            //失焦
            if (payType != 3) {
                //是单点
                ll_left.setBackgroundColor(mContext.getResources().getColor(R.color.gray_btm_left));
                ll_right.setBackgroundColor(mContext.getResources().getColor(R.color.gray_btm_right));
                ll_discount.setBackground(null);
            } else {
                //是包年 或包月
                ll_left.setBackgroundColor(mContext.getResources().getColor(R.color.gray_top_left));
                ll_right.setBackgroundColor(mContext.getResources().getColor(R.color.gray_top_right));
            }
        }

        //字体处理
        if (hasFocus) {
            //聚焦
            tv_money.setTextColor(deepColor);
            tv_yuan.setTextColor(deepColor);
            tv_origin_money.setTextColor(deepColor);
            tv_ka.setTextColor(deepColor);
            tv_description.setTextColor(deepColor);
        } else {
            //失焦
            if (payType == 3) {
                //是单点
                tv_money.setTextColor(normalColor);
                tv_yuan.setTextColor(normalColor);
                tv_ka.setTextColor(normalColor);
                tv_description.setTextColor(normalColor);
            } else {
                //是包年 或包月
                tv_money.setTextColor(textColorOrange);
                tv_yuan.setTextColor(textColorOrange);
                tv_ka.setTextColor(textColorOrange);
                tv_description.setTextColor(normalColor);
                tv_origin_money.setTextColor(normalColor);
            }
        }

        //二维码交互
        if (hasFocus) {
            //获得焦点时，刷新支付文案
//            ((PayActivity) mContext).refreshPayText(String.valueOf(price));
            ((PayActivityXiaoMi) mContext).refreshPayText(String.valueOf(price));
        }
    }

    public void setData(PayModel payModel) {
        if (payModel == null || payModel.price == null) {
            setVisibility(GONE);
            return;
        }
        //价格
        tv_money.setText(payModel.price);
        price = payModel.price;

        //原价格
        if (payType != 3 && !TextUtils.isEmpty(payModel.original_price)) {
            //非单点，且有值
            tv_origin_money.setText("（原价" + payModel.original_price + "元）");
            tv_origin_money.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tv_origin_money.setVisibility(GONE);
        }

        //1包年，2包月，3单点
        tv_ka.setText(payType == 3 ? "单点" : payModel.name);

        //限时特惠标签
        if (payModel.discount == 1) {
            ll_discount.setVisibility(VISIBLE);
        } else {
            ll_discount.setVisibility(GONE);
        }

        //描述
        if (payType == 1 || payType == 2) {
            tv_description.setText("所有视频都可以观看");
            tv_description.setVisibility(VISIBLE);
        } else if (payType == 3) {
            tv_description.setText("办理年卡月卡更优惠");
            tv_description.setVisibility(VISIBLE);
        }
    }
}
