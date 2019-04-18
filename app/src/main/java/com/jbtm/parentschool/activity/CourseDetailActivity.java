package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.adapter.CourseDetailAdapter;
import com.jbtm.parentschool.models.CommonModel;
import com.jbtm.parentschool.models.CommonWrapper;
import com.jbtm.parentschool.models.CourseModel;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.Util;
import com.jbtm.parentschool.widget.FullyGridLayoutManager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CourseDetailActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_title_me; // 点击跳转至个人信息
    private LinearLayout ll_title_buy; // 点击跳转课程详情
    private RelativeLayout rl_play; //全屏播放
    private RelativeLayout rl_buy; //单点这一课
    private ImageView iv_course;
    private TextView tv_title;
    private TextView tv_title_time;
    private TextView tv_flag1;
    private TextView tv_flag2;
    private TextView tv_flag3;
    private TextView tv_exporter;
    private TextView tv_summary;
    private TextView tv_time_progress;
    private RecyclerView recyclerView;

    private int materId;    //默认播放第一集
    private String materTitle;    //课程名称
    private int courseId;   //课程id
    private String coursePrice;   //课程价格
    private CountDownTimer timer;   //系统时间

    public static void startActivity(Context context, int courseId) {
        Intent intent = new Intent(context, CourseDetailActivity.class);
        intent.putExtra("courseId", courseId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

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
        recyclerView = findViewById(R.id.rv_course);

        iv_course = (ImageView) findViewById(R.id.iv_course);
        tv_title_time = findViewById(R.id.tv_title_time);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_flag1 = (TextView) findViewById(R.id.tv_flag1);
        tv_flag2 = (TextView) findViewById(R.id.tv_flag2);
        tv_flag3 = (TextView) findViewById(R.id.tv_flag3);
        tv_exporter = (TextView) findViewById(R.id.tv_exporter);
        tv_summary = (TextView) findViewById(R.id.tv_summary);
        rl_play = (RelativeLayout) findViewById(R.id.rl_play);
        rl_buy = (RelativeLayout) findViewById(R.id.rl_buy);
        tv_time_progress = (TextView) findViewById(R.id.tv_time_progress);

        ll_title_me.setOnClickListener(this);
        ll_title_buy.setOnClickListener(this);
        rl_play.setOnClickListener(this);
        rl_buy.setOnClickListener(this);

        FullyGridLayoutManager gridLayoutManager = new FullyGridLayoutManager(this, 3);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void initData() {
        //设置课程id
        courseId = getIntent().getIntExtra("courseId", 0);

        //获取详情页数据
        Map<String, Object> params = new HashMap<>();
        params.put("course_id", courseId);
        RequestUtil.getBasicMap(params);
        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getDetailData(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonWrapper>>(CourseDetailActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonWrapper> result) {
                        closeProgressDialog();

                        if (result.result != null && result.result.course != null) {
                            CourseModel courseModel = result.result.course;
                            //设置基本数据
                            setBaseData(courseModel);

                            //设置选集
                            setMaters(courseModel);

                            //默认播放第一集
                            if (courseModel.maters != null && courseModel.maters.size() > 0
                                    && courseModel.maters.get(0) != null) {
                                materId = courseModel.maters.get(0).ma_id;
                                materTitle = courseModel.maters.get(0).ma_title;
                            }

                            //课程价格
                            coursePrice = courseModel.price;
                        }
                    }
                });
    }

    //设置基本数据
    private void setBaseData(CourseModel course) {
        if (!TextUtils.isEmpty(course.photo)) {
            setImageView(iv_course, course.photo);
        }
        tv_title.setText(course.title);
        tv_flag1.setText(course.course_time);
        tv_flag2.setText(course.publish_y);
        if (course.tags != null && course.tags.size() > 0) {
            tv_flag3.setText(course.tags.get(0));
        }
        if (course.is_order || course.charge == 0) {
            //已购买过、免费，均不展示
            rl_buy.setVisibility(View.GONE);    //0：免费
        } else {
            rl_buy.setVisibility(View.VISIBLE);    //0：免费
        }
        tv_exporter.setText("主讲专家：" + course.expert_name);
        tv_summary.setText("简介：" + course.summary);
        tv_time_progress.setText("上次观看到：" + course.progress + "%");
    }

    //设置选集
    private void setMaters(CourseModel course) {
        CourseDetailAdapter adapter = new CourseDetailAdapter(this, course);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_me:  //个人信息
                PersonalInformationActivity.startActivity(this, 0);
                break;
            case R.id.ll_title_buy: //套餐购买
                PayActivityXiaoMi.startActivity(this);
                break;
            case R.id.rl_buy:  //单点购买
                PayActivityXiaoMi.startActivity(this, 1, courseId, coursePrice);
                break;
            case R.id.rl_play:  //全屏播放
                //去请求播放地址
                getPlayUrl(materId);
                break;
        }
    }

    //去请求播放地址
    public void getPlayUrl(final int materId) {
        Map<String, Object> params = new HashMap<>();
        params.put("ma_id", materId);
        RequestUtil.getBasicMap(params);
        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .getPlayUrl(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel<CommonModel>>(CourseDetailActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
                        closeProgressDialog();
                    }

                    @Override
                    public void onMySuccess(ResultModel<CommonModel> result) {
                        closeProgressDialog();

                        if (result.result != null && result.result.ma_url != null) {
                            //去播放页面
                            String url = result.result.ma_url;
                            if (Build.VERSION.SDK_INT >= 23) {
                                //7.0及以上走ijkplayer
                                VideoIjkActivity.startActivity(CourseDetailActivity.this, materId, url, materTitle);
                            } else {
                                //7.0及以下走jzplayer
                                VideoJzActivity.startActivity(CourseDetailActivity.this, materId, url, materTitle);
                            }
                        }
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
