package com.hxw.hxwvideoplayer.common.mediakernel;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;
import com.hxw.hxwvideoplayer.common.constans.HxwVideoPlayerConstant;
import com.hxw.hxwvideoplayer.ui.SimpleJzvd;

import java.util.Map;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;

/**
 * Created by MinhDV on 5/3/18.
 */
public class JZMediaExo extends JZMediaInterface implements Player.Listener {

    private ExoPlayer exoPlayer;
    private Runnable callback;
    private String TAG = "JZMediaExo";
    private long previousSeek = 0;

    private Context context;

    public JZMediaExo(Jzvd jzvd) {
        super(jzvd);

        context = jzvd.getContext();
//        mMediaHandlerThread = new HandlerThread("JZVD");
//        mMediaHandlerThread.start();
        mMediaHandler = new Handler(context.getMainLooper());//主线程
        handler = new Handler();
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(120_000, 120_000, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                .build();
        exoPlayer = new ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build();
        exoPlayer.addListener(this);
    }

    @Override
    public void start() {
        Log.d(TAG, "start");
        exoPlayer.play();
    }

    @Override
    public void prepare() {
        Log.d(TAG, "prepare");
        mMediaHandler.post(() -> {
            String url = jzvd.jzDataSource.getCurrentUrl().toString();
            Log.d(TAG, "URL Link = " + url);
            Uri uri = Uri.parse(url);
            DataSource.Factory dataSourceFactory;
            Map httpHeaders = null;
            String formatHint = null;
            if (jzvd instanceof SimpleJzvd) {
                formatHint = ((SimpleJzvd) jzvd).getFormatHint();
            }
            if (isHTTP(uri)) {
                DefaultHttpDataSource.Factory httpDataSourceFactory =
                        new DefaultHttpDataSource.Factory()
                                .setUserAgent("ExoPlayer")
                                .setAllowCrossProtocolRedirects(true);

                if (httpHeaders != null && !httpHeaders.isEmpty()) {
                    httpDataSourceFactory.setDefaultRequestProperties(httpHeaders);
                }
                dataSourceFactory = httpDataSourceFactory;
            } else {
                dataSourceFactory = new DefaultDataSource.Factory(context);
            }
            MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory, formatHint, context);
            exoPlayer.setMediaSource(mediaSource);
            setLooping(jzvd.jzDataSource.looping);
            callback = new onBufferingUpdate();
            exoPlayer.setVideoSurface(new Surface(SAVED_SURFACE));
            exoPlayer.prepare();
            start();
        });
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        handler.post(() -> jzvd.onVideoSizeChanged((int) (videoSize.width * videoSize.pixelWidthHeightRatio), videoSize.height));
    }

    @Override
    public void onRenderedFirstFrame() {
        Log.d(TAG, "onRenderedFirstFrame");
    }

    @Override
    public void pause() {
        Log.d(TAG, "pause");
        exoPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        int state = this.exoPlayer.getPlaybackState();
        switch (state) {
            case 1:
            case 4:
            default:
                return false;
            case 2:
            case 3:
                return this.exoPlayer.getPlayWhenReady();
        }
    }

    @Override
    public void seekTo(long time) {
        if (time != previousSeek) {
            if (time >= exoPlayer.getBufferedPosition()) {
                jzvd.onStatePreparingPlaying();
            }
            exoPlayer.seekTo(time);
            previousSeek = time;
            jzvd.seekToInAdvance = time;

        }
    }

    @Override
    public void release() {
        Log.d(TAG, "invoke release");
        SAVED_SURFACE = null;
        mMediaHandler.post(() -> {
            exoPlayer.setVideoSurface(null);
            exoPlayer.release();//release就不能放到主线程里，界面会卡顿
//                mMediaHandlerThread.quit();
        });
    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return exoPlayer.getDuration();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {

    }

    @Override
    public void setSpeed(float speed) {
        if (exoPlayer.getPlayWhenReady()) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0F);
            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public void setLooping(boolean value) {
        exoPlayer.setRepeatMode(value ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, int reason) {
        Log.d(TAG, "onTimelineChanged");
        Object manifest = exoPlayer.getCurrentManifest();
        if (manifest != null) {
            HlsManifest hlsManifest = (HlsManifest) manifest;
            // Do something with the manifest.
        }

        handler.post(() -> {
            if (reason == 0) {
                Log.i(TAG, "onInfo");
                jzvd.onInfo(reason, timeline.getPeriodCount());
            }
        });
    }

    @Override
    public void onTracksInfoChanged(TracksInfo tracksInfo) {
        Player.Listener.super.onTracksInfoChanged(tracksInfo);
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
//        Log.d(TAG, "onIsLoadingChanged:isLoading=" + isLoading);
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Log.d(TAG, "onPlaybackStateChanged:" + "/playbackState=" + playbackState);
        handler.post(() -> {
            switch (playbackState) {
                case Player.STATE_IDLE: {
                }
                break;
                case Player.STATE_BUFFERING: {
                    jzvd.onStatePreparingPlaying();
                    handler.post(callback);
                }
                break;
                case Player.STATE_READY: {
                    jzvd.onStatePlaying();
                }
                break;
                case Player.STATE_ENDED: {
                    jzvd.onCompletion();
                }
                break;
            }
        });
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Log.d(TAG, "onPlayWhenReadyChanged:" + "/ready=" + playWhenReady + ",reason=" + reason);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Log.e(TAG, "onPlayerError" + error.toString());
        handler.post(() -> jzvd.onError(1000, 1000));
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
        Log.d(TAG, "进度调节完成");
        handler.post(() -> jzvd.onSeekComplete());
    }

    @Override
    public void setSurface(Surface surface) {
        Log.d(TAG, "setSurface");
        exoPlayer.setVideoSurface(surface);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            final int percent = exoPlayer.getBufferedPercentage();
            handler.post(() -> jzvd.setBufferProgress(percent));
            if (percent < 100) {
                handler.postDelayed(callback, 300);
            } else {
                handler.removeCallbacks(callback);
            }
        }
    }

    private MediaSource buildMediaSource(
            Uri uri, DataSource.Factory mediaDataSourceFactory, String formatHint, Context context) {

        int type;
        if (formatHint == null) {
            String lastPathSegment = uri.getLastPathSegment();
            if (lastPathSegment == null) {
                type = C.TYPE_OTHER;
            } else {
                type = Util.inferContentType(lastPathSegment);
            }
        } else {
            switch (formatHint) {
                case HxwVideoPlayerConstant.VIDEO_FORMAT_SS:
                    type = C.TYPE_SS;
                    break;
                case HxwVideoPlayerConstant.VIDEO_FORMAT_DASH:
                    type = C.TYPE_DASH;
                    break;
                case HxwVideoPlayerConstant.VIDEO_FORMAT_HLS:
                    type = C.TYPE_HLS;
                    break;
                case HxwVideoPlayerConstant.VIDEO_FORMAT_OTHER:
                    type = C.TYPE_OTHER;
                    break;
                default:
                    type = -1;
                    break;
            }
        }
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSource.Factory(context, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        new DefaultDataSource.Factory(context, mediaDataSourceFactory))
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri));
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private static boolean isHTTP(Uri uri) {
        if (uri == null || uri.getScheme() == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme.equals("http") || scheme.equals("https");
    }
}
