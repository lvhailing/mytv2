package com.jbtm.parentschool.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.jbtm.parentschool.R;
import com.jbtm.parentschool.network.MyObserverAdapter;
import com.jbtm.parentschool.network.MyRemoteFactory;
import com.jbtm.parentschool.network.MyRequestProxy;
import com.jbtm.parentschool.network.model.ResultModel;
import com.jbtm.parentschool.utils.RequestUtil;
import com.jbtm.parentschool.utils.ToastUtil;
import com.zx.jcvideolib.Jzvd;
import com.zx.jcvideolib.JzvdStd;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 饺子视频内核
 */
public class VideoJzActivity extends AppCompatActivity {
    private JzvdStd jzvdStd;
    protected AudioManager mAudioManager;
    private Handler handler = new Handler();
    private int maxVolume;
    private long lastTime;
    private int materId; //章节id

    public static void startActivity(Context context, int materId, String url, String title) {
        Intent intent = new Intent(context, VideoJzActivity.class);
        intent.putExtra("materId", materId);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_jz);

        getSupportActionBar().hide();

        JzvdStd.TOOL_BAR_EXIST = false;
        JzvdStd.ACTION_BAR_EXIST = false;

        jzvdStd = findViewById(R.id.video_player);

        materId = getIntent().getIntExtra("materId", 0);
        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");

        //常见视频格式： mpeg/mpg/dat avi mov asf wmv 3gp mkv flv rmvb rm mtv amv dmv
//        String url = "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4";
//        String url = "http://2449.vod.myqcloud.com/2449_22ca37a6ea9011e5acaaf51d105342e3.f20.mp4";
//        String url = "http://jbtm-test.oss-cn-beijing.aliyuncs.com/v3.0/20181227/20181227115636754.mp4";
//        String title = "美国课程";

        jzvdStd.setUp(url == null ? "" : url, title == null ? "" : title, JzvdStd.SCREEN_WINDOW_LIST);
        jzvdStd.startVideo();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:  // 播放/暂停
                playOrPause();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:  //加声音
                volumeAdd();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:    //减声音
                volumeSub();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:    //快退
                backFast();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:   //快进
                goFast();
                break;
            case KeyEvent.KEYCODE_BACK:   //返回键
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastTime < 1500) {
                    //双击了返回键，上传播放进度记录，退出应用
                    getProgress();
                    finish();
                } else {
                    lastTime = System.currentTimeMillis();
                    ToastUtil.showCustom("再按退出播放");
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //快进
    public void goFast() {
        int currentProgress = jzvdStd.getMyProgress();
        jzvdStd.setMyProgress(currentProgress + 5);
        jzvdStd.onStopTrackingTouch(jzvdStd.getProgressBar());
    }

    //快退
    public void backFast() {
        int currentProgress = jzvdStd.getMyProgress();
        jzvdStd.setMyProgress(currentProgress - 5);
        jzvdStd.onStopTrackingTouch(jzvdStd.getProgressBar());
    }

    //加声音
    public void volumeAdd() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int volumePercent = currentVolume * 100 / maxVolume;
        jzvdStd.showVolumeDialog(0, volumePercent);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jzvdStd.dismissVolumeDialog();
            }
        }, 2000);
    }

    //减声音
    public void volumeSub() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int volumePercent = currentVolume * 100 / maxVolume;
        jzvdStd.showVolumeDialog(0, volumePercent);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jzvdStd.dismissVolumeDialog();
            }
        }, 2000);
    }

    //上传播放进度记录
    public void getProgress() {
        //当前时长 单位毫秒
        long progress = jzvdStd.getCurrentPositionWhenPlaying();
        Log.i("aaa", "时长： " + progress / 1000);

        Map<String, Object> map = new HashMap<>();
        map.put("ma_id", materId);
        map.put("progress_time", progress / 1000);
        RequestUtil.getBasicMap(map);

        MyRemoteFactory.getInstance().getProxy(MyRequestProxy.class)
                .updateProgress(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserverAdapter<ResultModel>(VideoJzActivity.this) {
                    @Override
                    public void onMyError(Throwable e) {
//                        ToastUtil.showCustom("调接口失败");
                    }

                    @Override
                    public void onMySuccess(ResultModel result) {
                    }
                });
    }

    // 播放/暂停
    public void playOrPause() {
        jzvdStd.startButton.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(jzvdStd.batteryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
