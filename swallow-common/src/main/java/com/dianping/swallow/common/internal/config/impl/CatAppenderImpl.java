//package com.dianping.swallow.common.internal.config.impl;
//
//import com.dianping.cat.Cat;
//import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.core.Filter;
//import org.apache.logging.log4j.core.Layout;
//import org.apache.logging.log4j.core.LogEvent;
//import org.apache.logging.log4j.core.appender.AbstractAppender;
//import org.apache.logging.log4j.core.appender.AppenderLoggingException;
//import org.apache.logging.log4j.core.config.plugins.Plugin;
//import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
//import org.apache.logging.log4j.core.config.plugins.PluginElement;
//import org.apache.logging.log4j.core.config.plugins.PluginFactory;
//import org.apache.logging.log4j.core.impl.ThrowableProxy;
//import org.apache.logging.log4j.core.layout.PatternLayout;
//
//import java.io.PrintWriter;
//import java.io.Serializable;
//import java.io.StringWriter;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReadWriteLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
///**
// *
// * @author mingdong.li
// *
// * 2015年11月4日 下午3:07:05
// */
//@Plugin(name = "CatAppender", category = "Core", elementType = "appender", printObject = true)
//public final class CatAppenderImpl extends AbstractAppender {
//
//    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
//
//    private final Lock readLock = rwLock.readLock();
//
//    protected CatAppenderImpl(String name, Filter filter,
//                              Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
//        super(name, filter, layout, ignoreExceptions);
//    }
//
//    @Override
//    public void append(LogEvent event) {
//        readLock.lock();
//        try {
//            boolean isTraceMode = Cat.getManager().isTraceMode();
//            Level level = event.getLevel();
//            if(level.isMoreSpecificThan(Level.ERROR)) {
//                this.logError(event);
//            } else if(isTraceMode) {
//                this.logTrace(event);
//            }
//        } catch (Exception ex) {
//            if (!ignoreExceptions()) {
//                throw new AppenderLoggingException(ex);
//            }
//        } finally {
//            readLock.unlock();
//        }
//    }
//
//    @PluginFactory
//    public static CatAppenderImpl createAppender(
//            @PluginAttribute("name") String name,
//            @PluginElement("Layout") Layout<? extends Serializable> layout,
//            @PluginElement("Filter") final Filter filter,
//            @PluginAttribute("otherAttribute") String otherAttribute) {
//        if (name == null) {
//            LOGGER.error("No name provided for CatAppenderImpl");
//            return null;
//        }
//        if (layout == null) {
//            layout = PatternLayout.createDefaultLayout();
//        }
//        return new CatAppenderImpl(name, filter, layout, true);
//    }
//
//    private void logError(LogEvent event) {
//        ThrowableProxy info = event.getThrownProxy();
//        if(info != null) {
//            Throwable exception = info.getThrowable();
//
//            Object message = event.getMessage();
//            if(message != null) {
//                Cat.logError(String.valueOf(message), exception);
//            } else {
//                Cat.logError(exception);
//            }
//        }
//
//    }
//
//    private void logTrace(LogEvent event) {
//        String type = "Log4j";
//        String name = event.getLevel().toString();
//        Object message = event.getMessage();
//        String data;
//        if(message instanceof Throwable) {
//            data = this.buildExceptionStack((Throwable)message);
//        } else {
//            data = event.getMessage().toString();
//        }
//
//        ThrowableProxy info = event.getThrownProxy();
//        if(info != null) {
//            data = data + '\n' + this.buildExceptionStack(info.getThrowable());
//        }
//
//        Cat.logTrace(type, name, "0", data);
//    }
//
//    private String buildExceptionStack(Throwable exception) {
//        if(exception != null) {
//            StringWriter writer = new StringWriter(2048);
//            exception.printStackTrace(new PrintWriter(writer));
//            return writer.toString();
//        } else {
//            return "";
//        }
//    }
//}