package com.saltedfishcaptain.flog;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Description:
 * <p>
 * Attention:
 * <p>
 * Created by Zhengyu.Xiong ; On 2017-12-06.
 */

public final class LogPrinter {
    private static final boolean IS_HIDE_LOG = !BuildConfig.DEBUG;

    private static final int LOGCAT_ONCE_PRINT_MAX_SIZE = 4000;
    private static final int JSON_LEVEL_INDENT = 2;
    // do not use this unless have no valid stacktrace
    private static final String DEFAULT_TAG = "FLog";

    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char TOP_LEFT_ROUND_CORNER = '╭';
    private static final char BOTTOM_LEFT_ROUND_CORNER = '╰';
    private static final char MIDDLE_CORNER = '╟';
    private static final char VERTICAL_DOUBLE_LINE = '║';
    private static final char VERTICAL_SINGLE_LINE = '│';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER +
            DOUBLE_DIVIDER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER +
            DOUBLE_DIVIDER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER +
            SINGLE_DIVIDER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final String TOP_LINE = TOP_LEFT_ROUND_CORNER +
            SINGLE_DIVIDER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final String BOTTOM_LINE = BOTTOM_LEFT_ROUND_CORNER +
            SINGLE_DIVIDER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    private String tag = Setting.tag;
    private int logType = Setting.logType;
    private boolean showThreadInfo = Setting.showThreadInfo;
    private boolean isSimpleStyle = Setting.isSimpleStyle;
    private boolean showHeardLine = Setting.showHeardLine;
    private boolean showFooterLine = Setting.showFooterLine;
    private boolean showCurrentTime = Setting.showCurrentTime;
    private ArrayList<String> errorMsg = new ArrayList<>();
    private String json = "";
    private Object object = null;

    // not safe
    private int methodCount = Setting.methodCount;
    private int offset = Setting.offset;
    private Class<?>[] excludeClasses;

    /*-------------------------------------------------*/

    private void resetConfig() {
        tag = Setting.tag;
        methodCount = Setting.methodCount;
        offset = Setting.offset;
        logType = Setting.logType;
        showThreadInfo = Setting.showThreadInfo;
        excludeClasses = null;
        isSimpleStyle = Setting.isSimpleStyle;
        showHeardLine = Setting.showHeardLine;
        showFooterLine = Setting.showFooterLine;
        showCurrentTime = Setting.showCurrentTime;
        errorMsg.clear();
        json = "";
        object = null;
    }

    /*--------------------------------------------------------------------------------------------*/

