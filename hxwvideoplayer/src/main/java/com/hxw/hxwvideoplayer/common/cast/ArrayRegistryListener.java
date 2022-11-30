package com.hxw.hxwvideoplayer.common.cast;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

/**
 * @author hu xuewen
 * @date 2022/1/18 10:09
 */
public class ArrayRegistryListener extends DefaultRegistryListener {

    private Context context;

    private Handler handler;

    private ArrayAdapter arrayAdapter;

    public ArrayRegistryListener(Context context, ArrayAdapter arrayAdapter) {
        this.context = context;
        this.arrayAdapter = arrayAdapter;
        this.handler = new Handler(context.getMainLooper());
    }

    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        Log.i("远程设备", "设备" + device.getDetails().getFriendlyName() + "发现失败" + "  ,异常：" + ex.getMessage());
//            runOnUiThread(()-> {
//                ToastUtil.shortShow("Discovery failed of '" + device.getDisplayString() + "': "
//                        + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"));
//            });
//            deviceRemoved(device);
    }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        super.remoteDeviceAdded(registry, device);
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        super.remoteDeviceRemoved(registry, device);
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    public void deviceAdded(final Device device) {
        // 控制设备搜索类型
        if (!device.getType().getType().equals(DeviceTypeEnum.MEDIA_RENDERER.getType())) {
            return;
        }

        handler.post(() -> {
            DeviceDisplay d = new DeviceDisplay(device);
            int position = arrayAdapter.getPosition(d);
            if (position >= 0) {
                // Device already in the list, re-set new value at same position
                arrayAdapter.remove(d);
                arrayAdapter.insert(d, position);
            } else {
                arrayAdapter.add(d);
            }
            arrayAdapter.notifyDataSetChanged();
        });
    }

    public void deviceRemoved(final Device device) {
        handler.post(() -> {
            arrayAdapter.remove(new DeviceDisplay(device));
            arrayAdapter.notifyDataSetChanged();
        });
    }
}
