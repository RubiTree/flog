package com.saltedfishcaptain.demo;

import android.util.Log;

import com.saltedfishcaptain.flog.FLog;
import com.saltedfishcaptain.flog.LogHandler;

/**
 * Description:
 * <p>
 * Attention:
 * <p>
 * Created by Zhengyu.Xiong ; On 2017-03-09.
 */

public class FLogTestCase {
    public static final String JSON_STRING = "{\"data\":{\"total\":\"5\",\"courses\":[{\"id\":\"125\",\"stage\":\"2\",\"grade\":\"7\",\"teach_subject_id\":\"2\",\"teach_subject_name\":\"math\",\"price\":\"0\",\"actual_price\":\"0\"},{\"id\":\"126\",\"stage\":\"2\",\"grade\":\"8\",\"teach_subject_id\":\"2\",\"teach_subject_name\":\"math\",\"price\":\"0\",\"actual_price\":\"0\"},{\"id\":\"127\",\"stage\":\"3\",\"grade\":\"10\",\"teach_subject_id\":\"2\",\"teach_subject_name\":\"math\",\"price\":\"0\",\"actual_price\":\"0\"},{\"id\":\"128\",\"stage\":\"3\",\"grade\":\"11\",\"teach_subject_id\":\"2\",\"teach_subject_name\":\"math\",\"price\":\"0\",\"actual_price\":\"0\"},{\"id\":\"129\",\"stage\":\"3\",\"grade\":\"12\",\"teach_subject_id\":\"2\",\"teach_subject_name\":\"Math\",\"price\":\"0\",\"actual_price\":\"0\"}]}}";

    public void basePrintTest(Object object){
        FLog.tag("FLog")
                .count(8)
                .showThread()
                .showTime()
                .withObject(object)
                .print("Hello FLog ! Config : count(8).showThread().showTime().withObject(object)");

        FLog.tag("FLog")
                .count(0)
                .withObject(object)
                .w()
                .print("Hello FLog ! Config : count(0).withObject(object).w()");

        FLog.tag("FLog")
                .excludeThis(this)
                .print("Hello FLog ! Config : excludeThis(this)");

        FLog.tag("FLog")
                .count(100)
                .singleLine()
                .print("Hello FLog ! Config : count(100).singleLine().showFooterLine().showHeardLine()");

        FLog.tag("FLog")
                .count(100)
                .singleLine()
                .showFooterLine()
                .showHeardLine()
                .print("Hello FLog ! Config : count(100).singleLine().showFooterLine().showHeardLine()");

        FLog.tag("FLog").offset(100).withJson(JSON_STRING).print("Hello FLog ! Config : offset(100).withJson(JSON_STRING)");

        FLog.tag("FLog").singleLine().showTime().print("singleLine().showTime()");

        FLog.tag("FLog").singleLine().showTime().setHandler(new LogHandler() {
            @Override
            public void handLog(int logType, String tag, String msg) {
                Log.e(tag, msg);
            }
        }).print("singleLine().showTime()");
    }
}