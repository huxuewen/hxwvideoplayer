package com.hxw.hxwvideoplayer.common.cast;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.hxw.hxwvideoplayer.common.listener.OnSuccessListener;
import com.hxw.hxwvideoplayer.utils.ToastUtil;

import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author hu xuewen
 * @date 2022/1/18 10:25
 */
public class CastManager {

    private Context context;

    private Device device;

    private ServiceConnectionImpl serviceConnection;

    private Handler handler;

    private static final DeviceType DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    private static final String DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" + "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">";

    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";

    public CastManager(Context context, ArrayAdapter arrayAdapter) {
        this.context = context;
        this.handler = new Handler(context.getMainLooper());
        serviceConnection = new ServiceConnectionImpl(new ArrayRegistryListener(context, arrayAdapter));
        serviceConnection.setBeforeListener(() ->
                handler.post(() -> {
                    arrayAdapter.clear();
                })
        );
        // 绑定服务
        context.getApplicationContext().bindService(
                new Intent(context.getApplicationContext(), AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    public void playAfterPush(Device device, String title, String url) {
        this.device = device;
        push(title, url, () -> play());
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void push(String title, String url) {
        try {
            Service service = device.findService(new UDAServiceType("AVTransport"));
            if (service == null) {
                handler.post(() -> {
                    ToastUtil.shortShow("该设备没有可以执行的服务");
                });
                return;
            }
            if (service.getActions().length == 0) {
                handler.post(() -> {
                    ToastUtil.shortShow("该设备没有可以执行的动作");
                });
                return;
            }
            String mediaData = pushMediaToRender(url, "id", title, "0");
            serviceConnection.getUpnpService().getControlPoint().execute(new PushAction(context, service, url, mediaData));
        } catch (Exception e) {
            ToastUtil.shortShow("投屏推送失败：" + e.getMessage());
        }
    }

    public void push(String title, String url, OnSuccessListener onSuccessListener) {
        try {
            Service service = device.findService(new UDAServiceType("AVTransport"));
            if (service == null) {
                handler.post(() -> {
                    ToastUtil.shortShow("该设备没有可以执行的服务");
                });
                return;
            }
            if (service.getActions().length == 0) {
                handler.post(() -> {
                    ToastUtil.shortShow("该设备没有可以执行的动作");
                });
                return;
            }
            String mediaData = pushMediaToRender(url, "id", title, "0");
            PushAction pushAction = new PushAction(context, service, url, mediaData);
            pushAction.setOnSuccessListener(onSuccessListener);
            serviceConnection.getUpnpService().getControlPoint().execute(pushAction);
        } catch (Exception e) {
            ToastUtil.shortShow("投屏推送失败：" + e.getMessage());
        }
    }

    public void play() {
        Service service = device.findService(new UDAServiceType("AVTransport"));
        if (service == null) {
            Log.w("TAG", "play failed, AVTransport service is null.");
//            mHandler.obtainMessage(MSG_PLAY_FAILED).sendToTarget();
            return;
        }
        serviceConnection.getUpnpService().getControlPoint().execute(new PlayAction(context, service));
    }

    private String pushMediaToRender(String url, String id, String name, String duration) {
        long size = 0;
        long bitrate = 0;
        Res res = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), size, url);

        String creator = "unknow";
        String resolution = "unknow";
        VideoItem videoItem = new VideoItem(id, "0", name, creator, res);

        String metadata = createItemMetadata(videoItem);
        Log.e("", "metadata: " + metadata);
        return metadata;
    }

    private String createItemMetadata(DIDLObject item) {
        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);

        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));

        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));
        String creator = item.getCreator();
        if (creator != null) {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator));

        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date now = new Date();
        String time = sdf.format(now);
        metadata.append(String.format("<dc:date>%s</dc:date>", time));

        // metadata.append(String.format("<upnp:album>%s</upnp:album>",
        // item.get);

        // <res protocolInfo="http-get:*:audio/mpeg:*"
        // resolution="640x478">http://192.168.1.104:8088/Music/07.我醒著做夢.mp3</res>

        Res res = item.getFirstResource();
        if (res != null) {
            // protocol info
            String protocolInfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null) {
                protocolInfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi
                        .getAdditionalInfo());
            }
            Log.e("", "protocolInfo: " + protocolInfo);

            // resolution, extra info, not adding yet
            String resolution = "";
            if (res.getResolution() != null && res.getResolution().length() > 0) {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (res.getDuration() != null && res.getDuration().length() > 0) {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            // metadata.append(String.format("<res %s>", protocolInfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolInfo, resolution, duration));

            // url
            String url = res.getValue();
            metadata.append(url);

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");

        metadata.append(DIDL_LITE_FOOTER);

        return metadata.toString();
    }

    public void getMediaInfo(Device device) {
//        check();
        Service service = device.findService(new UDAServiceType("AVTransport"));
        if (service == null) {
            Log.w("TAG", "getMediaInfo failed, AVTransport service is null.");
            return;
        }
        serviceConnection.getUpnpService().getControlPoint().execute(new GetMediaInfo(service) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.i("TAG", "getMediaInfo success");
            }

            @Override
            public void received(ActionInvocation invocation, MediaInfo mediaInfo) {
                Log.i("TAG", "getMediaInfo received," + mediaInfo.getMediaDuration() + "," + mediaInfo.getCurrentURI() + "," + mediaInfo.getCurrentURIMetaData());
//                mHandler.obtainMessage(MSG_ON_GET_MEDIAINFO, mediaInfo).sendToTarget();
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.e("TAG", "getMediaInfo failed," + defaultMsg);
            }
        });
    }

    public void destroy() {
        if (serviceConnection != null) {
            serviceConnection.removeRegister();
        }
        context.getApplicationContext().unbindService(serviceConnection);
    }
}
