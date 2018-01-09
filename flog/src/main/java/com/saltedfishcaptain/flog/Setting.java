package com.saltedfishcaptain.flog;

import android.text.TextUtils;
import android.util.Log;

/**
 * Description:
 * <p>
 * Attention:
 * <p>
 * Created by Zhengyu.Xiong ; On 2017-03-11.
 */

public final class Setting {
    static String dataFormat = "MM月dd日 HH:mm:ss (SSS'ms')";
    static String tag = "";
    static int methodCount = 2;
    static int offset = 0;
    static int logType = Log.DEBUG;
    static boolean showThreadInfo = false;
    static boolean isSimpleStyle = false;
    static boolean showHeardLine = false;
    static boolean showFooterLine = false;
    static boolean showCurrentTime = false;
    static LogHandler logHandler = null;

    /*--------------------------------------------------------------------------------------------*/

    public Setting tag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            Setting.tag = tag;
        }
        return this;
    }

    public Setting count(int methodCount) {
        if (methodCount >= 0) {
            Setting.methodCount = methodCount;
        }
        return this;
    }

    public Setting offset(int offset) {
        if (offset >= 0) {
            Setting.offset = offset;
        }
        return this;
    }

    public Setting d() {
        logType = Log.DEBUG;
        return this;
    }

    public Setting e() {
        logType = Log.ERROR;
        return this;
    }

    public Setting w() {
        logType = Log.WARN;
        return this;
    }

    public Setting i() {
        logType = Log.INFO;
        return this;
    }

    public Setting v() {
        logType = Log.VERBOSE;
        return this;
    }

    public Setting showThread() {
        showThreadInfo = true;
        return this;
    }

    public Setting singleLine() {
        isSimpleStyle = true;
        return this;
    }

    public Setting showHeardLine() {
        showHeardLine = true;
        return this;
    }

    public Setting showFooterLine() {
        showFooterLine = true;
        return this;
    }

    public Setting showTime() {
        showCurrentTime = true;
        return this;
    }

    public Setting dateFormat(String dataFormat) {
        Setting.dataFormat = dataFormat;
        return this;
    }

    public Setting setHandler(LogHandler logHandler) {
        Setting.logHandler = logHandler;
        return this;
    }
}