    public LogPrinter tag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            this.tag = tag;
        } else {
            errorMsg.add("You give a empty tag!");
        }
        return this;
    }

    // order after exclude
    public LogPrinter offset(int offset) {
        if (offset >= 0) {
            this.offset = offset;
        } else {
            errorMsg.add("You give a wrong offset!");
        }
        return this;
    }

    public LogPrinter count(int methodCount) {
        if (methodCount >= 0) {
            this.methodCount = methodCount;
        } else {
            errorMsg.add("You give a wrong methodCount!");
        }
        return this;
    }

    public LogPrinter showThread() {
        showThreadInfo = true;
        return this;
    }

    public LogPrinter showTime() {
        showCurrentTime = true;
        return this;
    }

    public LogPrinter singleLine() {
        isSimpleStyle = true;
        return this;
    }

    // 为了不出现打印错误，需要传入从当前调用类开始，按照调用顺序向外连续的类
    // order before offset
    // conflict with method excludeThis()
    public LogPrinter exclude(Class<?>... excludeClasses) {
        if (excludeClasses != null) {
            this.excludeClasses = excludeClasses;
        } else {
            errorMsg.add("You give a empty excludeClasses!");
        }
        return this;
    }

    // conflict with method exclude()
    public LogPrinter excludeThis(Object object) {
        if (object != null) {
            exclude(object.getClass());
        } else {
            errorMsg.add("You give a empty excludeClasses!");
        }
        return this;
    }

    public LogPrinter d() {
        logType = Log.DEBUG;
        return this;
    }

    public LogPrinter e() {
        logType = Log.ERROR;
        return this;
    }

    public LogPrinter w() {
        logType = Log.WARN;
        return this;
    }

    public LogPrinter i() {
        logType = Log.INFO;
        return this;
    }

    public LogPrinter v() {
        logType = Log.VERBOSE;
        return this;
    }

    public LogPrinter showHeardLine() {
        showHeardLine = true;
        return this;
    }

    public LogPrinter showFooterLine() {
        showFooterLine = true;
        return this;
    }

    public LogPrinter withJson(String json) {
        this.json = json;
        return this;
    }

    public LogPrinter withObject(Object object) {
        if (object != null) {
            this.object = object;
        } else {
            errorMsg.add("You add a null object!");
        }
        return this;
    }

    /*-------------------------------------------------*/

    public void print(String message, Object... args) {
        if (IS_HIDE_LOG) return;
        printMainProcess(message, args);
        resetConfig();
    }

    public void todo() {
        this.tag("todo").i().print(TODO);
    }

    /*--------------------------------------------------------------------------------------------*/

    private void printMainProcess(String msg, Object... args) {
        // 需要同步执行避免不同线程交叉打印造成排布混乱
        synchronized (LogPrinter.class) {
            StackTraceSegment validStackTraceSegment = getValidStackTraceSegment();

            validateTag(validStackTraceSegment);

            String message = createMessage(msg, args);
            if (isSimpleStyle) {
                printAllInSingleLine(validStackTraceSegment, message);
            } else {
                printCompletely(validStackTraceSegment, message);
            }
        }
    }

    // deal config : offset / methodCount / excludeClasses
    private StackTraceSegment getValidStackTraceSegment() {
        if (methodCount <= 0) return new StackTraceSegment();
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        // 1. skip one dalvik method and one java.lang method
        int includeStartIndex = 2;

        // 2. add excludeClasses to includeStartIndex
        if (trace.length <= includeStartIndex) return new StackTraceSegment();
        for (int i = includeStartIndex; i < trace.length; i++) {
            if (isMatchExcludeClassName(trace[i].getClassName())) {
                includeStartIndex++;
            } else {
                break;
            }
        }

        // 3. add custom offset to includeStartIndex
        includeStartIndex += offset;

        // 4. check includeStartIndex last
        if (trace.length <= includeStartIndex) {
            errorMsg.add("You give a too big offset! No stacktrace can be showed.");
            return new StackTraceSegment();
        }

        // 5. get valid count
        int maxPrintableTraceSize = trace.length - includeStartIndex;
        int count = Math.min(methodCount, maxPrintableTraceSize);

        int canNotPrintCount = methodCount - maxPrintableTraceSize;
        if (canNotPrintCount > 0) {
            errorMsg.add("Not enough stacktrace can be showed! Just show " +
                    maxPrintableTraceSize + ", " + canNotPrintCount + " less than except.");
        }

        return new StackTraceSegment(trace, includeStartIndex, count);
    }

    private boolean isMatchExcludeClassName(String name) {
        if (name.equals(LogPrinter.class.getName())) return true;

        if (name.equals(FLog.class.getName())) return true;

        if (excludeClasses != null && excludeClasses.length > 0) {
            for (Class<?> excludeClassName : excludeClasses) {
                if (name.equals(excludeClassName.getName())) return true;
            }
        }

        return false;
    }

    private void validateTag(StackTraceSegment trace) {
        if (TextUtils.isEmpty(tag)) {
            if (trace.isEmpty) tag = DEFAULT_TAG;
            tag = getSimpleClassName(trace.getFirstTrace().getClassName());
        }
    }

    private String createMessage(String message, Object... args) {
        if (message == null) {
            errorMsg.add("You give a null message!");
            return "";
        }
        if (args == null || args.length == 0) return message;
        return String.format(message, args);
    }

    /*--------------------------------------------------------------------------------------------*/

    // OneLine 模式所有的打印内容，不会打印设置引发的错误信息，也不会打印json和object, 注意兼容其他设置
    private void printAllInSingleLine(StackTraceSegment traceSegment, String msg) {
        printHeardLine();
        basePrintOneLine(VERTICAL_SINGLE_LINE + " " + getFormatOneLineContent(traceSegment, msg));
        printFooterLine();
    }

    /*-------------------------------------------------*/

    private void printCompletely(StackTraceSegment validStackTraceSegment, String message) {
        printTopBorder();
        printHeaderContent(validStackTraceSegment);
        printMessageContent(message);
        printAppendContent();
        printBottomBorder();
    }

    private void printHeaderContent(StackTraceSegment validStackTraceSegment) {
        printErrorInfo();
        printThreadInfo();
        printTimeInfo();
        printStackTraceSegment(validStackTraceSegment);
    }

    private void printMessageContent(String message) {
        printBigString(message);
    }

    private void printAppendContent() {
        printJsonData();
        printObjectData();
    }

    /*--------------------------------------------------------------------------------------------*/

    // allow print big size message
    private void printBigString(String content) {
        byte[] bytes = content.getBytes();
        int length = bytes.length;
        if (length <= LOGCAT_ONCE_PRINT_MAX_SIZE) {
            printAppropriateStringInMultiline(content);
        } else {
            for (int i = 0; i < length; i += LOGCAT_ONCE_PRINT_MAX_SIZE) {
                int count = Math.min(length - i, LOGCAT_ONCE_PRINT_MAX_SIZE);
                printAppropriateStringInMultiline(new String(bytes, i, count));
            }
        }
    }

    private void printAppropriateStringInMultiline(String content) {
        String[] lines = content.split(System.getProperty("line.separator"));
        for (String line : lines) {
            basePrintOneLine(VERTICAL_DOUBLE_LINE + " " + line);
        }
    }

    private void printErrorInfo() {
        if (errorMsg.isEmpty()) return;

        for (String errorText : errorMsg) {
            basePrintOneLine(VERTICAL_DOUBLE_LINE + " PrintConfigError: " + errorText);
        }
        printDivider();
    }

    private void printThreadInfo() {
        if (showThreadInfo) {
            basePrintOneLine(VERTICAL_DOUBLE_LINE +
                    " Thread: " + Thread.currentThread().getName() + this.toString());
            printDivider();
        }
    }

    private void printTimeInfo() {
        if (showCurrentTime) {
            basePrintOneLine(VERTICAL_DOUBLE_LINE + " CurrentTime: " + getCurrentTimeString());
            printDivider();
        }
    }

    private String getCurrentTimeString() {
        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat(Setting.dataFormat);
        return ft.format(now);
    }

    private void printJsonData() {
        String formatJson = getFormatJson(json);
        if (!TextUtils.isEmpty(formatJson)) {
            printDivider();
            printBigString(formatJson);
        }
    }

    private void printObjectData() {
        String formatObject = getFormatObject(object);
        if (!TextUtils.isEmpty(formatObject)) {
            printDivider();
            printBigString(formatObject);
        }
    }

    private void printStackTraceSegment(StackTraceSegment validStackTraceSegment) {
        if (validStackTraceSegment.isEmpty) return;

        printStackTrace(validStackTraceSegment.trace, validStackTraceSegment.includeStartIndex,
                validStackTraceSegment.count);
        printDivider();
    }

    private void printStackTrace(StackTraceElement[] trace, int includeStartIndex, int count) {
        int includeEndIndex = includeStartIndex + count - 1;
        String levelIndent = "";

        for (int i = includeEndIndex; i >= includeStartIndex; i--) {
            basePrintOneLine(getFormatStackTrace(levelIndent, trace[i]));
            levelIndent += "   ";
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    private void printTopBorder() {
        basePrintOneLine(TOP_BORDER);
    }

    private void printBottomBorder() {
        basePrintOneLine(BOTTOM_BORDER);
    }

    private void printDivider() {
        basePrintOneLine(MIDDLE_BORDER);
    }

    private void printHeardLine() {
        if (showHeardLine) basePrintOneLine(TOP_LINE);
    }

    private void printFooterLine() {
        if (showFooterLine) basePrintOneLine(BOTTOM_LINE);
    }

    private void basePrintOneLine(String content) {
        switch (logType) {
            case Log.ERROR:
                Log.e(tag, content);
                break;
            case Log.INFO:
                Log.i(tag, content);
                break;
            case Log.VERBOSE:
                Log.v(tag, content);
                break;
            case Log.WARN:
                Log.w(tag, content);
                break;
            case Log.ASSERT:
                Log.wtf(tag, content);
                break;
            case Log.DEBUG:
                // Fall through
            default:
                Log.d(tag, content);
                break;
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    private String getFormatJson(String json) {
        if (TextUtils.isEmpty(json)) {
            errorMsg.add("You give a empty Json!");
            return "";
        }

        try {
            json = json.trim();
            if (json.startsWith("{")) {
                return new JSONObject(json).toString(JSON_LEVEL_INDENT);
            }
            if (json.startsWith("[")) {
                return new JSONArray(json).toString(JSON_LEVEL_INDENT);
            }
            errorMsg.add("You give a invalid Json!");
        } catch (JSONException e) {
            errorMsg.add("You give a invalid Json!");
        }

        return "";
    }

    private String getFormatObject(Object object) {
        if (object == null) return "";
        return object.toString();
    }

    private String getFormatOneLineContent(StackTraceSegment traceSegment, String msg) {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ")
                .append(msg)
                .append(" ]");
        if (showThreadInfo) {
            builder.append("[ Thread: ")
                    .append(Thread.currentThread().getName())
                    .append(" ]");
        }
        if (showCurrentTime) {
            builder.append("[ Time: ")
                    .append(getCurrentTimeString())
                    .append(" ]");
        }
        if (!traceSegment.isEmpty) {
            StackTraceElement trace = traceSegment.getFirstTrace();
            builder.append("[ ")
                    .append(getSimpleClassName(trace.getClassName()))
                    .append(".")
                    .append(trace.getMethodName())
                    .append(" (")
                    .append(trace.getFileName())
                    .append(":")
                    .append(trace.getLineNumber())
                    .append(")]");
        }
        return builder.toString();
    }

    private String getFormatStackTrace(String levelIndent, StackTraceElement trace) {
        StringBuilder builder = new StringBuilder();
        builder.append("║ ")
                .append(levelIndent)
                .append(getSimpleClassName(trace.getClassName()))
                .append(".")
                .append(trace.getMethodName())
                .append(" (")
                .append(trace.getFileName())
                .append(":")
                .append(trace.getLineNumber())
                .append(")");
        return builder.toString();
    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    /*--------------------------------------------------------------------------------------------*/

    // 内部不保证合法
    private class StackTraceSegment {
        StackTraceElement[] trace;
        int includeStartIndex; // order is from near this to far
        int count;
        boolean isEmpty = true;

        public StackTraceSegment() {
        }

        public StackTraceSegment(StackTraceElement[] trace, int includeStartIndex, int count) {
            this.trace = trace;
            this.includeStartIndex = includeStartIndex;
            this.count = count;
            isEmpty = false;
        }

        // includeStartIndex 不能没初始化，不能超过大小，count不能为0
        public StackTraceElement getFirstTrace() {
            return trace[includeStartIndex];
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    private String TODO = "" +
            "　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　\n" +
            "　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　\n" +
            "　　　　　　　　囍囍囍囍囍囍囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　囍囍囍囍囍　　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　　　　　　囍囍囍囍囍　　　　　　　　　　　　　\n" +
            "　　　　　　　囍囍囍囍囍囍囍囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍囍囍囍　囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍囍　　　　　　　　　　　\n" +
            "　　　　　　　囍囍囍囍　　　囍囍　　囍囍囍囍囍　　　　　　　　　　　　　　　　囍囍囍囍　　　囍囍囍囍　　　　　　　　　　　　　　　　　　　囍囍　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　　　囍囍囍囍　　　囍囍囍囍　　　　　　　　　　\n" +
            "　　　　　　　囍囍　　　　　囍囍　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍　　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　囍囍囍　　　　　　囍囍囍囍　　　　　　　　　\n" +
            "　　　　　　　囍囍　　　　　囍囍　　　　　囍囍　　　　　　　　　　　　　　囍囍囍囍　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　　囍囍　　　　　　　囍囍囍囍　　　　　　　　　　　　　　　囍囍囍囍　　　　　　　囍囍囍　　　　　　　　　\n" +
            "　　　　　　　囍　　　　　　囍囍　　　　　囍囍　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍囍　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍囍　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　　囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　囍囍　　　　　　　囍囍囍囍　　　　　　　　　　　　　　　囍囍囍　　　　　　　　　囍囍囍　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　囍囍囍　　　　　　　　　　　　　　　　　　囍囍　　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　囍囍囍　　　　　　　囍囍囍　　　　　　　　　\n" +
            "　　　　　　　　　　　　　　囍囍　　　　　　　　　　　　　　　　　　　　　　囍囍囍囍　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍囍囍　　　　　囍囍囍囍　　　　　　　　　\n" +
            "　　　　　　　　　　　　　囍囍囍囍　　　　　　　　　　　　　　　　　　　　　　囍囍囍囍　　　囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍囍囍　　囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　囍囍囍囍　　　囍囍囍囍　　　　　　　　　　\n" +
            "　　　　　　　　　　　囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍囍囍囍囍　　　　　　　　　　　　　　　　　　　　　　囍囍囍囍囍囍囍囍　　　　　　　　　　　　\n" +
            "　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　\n" +
            "　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　";
}
