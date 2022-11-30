package com.hxw.hxwvideoplayer.utils;

import java.text.DecimalFormat;

/**
 * @author hu xuewen
 * @date 2022/1/14 16:25
 */
public class MathUtil {

    public final static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static String floatFormat(float f, DecimalFormat df) {
        Float aFloat = Float.valueOf(f);
        return df.format(aFloat);
    }

    public static String floatToString(float f, DecimalFormat df) {
        Float aFloat = Float.valueOf(f);
        String format = df.format(aFloat);
        return trimZero(format);
    }

    public static String trimZero(String s) {
        byte[] bytes = s.getBytes();
        for (int i = bytes.length - 1; i >= 0; i--) {
            // 非[0.]退出
            if (bytes[i] > 48 && bytes[i] < 58) {
                break;
            }
            // 移除.
            if (bytes[i] == 46) {
                bytes[i] = 0;
                break;
            }
            // 移除0
            if (bytes[i] == 48) {
                bytes[i] = 0;
            }
        }
        return new String(bytes);
    }
}
