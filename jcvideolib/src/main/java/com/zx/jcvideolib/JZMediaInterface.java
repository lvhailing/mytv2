package com.zx.jcvideolib;

import android.view.Surface;

/**
 * 自定义播放器
 */
public abstract class JZMediaInterface {

    public JZDataSource jzDataSource;

    public abstract void start();

    public abstract void prepare();

    public abstract void pause();

    public abstract boolean isPlaying();

    public abstract void seekTo(long time);

    public abstract void release();

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public abstract void setSurface(Surface surface);

    public abstract void setVolume(float leftVolume, float rightVolume);

    public abstract void setSpeed(float speed);
}
