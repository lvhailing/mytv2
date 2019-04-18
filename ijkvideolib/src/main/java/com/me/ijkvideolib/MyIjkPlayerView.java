package com.me.ijkvideolib;

/**
 * Created by lvhailing on 2019/3/13.
 * 类说明：
 */

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dl7.player.R.array;
import com.dl7.player.R.color;
import com.dl7.player.R.dimen;
import com.dl7.player.R.mipmap;
import com.dl7.player.danmaku.BaseDanmakuConverter;
import com.dl7.player.danmaku.BiliDanmukuParser;
import com.dl7.player.danmaku.OnDanmakuListener;
import com.dl7.player.media.AdapterMediaQuality;
import com.dl7.player.media.IjkVideoView;
import com.dl7.player.media.MediaQualityInfo;
import com.dl7.player.utils.AnimHelper;
import com.dl7.player.utils.MotionEventUtils;
import com.dl7.player.utils.NavUtils;
import com.dl7.player.utils.SDCardUtils;
import com.dl7.player.utils.SoftInputUtils;
import com.dl7.player.utils.StringUtils;
import com.dl7.player.utils.WindowUtils;
import com.dl7.player.widgets.ShareDialog;
import com.dl7.player.widgets.ShareDialog.OnDialogClickListener;
import com.dl7.player.widgets.ShareDialog.OnDialogDismissListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import master.flame.danmaku.controller.DrawHandler.Callback;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MyIjkPlayerView extends FrameLayout implements OnClickListener {
    private static final int MAX_VIDEO_SEEK = 1000;
    private static final int DEFAULT_HIDE_TIMEOUT = 5000;
    private static final int MSG_UPDATE_SEEK = 10086;
    private static final int MSG_ENABLE_ORIENTATION = 10087;
    private static final int INVALID_VALUE = -1;
    private IjkVideoView mVideoView;
    public ImageView mPlayerThumb;
    private ProgressBar mLoadingView;
    private IDanmakuView mDanmakuView;
    private TextView mTvVolume;
    private TextView mTvReplay;
    private TextView mTitle;
    private TextView mTvBrightness;
    private TextView mTvFastForward;
    private FrameLayout mFlTouchLayout;
    private LinearLayout mFullscreenTopBar;
    private ImageView mIvBackWindow;
    private FrameLayout mWindowTopBar;
    private ImageView mIvPlay;
    private ImageView mIvPlayCircle;
    private TextView mTvCurTime;
    private SeekBar mPlayerSeek;
    private TextView mTvEndTime;
    private ImageView mIvFullscreen;
    private LinearLayout mLlBottomBar;
    private FrameLayout mFlVideoBox;
    private ImageView mIvPlayerLock;
    private TextView mTvRecoverScreen;
    private TextView mTvSettings;
    private RadioGroup mAspectRatioOptions;
    private AppCompatActivity mAttachActivity;
    private Handler mHandler;
    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;
    private int mMaxVolume;
    private boolean mIsForbidTouch;
    private boolean mIsShowBar;
    private boolean mIsFullscreen;
    private boolean mIsPlayComplete;
    private boolean mIsSeeking;
    private long mTargetPosition;
    private int mCurPosition;
    private int mCurVolume;
    private float mCurBrightness;
    private int mInitHeight;
    private int mWidthPixels;
    private int mScreenUiVisibility;
    private OrientationEventListener mOrientationListener;
    private boolean mIsNeverPlay;
    private OnInfoListener mOutsideInfoListener;
    private boolean mIsForbidOrientation;
    private boolean mIsAlwaysFullScreen;
    private long mExitTime;
    private Matrix mVideoMatrix;
    private Matrix mSaveMatrix;
    private boolean mIsNeedRecoverScreen;
    private int mAspectOptionsHeight;
    private final OnSeekBarChangeListener mSeekListener;
    private Runnable mHideBarRunnable;
    private OnGestureListener mPlayerGestureListener;
    private Runnable mHideTouchViewRunnable;
    private OnTouchListener mPlayerTouchListener;
    private boolean mIsRenderingStart;
    private boolean mIsBufferingStart;
    private OnInfoListener mInfoListener;
    private static final int DEFAULT_QUALITY_TIME = 300;
    public static final int MEDIA_QUALITY_SMOOTH = 0;
    public static final int MEDIA_QUALITY_MEDIUM = 1;
    public static final int MEDIA_QUALITY_HIGH = 2;
    public static final int MEDIA_QUALITY_SUPER = 3;
    public static final int MEDIA_QUALITY_BD = 4;
    private static final int[] QUALITY_DRAWABLE_RES;
    private SparseArray<String> mVideoSource;
    private String[] mMediaQualityDesc;
    private View mFlMediaQuality;
    private TextView mIvMediaQuality;
    private ListView mLvMediaQuality;
    private AdapterMediaQuality mQualityAdapter;
    private List<MediaQualityInfo> mQualityData;
    private boolean mIsShowQuality;
    private int mCurSelectQuality;
    private ImageView mIvCancelSkip;
    private TextView mTvSkipTime;
    private TextView mTvDoSkip;
    private View mLlSkipLayout;
    private int mSkipPosition;
    private Runnable mHideSkipTipRunnable;
    private static final int NORMAL_STATUS = 501;
    private static final int INTERRUPT_WHEN_PLAY = 502;
    private static final int INTERRUPT_WHEN_PAUSE = 503;
    private int mVideoStatus;
    private static final int DANMAKU_TAG_BILI = 701;
    private static final int DANMAKU_TAG_ACFUN = 702;
    private static final int DANMAKU_TAG_CUSTOM = 703;
    private int mDanmakuTag;
    private ImageView mIvDanmakuControl;
    private TextView mTvOpenEditDanmaku;
    private SeekBar mDanmakuPlayerSeek;
    private View mEditDanmakuLayout;
    private EditText mEtDanmakuContent;
    private ImageView mIvCancelSend;
    private ImageView mIvDoSend;
    private View mDanmakuOptionsBasic;
    private RadioGroup mDanmakuTextSizeOptions;
    private RadioGroup mDanmakuTypeOptions;
    private RadioButton mDanmakuCurColor;
    private ImageView mDanmakuMoreColorIcon;
    private View mDanmakuMoreOptions;
    private RadioGroup mDanmakuColorOptions;
    private DanmakuContext mDanmakuContext;
    private BaseDanmakuParser mDanmakuParser;
    private ILoader mDanmakuLoader;
    private BaseDanmakuConverter mDanmakuConverter;
    private OnDanmakuListener mDanmakuListener;
    private boolean mIsEnableDanmaku;
    private int mDanmakuTextColor;
    private float mDanmakuTextSize;
    private int mDanmakuType;
    private int mBasicOptionsWidth;
    private int mMoreOptionsWidth;
    private long mDanmakuTargetPosition;
    private ProgressBar mPbBatteryLevel;
    private TextView mTvSystemTime;
    private ImageView mIvScreenshot;
    private MyIjkPlayerView.BatteryBroadcastReceiver mBatteryReceiver;
    //    private MyIjkPlayerView.ScreenBroadcastReceiver mScreenReceiver;
    private boolean mIsScreenLocked;
    private ShareDialog mShareDialog;
    private OnDialogClickListener mDialogClickListener;
    private OnDialogClickListener mInsideDialogClickListener;
    private OnDialogDismissListener mDialogDismissListener;
    private File mSaveDir;

    public MyIjkPlayerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MyIjkPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 10086) {
                    int pos = _setProgress();
                    if (!mIsSeeking && mIsShowBar && mVideoView.isPlaying()) {
                        msg = obtainMessage(10086);
                        sendMessageDelayed(msg, (long) (1000 - pos % 1000));
                    }
                } else if (msg.what == 10087 && mOrientationListener != null) {
                    mOrientationListener.enable();
                }

            }
        };
        mIsForbidTouch = false;
        mIsShowBar = true;
        mIsPlayComplete = false;
        mTargetPosition = -1L;
        mCurPosition = -1;
        mCurVolume = -1;
        mCurBrightness = -1.0F;
        mIsNeverPlay = true;
        mIsForbidOrientation = true;
        mIsAlwaysFullScreen = false;
        mExitTime = 0L;
        mVideoMatrix = new Matrix();
        mSaveMatrix = new Matrix();
        mIsNeedRecoverScreen = false;
        mSeekListener = new OnSeekBarChangeListener() {
            private long curPosition;

            public void onStartTrackingTouch(SeekBar bar) {
                mIsSeeking = true;
                _showControlBar(3600000);
                mHandler.removeMessages(10086);
                curPosition = (long) mVideoView.getCurrentPosition();
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) {
                    long duration = (long) mVideoView.getDuration();
                    mTargetPosition = duration * (long) progress / 1000L;
                    int deltaTime = (int) ((mTargetPosition - curPosition) / 1000L);
                    String desc;
                    if (mTargetPosition > curPosition) {
                        desc = StringUtils.generateTime(mTargetPosition) + "/" + StringUtils.generateTime(duration) + "\n+" + deltaTime + "秒";
                    } else {
                        desc = StringUtils.generateTime(mTargetPosition) + "/" + StringUtils.generateTime(duration) + "\n" + deltaTime + "秒";
                    }

                    _setFastForward(desc);
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                _hideTouchView();
                mIsSeeking = false;
                seekTo((int) mTargetPosition);
                mTargetPosition = -1L;
                _setProgress();
                _showControlBar(5000);
            }
        };
        mHideBarRunnable = new Runnable() {
            public void run() {
                _hideAllView(false);
            }
        };
        mPlayerGestureListener = new SimpleOnGestureListener() {
            private boolean isDownTouch;
            private boolean isVolume;
            private boolean isLandscape;
            private boolean isRecoverFromDanmaku;

            public boolean onDown(MotionEvent e) {
                isDownTouch = true;
                isRecoverFromDanmaku = recoverFromEditVideo();
                return super.onDown(e);
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mIsForbidTouch && !mIsNeverPlay) {
                    float mOldX = e1.getX();
                    float mOldY = e1.getY();
                    float deltaY = mOldY - e2.getY();
                    float deltaX = mOldX - e2.getX();
                    if (isDownTouch) {
                        isLandscape = Math.abs(distanceX) >= Math.abs(distanceY);
                        isVolume = mOldX > (float) getResources().getDisplayMetrics().widthPixels * 0.5F;
                        isDownTouch = false;
                    }

                    if (isLandscape) {
                        _onProgressSlide(-deltaX / (float) mVideoView.getWidth());
                    } else {
                        float percent = deltaY / (float) mVideoView.getHeight();
                        if (isVolume) {
                            _onVolumeSlide(percent);
                        } else {
                            _onBrightnessSlide(percent);
                        }
                    }
                }

                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isRecoverFromDanmaku) {
                    return true;
                } else {
                    if (mIsShowQuality) {
                        _toggleMediaQuality();
                    } else {
                        _toggleControlBar();
                    }

                    return true;
                }
            }

            public boolean onDoubleTap(MotionEvent e) {
                if (!mIsNeverPlay && !isRecoverFromDanmaku) {
                    if (!mIsForbidTouch) {
                        _refreshHideRunnable();
                        _togglePlayStatus();
                    }

                    return true;
                } else {
                    return true;
                }
            }
        };
        mHideTouchViewRunnable = new Runnable() {
            public void run() {
                _hideTouchView();
            }
        };
        mPlayerTouchListener = new OnTouchListener() {
            private static final int NORMAL = 1;
            private static final int INVALID_POINTER = 2;
            private static final int ZOOM_AND_ROTATE = 3;
            private int mode = 1;
            private PointF midPoint = new PointF(0.0F, 0.0F);
            private float degree = 0.0F;
            private int fingerFlag = -1;
            private float oldDist;
            private float scale;

            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case 0:
                        mode = 1;
                        mHandler.removeCallbacks(mHideBarRunnable);
                    case 1:
                    case 3:
                    case 4:
                    default:
                        break;
                    case 2:
                        if (mode == 3) {
                            float newRotate = MotionEventUtils.rotation(event, fingerFlag);
                            mVideoView.setVideoRotation((int) (newRotate - degree));
                            mVideoMatrix.set(mSaveMatrix);
                            float newDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                            scale = newDist / oldDist;
                            mVideoMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                            mVideoView.setVideoTransform(mVideoMatrix);
                        }
                        break;
                    case 5:
                        if (event.getPointerCount() == 3 && mIsFullscreen) {
                            _hideTouchView();
                            mode = 3;
                            MotionEventUtils.midPoint(midPoint, event);
                            fingerFlag = MotionEventUtils.calcFingerFlag(event);
                            degree = MotionEventUtils.rotation(event, fingerFlag);
                            oldDist = MotionEventUtils.calcSpacing(event, fingerFlag);
                            mSaveMatrix = mVideoView.getVideoTransform();
                        } else {
                            mode = 2;
                        }
                        break;
                    case 6:
                        if (mode == 3) {
                            mIsNeedRecoverScreen = mVideoView.adjustVideoView(scale);
                            if (mIsNeedRecoverScreen && mIsShowBar) {
                                mTvRecoverScreen.setVisibility(VISIBLE);
                            }
                        }

                        mode = 2;
                }

                if (mode == 1) {
                    if (mGestureDetector.onTouchEvent(event)) {
                        return true;
                    }

                    if (MotionEventCompat.getActionMasked(event) == 1) {
                        _endGesture();
                    }
                }

                return false;
            }
        };
        mIsRenderingStart = false;
        mIsBufferingStart = false;
        mInfoListener = new OnInfoListener() {
            public boolean onInfo(IMediaPlayer iMediaPlayer, int status, int extra) {
                _switchStatus(status);
                if (mOutsideInfoListener != null) {
                    mOutsideInfoListener.onInfo(iMediaPlayer, status, extra);
                }

                return true;
            }
        };
        mVideoSource = new SparseArray();
        mIsShowQuality = false;
        mCurSelectQuality = 0;
        mSkipPosition = -1;
        mHideSkipTipRunnable = new Runnable() {
            public void run() {
                _hideSkipTip();
            }
        };
        mVideoStatus = 501;
        mDanmakuTag = 701;
        mIsEnableDanmaku = false;
        mDanmakuTextColor = -1;
        mDanmakuTextSize = -1.0F;
        mDanmakuType = 1;
        mBasicOptionsWidth = -1;
        mMoreOptionsWidth = -1;
        mDanmakuTargetPosition = -1L;
        mIsScreenLocked = false;
        mInsideDialogClickListener = new OnDialogClickListener() {
            public void onShare(Bitmap bitmap, Uri uri) {
                if (mDialogClickListener != null) {
                    mDialogClickListener.onShare(bitmap, mVideoView.getUri());
                }

                File file = new File(mSaveDir, System.currentTimeMillis() + ".jpg");

                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                    Toast.makeText(mAttachActivity, "保存成功，路径为:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (IOException var5) {
                    Toast.makeText(mAttachActivity, "保存本地失败", Toast.LENGTH_SHORT).show();
                }

            }
        };
        mDialogDismissListener = new OnDialogDismissListener() {
            public void onDismiss() {
                recoverFromEditVideo();
            }
        };
        _initView(context);

        setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mTvReplay.setVisibility(VISIBLE);
                mIvPlayCircle.setVisibility(VISIBLE);
            }
        });
    }

    private void _initView(Context context) {
        if (context instanceof AppCompatActivity) {
            mAttachActivity = (AppCompatActivity) context;
            View.inflate(context, R.layout.ijkplayer_layout_player_view, this);
            mVideoView = (IjkVideoView) findViewById(R.id.video_view);
            mPlayerThumb = (ImageView) findViewById(R.id.iv_thumb);
            mLoadingView = (ProgressBar) findViewById(R.id.pb_loading);
            mTitle = (TextView) findViewById(R.id.title);
            mTvVolume = (TextView) findViewById(R.id.tv_volume);
            mTvReplay = (TextView) findViewById(R.id.tv_replay);
            mTvBrightness = (TextView) findViewById(R.id.tv_brightness);
            mTvFastForward = (TextView) findViewById(R.id.tv_fast_forward);
            mFlTouchLayout = (FrameLayout) findViewById(R.id.fl_touch_layout);
            mFullscreenTopBar = (LinearLayout) findViewById(R.id.fullscreen_top_bar);
            mIvBackWindow = (ImageView) findViewById(R.id.iv_back_window);
            mWindowTopBar = (FrameLayout) findViewById(R.id.window_top_bar);
            mIvPlay = (ImageView) findViewById(R.id.iv_play);
            mTvCurTime = (TextView) findViewById(R.id.tv_cur_time);
            mPlayerSeek = (SeekBar) findViewById(R.id.player_seek);
            mTvEndTime = (TextView) findViewById(R.id.tv_end_time);
            mIvFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
            mLlBottomBar = (LinearLayout) findViewById(R.id.ll_bottom_bar);
            mFlVideoBox = (FrameLayout) findViewById(R.id.fl_video_box);
            mIvPlayerLock = (ImageView) findViewById(R.id.iv_player_lock);
            mIvPlayCircle = (ImageView) findViewById(R.id.iv_play_circle);
            mTvRecoverScreen = (TextView) findViewById(R.id.tv_recover_screen);
            mTvSettings = (TextView) findViewById(R.id.tv_settings);
            mAspectRatioOptions = (RadioGroup) findViewById(R.id.aspect_ratio_group);
            mAspectOptionsHeight = getResources().getDimensionPixelSize(dimen.aspect_btn_size) * 4;
            mAspectRatioOptions.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.aspect_fit_parent) {
                        mVideoView.setAspectRatio(0);
                    } else if (checkedId == R.id.aspect_fit_screen) {
                        mVideoView.setAspectRatio(1);
                    } else if (checkedId == R.id.aspect_16_and_9) {
                        mVideoView.setAspectRatio(4);
                    } else if (checkedId == R.id.aspect_4_and_3) {
                        mVideoView.setAspectRatio(5);
                    }

                    AnimHelper.doClipViewHeight(mAspectRatioOptions, mAspectOptionsHeight, 0, 150);
                }
            });
            //全屏
            mVideoView.setAspectRatio(1);
            _initMediaQuality();
            _initVideoSkip();
            _initReceiver();
            mIvPlay.setOnClickListener(this);
            mIvFullscreen.setOnClickListener(this);
            mIvBackWindow.setOnClickListener(this);
            mIvPlayerLock.setOnClickListener(this);
            mIvPlayCircle.setOnClickListener(this);
            mTvRecoverScreen.setOnClickListener(this);
            mTvSettings.setOnClickListener(this);
        } else {
            throw new IllegalArgumentException("Context must be AppCompatActivity");
        }
    }

    private void _initMediaPlayer() {
        IjkMediaPlayer.loadLibrariesOnce((IjkLibLoader) null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mAudioManager = (AudioManager) mAttachActivity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        try {
            int e = android.provider.Settings.System.getInt(mAttachActivity.getContentResolver(), "screen_brightness");
            float progress = 1.0F * (float) e / 255.0F;
            WindowManager.LayoutParams layout = mAttachActivity.getWindow().getAttributes();
            layout.screenBrightness = progress;
            mAttachActivity.getWindow().setAttributes(layout);
        } catch (SettingNotFoundException var4) {
            var4.printStackTrace();
        }

        mPlayerSeek.setMax(1000);
        mPlayerSeek.setOnSeekBarChangeListener(mSeekListener);
        mVideoView.setOnInfoListener(mInfoListener);
        mGestureDetector = new GestureDetector(mAttachActivity, mPlayerGestureListener);
        mFlVideoBox.setClickable(true);
        mFlVideoBox.setOnTouchListener(mPlayerTouchListener);
        mOrientationListener = new OrientationEventListener(mAttachActivity) {
            public void onOrientationChanged(int orientation) {
                _handleOrientation(orientation);
            }
        };
        if (mIsForbidOrientation) {
            mOrientationListener.disable();
        }

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mInitHeight == 0) {
            mInitHeight = getHeight();
            mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        }

    }

    public void onResume() {
        if (mIsScreenLocked) {
            mVideoView.setRender(2);
            mIsScreenLocked = false;
        }

        mVideoView.resume();
        if (!mIsForbidTouch && !mIsForbidOrientation) {
            mOrientationListener.enable();
        }

        if (mCurPosition != -1) {
            seekTo(mCurPosition);
            mCurPosition = -1;
        }

    }

    public void onPause() {
        mCurPosition = mVideoView.getCurrentPosition();
        mVideoView.pause();
        mIvPlay.setSelected(false);
        mOrientationListener.disable();
        _pauseDanmaku();
    }

    public int onDestroy() {
        int curPosition = mVideoView.getCurrentPosition();
        mVideoView.destroy();
        IjkMediaPlayer.native_profileEnd();
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }

        if (mShareDialog != null) {
            mShareDialog.dismiss();
            mShareDialog = null;
        }

        mAttachActivity.unregisterReceiver(mBatteryReceiver);
