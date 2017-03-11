package com.saltedfishcaptain.demo;

import android.app.Application;

import com.saltedfishcaptain.flog.FLog;

/**
 * Description:
 * <p>
 * Attention:
 * <p>
 * Created by Zhengyu.Xiong ; On 2017-03-11.
 */

public class FLogApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        configFLog();
    }

    private void configFLog() {
        FLog.init().count(6).dateFormat("MM月dd日 hh:mm:ss SSS").v();
    }
}
