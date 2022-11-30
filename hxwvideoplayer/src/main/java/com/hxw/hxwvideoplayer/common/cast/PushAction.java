package com.hxw.hxwvideoplayer.common.cast;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.hxw.hxwvideoplayer.common.listener.OnErrorListener;
import com.hxw.hxwvideoplayer.common.listener.OnSuccessListener;
import com.hxw.hxwvideoplayer.utils.ToastUtil;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

/**
 * @author hu xuewen
 * @date 2022/1/18 11:07
 */
public class PushAction extends SetAVTransportURI {

    private Context context;

    private Handler handler;

    private OnSuccessListener onSuccessListener;

    private OnErrorListener onErrorListener;

    public PushAction(Context context, Service service, String uri, String metadata) {
        super(service, uri, metadata);
        this.context = context;
        handler = new Handler(context.getMainLooper());
    }

    public void setOnSuccessListener(OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void success(ActionInvocation invocation) {
        Log.d("", "setAVTransportURI success.");
        handler.post(() -> {
            ToastUtil.shortShow("投屏推送成功");
            if (onSuccessListener != null) {
                onSuccessListener.success();
            }
        });
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        Log.d("exec", "setAVTransportURI failed," + defaultMsg);
        handler.post(() -> {
            ToastUtil.shortShow("投屏推送失败");
            if (onErrorListener != null) {
                onErrorListener.error();
            }
        });

    }
}
