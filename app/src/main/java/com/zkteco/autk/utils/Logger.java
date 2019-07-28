package com.zkteco.autk.utils;

import android.util.Log;

/**
 * ULog tool class
 */
public class Logger {

    /**
     * log enable var
     */
    public static final boolean DEBUG = Utils.DEBUG;

    /**
     * @return get method name and line number
     */
    private static String getStackTraceString() {
        Thread current = Thread.currentThread();
        StackTraceElement[] stack = current.getStackTrace();
        // stack[0].getMethodName() = getThreadStackTrace
        // stack[1].getMethodName() = getStackTrace
        // stack[2].getMethodName() = getStackTraceString
        // stack[3].getMethodName() = getStackTraceString parent function
        String result = "(line " + stack[4].getLineNumber() + ")" + stack[4].getMethodName() + "():";
        return result;
    }

    /**
     * @param tag
     * @param logMsg
     */
    public static void v(String tag, String logMsg) {
        if (DEBUG) android.util.Log.v(tag, getStackTraceString() + logMsg);
    }

    /**
     * @param tag
     * @param logMsg
     * @param throwable
     */
    public static void v(String tag, String logMsg, Throwable throwable) {
        if (DEBUG) android.util.Log.v(tag, getStackTraceString() + logMsg, throwable);
    }

    /**
     * @param tag
     * @param logMsg
     */
    public static void d(String tag, String logMsg) {
        if (DEBUG) android.util.Log.d(tag, getStackTraceString() + logMsg);
    }

    /**
     * @param tag
     * @param logMsg
     * @param throwable
     */
    public static void d(String tag, String logMsg, Throwable throwable) {
        if (DEBUG) android.util.Log.d(tag, getStackTraceString() + logMsg, throwable);
    }

    /**
     * @param tag
     * @param logMsg
     */
    public static void i(String tag, String logMsg) {
        if (DEBUG) android.util.Log.i(tag, getStackTraceString() + logMsg);
    }

    /**
     * @param tag
     * @param logMsg
     * @param throwable
     */
    public static void i(String tag, String logMsg, Throwable throwable) {
        if (DEBUG) android.util.Log.i(tag, getStackTraceString() + logMsg, throwable);
    }

    /**
     * @param tag
     * @param logMsg
     */
    public static void w(String tag, String logMsg) {
        if (DEBUG) android.util.Log.w(tag, getStackTraceString() + logMsg);
    }

    /**
     * @param tag
     * @param logMsg
     * @param throwable
     */
    public static void w(String tag, String logMsg, Throwable throwable) {
        if (DEBUG) android.util.Log.w(tag, getStackTraceString() + logMsg, throwable);
    }

    /**
     * @param tag
     * @param logMsg
     */
    public static void e(String tag, String logMsg) {
        android.util.Log.e(tag, getStackTraceString() + logMsg);
    }

    /**
     * @param tag
     * @param logMsg
     * @param throwable
     */
    public static void e(String tag, String logMsg, Throwable throwable) {
        android.util.Log.e(tag, getStackTraceString() + logMsg, throwable);
    }
}
