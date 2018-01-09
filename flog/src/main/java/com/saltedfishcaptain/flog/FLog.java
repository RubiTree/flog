package com.saltedfishcaptain.flog;

/**
 * Description:
 * 1. 通过链式调用非常灵活得对打印内容进行配置：tag、调用栈、线程信息、时间信息、级别、内容、附加线、极简风格...
 * 2. 配置过程对链式调用顺序没有要求，只要在最后调用print方法，就表示配置结束
 * <p>
 * Attention:
 * 1. 参考了 https://github.com/orhanobut/logger 让打印内容更加美观
 * 2. FLog在common模块处于release版本时不会进行打印
 * <p>
 * Created by Zhengyu.Xiong ; On 2017-02-02.
 */

public class FLog {
    private static final ThreadLocal<LogPrinter> LOCAL_PRINTER = new ThreadLocal<>();

    private static LogPrinter getPrinter() {
        LogPrinter printer = LOCAL_PRINTER.get();
        if (printer == null) {
            setPrinter();
            printer = LOCAL_PRINTER.get();
        }
        return printer;
    }

    private static void setPrinter() {
        LOCAL_PRINTER.set(new LogPrinter());
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * Suggest call this method only once in application class, begin with init() and end anywhere.
     * This setting will just replace the default value at each print.
     * Your new setting at each print will cover this setting.
     */
    public static Setting init(){
        return new Setting();
    }

    /*--------------------------------------------------------------------------------------------*/

    /**
     * @param tag 每一行Log方框左边显示的标签，默认标签是当前类类名
     */
    public static LogPrinter tag(String tag) {
        return getPrinter().tag(tag);
    }

    /*-------------------------------------------------*/

    public static LogPrinter d() {
        return getPrinter().d();
    }

    public static LogPrinter e() {
        return getPrinter().e();
    }

    public static LogPrinter w() {
        return getPrinter().w();
    }

    public static LogPrinter i() {
        return getPrinter().i();
    }

    public static LogPrinter v() {
        return getPrinter().v();
    }

    /*-------------------------------------------------*/

    /**
     * @param offset 期望打印的第一个调用栈与当前调用方法的偏移量，默认偏移量是0
     */
    public static LogPrinter offset(int offset) {
        return getPrinter().offset(offset);
    }

    /**
     * @param count 期望打印的调用栈数，默认是2
     */
    public static LogPrinter count(int count) {
        return getPrinter().count(count);
    }

    /**
     * @param excludeClasses 传入所有不打印调用栈的类的类名
     */
    public static LogPrinter exclude(Class<?>... excludeClasses) {
        return getPrinter().exclude(excludeClasses);
    }

    /**
     * @param object 传入不打印调用栈的类的对象，方便在需要排除当前类的时候直接传入this
     */
    public static LogPrinter excludeThis(Object object) {
        return getPrinter().exclude(object.getClass());
    }

    /*-------------------------------------------------*/

    /**
     * 在Log信息上方显示当前线程，默认不显示
     */
    public static LogPrinter showThread() {
        return getPrinter().showThread();
    }

    /**
     * 在Log信息上方显示当前时间，默认不显示
     */
    public static LogPrinter showTime() {
        return getPrinter().showTime();
    }

    /**
     * 在Log信息下方添加显示格式化的json内容
     */
    public static LogPrinter withJson(String json){
        return getPrinter().withJson(json);
    }

    /**
     * 在Log信息下方添加显示格式化的object内容
     */
    public static LogPrinter withObject(String object){
        return getPrinter().withObject(object);
    }

    /*-------------------------------------------------*/

    /**
     * 设置Log打印成单行样式，一行显示所有信息，此时count的设置会失效，也无法打印json和throwable
     */
    public static LogPrinter singleLine() {
        return getPrinter().singleLine();
    }

    /**
     * 给打印内容另外添加头线、尾线， oneLine模式生效
     */
    public static LogPrinter showHeardLine() {
        return getPrinter().showHeardLine();
    }

    public static LogPrinter showFooterLine() {
        return getPrinter().showFooterLine();
    }

    /*-------------------------------------------------*/

    /**
     * 增加自定义log处理步骤
     */
    public static LogPrinter setHandler(LogHandler logHandler) {
        return getPrinter().setHandler(logHandler);
    }

    /*-------------------------------------------------*/

    public static void todo(){
        getPrinter().todo();
    }

    /*-------------------------------------------------*/

    /**
     * 打印信息主体，支持格式化输出
     */
    public static void print(String message, Object... args) {
        getPrinter().print(message, args);
    }
}