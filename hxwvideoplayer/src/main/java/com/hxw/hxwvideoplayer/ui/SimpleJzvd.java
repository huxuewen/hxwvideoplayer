package com.hxw.hxwvideoplayer.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hxw.hxwvideoplayer.HxwVideoPlayerManager;
import com.hxw.hxwvideoplayer.R;
import com.hxw.hxwvideoplayer.utils.MathUtil;
import com.hxw.hxwvideoplayer.utils.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * @author xuewen hu
 * @date 2021/10/22 17:19
 */
public class SimpleJzvd extends JzvdStd implements VideoSpeedPopup.SpeedChangeListener {

    protected ImageView forward_30Button;

    protected ImageView replay_30Button;

    protected TextView videoSpeedText;

    protected float videoSpeed = 1.0f;

    protected ImageView lockImageView;

    protected boolean isLockScreen;

    protected float starX, startY;

    //倍数弹窗
    private VideoSpeedPopup videoSpeedPopup;

    // 投屏
    private ImageView castScreenView;

    // 长按倍速dialog
    Dialog longClickSpeedDialog;
    private Timer longClickSpeedDialogTimer;
    private volatile boolean isLongClickSpeedState;

    /**
     * 视频格式
     */
    private String formatHint = null;

    /**
     * 复制视频地址
     */
    private ImageView copyUrlImageView;

    public SimpleJzvd(Context context) {
        super(context);
    }

    public SimpleJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setUp(String url, String title, int screen, Class mediaInterfaceClass) {
        this.setUp(url, title, null, screen, mediaInterfaceClass);
    }

    public void setUp(String url, String title, String formatHint, int screen, Class mediaInterfaceClass) {
//        HttpProxyCacheServer videoProxy = HaojumaoApplication.getVideoProxy();
//        String proxyUrl = videoProxy.getProxyUrl(url);
//        setUp(new JZDataSource(proxyUrl, title), screen, mediaInterfaceClass);

        this.formatHint = formatHint;
        setUp(new JZDataSource(url, title), screen, mediaInterfaceClass);

        if (screen == SCREEN_FULLSCREEN) {
            gotoFullscreen();
        }
    }

