package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.models.CommonModel;
import com.jbtm.parentschool.models.HomeWrapper;
import com.jbtm.parentschool.models.PayModel;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.Util;
import com.jbtm.parentschool.utils.ZXingUtil;
import com.jbtm.parentschool.widget.PayTypeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 本页面是微信支付宝支付
 */
public class PayActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_title_me; // 点击跳转至个人信息
    private LinearLayout ll_title_buy; // 点击跳转课程详情
    private LinearLayout ll_dandian_arrow; // 单点的箭头，在没有单点是需隐藏
    private PayTypeView yearView;
    private PayTypeView monthView;
    private PayTypeView dandianView;
    private ImageView iv_year_arrow;
    private ImageView iv_month_arrow;
    private ImageView iv_dandian_arrow;
    private ImageView iv_zfb;
    private ImageView iv_wx;
    private ImageView iv_qrcode;
    private ImageView iv_pay_success;
    private TextView tv_title_time;
    private TextView tv_pay_result_left;
    private TextView tv_pay_result_mid;
    private TextView tv_pay_result_right;
    private View v_pay_bg;
    private ProgressBar pb;
    private int from;   //0（默认值）从顶部flag来，则包年聚焦。1从单点购买来，则单点聚焦
    private List<PayModel> payModelList;
    private int courseId;   //支付时 课程ID（点播方式必传）
    private int mKaType = 1;    //1包年，2包月，3单点。默认包年套餐
    private int mPayType = 2;    //1微信，2支付宝。默认支付宝
    private CountDownTimer countDownTimer;   //轮询器，每10秒轮询一次支付结果
    private CountDownTimer clockTimer;   //时钟
    private int orderId;    //每次生成二维码后也会对应生成一个orderId，轮询时去最新的orderId


    //头部logo点击，套餐购买
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PayActivity.class);
        context.startActivity(intent);
    }

    //单点购买
    public static void startActivity(Context context, int from, int courseId, String coursePrice) {
        Intent intent = new Intent(context, PayActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("courseId", courseId);
        intent.putExtra("coursePrice", coursePrice);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        initView();
        initData();
        registerReceiver(); //退出登录时该界面退出
        startClock();
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
        tv_pay_result_left = findViewById(R.id.tv_pay_result_left);
        tv_pay_result_mid = findViewById(R.id.tv_pay_result_mid);
        tv_pay_result_right = findViewById(R.id.tv_pay_result_right);
        iv_zfb = findViewById(R.id.iv_zfb);
        iv_wx = findViewById(R.id.iv_wx);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        iv_pay_success = findViewById(R.id.iv_pay_success);
        v_pay_bg = findViewById(R.id.v_pay_bg);
        pb = findViewById(R.id.pb);

        ll_title_me.setOnClickListener(this);
        ll_title_buy.setOnClickListener(this);
//        iv_zfb.setOnClickListener(this);
//        iv_wx.setOnClickListener(this);

        listenFocus(yearView);
        listenFocus(monthView);
        listenFocus(dandianView);
        listenFocus(iv_zfb);
        listenFocus(iv_wx);
    }

    private void initData() {
        //0（默认值）从顶部flag来，则包年聚焦。1从单点购买来，则单点聚焦
        from = getIntent().getIntExtra("from", 0);

        Map<String, Object> map = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getHomeData(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<HomeWrapper>>(PayActivity.this) {
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

                            if (from != 1) {
                                //二维码支付结果文案处理，不是单点时。默认取年卡价格
                                refreshPayText(payModelList.get(0).price);
                            }
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
            refreshPayText(coursePrice);
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
//            case R.id.iv_zfb:   //支付宝支付
//                getPayUrl(2);
//                break;
//            case R.id.iv_wx:   //微信支付
//                getPayUrl(1);
//                break;
        }
    }

    //先获取支付的url，然后生成二维码
    private void getPayUrl() {
        Map<String, Object> map = new HashMap<>();
        map.put("pay_type", mPayType);      //支付方式（1微信 2支付宝 3小米支付）
        map.put("order_type", mKaType);  //订单类型（1包年 2包月 3点播）
        if (courseId != 0) {
            //非点播不传
            map.put("content_id", courseId);      //课程ID（点播方式必传）
        }
        RequestUtil.getBasicMap(map);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .makeOrder(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonModel>>(PayActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonModel> result) {
                        closeProgressDialog();

                        if (result.result != null) {
                            //订单的付款二维码地址
                            String qrCodeUrl = result.result.qrcode_url;

                            //设置二维码图片
                            Bitmap bitmap = ZXingUtil.createQRImage(qrCodeUrl, iv_qrcode.getWidth(), iv_qrcode.getHeight());
                            iv_qrcode.setImageBitmap(bitmap);
                            refreshQrCode(true);

                            //开启轮询，获取支付结果
                            orderId = result.result.order_id;
                            startCountDown();
                        }
                    }
                });
    }

    //开启轮询，获取支付结果
    private void startCountDown() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(30 * 60_000, 10_000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //轮询支付接口
                    getPayResult(orderId);
                }

                @Override
                public void onFinish() {
                }
            };
            countDownTimer.start();
        }
    }

    //轮询支付接口
    private void getPayResult(int orderId) {
        Map<String, Object> map = new HashMap<>();
        map.put("order_id", orderId);
        RequestUtil.getBasicMap(map);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getPayResult(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel>(PayActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel result) {
                        closeProgressDialog();
                        ToastUtil.showCustom(result.msg);
                        //成获取到支付结果
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            countDownTimer = null;
                        }
                        showPaySuccess();
                    }
                });
    }

    //套餐焦点变换时，刷新付款金额
    public void refreshPayText(String price) {
        tv_pay_result_left.setText("扫码支付");
        tv_pay_result_mid.setText(price);
        setVisible(tv_pay_result_mid);
        setVisible(tv_pay_result_right);
//        UIUtil.setPayResultStyle(tv_pay_result, "扫码支付" + price + "元");
    }

    //套餐焦点变换时，刷新二维码和加载条
    public void refreshQrCode(boolean isShowQrCode) {
        if (isShowQrCode) {
            //显示二维码
            setVisible(iv_qrcode);
            setInvisible(pb);
            setInvisible(v_pay_bg);
            setInvisible(iv_pay_success);
        } else {
            //显示加载框
            setVisible(pb);
            setInvisible(iv_qrcode);
            setInvisible(v_pay_bg);
            setInvisible(iv_pay_success);
        }
    }

    //付款成功后刷新二维码
    private void showPaySuccess() {
        setGone(tv_pay_result_mid);
        setGone(tv_pay_result_right);
        tv_pay_result_left.setText("付款成功！");
        setVisible(v_pay_bg);
        setVisible(iv_pay_success);
        setInvisible(pb);
    }

    private void listenFocus(View itemView) {
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //焦点变化时：获取二维码地址并展示
                dealPayUrl(v, hasFocus);

                if (v instanceof PayTypeView) {
                    PayTypeView viewModel = (PayTypeView) v;

                    //焦点变化时：处理各子view背景字体
                    dealKaBg(viewModel, hasFocus);

                    //焦点变化时：处理箭头
//                    dealArrow(hasFocus, viewModel.getPayType());

                    //焦点变化时：处理各子view动画
//                    dealFocusAnim(v, hasFocus,200);

                } else if (v instanceof ImageView) {
                    //焦点变化时：设置支付宝、微信的图片bg
                    ImageView iv = (ImageView) v;
                    dealKaBg(iv, hasFocus);
                }
            }
        });
    }

    //聚焦或失焦时，设置图片bg
    private void dealKaBg(ImageView iv, boolean b) {
        if (b) {
            //清空所有图片 bg
            iv_wx.setBackgroundResource(R.drawable.wx_normal);
            iv_zfb.setBackgroundResource(R.drawable.zfb_normal);
            //为聚焦图片设置bg
            if (iv.getId() == R.id.iv_wx) {
                //微信
                iv_wx.setBackgroundResource(R.drawable.wx_selected);
            } else {
                //支付宝
                iv_zfb.setBackgroundResource(R.drawable.zfb_selected);
            }
        } else {
            if (iv.getId() == R.id.iv_wx) {
                //微信
                iv_wx.setBackgroundResource(R.drawable.wx_selected);
            } else {
                //支付宝
                iv_zfb.setBackgroundResource(R.drawable.zfb_selected);
            }
        }
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

    //焦点变化时：获取二维码地址并展示
    private void dealPayUrl(View v, boolean hasFocus) {
        if (hasFocus) {
            refreshQrCode(false);
            if (v.getId() == R.id.v_year) {
                //包月：1包年，2包月，3单点
                mKaType = 1;
            } else if (v.getId() == R.id.v_month) {
                //包月：1包年，2包月，3单点
                mKaType = 2;
            } else if (v.getId() == R.id.v_dandian) {
                //包月：1包年，2包月，3单点
                mKaType = 3;
            } else if (v.getId() == R.id.iv_wx) {
                //微信支付：支付方式（1微信 2支付宝）
                mPayType = 1;
            } else if (v.getId() == R.id.iv_zfb) {
                //支付宝支付：支付方式（1微信 2支付宝）
                mPayType = 2;
            }
            getPayUrl();
        }
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (countDownTimer != null) {
            countDownTimer.start();
        }
        if (clockTimer != null) {
            clockTimer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer = null;
        }
        if (clockTimer != null) {
            clockTimer = null;
        }
    }
}
