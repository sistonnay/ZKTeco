package com.zkteco.autk.models;

import com.zkteco.autk.utils.Logger;
import com.zkteco.autk.utils.Utils;

/**
 * Created by tonnay on 18-2-6.
 */

public class TimerTool {

    private static final String TAG = Utils.TAG + "#" + TimerTool.class.getSimpleName();

    private static volatile TimerTool INSTANCE;

    private long oldMillis = System.currentTimeMillis();

    private TimerTool() {

    }

    public static TimerTool getInstance() {
        if (INSTANCE == null) {
            synchronized (TimerTool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TimerTool();
                }
            }
        }
        return INSTANCE;
    }

    public void start(String fun) {
        oldMillis = System.currentTimeMillis();
        Logger.v(TAG, fun + " start at " + oldMillis);
    }

    public void stop(String fun) {
        long newMillis = System.currentTimeMillis();
        Logger.v(TAG, fun + " stop at " + newMillis);
        Logger.v(TAG, fun + " total spend " + (newMillis - oldMillis));
    }
}
