package com.hxw.hxwvideoplayer.common.cast;

/**
 * @author hu xuewen
 * @date 2022/7/16 20:15
 */
public enum DeviceTypeEnum {

    MEDIA_RENDERER("MediaRenderer"),
    INTERNET_GATEWAY_DEVICE("InternetGatewayDevice"),
    MEDIA_SERVER("MediaServer"),
    ;

    private String type;

    DeviceTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
