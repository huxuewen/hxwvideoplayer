package com.hxw.hxwvideoplayer.common.cast;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hxw.hxwvideoplayer.utils.ToastUtil;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Play;

/**
 * @author hu xuewen
 * @date 2022/1/18 11:17
 */
public class PlayAction extends Play {

    private Context context;

    private Handler handler;

    public PlayAction(Context context, Service service) {
        super(service);
        this.context = context;
        handler = new Handler(context.getMainLooper());
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        Log.e("TAG", "play failed," + defaultMsg);
//                mHandler.obtainMessage(MSG_PLAY_FAILED).sendToTarget();
        handler.post(() -> {
            ToastUtil.shortShow("投屏播放失败");
        });
    }

    @Override
    public void success(ActionInvocation invocation) {
        Log.e("TAG", "play success");
//                mHandler.obtainMessage(MSG_ON_PLAY).sendToTarget();
//                getMediaInfo(device);
        handler.post(() -> {
            ToastUtil.shortShow("投屏播放成功");
        });
    }
}
