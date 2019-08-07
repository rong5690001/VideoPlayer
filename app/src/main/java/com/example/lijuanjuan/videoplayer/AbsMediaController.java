package com.example.lijuanjuan.videoplayer;

import android.content.Context;
import android.widget.FrameLayout;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 播放控制器抽象类
 */
public abstract class AbsMediaController extends FrameLayout implements View.OnClickListener {

    private Context mContext;
    private IVideoPlayer mVideoPlayer;

    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;

    public AbsMediaController(Context context) {
        super(context);
    }

    /**
     * 更新进度
     */
    public abstract void updateProgress();

    /**
     * 当播放状态改变时，更新UI
     * @param playStatus
     * {@link IVideoPlayer#STATE_ERROR}
     * {@link IVideoPlayer#STATE_IDLE}
     * {@link IVideoPlayer#STATE_PREPARING}
     * {@link IVideoPlayer#STATE_PREPARED}
     * {@link IVideoPlayer#STATE_PLAYING}
     * {@link IVideoPlayer#STATE_PAUSED}
     * {@link IVideoPlayer#STATE_BUFFERING_PLAYING}
     * {@link IVideoPlayer#STATE_BUFFERING_PAUSED}
     * {@link IVideoPlayer#STATE_COMPLETED}
     *
     */
    public abstract void onPlayStatesChanged(int playStatus);

    /**
     * 当播放模式（全屏模式、正常模式）改变时，更新UI
     * @param model
     * {@link IVideoPlayer#MODE_NORMAL}
     * {@link IVideoPlayer#MODE_FULL_SCREEN}
     */
    public abstract void onPlayModeChanged(int model);

    /**
     * 重置控制器
     */
    public abstract void reset();

    /**
     * 设置播放器
     * @param mVideoPlayer
     */
    public void setVideoPlayer(IVideoPlayer mVideoPlayer) {
        this.mVideoPlayer = mVideoPlayer;
    }

    /**
     * 开始更新进度
     */
    public void startUpdateTimer() {
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    updateProgress();
                }
            };
        }
    }

    /**
     * 取消更新进度
     */
    public void cancelUpdateTimer(){
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    @Override
    public void onClick(View v) {

    }
}
