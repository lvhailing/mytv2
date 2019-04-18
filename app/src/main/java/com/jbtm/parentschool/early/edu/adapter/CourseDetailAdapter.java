package com.jbtm.parentschool.early.edu.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jbtm.parentschool.Constants;
import com.jbtm.parentschool.R;
import com.jbtm.parentschool.activity.CourseDetailActivity;
import com.jbtm.parentschool.models.CourseModel;
import com.jbtm.parentschool.models.MaterModel;
import com.jbtm.parentschool.utils.ToastUtil;
import com.jbtm.parentschool.utils.UIUtil;

import java.util.List;

public class CourseDetailAdapter extends RecyclerView.Adapter<CourseDetailAdapter.ViewHolder> {
    private List<MaterModel> list;
    private String photoUrl;
    private Context mContext;
    private int picWidth;
    private int picHeight;

    public CourseDetailAdapter(Context context, CourseModel course) {
        super();
        mContext = context;
        this.list = course.maters;
        this.photoUrl = course.photo;

        //计算图片大小，解决模糊问题
        int screenWidth = UIUtil.getScreenWidth((CourseDetailActivity) mContext);
        int screenHeight = UIUtil.getScreenHeight((CourseDetailActivity) mContext);
        picWidth = screenWidth * 235 * 2 / 1920;
        picHeight = screenHeight * 147 * 2 / 1080;
    }

    public void setData(CourseModel course) {
        this.list = course.maters;
        this.photoUrl = course.photo;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_detail, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        if (!TextUtils.isEmpty(photoUrl)) {
            setImageView(viewHolder.iv_mater, photoUrl);
        }
        viewHolder.tv_title.setText(list.get(position).ma_title);
        viewHolder.tv_time.setText(list.get(position).ma_time_format);

        listenViewFocus(viewHolder.itemView);
        listenViewClick(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_mater;
        private TextView tv_title;
        private TextView tv_time;
        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            iv_mater = itemView.findViewById(R.id.iv_mater);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }

    private void listenViewClick(View itemView, final int position) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showCustom("请稍等");
                //去请求播放地址
                ((CourseDetailActivity) mContext).getPlayUrl(list.get(position).ma_id);
            }
        });
    }

    private void listenViewFocus(View itemView) {
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    ViewCompat.animate(v)
                            .scaleX(Constants.scaleValueMid)
                            .scaleY(Constants.scaleValueMid)
                            .setDuration(Constants.scaleTime)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onAnimationStart(View view) {
                                    //防止被其他view z轴方向覆盖
                                    UIUtil.bringToFront(view);
                                }
                            })
                            .start();
                } else {
                    ViewCompat.animate(v)
                            .scaleX(1)
                            .scaleY(1)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onAnimationStart(View view) {
                                    //防止z轴方向，覆盖其他view
                                    UIUtil.bringToBackground(view);
                                }
                            })
                            .start();
                }
            }
        });
    }

    private void setImageView(ImageView imageView, String url) {
        if (picWidth != 0 && picHeight != 0) {
            //按屏幕宽高加载
            Glide.with(mContext)
                    .load(url)
                    .override(picWidth, picHeight)
                    .into(imageView);
        } else {
            //按默认宽高加载
            Glide.with(mContext)
                    .load(url)
                    .into(imageView);
        }
    }
}