//        mAttachActivity.unregisterReceiver(mScreenReceiver);
        mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return curPosition;
    }

    public boolean handleVolumeKey(int keyCode) {
        if (keyCode == 24) {
            _setVolume(true);
            return true;
        } else if (keyCode == 25) {
            _setVolume(false);
            return true;
        } else {
            return false;
        }
    }

    public boolean onBackPressed() {
        if (recoverFromEditVideo()) {
            return true;
        } else if (mIsAlwaysFullScreen) {
            _exit();
            return true;
        } else if (mIsFullscreen) {
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (mIsForbidTouch) {
                mIsForbidTouch = false;
                mIvPlayerLock.setSelected(false);
                _setControlBarVisible(mIsShowBar);
            }

            return true;
        } else {
            return false;
        }
    }

    public MyIjkPlayerView init() {
        _initMediaPlayer();
        return this;
    }

    public MyIjkPlayerView setVideoPath(String url) {
        return setVideoPath(Uri.parse(url));
    }

    public MyIjkPlayerView setVideoPath(Uri uri) {
        mVideoView.setVideoURI(uri);
        if (mCurPosition != -1) {
            seekTo(mCurPosition);
            mCurPosition = -1;
        } else {
            seekTo(0);
        }

        return this;
    }

    public MyIjkPlayerView setVideoTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public MyIjkPlayerView alwaysFullScreen() {
        mIsAlwaysFullScreen = true;
        _setFullScreen(true);
        mIvFullscreen.setVisibility(GONE);
        mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        _setUiLayoutFullscreen();
        return this;
    }

    public void start() {
        if (mIsPlayComplete) {
            if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                mDanmakuView.seekTo(Long.valueOf(0L));
                mDanmakuView.pause();
            }

            mIsPlayComplete = false;
        }

        if (!mVideoView.isPlaying()) {
            mIvPlay.setSelected(true);
            mVideoView.start();
            mHandler.sendEmptyMessage(10086);
        }

        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            mIvPlayCircle.setVisibility(GONE);
            mLoadingView.setVisibility(VISIBLE);
            mIsShowBar = false;
            _loadDanmaku();
        }

        mAttachActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }

    public void pause() {
        mIvPlay.setSelected(false);
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }

        _pauseDanmaku();
        mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void seekTo(int position) {
        mVideoView.seekTo(position);
        mDanmakuTargetPosition = (long) position;
    }

    public void stop() {
        pause();
        mVideoView.stopPlayback();
    }

    public void reset() {
    }

    private void _hideAllView(boolean isTouchLock) {
        mFlTouchLayout.setVisibility(GONE);
        mFullscreenTopBar.setVisibility(GONE);
        mWindowTopBar.setVisibility(GONE);
        mLlBottomBar.setVisibility(GONE);
        _showAspectRatioOptions(false);
        if (!isTouchLock) {
            mIvPlayerLock.setVisibility(GONE);
            mIsShowBar = false;
        }

        if (mIsEnableDanmaku) {
            mDanmakuPlayerSeek.setVisibility(GONE);
        }

        if (mIsNeedRecoverScreen) {
            mTvRecoverScreen.setVisibility(GONE);
        }

    }

    private void _setControlBarVisible(boolean isShowBar) {
        if (mIsNeverPlay) {
            mIvPlayCircle.setVisibility(isShowBar ? VISIBLE : GONE);
        } else if (mIsForbidTouch) {
            mIvPlayerLock.setVisibility(isShowBar ? VISIBLE : GONE);
        } else {
            mLlBottomBar.setVisibility(isShowBar ? VISIBLE : GONE);
            if (!isShowBar) {
                _showAspectRatioOptions(false);
            }

            if (mIsFullscreen) {
                mTvSystemTime.setText(StringUtils.getCurFormatTime());
                mFullscreenTopBar.setVisibility(isShowBar ? VISIBLE : GONE);
                mWindowTopBar.setVisibility(GONE);
                mIvPlayerLock.setVisibility(isShowBar ? VISIBLE : GONE);
                if (mIsEnableDanmaku) {
                    mDanmakuPlayerSeek.setVisibility(isShowBar ? VISIBLE : GONE);
                }

                if (mIsNeedRecoverScreen) {
                    mTvRecoverScreen.setVisibility(isShowBar ? VISIBLE : GONE);
                }
            } else {
                mWindowTopBar.setVisibility(isShowBar ? GONE : GONE);   //返回键调整 tv不需要
                mFullscreenTopBar.setVisibility(GONE);
                mIvPlayerLock.setVisibility(GONE);
                if (mIsEnableDanmaku) {
                    mDanmakuPlayerSeek.setVisibility(GONE);
                }

                if (mIsNeedRecoverScreen) {
                    mTvRecoverScreen.setVisibility(GONE);
                }
            }
        }

    }

    private void _toggleControlBar() {
        mIsShowBar = !mIsShowBar;
        _setControlBarVisible(mIsShowBar);
        if (mIsShowBar) {
            mHandler.postDelayed(mHideBarRunnable, 5000L);
            mHandler.sendEmptyMessage(10086);
        }

    }

    public void _showControlBar(int timeout) {
        if (!mIsShowBar) {
            _setProgress();
            mIsShowBar = true;
        }

        _setControlBarVisible(true);
        mHandler.sendEmptyMessage(10086);
        mHandler.removeCallbacks(mHideBarRunnable);
        if (timeout != 0) {
            mHandler.postDelayed(mHideBarRunnable, (long) timeout);
        }

    }

    public void _togglePlayStatus() {
        if (mVideoView.isPlaying()) {
            pause();
            mIvPlayCircle.setVisibility(VISIBLE);
        } else {
            start();
            mIvPlayCircle.setVisibility(GONE);
        }
        //重播文案隐藏（在播放结束时显示）
        if (mTvReplay.getVisibility() == VISIBLE) {
            mTvReplay.setVisibility(GONE);
        }
    }

    public void _refreshHideRunnable() {
        mHandler.removeCallbacks(mHideBarRunnable);
        mHandler.postDelayed(mHideBarRunnable, 5000L);
    }

    private void _togglePlayerLock() {
        mIsForbidTouch = !mIsForbidTouch;
        mIvPlayerLock.setSelected(mIsForbidTouch);
        if (mIsForbidTouch) {
            mOrientationListener.disable();
            _hideAllView(true);
        } else {
            if (!mIsForbidOrientation) {
                mOrientationListener.enable();
            }

            mFullscreenTopBar.setVisibility(VISIBLE);
            mLlBottomBar.setVisibility(VISIBLE);
            if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setVisibility(VISIBLE);
            }

            if (mIsNeedRecoverScreen) {
                mTvRecoverScreen.setVisibility(VISIBLE);
            }
        }

    }

    private void _toggleMediaQuality() {
        if (mFlMediaQuality.getVisibility() == GONE) {
            mFlMediaQuality.setVisibility(VISIBLE);
        }

        if (mIsShowQuality) {
            ViewCompat.animate(mFlMediaQuality).translationX((float) mFlMediaQuality.getWidth()).setDuration(300L);
            mIsShowQuality = false;
        } else {
            ViewCompat.animate(mFlMediaQuality).translationX(0.0F).setDuration(300L);
            mIsShowQuality = true;
        }

    }

    private void _showAspectRatioOptions(boolean isShow) {
        if (isShow) {
            AnimHelper.doClipViewHeight(mAspectRatioOptions, 0, mAspectOptionsHeight, 150);
        } else {
            android.view.ViewGroup.LayoutParams layoutParams = mAspectRatioOptions.getLayoutParams();
            layoutParams.height = 0;
        }

    }

    public void onClick(View v) {
        _refreshHideRunnable();
        int id = v.getId();
        if (id == R.id.iv_back_window) {
            mAttachActivity.finish();
        } else if (id != R.id.iv_play && id != R.id.iv_play_circle) {
            if (id == R.id.iv_fullscreen) {
                _toggleFullScreen();
            } else if (id == R.id.iv_player_lock) {
                _togglePlayerLock();
            } else if (id == R.id.iv_media_quality) {
                if (!mIsShowQuality) {
                    _toggleMediaQuality();
                }
            } else if (id == R.id.iv_cancel_skip) {
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
            } else if (id == R.id.tv_do_skip) {
                mLoadingView.setVisibility(VISIBLE);
                seekTo(mSkipPosition);
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
                _setProgress();
            } else if (id == R.id.iv_danmaku_control) {
                _toggleDanmakuShow();
            } else if (id == R.id.tv_open_edit_danmaku) {
                if (mDanmakuListener == null || mDanmakuListener.isValid()) {
                    editVideo();
                    mEditDanmakuLayout.setVisibility(VISIBLE);
                    SoftInputUtils.setEditFocusable(mAttachActivity, mEtDanmakuContent);
                }
            } else if (id == R.id.iv_cancel_send) {
                recoverFromEditVideo();
            } else if (id == R.id.iv_do_send) {
                recoverFromEditVideo();
                sendDanmaku(mEtDanmakuContent.getText().toString(), false);
                mEtDanmakuContent.setText("");
            } else if (id == R.id.input_options_more) {
                _toggleMoreColorOptions();
            } else if (id == R.id.iv_screenshot) {
                _doScreenshot();
            } else if (id == R.id.tv_recover_screen) {
                mVideoView.resetVideoView(true);
                mIsNeedRecoverScreen = false;
                mTvRecoverScreen.setVisibility(GONE);
            } else if (id == R.id.tv_settings) {
                _showAspectRatioOptions(true);
            }
        } else {
            _togglePlayStatus();
        }

    }

    public MyIjkPlayerView enableOrientation() {
        mIsForbidOrientation = false;
        mOrientationListener.enable();
        return this;
    }

    private void _toggleFullScreen() {
        if (WindowUtils.getScreenOrientation(mAttachActivity) == 0) {
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }

    private void _setFullScreen(boolean isFullscreen) {
        mIsFullscreen = isFullscreen;
        _toggleDanmakuView(isFullscreen);
        _handleActionBar(isFullscreen);
        _changeHeight(isFullscreen);
        mIvFullscreen.setSelected(isFullscreen);
        mHandler.post(mHideBarRunnable);
        mIvMediaQuality.setVisibility(isFullscreen ? VISIBLE : GONE);
        mLlBottomBar.setBackgroundResource(isFullscreen ? color.bg_video_view : 17170445);
        if (mIsShowQuality && !isFullscreen) {
            _toggleMediaQuality();
        }

        if (mIsNeedRecoverScreen) {
            if (isFullscreen) {
                mVideoView.adjustVideoView(1.0F);
                mTvRecoverScreen.setVisibility(mIsShowBar ? VISIBLE : GONE);
            } else {
                mVideoView.resetVideoView(false);
                mTvRecoverScreen.setVisibility(GONE);
            }
        }

        if (!isFullscreen) {
            _showAspectRatioOptions(false);
        }

    }

    @SuppressLint("WrongConstant")
    private void _handleOrientation(int orientation) {
        if (!mIsNeverPlay) {
            if (mIsFullscreen && !mIsAlwaysFullScreen) {
                if (orientation >= 0 && orientation <= 30 || orientation >= 330) {
                    mAttachActivity.setRequestedOrientation(1);
                }
            } else if (orientation >= 60 && orientation <= 120) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.CONFIG_TOUCHSCREEN);
            } else if (orientation >= 240 && orientation <= 300) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        }
    }

    private void _refreshOrientationEnable() {
        if (!mIsForbidOrientation) {
            mOrientationListener.disable();
            mHandler.removeMessages(10087);
            mHandler.sendEmptyMessageDelayed(10087, 3000L);
        }

    }

    private void _handleActionBar(boolean isFullscreen) {
        ActionBar supportActionBar = mAttachActivity.getSupportActionBar();
        if (supportActionBar != null) {
            if (isFullscreen) {
                supportActionBar.hide();
            } else {
                supportActionBar.show();
            }
        }

    }

    private void _changeHeight(boolean isFullscreen) {
        if (!mIsAlwaysFullScreen) {
            android.view.ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (isFullscreen) {
                layoutParams.height = mWidthPixels;
            } else {
                layoutParams.height = mInitHeight;
            }

            setLayoutParams(layoutParams);
        }
    }

    private void _setUiLayoutFullscreen() {
        if (VERSION.SDK_INT >= 14) {
            View decorView = mAttachActivity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(5894);
            mAttachActivity.getWindow().addFlags(1024);
        }

    }

    public void configurationChanged(Configuration newConfig) {
        _refreshOrientationEnable();
        if (VERSION.SDK_INT >= 14) {
            View decorView;
            if (newConfig.orientation == 2) {
                decorView = mAttachActivity.getWindow().getDecorView();
                mScreenUiVisibility = decorView.getSystemUiVisibility();
                decorView.setSystemUiVisibility(5894);
                _setFullScreen(true);
                mAttachActivity.getWindow().addFlags(1024);
            } else if (newConfig.orientation == 1) {
                decorView = mAttachActivity.getWindow().getDecorView();
                decorView.setSystemUiVisibility(mScreenUiVisibility);
                _setFullScreen(false);
                mAttachActivity.getWindow().clearFlags(1024);
            }
        }

    }

    private void _exit() {
        if (System.currentTimeMillis() - mExitTime > 2000L) {
            Toast.makeText(mAttachActivity, "再按一次退出", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            mAttachActivity.finish();
        }

    }

    private int _setProgress() {
        if (mVideoView != null && !mIsSeeking) {
            int position = mVideoView.getCurrentPosition();
            int duration = mVideoView.getDuration();
            if (duration > 0) {
                long pos = 1000L * (long) position / (long) duration;
                mPlayerSeek.setProgress((int) pos);
                if (mIsEnableDanmaku) {
                    mDanmakuPlayerSeek.setProgress((int) pos);
                }
            }

            int percent = mVideoView.getBufferPercentage();
            mPlayerSeek.setSecondaryProgress(percent * 10);
            if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setSecondaryProgress(percent * 10);
            }

            mTvEndTime.setText(StringUtils.generateTime((long) duration));
            mTvCurTime.setText(StringUtils.generateTime((long) position));
            return position;
        } else {
            return 0;
        }
    }

    private void _setFastForward(String time) {
        if (mFlTouchLayout.getVisibility() == GONE) {
            mFlTouchLayout.setVisibility(VISIBLE);
        }

        if (mTvFastForward.getVisibility() == GONE) {
            mTvFastForward.setVisibility(VISIBLE);
        }

        mTvFastForward.setText(time);
    }

    public void _hideTouchView() {
        if (mFlTouchLayout.getVisibility() == VISIBLE) {
            mTvFastForward.setVisibility(GONE);
            mTvVolume.setVisibility(GONE);
            mTvBrightness.setVisibility(GONE);
            mFlTouchLayout.setVisibility(GONE);
        }

    }

    public void _onProgressSlide(float percent) {
        int position = mVideoView.getCurrentPosition();
        long duration = (long) mVideoView.getDuration();
        long deltaMax = Math.min(100000L, duration / 2L);
        long delta = (long) ((float) deltaMax * percent);
        mTargetPosition = delta + (long) position;
        if (mTargetPosition > duration) {
            mTargetPosition = duration;
        } else if (mTargetPosition <= 0L) {
            mTargetPosition = 0L;
        }

        int deltaTime = (int) ((mTargetPosition - (long) position) / 1000L);
        String desc;
        if (mTargetPosition > (long) position) {
            desc = StringUtils.generateTime(mTargetPosition) + "/" + StringUtils.generateTime(duration) + "\n+" + deltaTime + "秒";
        } else {
            desc = StringUtils.generateTime(mTargetPosition) + "/" + StringUtils.generateTime(duration) + "\n" + deltaTime + "秒";
        }

        _setFastForward(desc);
    }

    private void _setVolumeInfo(int volume) {
        if (mFlTouchLayout.getVisibility() == GONE) {
            mFlTouchLayout.setVisibility(VISIBLE);
        }

        if (mTvVolume.getVisibility() == GONE) {
            mTvVolume.setVisibility(VISIBLE);
        }

        mTvVolume.setText(volume * 100 / mMaxVolume + "%");
    }

    public void _onVolumeSlide(float percent) {
        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }

        int index = (int) (percent * (float) mMaxVolume) + mCurVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        _setVolumeInfo(index);
    }

    public void _setVolume(boolean isIncrease) {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (isIncrease) {
            curVolume += mMaxVolume / 15;
        } else {
            curVolume -= mMaxVolume / 15;
        }

        if (curVolume > mMaxVolume) {
            curVolume = mMaxVolume;
        } else if (curVolume < 0) {
            curVolume = 0;
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        _setVolumeInfo(curVolume);
        mHandler.removeCallbacks(mHideTouchViewRunnable);
        mHandler.postDelayed(mHideTouchViewRunnable, 1000L);
    }

    private void _setBrightnessInfo(float brightness) {
        if (mFlTouchLayout.getVisibility() == GONE) {
            mFlTouchLayout.setVisibility(VISIBLE);
        }

        if (mTvBrightness.getVisibility() == GONE) {
            mTvBrightness.setVisibility(VISIBLE);
        }

        mTvBrightness.setText(Math.ceil((double) (brightness * 100.0F)) + "%");
    }

    private void _onBrightnessSlide(float percent) {
        if (mCurBrightness < 0.0F) {
            mCurBrightness = mAttachActivity.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness < 0.0F) {
                mCurBrightness = 0.5F;
            } else if (mCurBrightness < 0.01F) {
                mCurBrightness = 0.01F;
            }
        }

        WindowManager.LayoutParams attributes = mAttachActivity.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness > 1.0F) {
            attributes.screenBrightness = 1.0F;
        } else if (attributes.screenBrightness < 0.01F) {
            attributes.screenBrightness = 0.01F;
        }

        _setBrightnessInfo(attributes.screenBrightness);
        mAttachActivity.getWindow().setAttributes(attributes);
    }

    public void _endGesture() {
        if (mTargetPosition >= 0L && mTargetPosition != (long) mVideoView.getCurrentPosition()) {
            seekTo((int) mTargetPosition);
            mPlayerSeek.setProgress((int) (mTargetPosition * 1000L / (long) mVideoView.getDuration()));
            if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setProgress((int) (mTargetPosition * 1000L / (long) mVideoView.getDuration()));
            }

            mTargetPosition = -1L;
        }

        _hideTouchView();
        _refreshHideRunnable();
        mCurVolume = -1;
        mCurBrightness = -1.0F;
    }

    public void _endGesture2() {
        if (mTargetPosition >= 0L && mTargetPosition != (long) mVideoView.getCurrentPosition()) {
            seekTo((int) mTargetPosition);
            mPlayerSeek.setProgress((int) (mTargetPosition * 1000L / (long) mVideoView.getDuration()));
            if (mIsEnableDanmaku) {
                mDanmakuPlayerSeek.setProgress((int) (mTargetPosition * 1000L / (long) mVideoView.getDuration()));
            }

            mTargetPosition = -1L;
        }

        mCurVolume = -1;
        mCurBrightness = -1.0F;
    }

    private void _switchStatus(int status) {
        switch (status) {
            case 3:
                mIsRenderingStart = true;
            case 702:
                mIsBufferingStart = false;
                mLoadingView.setVisibility(GONE);
                mPlayerThumb.setVisibility(GONE);
                mHandler.sendEmptyMessage(10086);
                if (mSkipPosition != -1) {
                    _showSkipTip();
                }

                if (mVideoView.isPlaying()) {
                    _resumeDanmaku();
                }
                break;
            case 331:
                _pauseDanmaku();
            case 332:
            default:
                break;
            case 334:
                if (mIsRenderingStart && !mIsBufferingStart) {
                    _resumeDanmaku();
                }
                break;
            case 336:
                pause();
                mIsPlayComplete = true;
                break;
            case 701:
                mIsBufferingStart = true;
                _pauseDanmaku();
                if (!mIsNeverPlay) {
                    mLoadingView.setVisibility(VISIBLE);
                }
        }

    }

    public void setOnPreparedListener(OnPreparedListener l) {
        mVideoView.setOnPreparedListener(l);
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mVideoView.setOnCompletionListener(l);
    }

    public void setOnErrorListener(OnErrorListener l) {
        mVideoView.setOnErrorListener(l);
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOutsideInfoListener = l;
    }

    public void setDanmakuListener(OnDanmakuListener danmakuListener) {
        mDanmakuListener = danmakuListener;
    }

    private void _initMediaQuality() {
        mMediaQualityDesc = getResources().getStringArray(array.media_quality);
        mFlMediaQuality = findViewById(R.id.fl_media_quality);
        mIvMediaQuality = (TextView) findViewById(R.id.iv_media_quality);
        mIvMediaQuality.setOnClickListener(this);
        mLvMediaQuality = (ListView) findViewById(R.id.lv_media_quality);
        mQualityAdapter = new AdapterMediaQuality(mAttachActivity);
        mLvMediaQuality.setAdapter(mQualityAdapter);
        mLvMediaQuality.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurSelectQuality != ((MediaQualityInfo) mQualityAdapter.getItem(position)).getIndex()) {
                    setMediaQuality(((MediaQualityInfo) mQualityAdapter.getItem(position)).getIndex());
                    mLoadingView.setVisibility(VISIBLE);
                    start();
                }

                _toggleMediaQuality();
            }
        });
    }

    public MyIjkPlayerView setVideoSource(String mediaSmooth, String mediaMedium, String mediaHigh, String mediaSuper, String mediaBd) {
        boolean isSelect = true;
        mQualityData = new ArrayList();
        if (mediaSmooth != null) {
            mVideoSource.put(0, mediaSmooth);
            mQualityData.add(new MediaQualityInfo(0, mMediaQualityDesc[0], isSelect));
            mCurSelectQuality = 0;
            isSelect = false;
        }

        if (mediaMedium != null) {
            mVideoSource.put(1, mediaMedium);
            mQualityData.add(new MediaQualityInfo(1, mMediaQualityDesc[1], isSelect));
            if (isSelect) {
                mCurSelectQuality = 1;
            }

            isSelect = false;
        }

        if (mediaHigh != null) {
            mVideoSource.put(2, mediaHigh);
            mQualityData.add(new MediaQualityInfo(2, mMediaQualityDesc[2], isSelect));
            if (isSelect) {
                mCurSelectQuality = 2;
            }

            isSelect = false;
        }

        if (mediaSuper != null) {
            mVideoSource.put(3, mediaSuper);
            mQualityData.add(new MediaQualityInfo(3, mMediaQualityDesc[3], isSelect));
            if (isSelect) {
                mCurSelectQuality = 3;
            }

            isSelect = false;
        }

        if (mediaBd != null) {
            mVideoSource.put(4, mediaBd);
            mQualityData.add(new MediaQualityInfo(4, mMediaQualityDesc[4], isSelect));
            if (isSelect) {
                mCurSelectQuality = 4;
            }
        }

        mQualityAdapter.updateItems(mQualityData);
        mIvMediaQuality.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, ContextCompat.getDrawable(mAttachActivity, QUALITY_DRAWABLE_RES[mCurSelectQuality]), (Drawable) null, (Drawable) null);
        mIvMediaQuality.setText(mMediaQualityDesc[mCurSelectQuality]);
        setVideoPath((String) mVideoSource.get(mCurSelectQuality));
        return this;
    }

    public MyIjkPlayerView setMediaQuality(int quality) {
        if (mCurSelectQuality != quality && mVideoSource.get(quality) != null) {
            mQualityAdapter.setMediaQuality(quality);
            mIvMediaQuality.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, ContextCompat.getDrawable(mAttachActivity, QUALITY_DRAWABLE_RES[quality]), (Drawable) null, (Drawable) null);
            mIvMediaQuality.setText(mMediaQualityDesc[quality]);
            mCurSelectQuality = quality;
            if (mVideoView.isPlaying()) {
                mCurPosition = mVideoView.getCurrentPosition();
                mVideoView.release(false);
            }

            mVideoView.setRender(2);
            setVideoPath((String) mVideoSource.get(quality));
            return this;
        } else {
            return this;
        }
    }

    private void _initVideoSkip() {
        mLlSkipLayout = findViewById(R.id.ll_skip_layout);
        mIvCancelSkip = (ImageView) findViewById(R.id.iv_cancel_skip);
        mTvSkipTime = (TextView) findViewById(R.id.tv_skip_time);
        mTvDoSkip = (TextView) findViewById(R.id.tv_do_skip);
        mIvCancelSkip.setOnClickListener(this);
        mTvDoSkip.setOnClickListener(this);
    }

    public int getCurPosition() {
        return mVideoView.getCurrentPosition();
    }

    public MyIjkPlayerView setSkipTip(int targetPosition) {
        mSkipPosition = targetPosition;
        return this;
    }

    private void _showSkipTip() {
        if (mSkipPosition != -1 && mLlSkipLayout.getVisibility() == GONE) {
            mLlSkipLayout.setVisibility(VISIBLE);
            mTvSkipTime.setText(StringUtils.generateTime((long) mSkipPosition));
            AnimHelper.doSlideRightIn(mLlSkipLayout, mWidthPixels, 0, 800);
            mHandler.postDelayed(mHideSkipTipRunnable, 15000L);
        }

    }

    private void _hideSkipTip() {
        if (mLlSkipLayout.getVisibility() != GONE) {
            ViewCompat.animate(mLlSkipLayout).translationX((float) (-mLlSkipLayout.getWidth())).alpha(0.0F).setDuration(500L).setListener(new ViewPropertyAnimatorListenerAdapter() {
                public void onAnimationEnd(View view) {
                    mLlSkipLayout.setVisibility(GONE);
                }
            }).start();
            mSkipPosition = -1;
        }
    }

    private void _initDanmaku() {
        mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
        mIvDanmakuControl = (ImageView) findViewById(R.id.iv_danmaku_control);
        mTvOpenEditDanmaku = (TextView) findViewById(R.id.tv_open_edit_danmaku);
        mEditDanmakuLayout = findViewById(R.id.ll_edit_danmaku);
        mEtDanmakuContent = (EditText) findViewById(R.id.et_danmaku_content);
        mIvCancelSend = (ImageView) findViewById(R.id.iv_cancel_send);
        mIvDoSend = (ImageView) findViewById(R.id.iv_do_send);
        mDanmakuPlayerSeek = (SeekBar) findViewById(R.id.danmaku_player_seek);
        mDanmakuPlayerSeek.setMax(1000);
        mDanmakuPlayerSeek.setOnSeekBarChangeListener(mSeekListener);
        mIvDanmakuControl.setOnClickListener(this);
        mTvOpenEditDanmaku.setOnClickListener(this);
        mIvCancelSend.setOnClickListener(this);
        mIvDoSend.setOnClickListener(this);
        int navigationBarHeight = NavUtils.getNavigationBarHeight(mAttachActivity);
        if (navigationBarHeight > 0) {
            mEditDanmakuLayout.setPadding(0, 0, navigationBarHeight, 0);
        }

        int oneBtnWidth = getResources().getDimensionPixelOffset(dimen.danmaku_input_options_color_radio_btn_size);
        mMoreOptionsWidth = oneBtnWidth * 12;
        mDanmakuOptionsBasic = findViewById(R.id.input_options_basic);
        mDanmakuMoreOptions = findViewById(R.id.input_options_more);
        mDanmakuMoreOptions.setOnClickListener(this);
        mDanmakuCurColor = (RadioButton) findViewById(R.id.input_options_color_current);
        mDanmakuMoreColorIcon = (ImageView) findViewById(R.id.input_options_color_more_icon);
        mDanmakuTextSizeOptions = (RadioGroup) findViewById(R.id.input_options_group_textsize);
        mDanmakuTypeOptions = (RadioGroup) findViewById(R.id.input_options_group_type);
        mDanmakuColorOptions = (RadioGroup) findViewById(R.id.input_options_color_group);
        mDanmakuTextSizeOptions.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.input_options_small_textsize) {
                    mDanmakuTextSize = 25.0F * (mDanmakuParser.getDisplayer().getDensity() - 0.6F) * 0.7F;
                } else if (checkedId == R.id.input_options_medium_textsize) {
                    mDanmakuTextSize = 25.0F * (mDanmakuParser.getDisplayer().getDensity() - 0.6F);
                }

            }
        });
        mDanmakuTypeOptions.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.input_options_rl_type) {
                    mDanmakuType = 1;
                } else if (checkedId == R.id.input_options_top_type) {
                    mDanmakuType = 5;
                } else if (checkedId == R.id.input_options_bottom_type) {
                    mDanmakuType = 4;
                }

            }
        });
        mDanmakuColorOptions.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String color = (String) findViewById(checkedId).getTag();
                mDanmakuTextColor = Color.parseColor(color);
                mDanmakuCurColor.setBackgroundColor(mDanmakuTextColor);
            }
        });
    }

    private void _loadDanmaku() {
        if (mIsEnableDanmaku) {
            mDanmakuContext = DanmakuContext.create();
            if (mDanmakuParser == null) {
                mDanmakuParser = new BaseDanmakuParser() {
                    protected Danmakus parse() {
                        return new Danmakus();
                    }
                };
            }

            mDanmakuView.setCallback(new Callback() {
                public void prepared() {
                    if (mVideoView.isPlaying() && !mIsBufferingStart) {
                        mDanmakuView.start();
                    }

                }

                public void updateTimer(DanmakuTimer timer) {
                }

                public void danmakuShown(BaseDanmaku danmaku) {
                }

                public void drawingFinished() {
                }
            });
            mDanmakuView.enableDanmakuDrawingCache(true);
            mDanmakuView.prepare(mDanmakuParser, mDanmakuContext);
        }

    }

    public MyIjkPlayerView enableDanmaku() {
        mIsEnableDanmaku = true;
        _initDanmaku();
        if (mIsAlwaysFullScreen) {
            _toggleDanmakuView(true);
        }

        return this;
    }

    public MyIjkPlayerView setDanmakuSource(InputStream stream) {
        if (stream == null) {
            return this;
        } else if (!mIsEnableDanmaku) {
            throw new RuntimeException("Danmaku is disable, use enableDanmaku() first");
        } else {
            if (mDanmakuLoader == null) {
                mDanmakuLoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
            }

            try {
                mDanmakuLoader.load(stream);
            } catch (IllegalDataException var3) {
                var3.printStackTrace();
            }

            IDataSource<?> dataSource = mDanmakuLoader.getDataSource();
            if (mDanmakuParser == null) {
                mDanmakuParser = new BiliDanmukuParser();
            }

            mDanmakuParser.load(dataSource);
            return this;
        }
    }

    public MyIjkPlayerView setDanmakuSource(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return this;
        } else if (!mIsEnableDanmaku) {
            throw new RuntimeException("Danmaku is disable, use enableDanmaku() first");
        } else {
            if (mDanmakuLoader == null) {
                mDanmakuLoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
            }

            try {
                mDanmakuLoader.load(uri);
            } catch (IllegalDataException var3) {
                var3.printStackTrace();
            }

            IDataSource<?> dataSource = mDanmakuLoader.getDataSource();
            if (mDanmakuParser == null) {
                mDanmakuParser = new BiliDanmukuParser();
            }

            mDanmakuParser.load(dataSource);
            return this;
        }
    }

    public MyIjkPlayerView setDanmakuCustomParser(BaseDanmakuParser parser, ILoader loader, BaseDanmakuConverter converter) {
        mDanmakuParser = parser;
        mDanmakuLoader = loader;
        mDanmakuConverter = converter;
        return this;
    }

    public MyIjkPlayerView showOrHideDanmaku(boolean isShow) {
        if (isShow) {
            mIvDanmakuControl.setSelected(false);
            mDanmakuView.show();
        } else {
            mIvDanmakuControl.setSelected(true);
            mDanmakuView.hide();
        }

        return this;
    }

    public void sendDanmaku(String text, boolean isLive) {
        if (!mIsEnableDanmaku) {
            throw new RuntimeException("Danmaku is disable, use enableDanmaku() first");
        } else if (TextUtils.isEmpty(text)) {
            Toast.makeText(mAttachActivity, "内容为空", Toast.LENGTH_SHORT).show();
        } else {
            BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(mDanmakuType);
            if (danmaku != null && mDanmakuView != null) {
                if (mDanmakuTextSize == -1.0F) {
                    mDanmakuTextSize = 25.0F * (mDanmakuParser.getDisplayer().getDensity() - 0.6F);
                }

                danmaku.text = text;
                danmaku.padding = 5;
                danmaku.isLive = isLive;
                danmaku.priority = 0;
                danmaku.textSize = mDanmakuTextSize;
                danmaku.textColor = mDanmakuTextColor;
                danmaku.underlineColor = -16711936;
                danmaku.setTime(mDanmakuView.getCurrentTime() + 500L);
                mDanmakuView.addDanmaku(danmaku);
                if (mDanmakuListener != null) {
                    if (mDanmakuConverter != null) {
                        mDanmakuListener.onDataObtain(mDanmakuConverter.convertDanmaku(danmaku));
                    } else {
                        mDanmakuListener.onDataObtain(danmaku);
                    }
                }

            }
        }
    }

    public void editVideo() {
        if (mVideoView.isPlaying()) {
            pause();
            mVideoStatus = 502;
        } else {
            mVideoStatus = 503;
        }

        _hideAllView(false);
    }

    public boolean recoverFromEditVideo() {
        if (mVideoStatus == 501) {
            return false;
        } else {
            if (mIsFullscreen) {
                _recoverScreen();
            }

            if (mVideoStatus == 502) {
                start();
            }

            mVideoStatus = 501;
            return true;
        }
    }

    private void _resumeDanmaku() {
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            if (mDanmakuTargetPosition != -1L) {
                mDanmakuView.seekTo(Long.valueOf(mDanmakuTargetPosition));
                mDanmakuTargetPosition = -1L;
            } else {
                mDanmakuView.resume();
            }
        }

    }

    private void _pauseDanmaku() {
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }

    }

    private void _toggleDanmakuShow() {
        if (mIvDanmakuControl.isSelected()) {
            showOrHideDanmaku(true);
        } else {
            showOrHideDanmaku(false);
        }

    }

    private void _toggleDanmakuView(boolean isShow) {
        if (mIsEnableDanmaku) {
            if (isShow) {
                mIvDanmakuControl.setVisibility(VISIBLE);
                mTvOpenEditDanmaku.setVisibility(VISIBLE);
                mDanmakuPlayerSeek.setVisibility(VISIBLE);
                mPlayerSeek.setVisibility(GONE);
            } else {
                mIvDanmakuControl.setVisibility(GONE);
                mTvOpenEditDanmaku.setVisibility(GONE);
                mDanmakuPlayerSeek.setVisibility(GONE);
                mPlayerSeek.setVisibility(VISIBLE);
            }
        }

    }

    private void _recoverScreen() {
        mEditDanmakuLayout.clearFocus();
        mEditDanmakuLayout.setVisibility(GONE);
        SoftInputUtils.closeSoftInput(mAttachActivity);
        _setUiLayoutFullscreen();
        if (mDanmakuColorOptions.getWidth() != 0) {
            _toggleMoreColorOptions();
        }

    }

    private void _toggleMoreColorOptions() {
        if (mBasicOptionsWidth == -1) {
            mBasicOptionsWidth = mDanmakuOptionsBasic.getWidth();
        }

        if (mDanmakuColorOptions.getWidth() == 0) {
            AnimHelper.doClipViewWidth(mDanmakuOptionsBasic, mBasicOptionsWidth, 0, 300);
            AnimHelper.doClipViewWidth(mDanmakuColorOptions, 0, mMoreOptionsWidth, 300);
            ViewCompat.animate(mDanmakuMoreColorIcon).rotation(180.0F).setDuration(150L).setStartDelay(250L).start();
        } else {
            AnimHelper.doClipViewWidth(mDanmakuOptionsBasic, 0, mBasicOptionsWidth, 300);
            AnimHelper.doClipViewWidth(mDanmakuColorOptions, mMoreOptionsWidth, 0, 300);
            ViewCompat.animate(mDanmakuMoreColorIcon).rotation(0.0F).setDuration(150L).setStartDelay(250L).start();
        }

    }

    private void _initReceiver() {
        mPbBatteryLevel = (ProgressBar) findViewById(R.id.pb_battery);
        mTvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        mTvSystemTime.setText(StringUtils.getCurFormatTime());
        mBatteryReceiver = new MyIjkPlayerView.BatteryBroadcastReceiver();
//        mScreenReceiver = new MyIjkPlayerView.ScreenBroadcastReceiver(null);
        mAttachActivity.registerReceiver(mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
//        mAttachActivity.registerReceiver(mScreenReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
        mIvScreenshot = (ImageView) findViewById(R.id.iv_screenshot);
        mIvScreenshot.setOnClickListener(this);
        if (SDCardUtils.isAvailable()) {
            _createSaveDir(SDCardUtils.getRootPath() + File.separator + "IjkPlayView");
        }

    }

    private void _doScreenshot() {
        editVideo();
        _showShareDialog(mVideoView.getScreenshot());
    }

    private void _showShareDialog(Bitmap bitmap) {
        if (mShareDialog == null) {
            mShareDialog = new ShareDialog();
            mShareDialog.setClickListener(mInsideDialogClickListener);
            mShareDialog.setDismissListener(mDialogDismissListener);
            if (mDialogClickListener != null) {
                mShareDialog.setShareMode(true);
            }
        }

        mShareDialog.setScreenshotPhoto(bitmap);
        mShareDialog.show(mAttachActivity.getSupportFragmentManager(), "share");
    }

    public MyIjkPlayerView setDialogClickListener(OnDialogClickListener dialogClickListener) {
        mDialogClickListener = dialogClickListener;
        if (mShareDialog != null) {
            mShareDialog.setShareMode(true);
        }

        return this;
    }

    private void _createSaveDir(String path) {
        mSaveDir = new File(path);
        if (!mSaveDir.exists()) {
            mSaveDir.mkdirs();
        } else if (!mSaveDir.isDirectory()) {
            mSaveDir.delete();
            mSaveDir.mkdirs();
        }

    }

    public MyIjkPlayerView setSaveDir(String path) {
        _createSaveDir(path);
        return this;
    }

    static {
        QUALITY_DRAWABLE_RES = new int[]{mipmap.ic_media_quality_smooth, mipmap.ic_media_quality_medium, mipmap.ic_media_quality_high, mipmap.ic_media_quality_super, mipmap.ic_media_quality_bd};
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                mIsScreenLocked = true;
            }

        }
    }

    class BatteryBroadcastReceiver extends BroadcastReceiver {
        private static final int BATTERY_LOW_LEVEL = 15;

        BatteryBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int curPower = level * 100 / scale;
                int status = intent.getIntExtra("status", 1);
                if (status == 2) {
                    mPbBatteryLevel.setSecondaryProgress(0);
                    mPbBatteryLevel.setProgress(curPower);
                    mPbBatteryLevel.setBackgroundResource(mipmap.ic_battery_charging);
                } else if (curPower < 15) {
                    mPbBatteryLevel.setProgress(0);
                    mPbBatteryLevel.setSecondaryProgress(curPower);
                    mPbBatteryLevel.setBackgroundResource(mipmap.ic_battery_red);
                } else {
                    mPbBatteryLevel.setSecondaryProgress(0);
                    mPbBatteryLevel.setProgress(curPower);
                    mPbBatteryLevel.setBackgroundResource(mipmap.ic_battery);
                }
            }

        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface DanmakuTag {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    public @interface MediaQuality {
    }
}
