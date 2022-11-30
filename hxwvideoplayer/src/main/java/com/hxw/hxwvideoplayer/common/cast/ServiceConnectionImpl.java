package com.hxw.hxwvideoplayer.common.cast;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.hxw.hxwvideoplayer.common.listener.OnAfterListener;
import com.hxw.hxwvideoplayer.common.listener.OnBeforeListener;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.message.header.DeviceTypeHeader;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;

/**
 * @author hu xuewen
 * @date 2022/1/12 12:00
 */
public class ServiceConnectionImpl implements ServiceConnection {

    private static final DeviceType DEVICE_TYPE = new UDADeviceType("MediaRenderer");
    private static final DeviceTypeHeader MediaRendererHeader = new DeviceTypeHeader(DEVICE_TYPE);
    private static final UDADeviceTypeHeader MediaRendererHeader2 = new UDADeviceTypeHeader(DEVICE_TYPE);

    private ArrayRegistryListener registryListener;

    private AndroidUpnpService upnpService;

    public AndroidUpnpService getUpnpService() {
        return upnpService;
    }

    private OnBeforeListener beforeListener;

    private OnAfterListener afterListener;

    public ServiceConnectionImpl(ArrayRegistryListener registryListener) {
        this.registryListener = registryListener;
    }

    public void setBeforeListener(OnBeforeListener beforeListener) {
        this.beforeListener = beforeListener;
    }

    public void setAfterListener(OnAfterListener afterListener) {
        this.afterListener = afterListener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        upnpService = (AndroidUpnpService) service;

        // Clear the list
        if (beforeListener != null) {
            beforeListener.before();
        }

        // Get ready for future device advertisements
        upnpService.getRegistry().addListener(registryListener);

        // Now add all devices to the list we already know about
        for (Device device : upnpService.getRegistry().getDevices()) {
            registryListener.deviceAdded(device);
        }

        // Search asynchronously for all devices, they will respond soon
        // 搜索所有设备
        upnpService.getControlPoint().search();
        // 搜索可投屏设备
//        upnpService.getControlPoint().search(MediaRendererHeader);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        upnpService.getRegistry().shutdown();
        upnpService = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        upnpService.getRegistry().shutdown();
        upnpService = null;
    }

    public void removeRegister() {
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
    }
}
