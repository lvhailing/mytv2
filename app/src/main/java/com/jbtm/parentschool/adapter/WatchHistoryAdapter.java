package com.jbtm.parentschool.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.CourseDetailActivity;
import com.jbtm.parentschool.activity.PersonalInformationActivity;
import com.jbtm.parentschool.models.WatchHistoryModel;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.UIUtil;

import java.util.List;

public class WatchHistoryAdapter extends RecyclerView.Adapter<WatchHistoryAdapter.ViewHolder> {
    private List<WatchHistoryModel> list;
    private Context mContext;
    private int from;   //0，从观看记录来。1，从订购信息来

    public WatchHistoryAdapter(Context context, List<WatchHistoryModel> list, int from) {
        super();
        mContext = context;
        this.list = list;
        this.from = from;
    }

    public void setData(List<WatchHistoryModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_watch_history, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.tv_progress.setText("已观看" + list.get(position).progress + "%");

        setImageView(viewHolder.iv, list.get(position).photo);

        listenViewFocus(viewHolder.itemView, viewHolder.v_bg, position);
        listenViewClick(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv;
        private ImageView v_bg;    //边框
        private TextView tv_progress;
        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            iv = itemView.findViewById(R.id.iv);
            v_bg = itemView.findViewById(R.id.v_bg);
            tv_progress = itemView.findViewById(R.id.tv_progress);
        }
    }

    private void listenViewClick(View itemView, final int position) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtil.showCustom(list.get(position).title);
                CourseDetailActivity.startActivity(mContext, list.get(position).course_id);
            }
        });
    }

    private void listenViewFocus(View itemView, final ImageView v_bg, final int position) {
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValue)
                            .scaleY(Constants.scaleValue)
                            .setDuration(Constants.scaleTime)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onAnimationStart(View view) {
                                    v_bg.setVisibility(View.VISIBLE);
                                    //防止被其他view z轴方向覆盖
                                    UIUtil.bringToFront(view);
                                }
                            })
                            .start();
                    if (position % 4 == 0) {
                        setNextFocus(v);
                    }
                } else {
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .scaleY(1)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onAnimationStart(View view) {
                                    v_bg.setVisibility(View.GONE);
                                    //防止z轴方向，覆盖其他view
                                    UIUtil.bringToBackground(view);
                                }
                            })
                            .start();
                }
            }
        });
    }

    private void setNextFocus(View v) {
        int id;
        if (from == 1) {
            id = ((PersonalInformationActivity) mContext).findViewById(R.id.tv_menu_buy).getId();
        } else {
            id = ((PersonalInformationActivity) mContext).findViewById(R.id.tv_menu_watch_history).getId();
        }
        v.setNextFocusLeftId(id);
    }

    private void setImageView(ImageView imageView, String url) {
        Glide.with(mContext)
                .load(url)
                .into(imageView);
    }
}
