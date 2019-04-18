package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.adapter.HomeAdapter;
import com.jbtm.parentschool.dialog.ExitAppDialog;
import com.jbtm.parentschool.models.HomeColumnModel;
import com.jbtm.parentschool.models.HomeCourseModel;
import com.jbtm.parentschool.models.HomeWrapper;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private long lastTime;
    private LinearLayout ll_jx_2items;   //精选的两个item
    private RelativeLayout rl_jx_item_1;   //精选的两个item
    private RelativeLayout rl_jx_item_2;   //精选的两个item
    private TextView tv_menu_jx;
    private TextView tv_title_time;
    private TextView tv_menu_1;
    private TextView tv_menu_2;
    private TextView tv_menu_3;
    private TextView tv_menu_4;
    private TextView tv_menu_5;
    private TextView tv_menu_6;
    private List<TextView> textViewList;
    private HomeAdapter adapter;
    private HomeWrapper homeWrapper;
    private int currentFocus;
    private CountDownTimer timer;   //系统时间

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();
        initData();
        registerReceiver(); //退出登录时该界面退出
        startClock();
    }

    private void initView() {
        getSupportActionBar().hide();

        showProgressDialog(this);

        final LinearLayout ll_title_me = findViewById(R.id.ll_title_me);
        final LinearLayout ll_title_buy = findViewById(R.id.ll_title_buy);
        RecyclerView recyclerView = findViewById(R.id.rv_course);
        tv_title_time = findViewById(R.id.tv_title_time);
        tv_menu_jx = findViewById(R.id.tv_menu_jx);
        tv_menu_1 = findViewById(R.id.tv_menu_1);
        tv_menu_2 = findViewById(R.id.tv_menu_2);
        tv_menu_3 = findViewById(R.id.tv_menu_3);
        tv_menu_4 = findViewById(R.id.tv_menu_4);
        tv_menu_5 = findViewById(R.id.tv_menu_5);
        tv_menu_6 = findViewById(R.id.tv_menu_6);

        ll_jx_2items = findViewById(R.id.ll_jx_2items);
        rl_jx_item_1 = findViewById(R.id.rl_jx_item_1);
        rl_jx_item_2 = findViewById(R.id.rl_jx_item_2);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setFocusable(false);
        adapter = new HomeAdapter(this, null);
        recyclerView.setAdapter(adapter);

        ll_title_me.setOnClickListener(this);
        ll_title_buy.setOnClickListener(this);
        rl_jx_item_1.setOnClickListener(this);
        rl_jx_item_2.setOnClickListener(this);

        listenTvFocus(tv_menu_jx);
        listenTvFocus(tv_menu_1);
        listenTvFocus(tv_menu_2);
        listenTvFocus(tv_menu_3);
        listenTvFocus(tv_menu_4);
        listenTvFocus(tv_menu_5);
        listenTvFocus(tv_menu_6);
        listenJXFocus(rl_jx_item_1);
        listenJXFocus(rl_jx_item_2);

        textViewList = new ArrayList<>();
        textViewList.add(tv_menu_jx);
        textViewList.add(tv_menu_1);
        textViewList.add(tv_menu_2);
        textViewList.add(tv_menu_3);
        textViewList.add(tv_menu_4);
        textViewList.add(tv_menu_5);
        textViewList.add(tv_menu_6);


        //设置头部个人信息flag的左键、下键聚焦的menu
        ll_title_me.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //下键
                    int id = textViewList.get(currentFocus).getId();
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
                    int id = textViewList.get(currentFocus).getId();
                    ll_title_buy.setNextFocusDownId(id);
                }
            }
        });
    }

    private void initData() {
        Map<String, Object> map = RequestUtil.getBasicMapNoBusinessParams();

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getHomeData(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<HomeWrapper>>(HomeActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<HomeWrapper> result) {
                        closeProgressDialog();

                        if (result.result != null) {
                            homeWrapper = result.result;
                            //设置menu
                            setMenu();

                            //设置精选数据
                            setMenuJxData();
                        }
                    }
                });
    }

    //设置精选数据
    private void setMenuJxData() {
        List<HomeCourseModel> recommendList = homeWrapper.recommend_list;
        if (recommendList == null || recommendList.size() <= 0) {
            return;
        }

        //设置两张精选的图片
        ImageView iv1 = rl_jx_item_1.findViewById(R.id.iv);
        ImageView iv2 = rl_jx_item_2.findViewById(R.id.iv);
        if (recommendList.size() == 1) {
            iv2.setVisibility(View.GONE);
            setImageView(iv1, recommendList.get(0).photo);
            rl_jx_item_2.setFocusable(false);
            adapter.setData(null);
            return;
        }
        setImageView(iv1, recommendList.get(0).photo);
        setImageView(iv2, recommendList.get(1).photo);

        if (recommendList.size() > 2) {
            //设置recyclerView，排除精选的两个
            adapter.setData(recommendList.subList(2, recommendList.size()));
        }
    }

    //设置menu
    private void setMenu() {
        List<HomeColumnModel> columnList = homeWrapper.column_list;
        if (columnList != null && columnList.size() > 0) {
            for (int i = 1; i <= columnList.size(); i++) {
                textViewList.get(i).setText(columnList.get(i - 1).title);
                textViewList.get(i).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_me:
                PersonalInformationActivity.startActivity(this, 0);
                break;
            case R.id.ll_title_buy:
                PayActivityXiaoMi.startActivity(this);
                break;
            case R.id.rl_jx_item_1: //精选课程，大图第1张
                CourseDetailActivity.startActivity(this, homeWrapper.recommend_list.get(0).course_id);
                break;
            case R.id.rl_jx_item_2: //精选课程，大图第2张
                CourseDetailActivity.startActivity(this, homeWrapper.recommend_list.get(1).course_id);
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //返回键
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

    private void listenTvFocus(TextView v) {
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (homeWrapper == null) {
                    return;
                }

                //设置焦点文字背景颜色
                setTextBg((TextView) view, b);

                //设置焦点文字大小
                setTextSize((TextView) view, b);

                if (b) {
                    //精选tab
                    if (view.getId() == R.id.tv_menu_jx) {
                        setVisible(ll_jx_2items);
                        setMenuJxData();
                        currentFocus = 0;
                        return;
                    }

                    //非精选tab
                    setGone(ll_jx_2items);
                    if (view.getId() == R.id.tv_menu_1) {
                        currentFocus = 1;
                        adapter.setData(homeWrapper.column_list.get(0).items);  //第1个menu
                    } else if (view.getId() == R.id.tv_menu_2) {
                        currentFocus = 2;
                        adapter.setData(homeWrapper.column_list.get(1).items);  //第2个menu
                    } else if (view.getId() == R.id.tv_menu_3) {
                        currentFocus = 3;
                        adapter.setData(homeWrapper.column_list.get(2).items);  //第3个menu
                    } else if (view.getId() == R.id.tv_menu_4) {
                        currentFocus = 4;
                        adapter.setData(homeWrapper.column_list.get(3).items);  //第4个menu
                    } else if (view.getId() == R.id.tv_menu_5) {
                        currentFocus = 5;
                        adapter.setData(homeWrapper.column_list.get(4).items);  //第5个menu
                    } else if (view.getId() == R.id.tv_menu_6) {
                        currentFocus = 6;
                        adapter.setData(homeWrapper.column_list.get(5).items);  //第6个menu
                    }
                }
            }
        });
    }

    private void listenJXFocus(final RelativeLayout itemView) {
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValueSmall)
                            .scaleY(Constants.scaleValueSmall)
                            .setDuration(Constants.scaleTime)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(View view) {
                                    view.findViewById(R.id.v_bg).setVisibility(View.VISIBLE);
                                }
                            })
                            .start();

                    //为第一张精选设置左聚焦点
                    if (v.getId() == R.id.rl_jx_item_1) {
                        int id = findViewById(R.id.tv_menu_jx).getId();
                        rl_jx_item_1.setNextFocusLeftId(id);
                    }
                } else {
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .scaleY(1)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(View view) {
                                    view.findViewById(R.id.v_bg).setVisibility(View.GONE);
                                }
                            })
                            .start();
                }
            }
        });
    }

    //聚焦或失焦时，设置menu背景
    private void setTextBg(TextView view, boolean b) {
        if (b) {
            //清空所有menu bg
            List<HomeColumnModel> columnList = homeWrapper.column_list;
            if (columnList != null && columnList.size() > 0) {
                for (int i = 0; i <= columnList.size(); i++) {
                    textViewList.get(i).setBackground(null);
                }
            }
            //为聚焦menu设置bg
            view.setBackground(getResources().getDrawable(R.drawable.bg_rect_green_focused));
        } else {
            //为失焦menu设置痕迹
            view.setBackground(getResources().getDrawable(R.drawable.bg_rect_green_selected));
        }
    }

    public TextView getCurrentMenu() {
        return textViewList.get(currentFocus);
    }

    private void setVisible(View view) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void setGone(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
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
