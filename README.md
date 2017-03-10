# FLog
A friendly, flexible, feature-rich and fair logger for Android.

## Feature
- Tag、调用栈、线程信息、时间信息、级别、内容、附加线、极简风格...
- Tag, call stack, thread information, time information, level, content, additional lines, minimalist style...

## Use
### Dependencies
```groovy
debugCompile 'com.github.SaltedfishCaptain:flog:1.0.0:debug@aar'
releaseCompile 'com.github.SaltedfishCaptain:flog:1.0.0:release@aar'
```

### Sample
```java
FLog.print("Hello FLog");

FLog.tag("FLog").print("Hello FLog");

FLog.tag("FLog").count(4).showThread().showTime().print("Hello FLog");

FLog.tag("FLog").count(100).excludeThis(this).withJson(JSON_STRING).print("Hello FLog");

FLog.tag("FLog").singleLine().showFooterLine().showHeardLine().print("Hello FLog");

...
```

## Tips
1. 通过链式调用非常灵活得对打印内容进行配置：tag、调用栈、线程信息、时间信息、级别、内容、附加线、极简风格...
2. 配置过程对链式调用顺序没有要求，只要在最后调用print方法，就表示配置结束
3. The chain is very flexible to call the configuration of the print content: tag, call stack, thread information, time information, level, content, additional lines, minimalist style...
4. The configuration process does not require a chain call sequence, as long as the print method is called at the end, the configuration is completed.

## APIs

1. `tag(String tag)`
2. `offset(int offset)`
3. `count(int count)`
4. `exclude(Class<?>... excludeClasses)`
5. `excludeThis(Object object)`
6. `showThread()`
7. `showTime()`
8. `withJson(String json)`
9. `withObject(String object)`
10. `singleLine()`
11. `showHeardLine()`
12. `showFooterLine()`
13. `todo()`
14. `d()`
15. `e()`
16. `w()`
17. `i()`
18. `v()`
19. `print(String message, Object... args)`