    @Override
    public void init(Context context) {
        super.init(context);

        // 配置
        // 进度条阻尼系数
        PROGRESS_DRAG_RATE = 2.0f;

        forward_30Button = findViewById(R.id.forward_30_btn);
        replay_30Button = findViewById(R.id.replay_30_btn);
        videoSpeedText = findViewById(R.id.video_speed_text);
        lockImageView = findViewById(R.id.lock);
        castScreenView = findViewById(R.id.cast_screen);
        copyUrlImageView = findViewById(R.id.copy_url);

        if (forward_30Button == null) {
            forward_30Button = new ImageView(context);
        }
        if (replay_30Button == null) {
            replay_30Button = new ImageView(context);
        }
        if (videoSpeedText == null) {
            videoSpeedText = new TextView(context);
        }
        if (lockImageView == null) {
            lockImageView = new ImageView(context);
        }
        if (castScreenView == null) {
            castScreenView = new ImageView(context);
        }

        forward_30Button.setOnClickListener(this);
        replay_30Button.setOnClickListener(this);
        videoSpeedText.setOnClickListener(this);
        lockImageView.setOnClickListener(this);
        castScreenView.setOnClickListener(this);
        copyUrlImageView.setOnClickListener(this);

        // 长按倍数播放
        createTouchSpeedDialog(context);

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    public void play() {
        startPreloading();
    }

    /**
     * 播放结束
     */
    public void playEnd() {
        dismissSpeedDialog();
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        this.playEnd();
    }

    @Override
    protected void clickRetryBtn() {
        Log.d(TAG, "视频重试");
        if (jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            Toast.makeText(jzvdContext, getResources().getString(cn.jzvd.R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        startPreloading();
    }

    /**
     * 点击事件处理
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.forward_30_btn) {
            onClickForward30();
        } else if (i == R.id.replay_30_btn) {
            onClickReplay30();
        } else if (i == R.id.video_speed_text) {
            onClickSpeed();
        } else if (i == R.id.lock) {
            onClickLockScreen();
        } else if (i == R.id.cast_screen) {
            onClickCastScreen();
        } else if (i == R.id.copy_url) {
            onClickCopyUrl();
        }
    }

    /**
     * 快进30s
     */
    public void onClickForward30() {
        setForWardAndReplayControlsVisibility(View.INVISIBLE, View.INVISIBLE);
        long currentPosition = mediaInterface.getCurrentPosition();
        mediaInterface.seekTo(currentPosition + (30 * (long) Math.pow(10, 3)));
    }

    /**
     * 快退30s
     */
    public void onClickReplay30() {
        setForWardAndReplayControlsVisibility(View.INVISIBLE, View.INVISIBLE);
        long currentPosition = mediaInterface.getCurrentPosition();
        mediaInterface.seekTo(currentPosition - (30 * (long) Math.pow(10, 3)));
    }

    /**
     * 点击倍速弹窗
     */
    public void onClickSpeed() {
        if (videoSpeedPopup == null) {
            videoSpeedPopup = new VideoSpeedPopup(getContext());
            videoSpeedPopup.setSpeedChangeListener(this);
        }
        videoSpeedPopup.showAtLocation(getRootView(), Gravity.RIGHT, 0, 0);
    }

    /**
     * 锁屏
     */
    public void onClickLockScreen() {
        if (screen == SCREEN_FULLSCREEN) {
            lockImageView.setTag(1);
            if (!isLockScreen) {
                isLockScreen = true;
                lockImageView.setImageResource(R.drawable.lock);
//				dissmissControlView();

                bottomContainer.setVisibility(View.INVISIBLE);
                topContainer.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);

                // 自定义按钮
                forward_30Button.setVisibility(View.INVISIBLE);
                replay_30Button.setVisibility(View.INVISIBLE);
                videoSpeedText.setVisibility(View.INVISIBLE);
                castScreenView.setVisibility(View.INVISIBLE);
            } else {
                isLockScreen = false;
                lockImageView.setImageResource(R.drawable.unlock);
                bottomContainer.setVisibility(VISIBLE);
                topContainer.setVisibility(VISIBLE);
                startButton.setVisibility(VISIBLE);
                bottomProgressBar.setVisibility(GONE);

                // 自定义按钮
                forward_30Button.setVisibility(View.VISIBLE);
                replay_30Button.setVisibility(View.VISIBLE);
                videoSpeedText.setVisibility(View.VISIBLE);
                castScreenView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 点击投屏
     */
    public void onClickCastScreen() {
        backPress();
    }

    /**
     * 点击复制视频地址
     */
    public void onClickCopyUrl() {
        //获取剪切板管理器
        ClipboardManager cm = (ClipboardManager) HxwVideoPlayerManager.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
        //设置内容到剪切板
        cm.setPrimaryClip(
                ClipData.newPlainText(null, (CharSequence) jzDataSource.getCurrentUrl())
        );
        ToastUtil.shortShow("已复制");
    }

    //这里应该还没有判断完  目前还没有测试出什么问题  这里是拦截父亲得一些事件比如滑动快进 改变亮度
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                starX = event.getX();
                startY = event.getY();
                if (screen == SCREEN_FULLSCREEN && isLockScreen) {
                    return true;
                }
                if (state == Jzvd.STATE_PLAYING) {
                    startSpeedDialog();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (screen == SCREEN_FULLSCREEN && isLockScreen) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (screen == SCREEN_FULLSCREEN && isLockScreen) {
                    //&& Math.abs(Math.abs(event.getX() - starX)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()  && Math.abs(Math.abs(event.getY() - startY)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                    if (event.getX() == starX || event.getY() == startY) {
                        startDismissControlViewTimer();
                        onClickUiToggle();
                        bottomProgressBar.setVisibility(VISIBLE);
                    }
                    return true;
                }
                dismissSpeedDialog();
                break;
        }
        return super.onTouch(v, event);
    }

    /**
     * 隐藏UI
     */
    @Override
    public void dissmissControlView() {
        if (state != STATE_NORMAL
                && state != STATE_ERROR
                && state != STATE_AUTO_COMPLETE) {
            post(() -> {
                bottomContainer.setVisibility(View.INVISIBLE);
                topContainer.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);

                // 自定义按钮
                forward_30Button.setVisibility(View.INVISIBLE);
                replay_30Button.setVisibility(View.INVISIBLE);
                videoSpeedText.setVisibility(View.INVISIBLE);
                lockImageView.setVisibility(View.INVISIBLE);
                castScreenView.setVisibility(View.INVISIBLE);
                copyUrlImageView.setVisibility(View.INVISIBLE);

                if (screen != SCREEN_TINY) {
                    bottomProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_simple_jzvd;
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
        forward_30Button.setVisibility(View.GONE);
        replay_30Button.setVisibility(View.GONE);
        videoSpeedText.setVisibility(View.GONE);
        lockImageView.setVisibility(View.GONE);
        castScreenView.setVisibility(View.GONE);
        copyUrlImageView.setVisibility(View.GONE);
    }

    @Override
    public void changeStartButtonSize(int size) {
        super.changeStartButtonSize(size);
        ViewGroup.LayoutParams lp = forward_30Button.getLayoutParams();
        lp.height = size;
        lp.width = size;
        lp = replay_30Button.getLayoutParams();
        lp.height = size;
        lp.width = size;
    }

    @Override
    public void changeUiToPlayingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                if (state == STATE_PLAYING) {
                    setCustomControlsVisibility(View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                } else {
                    setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                }
                updateStartImage();
                break;
            case SCREEN_FULLSCREEN:
                if (!isLockScreen) {
                    setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                            View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                    if (state == STATE_PLAYING) {
                        setCustomControlsVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
                    } else {
                        setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
                    }
                } else {
                    setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                            View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                    setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                }
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }

    }

    @Override
    public void changeUiToPlayingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }

    }

    @Override
    public void changeUiToPauseShow() {
        switch (screen) {
            case SCREEN_NORMAL:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                if (state == STATE_PLAYING) {
                    setCustomControlsVisibility(View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                } else {
                    setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                }
                updateStartImage();
                break;
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                if (state == STATE_PLAYING) {
                    setCustomControlsVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
                } else {
                    setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE);
                }
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    @Override
    public void changeUiToPauseClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }

    }

    @Override
    public void updateStartImage() {
        if (!isLockScreen) {
            if (state == STATE_PLAYING) {
                startButton.setVisibility(VISIBLE);
                startButton.setImageResource(cn.jzvd.R.drawable.jz_click_pause_selector);
                replayTextView.setVisibility(GONE);
            } else if (state == STATE_ERROR) {
                startButton.setVisibility(INVISIBLE);
                replayTextView.setVisibility(GONE);
            } else if (state == STATE_AUTO_COMPLETE) {
                startButton.setVisibility(VISIBLE);
                startButton.setImageResource(cn.jzvd.R.drawable.jz_click_replay_selector);
                replayTextView.setVisibility(VISIBLE);
            } else {
                startButton.setImageResource(cn.jzvd.R.drawable.jz_click_play_selector);
                replayTextView.setVisibility(GONE);
            }
        } else {
            startButton.setVisibility(GONE);
            replayTextView.setVisibility(GONE);
        }
    }

    public void setForWardAndReplayControlsVisibility(int forward_30, int replay_30) {
        forward_30Button.setVisibility(forward_30);
        replay_30Button.setVisibility(replay_30);
    }

    public void setCustomControlsVisibility(int forward_30, int replay_30, int videoSpeed, int lock, int castScreen, int copyUrl) {
        forward_30Button.setVisibility(forward_30);
        replay_30Button.setVisibility(replay_30);
        videoSpeedText.setVisibility(videoSpeed);
        lockImageView.setVisibility(lock);
        castScreenView.setVisibility(castScreen);
        copyUrlImageView.setVisibility(copyUrl);
    }

    @Override
    public void speedChange(float speed) {
        setSpeed(speed);
        videoSpeed = speed;
        ToastUtil.shortShow(String.format("切换到%s倍速", MathUtil.floatFormat(speed, MathUtil.decimalFormat)));
    }

    public void setSpeed(float speed) {
        mediaInterface.setSpeed(speed);
    }

    /**
     * 关闭弹窗
     */
    private void dismissPopup() {
        if (videoSpeedPopup != null) {
            videoSpeedPopup.dismiss();
        }
    }

    @Override
    protected void touchActionMove(float x, float y) {
        Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
        float deltaX = x - mDownX;
        float deltaY = y - mDownY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        if (screen == SCREEN_FULLSCREEN) {
            //拖动的是NavigationBar和状态栏
            if (mDownX > JZUtils.getScreenWidth(getContext()) || mDownY < JZUtils.getStatusBarHeight(getContext())) {
                return;
            }
            if (!mChangePosition && !mChangeVolume && !mChangeBrightness && !isLongClickSpeedState) {
                if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                    cancelProgressTimer();
                    if (absDeltaX >= THRESHOLD) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true;
                            mGestureDownPosition = getCurrentPositionWhenPlaying();
                        }
                    } else {
                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                        float mDownWidth = screen != SCREEN_FULLSCREEN ? mScreenHeight : mScreenWidth;
                        if (mDownX < mDownWidth * 0.5f) {//左侧改变亮度
                            mChangeBrightness = true;
                            WindowManager.LayoutParams lp = JZUtils.getWindow(getContext()).getAttributes();
                            if (lp.screenBrightness < 0) {
                                try {
                                    mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                    Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                } catch (Settings.SettingNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mGestureDownBrightness = lp.screenBrightness * 255;
                                Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                            }
                        } else {//右侧改变声音
                            mChangeVolume = true;
                            mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        }
                    }
                }
            }
        }
        if (mChangePosition) {
            long totalTimeDuration = getDuration();
            if (PROGRESS_DRAG_RATE <= 0) {
                Log.d(TAG, "error PROGRESS_DRAG_RATE value");
                PROGRESS_DRAG_RATE = 1f;
            }
            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / (mScreenWidth * PROGRESS_DRAG_RATE));
            if (mSeekTimePosition > totalTimeDuration) {
                mSeekTimePosition = totalTimeDuration;
            }
            String seekTime = JZUtils.stringForTime(mSeekTimePosition);
            String totalTime = JZUtils.stringForTime(totalTimeDuration);

            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        }
        if (mChangeVolume) {
            deltaY = -deltaY;
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
            //dialog中显示百分比
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
            showVolumeDialog(-deltaY, volumePercent);
        }

        if (mChangeBrightness) {
            deltaY = -deltaY;
            int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
            WindowManager.LayoutParams params = JZUtils.getWindow(getContext()).getAttributes();
            if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                params.screenBrightness = 1;
            } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                params.screenBrightness = 0.01f;
            } else {
                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
            }
            JZUtils.getWindow(getContext()).setAttributes(params);
            //dialog中显示百分比
            int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
            showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        setForWardAndReplayControlsVisibility(View.INVISIBLE, View.INVISIBLE);
    }

