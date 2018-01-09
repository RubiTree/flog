package com.saltedfishcaptain.flog;

/**
 * Description:
 * <p>
 * Attention:
 * <p>
 * Created by Zhengyu.Xiong ; On 2018-01-09.
 */

public interface LogHandler {
    void handLog(int logType, String tag, String msg);
}