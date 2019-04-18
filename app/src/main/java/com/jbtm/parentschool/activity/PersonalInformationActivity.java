package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.dialog.ExitAppDialog;
import com.jbtm.parentschool.models.CommonWrapper;
import com.jbtm.parentschool.models.OrderWrapper;
import com.jbtm.parentschool.models.PayModel;
import com.jbtm.parentschool.models.WatchHistoryModel;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.SPUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.Util;
import com.jbtm.parentschool.widget.BuyKaAndDandianView;
import com.jbtm.parentschool.widget.BuyKaView;
import com.jbtm.parentschool.widget.BuyNothingView;
import com.jbtm.parentschool.widget.PersonalLoginNoView;
import com.jbtm.parentschool.widget.PersonalLoginYesView;
import com.jbtm.parentschool.widget.WatchHistoryView;

import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by junde on 2018/12/7.
 */

public class PersonalInformationActivity extends BaseActivity implements View.OnClickListener {
    private PersonalLoginYesView v_personal_login_yes;
    private PersonalLoginNoView v_personal_login_no;
    private BuyKaAndDandianView v_buy_ka_and_dandian;
    private BuyKaView v_buy_ka;
    private BuyNothingView v_buy_nothing;   //无订购信息 或 无观看记录
    private WatchHistoryView v_watch_history;

    private OrderWrapper orderWrapper;
    private CommonWrapper historyWrapper;

    private int from;   //0（默认值）从顶部flag来。1未登录，从登录来
    private TextView tv_menu_personal;
    private TextView tv_menu_buy;
    private TextView tv_menu_watch_history;
    public static String loginOutBroadcast = "com.jbtm.parentschool.loginOutBroadcast";
    private long lastTime;
    private int currentFocus;
    private TextView tv_title_time;
    private CountDownTimer timer;   //系统时间

    public static void startActivity(Context context, int from) {
        //0（默认值）从顶部flag来。1未登录，从登录来
        Intent intent = new Intent(context, PersonalInformationActivity.class);
        intent.putExtra("from", from);
        context.startActivity(intent);
        if (from == 1) {
            //未登录 先关闭所有历史界面，再打开登录页面
            sendLoginOutBroadcast(context);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_infomation);

        initView();
        if (from != 1) {
            //已登录才去请求接口
            getOrder();
            getHistory();
        }
        startClock();
    }

