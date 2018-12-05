package com.partnerx.roboth_server;

import android.text.TextUtils;
import android.util.Log;

public class LogMgr {

    public static final String SEPARATOR = ",";
    private final static int LOG_LEVEL = 1;
    private final static int ERROR = 1;
    private final static int WARN = 2;
    private final static int INFO = 3;
    private final static int DEBUG = 4;
    private final static int VERBOSE = 5;

    public static void v(String message) {
        if (LOG_LEVEL <= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.v(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void v(String tag, String message) {
        if (LOG_LEVEL <= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "Control-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.v(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void d(String message) {
        if (LOG_LEVEL <= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.d(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void d(String tag, String message) {
        if (LOG_LEVEL <= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "Control-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.d(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void i(String message) {
        if (LOG_LEVEL <= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.i(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void i(String tag, String message) {
        if (LOG_LEVEL <= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "Control-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.i(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void w(String message) {
        if (LOG_LEVEL <= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.w(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void w(String tag, String message) {
        if (LOG_LEVEL <= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            tag = "Control-" + tag;
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.w(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void e(String message) {
        if (LOG_LEVEL <= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.e(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    public static void e(String tag, String message) {
        if (LOG_LEVEL <= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.e(tag, getLogInfo(stackTraceElement) + message);
        }
    }

    /**
     * Get default tag name
     */
    public static String getDefaultTag(StackTraceElement stackTraceElement) {
        String fileName = stackTraceElement.getFileName();
        String stringArray[] = fileName.split("\\.");
        String tag = stringArray[0];
        return "Control-" + tag;
    }

    /**
     * get stack info
     */
    public static String getLogInfo(StackTraceElement stackTraceElement) {
        StringBuilder logInfoStringBuilder = new StringBuilder();
        // thread name
        String threadName = Thread.currentThread().getName();
        // thread ID
        long threadID = Thread.currentThread().getId();
        // file name
        String fileName = stackTraceElement.getFileName();
        // class name
        String className = stackTraceElement.getClassName();
        // method
        String methodName = stackTraceElement.getMethodName();
        // code line
        int lineNumber = stackTraceElement.getLineNumber();

        logInfoStringBuilder.append("[ ");
        /*
         * logInfoStringBuilder.append("threadID=" + threadID).append(SEPARATOR); 
         * logInfoStringBuilder.append("threadName=" + threadName).append(SEPARATOR);
         * logInfoStringBuilder.append("fileName=" + fileName).append(SEPARATOR); 
         * logInfoStringBuilder.append("className=" + className).append(SEPARATOR);
         * logInfoStringBuilder.append("methodName=" + methodName).append(SEPARATOR);
         */
        logInfoStringBuilder.append("lineNumber =" + lineNumber);
        logInfoStringBuilder.append(" ] ");
        methodName = logInfoStringBuilder.toString();
        logInfoStringBuilder = null;
        return methodName;
    }
}
