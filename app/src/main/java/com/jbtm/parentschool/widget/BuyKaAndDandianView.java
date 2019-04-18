package com.jbtm.parentschool.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.PayActivityXiaoMi;
import com.jbtm.parentschool.adapter.WatchHistoryAdapter;
import com.jbtm.parentschool.models.PayModel;
import com.jbtm.parentschool.models.WatchHistoryModel;

import java.util.List;


public class BuyKaAndDandianView extends LinearLayout {
    private RecyclerView recyclerView;
    private TextView tv_valid_time;
    private TextView tv_buy_again;
    private TextView tv_ka;
    private LinearLayout ll_dandian;
    private Context mContext;
    private WatchHistoryAdapter adapter;

    public BuyKaAndDandianView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BuyKaAndDandianView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.view_buy_ka_and_dandian, this);

        recyclerView = view.findViewById(R.id.rv_dandian);
        tv_ka = view.findViewById(R.id.tv_ka);
        tv_valid_time = view.findViewById(R.id.tv_valid_time);
        tv_buy_again = view.findViewById(R.id.tv_buy_again);
        ll_dandian = view.findViewById(R.id.ll_dandian);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setFocusable(false);

        adapter = new WatchHistoryAdapter(mContext, null, 1);
        recyclerView.setAdapter(adapter);

        tv_buy_again.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PayActivityXiaoMi.startActivity(mContext);
            }
        });

        listenFocus(tv_buy_again);
    }

    public void setKaInfo(PayModel payModel) {
        if (payModel == null) {
            ll_dandian.setVisibility(GONE);
            return;
        }
        ll_dandian.setVisibility(VISIBLE);
        tv_ka.setText(payModel.name);
        tv_valid_time.setText(payModel.valid_time);
    }

    public void setDandianInfo(List<WatchHistoryModel> list) {
        adapter.setData(list);
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
