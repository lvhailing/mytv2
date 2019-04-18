package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.models.HomeWrapper;
import com.jbtm.parentschool.models.PayModel;
import com.jbtm.parentschool.models.PayModelXiaoMi;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.Util;
import com.jbtm.parentschool.widget.PayTypeView;
import com.xiaomi.mitv.osspay.sdk.data.PayOrder;
import com.xiaomi.mitv.osspay.sdk.proxy.PayCallback;
import com.xiaomi.mitv.osspay.sdk.proxy.ThirdPayProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 本页面仅小米支付
 */
public class PayActivityXiaoMi extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_title_me; // 点击跳转至个人信息
    private LinearLayout ll_title_buy; // 点击跳转课程详情
    private LinearLayout ll_dandian_arrow; // 单点的箭头，在没有单点是需隐藏
    private PayTypeView yearView;
    private PayTypeView monthView;
    private PayTypeView dandianView;
    private ImageView iv_year_arrow;
    private ImageView iv_month_arrow;
    private ImageView iv_dandian_arrow;
    private ImageView iv_pay_success;
    private TextView tv_title_time;
    private ProgressBar pb;
    private TextView tv_buy;
    private List<PayModel> payModelList;
    private int courseId;   //支付时 课程ID（点播方式必传）
    private int mKaType = 1;    //1包年，2包月，3单点。默认包年套餐
    private ThirdPayProxy thirdPayProxy;
    private CountDownTimer clockTimer;   //时钟
    private List<Integer> successPayTypeList = new ArrayList<>();//已成功支付过的套餐，用于右侧的购买按钮/对号flag的展示

    //头部logo点击，套餐购买
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PayActivityXiaoMi.class);
        context.startActivity(intent);
    }

    //单点购买
    public static void startActivity(Context context, int from, int courseId, String coursePrice) {
        Intent intent = new Intent(context, PayActivityXiaoMi.class);
        intent.putExtra("from", from);
        intent.putExtra("courseId", courseId);
        intent.putExtra("coursePrice", coursePrice);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_xiaomi);

        initView();
        initData();
        initPay();
        registerReceiver(); //退出登录时该界面退出
        startClock();
    }

    private void initPay() {
        thirdPayProxy = ThirdPayProxy.instance(this);

        //false：开发环境；true：测试环境。 默认值为false，正式开发环境
        //若电视/盒子版本不支持，设置无效
        thirdPayProxy.setUsePreview(true);
    }

    private void initView() {
        getSupportActionBar().hide();

        showProgressDialog(this);

        ll_title_me = findViewById(R.id.ll_title_me);
        ll_title_buy = findViewById(R.id.ll_title_buy);

        yearView = findViewById(R.id.v_year);
        monthView = findViewById(R.id.v_month);
        dandianView = findViewById(R.id.v_dandian);
        iv_year_arrow = findViewById(R.id.iv_year_arrow);
        iv_month_arrow = findViewById(R.id.iv_month_arrow);
        iv_dandian_arrow = findViewById(R.id.iv_dandian_arrow);
        ll_dandian_arrow = findViewById(R.id.ll_dandian_arrow);
        tv_title_time = findViewById(R.id.tv_title_time);
        tv_buy = findViewById(R.id.tv_buy);
        iv_pay_success = findViewById(R.id.iv_pay_success);
        pb = findViewById(R.id.pb);

        ll_title_me.setOnClickListener(this);
        ll_title_buy.setOnClickListener(this);
        tv_buy.setOnClickListener(this);

        listenFocus(yearView);
        listenFocus(monthView);
        listenFocus(dandianView);
//        listenFocus(tv_buy);
    }

    private void initData() {
        //0（默认值）从顶部flag来，则包年聚焦。1从单点购买来，则单点聚焦
        int from = getIntent().getIntExtra("from", 0);

        Map<String, Object> map = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getHomeData(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<HomeWrapper>>(PayActivityXiaoMi.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<HomeWrapper> result) {
                        closeProgressDialog();

                        if (result.result != null) {
                            payModelList = result.result.package_list;

                            //设置年卡
                            yearView.setPayType(1);
                            yearView.setData(payModelList.get(0));

                            //设置月卡
                            monthView.setPayType(2);
                            monthView.setData(payModelList.get(1));
                        }
                    }
                });

        if (from == 1) {
            //从单点购买来，则单点聚焦
            //设置单点
            //支付时 课程ID（点播方式必传）
            courseId = getIntent().getIntExtra("courseId", 0);
            String coursePrice = getIntent().getStringExtra("coursePrice");
            dandianView.setVisibility(View.VISIBLE);
            dandianView.setPayType(3);
            dandianView.setData(new PayModel("单点", coursePrice, null, 0));
            dandianView.requestFocus();
            return;
        }

        //从顶部flag来，则包年聚焦
        dandianView.setVisibility(View.GONE);
        //单点的箭头，在没有单点是需隐藏
        ll_dandian_arrow.setVisibility(View.GONE);
        yearView.requestFocus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_me:
                PersonalInformationActivity.startActivity(this, 0);
                break;
            case R.id.ll_title_buy:
                ToastUtil.showCustom("已经在本页");
                break;
            case R.id.tv_buy:   //小米支付
                showPaying();
                getPayOrderInfo();
                break;
        }
    }

    //用户点击购买按钮后 客户端请求后台接口
    private void getPayOrderInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("pay_type", 3);      //支付方式（1微信 2支付宝 3小米支付）
        map.put("order_type", mKaType);  //订单类型（1包年 2包月 3点播）
        if (courseId != 0) {
            //非点播不传
            map.put("content_id", courseId);      //课程ID（点播方式必传）
        }

        RequestUtil.getBasicMap(map);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .makeOrderXiaoMi(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<PayModelXiaoMi>>(PayActivityXiaoMi.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
                        //支付失败，重新显示支付按钮
                        showPayFail();
                    }

                    @Override
                    public void onMySuccess(ResultModel<PayModelXiaoMi> result) {
                        closeProgressDialog();

                        if (result.result != null) {
                            //用户点击购买按钮后 客户端请求后台接口，接口返回的支付对象
                            callXiaoMiApp(result.result.mi_param);
                        }
                    }
                });
    }

    private void callXiaoMiApp(PayModelXiaoMi.MiParam pay) {
        //此设置是确保能调起支付
        thirdPayProxy.setUsePreview(false);
        //调起小米支付app
        thirdPayProxy.createOrderAndPay(pay.app_id, pay.cust_order_id, pay.product_name, pay.price, pay.order_desc, pay.extra_data, new PayCallback() {

            @Override
            public void onSuccess(PayOrder payOrder) {
                showPaySuccess();

                //放入支付成功的列表
                if (!successPayTypeList.contains(mKaType)) {
                    successPayTypeList.add(mKaType);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (code == 50002) {
                    //bind to service failed
                    ToastUtil.showCustom("支付取消");
                } else if (code == 40108) {
                    ToastUtil.showCustom("无效客户端");
                } else {
                    ToastUtil.showCustom("支付失败，请重试");
                }

                //支付失败，重新显示支付按钮
                showPayFail();
            }
        });
    }

    //支付失败，重新显示支付按钮
    private void showPayFail() {
        setVisible(tv_buy);
        setInvisible(iv_pay_success);
        setInvisible(pb);
    }

    //支付中
    private void showPaying() {
        setVisible(pb);
        setInvisible(iv_pay_success);
        setInvisible(tv_buy);
    }

    //付款成功后刷新
    private void showPaySuccess() {
        setVisible(iv_pay_success);
        setInvisible(tv_buy);
        setInvisible(pb);
    }

    private void listenFocus(PayTypeView itemView) {
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //焦点变化时：刷新支付类型
                dealKaType(v, hasFocus);

                //焦点变化时：判断右侧该显示购买按钮还是支付成功按钮
                dealRightBtn();

                //焦点变化时：处理各子view背景字体
                PayTypeView viewModel = (PayTypeView) v;
                dealKaBg(viewModel, hasFocus);
            }
        });
    }

    //聚焦或失焦时，设置购买按钮的bg
    private void listenFocus(TextView view) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValue)
                            .setDuration(Constants.scaleTime)
                            .start();
                } else {
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .start();
                }
            }
        });
    }

    //聚焦或失焦时，设置套餐bg
    private void dealKaBg(PayTypeView viewModel, boolean b) {
        if (b) {
            //清空所有套餐bg
            yearView.setFocus(false);
            monthView.setFocus(false);
            dandianView.setFocus(false);
            dealFocusAnim(yearView, false, 0);
            dealFocusAnim(monthView, false, 0);
            dealFocusAnim(dandianView, false, 0);
            dealArrow(false, yearView.getPayType());
            dealArrow(false, monthView.getPayType());
            dealArrow(false, dandianView.getPayType());
            //为聚焦套餐设置bg
            viewModel.setFocus(true);
            dealArrow(true, viewModel.getPayType());
            dealFocusAnim(viewModel, true, 0);
        } else {
            //为失焦套餐设置痕迹
            viewModel.setFocus(true);
            dealFocusAnim(viewModel, true, 0);
            dealArrow(true, viewModel.getPayType());
        }
    }

    //焦点变化时：刷新支付类型
    private void dealKaType(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == R.id.v_year) {
                //包月：1包年，2包月，3单点
                mKaType = 1;
            } else if (v.getId() == R.id.v_month) {
                //包月：1包年，2包月，3单点
                mKaType = 2;
            } else if (v.getId() == R.id.v_dandian) {
                //包月：1包年，2包月，3单点
                mKaType = 3;
            }
        }
    }

    //焦点变化时：判断右侧该显示购买按钮还是支付成功按钮
    private void dealRightBtn() {
        for (Integer item : successPayTypeList) {
            if (mKaType == item) {
                //右侧显示成功flag
                showPaySuccess();
                return;
            }
        }
        //如果都没有，则显示购买按钮
        showPayFail();
    }

    //处理焦点时动画
    private void dealFocusAnim(View v, boolean hasFocus, int scaleTime) {
        if (hasFocus && v.getScaleX() != 0) {
            ViewCompat.animate(v)
                    .scaleX(Constants.scaleValueSmall)
//                    .scaleY(Constants.scaleValue)
                    .setDuration(scaleTime)
                    .start();
        } else {
            ViewCompat.animate(v)
                    .scaleX(1)
//                    .scaleY(1)
                    .start();
        }
    }

    //处理焦点时箭头
    private void dealArrow(boolean hasFocus, int payType) {
        //1包年，2包月，3单点
        if (payType == 1) {
            if (hasFocus) {
                setVisible(iv_year_arrow);
            } else {
                setGone(iv_year_arrow);
            }
        } else if (payType == 2) {
            if (hasFocus) {
                setVisible(iv_month_arrow);
            } else {
                setGone(iv_month_arrow);
            }
        } else if (payType == 3) {
            if (hasFocus) {
                setVisible(iv_dandian_arrow);
            } else {
                setGone(iv_dandian_arrow);
            }
        }
    }

    private void setVisible(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void setInvisible(View view) {
        if (view.getVisibility() != View.INVISIBLE) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void setGone(View view) {
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 空方法 防止切换微信支付宝时遗漏
     * 参考{@link PayTypeView#setFocus}方法的最后两行代码
     */
    public void refreshPayText(String str) {
    }

    private void startClock() {
        if (clockTimer == null) {
            clockTimer = new CountDownTimer(3 * 60 * 60_000, 10_000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //改变时间
                    tv_title_time.setText(Util.getClockTime());
                }

                @Override
                public void onFinish() {
                }
            };
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clockTimer != null) {
            clockTimer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockTimer != null) {
            clockTimer = null;
        }
    }
}