    public void startSpeedDialog() {
        longClickSpeedDialogTimer = new Timer();
        longClickSpeedDialogTimer.schedule(new StartLongClickSpeedDialogTimerTask(), 500);
    }

    public void dismissSpeedDialog() {
        if (longClickSpeedDialogTimer != null) {
            longClickSpeedDialogTimer.cancel();
        }
        if (isLongClickSpeedState) {
            onLongClickSpeed(videoSpeed, false);
        }
    }

    public void onLongClickSpeed(float speed, boolean show) {
        Log.d(TAG, "onLongClickSpeed：" + speed);
        setSpeed(speed);
        if (longClickSpeedDialog != null) {
            if (show) {
                longClickSpeedDialog.show();
                isLongClickSpeedState = true;
            } else {
                longClickSpeedDialog.dismiss();
                isLongClickSpeedState = false;
            }
        }
    }

    public void destroy() {
        mediaInterface.release();
    }

    private void createTouchSpeedDialog(Context context) {
        longClickSpeedDialog = new Dialog(context);
        longClickSpeedDialog.setContentView(R.layout.hxw_touch_speed_dialog);
        ImageView imageView = longClickSpeedDialog.findViewById(R.id.tsd_image);
        Glide.with(imageView.getContext()).asGif().load(R.drawable.speed).into(imageView);

        Window window = longClickSpeedDialog.getWindow();
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.TOP;
        // y轴偏移
        localLayoutParams.y = 80;
        window.setAttributes(localLayoutParams);
    }

    private class StartLongClickSpeedDialogTimerTask extends TimerTask {

        @Override
        public void run() {
            if (!isLockScreen &&
                    state == STATE_PLAYING &&
                    !isLongClickSpeedState &&
                    screen == SCREEN_FULLSCREEN &&
                    !mChangePosition &&
                    !mChangeVolume &&
                    !mChangeBrightness
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Executor mainExecutor = jzvdContext.getMainExecutor();
                    mainExecutor.execute(() -> onLongClickSpeed(3.0f, true));
                } else {
                    Handler mainHandler = new Handler(jzvdContext.getMainLooper());
                    mainHandler.post(() -> onLongClickSpeed(3.0f, true));
                }
            }
        }
    }

    public String getFormatHint() {
        return formatHint;
    }

    public void setFormatHint(String formatHint) {
        this.formatHint = formatHint;
    }

    @Override
    public void onClickUiToggle() {
        // 加载视频时
        if (state == STATE_PREPARING_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPreparingPlayingClear();
            } else {
                changeUiToPreparingPlayingShow();
            }
            return;
        }
        super.onClickUiToggle();
    }

    public void changeUiToPreparingPlayingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPreparingPlayingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                setCustomControlsVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }
    }
}