    private void initView() {
        getSupportActionBar().hide();

        tv_title_time = findViewById(R.id.tv_title_time);
        v_personal_login_yes = findViewById(R.id.v_personal_login_yes);
        v_personal_login_no = findViewById(R.id.v_personal_login_no);
        v_buy_ka_and_dandian = findViewById(R.id.v_buy_ka_and_dandian);
        v_buy_ka = findViewById(R.id.v_buy_ka);
        v_buy_nothing = findViewById(R.id.v_buy_nothing);
        v_watch_history = findViewById(R.id.v_watch_history);

        final LinearLayout ll_title_me = findViewById(R.id.ll_title_me);
        final LinearLayout ll_title_buy = findViewById(R.id.ll_title_buy);
        tv_menu_personal = findViewById(R.id.tv_menu_personal);
        tv_menu_buy = findViewById(R.id.tv_menu_buy);
        tv_menu_watch_history = findViewById(R.id.tv_menu_watch_history);

        //focus listener
        listenTvFocus(tv_menu_personal);
        listenTvFocus(tv_menu_buy);
        listenTvFocus(tv_menu_watch_history);

        //click listener
        ll_title_me.setOnClickListener(this);
        ll_title_buy.setOnClickListener(this);

        //0（默认值）从顶部flag来。1未登录，从登录来
        from = getIntent().getIntExtra("from", 0);

        if (from == 1) {
            //未登录，则禁用focus
            tv_menu_personal.setFocusable(false);
            tv_menu_buy.setFocusable(false);
            tv_menu_watch_history.setFocusable(false);
            ll_title_me.setFocusable(false);
            ll_title_buy.setFocusable(false);

            //未登录界面呈现
            setVisible(v_personal_login_no);
            v_personal_login_no.setFrom(from);
        } else {
            //已登录，则默认选中个人信息
            tv_menu_personal.requestFocus();
            setVisible(v_personal_login_yes);
        }

        //设置头部个人信息flag的左键、下键聚焦的menu
        ll_title_me.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //下键
                    int id = getCurrentMenu().getId();
                    ll_title_me.setNextFocusDownId(id);
                    ll_title_me.setNextFocusLeftId(id);
                }
            }
        });

        //设置头部购买flag的下键聚焦的menu
        ll_title_buy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //下键
                    int id = getCurrentMenu().getId();
                    ll_title_buy.setNextFocusDownId(id);
                }
            }
        });
    }

    private int buyIndex = 0;

    //focus listener
    private void listenTvFocus(TextView v) {
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                //设置焦点文字大小
                setTextSize((TextView) view, b);

                //设置焦点文字背景颜色
                setTextBg((TextView) view, b);

                //获取到焦点时刷新界面
                if (b) {
                    switch (view.getId()) {
                        case R.id.tv_menu_personal: //个人信息
                            currentFocus = 1;
                            setPersonalInfo();
                            break;
                        case R.id.tv_menu_buy: //订购信息
                            currentFocus = 2;
                            setOrder();
//                            setMenuBuy();
                            break;
                        case R.id.tv_menu_watch_history: //观看记录
                            currentFocus = 3;
                            //观看记录
                            setHistory();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_me:
                ToastUtil.showCustom("已经在本页");
                break;
            case R.id.ll_title_buy:
                PayActivityXiaoMi.startActivity(this);
                break;
        }
    }

    //获取订购信息
    private void getOrder() {
        Map<String, Object> params = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getMyOrders(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<OrderWrapper>>(PersonalInformationActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<OrderWrapper> result) {
                        orderWrapper = result.result;
                    }
                });
    }

    //获取播放记录
    private void getHistory() {
        Map<String, Object> map = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getHistory(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonWrapper>>(PersonalInformationActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonWrapper> result) {
                        historyWrapper = result.result;
                    }
                });
    }

    //将个人信息，呈现在界面上
    private void setPersonalInfo() {
        if (TextUtils.isEmpty(SPUtil.getToken())) {
            //：未登录显示
            setVisible(v_personal_login_no);
        } else {
            //个人信息：已登录显示
            setVisible(v_personal_login_yes);
        }
    }

    //将订购信息，呈现在界面上
    private void setOrder() {
        if (orderWrapper == null) {
            //订购信息：都没订
            v_buy_nothing.setType(1);
            setVisible(v_buy_nothing);
            return;
        }
        //套餐包业务
        List<PayModel> myPackages = orderWrapper.my_packages;
        //单点业务
        List<WatchHistoryModel> myCourses = orderWrapper.my_courses;

        boolean hasKa = myPackages != null && myPackages.size() > 0;
        boolean hasDandian = myCourses != null && myCourses.size() > 0;

        if (hasKa && hasDandian) {
            //订购信息：订了卡和单点
            setVisible(v_buy_ka_and_dandian);
            v_buy_ka_and_dandian.setKaInfo(myPackages.get(0));
            v_buy_ka_and_dandian.setDandianInfo(myCourses);
        } else if (hasDandian) {
            //订购信息：仅订了单点
            setVisible(v_buy_ka_and_dandian);
            v_buy_ka_and_dandian.setKaInfo(null);
            v_buy_ka_and_dandian.setDandianInfo(myCourses);
        } else if (hasKa) {
            //订购信息：仅订了卡
            setVisible(v_buy_ka);
            v_buy_ka.setKaInfo(myPackages.get(0));
        } else {
            //订购信息：都没订
            setVisible(v_buy_nothing);
        }
    }

    //将观看记录信息，呈现在界面上
    private void setHistory() {
        if (historyWrapper == null || historyWrapper.courses == null) {
            //没有观看记录
            v_buy_nothing.setType(2);
            setVisible(v_buy_nothing);
            return;
        }
        setVisible(v_watch_history);
        v_watch_history.setData(historyWrapper.courses);
    }

    //将本地写死的数据，呈现在界面上
    private void setMenuBuy() {
        buyIndex++;
        if (buyIndex == 1) {
            //订购信息：订了卡和单点
            setVisible(v_buy_ka_and_dandian);
            v_buy_ka_and_dandian.setKaInfo(new PayModel("包全年套餐", "100天"));
        } else if (buyIndex == 2) {
            //订购信息：仅订了单点
            setVisible(v_buy_ka_and_dandian);
            v_buy_ka_and_dandian.setKaInfo(null);
        } else if (buyIndex == 3) {
            //订购信息：仅订了卡
            setVisible(v_buy_ka);
            v_buy_ka.setKaInfo(new PayModel("包全年套餐", "100天"));
        } else if (buyIndex == 4) {
            //订购信息：都没订
            v_buy_nothing.setType(1);
            setVisible(v_buy_nothing);
            buyIndex = 0;
        }
    }

    private void setTextSize(TextView view, boolean b) {
        if (b) {
//            view.setTextSize(20);
            view.setTextColor(Color.argb(255, 255, 255, 255));
        } else {
//            view.setTextSize(19);
            view.setTextColor(Color.argb(205, 239, 239, 239));
        }
    }

    //聚焦或失焦时，设置menu背景
    private void setTextBg(TextView view, boolean b) {
        if (b) {
            //清空所有menu bg
            tv_menu_personal.setBackground(null);
            tv_menu_buy.setBackground(null);
            tv_menu_watch_history.setBackground(null);
            //为聚焦menu设置bg
            view.setBackground(getResources().getDrawable(R.drawable.bg_rect_green_focused));
        } else {
            //为失焦menu设置痕迹
            view.setBackground(getResources().getDrawable(R.drawable.bg_rect_green_selected));
        }
    }

    private void setVisible(View view) {
        setGone(v_personal_login_yes);
        setGone(v_personal_login_no);
        setGone(v_buy_ka_and_dandian);
        setGone(v_buy_ka);
        setGone(v_buy_nothing);
        setGone(v_watch_history);

        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void setGone(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }

    public void gotoLogin() {
        //退出登录，应关闭之前所有界面，重新打开登录页
        SPUtil.setPhone("");
        SPUtil.setToken("");
        PersonalInformationActivity.startActivity(this, 1);
        sendLoginOutBroadcast(this);
        finish();
    }

    private static void sendLoginOutBroadcast(Context context) {
        //首页、详情页、购买页退出
        Intent intent = new Intent();
        intent.setAction(loginOutBroadcast);
        context.sendBroadcast(intent);
    }

    //获取当前聚焦、或最后一个聚焦过的menu
    private TextView getCurrentMenu() {
        TextView textView;
        if (currentFocus == 1) {
            textView = findViewById(R.id.tv_menu_personal);
        } else if (currentFocus == 2) {
            textView = findViewById(R.id.tv_menu_buy);
        } else {
            textView = findViewById(R.id.tv_menu_watch_history);
        }
        return textView;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && from == 1) {
            //按了返回键，并且是从未登录来
            long nowTime = System.currentTimeMillis();
            if (nowTime - lastTime < 1500) {
                //双击了返回键，退出应用
                showExitAppDialog();
            } else {
                lastTime = System.currentTimeMillis();
                ToastUtil.showCustom("再按退出应用");
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitAppDialog() {
        final ExitAppDialog dialog = new ExitAppDialog(this);
        dialog.show();
        dialog.setOnMyClickListener(new ExitAppDialog.MyClickListener() {
            @Override
            public void moreTime() {
                dialog.dismiss();
            }

            @Override
            public void exit() {
                dialog.dismiss();
                finish();
            }
        });
    }

    private void startClock() {
        if (timer == null) {
            timer = new CountDownTimer(3 * 60 * 60_000, 10_000) {
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
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer = null;
        }
    }
}
