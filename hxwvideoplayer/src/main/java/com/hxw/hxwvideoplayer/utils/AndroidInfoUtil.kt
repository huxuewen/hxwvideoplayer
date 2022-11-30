package com.hxw.hxwvideoplayer.utils

import com.hxw.hxwvideoplayer.HxwVideoPlayerManager

/**
 * @author xuewen hu
 * @date 2021/9/4 10:46
 */
object AndroidInfoUtil {

    @JvmStatic
    fun getColor(resource: Int): Int {
        return HxwVideoPlayerManager.applicationContext.getColor(resource)
    }
}