package com.cds.comb;

import android.app.Application;

import com.cds.comb.util.CrashHandler;

/**
 * @Author: chengzj
 * @CreateDate: 2019/1/4 17:06
 * @Version: 3.0.0
 */
public class App extends Application {
    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CrashHandler.getInstance().init(this);
    }
}
