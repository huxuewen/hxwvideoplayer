package com.hxw.hxwvideoplayer;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * @author hu xuewen
 * @date 2022/7/13 23:15
 */
public class HxwVideoPlayerManager {

    public static Context applicationContext;

    public static HttpProxyCacheServer videoProxy;

    public static void init(Context context) {
        applicationContext = context;
        initHttpProxyCacheServer();
    }

    /**
     * @return
     */
    public static void initHttpProxyCacheServer() {
        // 视频缓存代理初始化
        videoProxy = new HttpProxyCacheServer(applicationContext);
    }
}
