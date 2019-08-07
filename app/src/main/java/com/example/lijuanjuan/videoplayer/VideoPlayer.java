package com.example.lijuanjuan.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 视频播放器
 */
public class VideoPlayer extends FrameLayout implements IVideoPlayer, TextureView.SurfaceTextureListener {

    private Context mContext;
    private AudioManager mAudioManager;

    private int mCurrentState;//当前播放状态
    private int mCurrentMode;//当前播放模式
    private int mCurrentPosition;//当前播放位置
    private int mBufferPercentage;//当前播放百分比

    private FrameLayout mContrainer;
    private AbsMediaController mMediaController;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private boolean mContinueFromLastPosition = true;
    private int mSeekToPosition;//指定播放位置
    private String mUrl;

    public VideoPlayer(Context context) {
        super(context, null);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    /**
     * 初始化播放器，设置背景色为黑色
     */
    private void init() {
        mContrainer = new FrameLayout(mContext);
        mContrainer.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mContrainer, params);
    }

    /**
     * 初始化音频管理器，获取声音焦点
     */
    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    /**
     * 初始化TextureView
     */
    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            mContrainer.addView(mTextureView, params);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            openVideoPlayer();
        } else {
            mTextureView.setSurfaceTexture(surface);
        }
    }

    private void openVideoPlayer() {
        //设置屏幕常亮
        setKeepScreenOn(true);

        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);

        try {
            mMediaPlayer.setDataSource(mUrl);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            onPlayStatesChanged(mCurrentState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void start() {
        /**
         * 只有在未开始状态才能开始
         */
        if (mCurrentState == STATE_IDLE) {
            initAudioManager();
            initMediaPlayer();
            initTextureView();
        }
    }

    @Override
    public void start(int position) {
        mSeekToPosition = position;
        start();
    }

    @Override
    public void restart() {
        if (isPaused()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            onPlayStatesChanged(mCurrentState);
        } else if (isBufferingPaused()) {
            mMediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            onPlayStatesChanged(mCurrentState);
        } else if (isCompleted() || isError()) {
            mMediaPlayer.reset();
            openVideoPlayer();
        } else {

        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer == null || mMediaController == null) {
            return;
        }
        if (isPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            onPlayStatesChanged(mCurrentState);
        } else if (isBufferingPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            onPlayStatesChanged(mCurrentState);
        }
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            onPlayStatesChanged(mCurrentState);
            mp.start();
            //从上次播放位置开始播放
            if (mContinueFromLastPosition) {
                mp.seekTo(mSeekToPosition);
            }
        }
    };

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    //播放器开始渲染
                    mCurrentState = STATE_PLAYING;
                    onPlayStatesChanged(mCurrentState);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    // MediaPlayer暂时不播放，以缓冲更多的数据
                    if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_BUFFERING_PLAYING;
                    } else {
                        mCurrentState = STATE_BUFFERING_PAUSED;
                    }
                    onPlayStatesChanged(mCurrentState);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    //缓冲完成,继续播放或者暂停
                    if (mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_PAUSED;
                        onPlayStatesChanged(mCurrentState);
                    }

                    if (mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_PLAYING;
                        onPlayStatesChanged(mCurrentState);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_COMPLETED;
            onPlayStatesChanged(mCurrentState);
            setKeepScreenOn(false);
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mCurrentState = STATE_ERROR;
            onPlayStatesChanged(mCurrentState);
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercentage = percent;
        }
    };

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mCurrentMode == MODE_FULL_SCREEN;
    }

    @Override
    public boolean isNormal() {
        return mCurrentMode == MODE_NORMAL;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void enterFullScreen() {
        if (isFullScreen() || !(mContext instanceof Activity)) {
            return;
        }

        Activity activity = (Activity) mContext;
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        this.removeView(mContrainer);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT);
        contentView.addView(mContrainer, params);

        mCurrentMode = MODE_FULL_SCREEN;
        if (mMediaController != null) {
            mMediaController.onPlayModeChanged(mCurrentMode);
        }
    }

    @Override
    public boolean exitFullScreen() {
        if (isFullScreen() && (mContext instanceof Activity)) {

            Activity activity = (Activity) mContext;
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ViewGroup contentView = activity.findViewById(android.R.id.content);
            contentView.removeView(mContrainer);

            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContrainer, params);
            mCurrentMode = MODE_NORMAL;
            if (mMediaController != null) {
                mMediaController.onPlayModeChanged(mCurrentMode);
            }
            return true;
        }
        return false;
    }

    @Override
    public void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mContrainer.removeView(mTextureView);

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        mCurrentState = STATE_IDLE;
    }

    @Override
    public void release() {

        //退出全屏
        if (isFullScreen()) {
            exitFullScreen();
        }

        //释放播放器
        releasePlayer();

        //恢复控制器
        if (mMediaController != null) {
            mMediaController.reset();
        }

        Runtime.getRuntime().gc();
    }

    public void setmContinueFromLastPosition(boolean mContinueFromLastPosition) {
        this.mContinueFromLastPosition = mContinueFromLastPosition;
    }

    /**
     * 播放状态改变
     *
     * @param states
     */
    private void onPlayStatesChanged(int states) {
        if (mMediaController != null) {
            onPlayStatesChanged(states);
        }
    }
}
