package com.ddup4.autonav;

import android.content.Context;

import com.okandroid.boot.App;
import com.okandroid.boot.data.AppIDManager;
import com.okandroid.boot.data.FrescoManager;
import com.okandroid.boot.data.ProcessManager;
import com.okandroid.boot.data.StorageManager;
import com.okandroid.boot.lang.Log;
import com.okandroid.boot.thread.Threads;

/**
 * Created by idonans on 2017/7/15.
 */

public class AppInit {

    private static final String TAG = "AppInit";
    private static boolean sInit;

    private AppInit() {
    }

    public static synchronized void init(Context context) {
        if (sInit) {
            return;
        }
        context = context.getApplicationContext();
        new App.Config.Builder()
                .setContext(context)
                .setBuildConfigAdapter(new BuildConfigAdapterImpl())
                .build()
                .init();

        initOnMainProcess();
        sInit = true;
    }

    private static void initOnMainProcess() {
        if (!ProcessManager.getInstance().isMainProcess()) {
            return;
        }

        Threads.postBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG + " init in background on main process");
                    StorageManager.getInstance();
                    FrescoManager.getInstance();
                    AppIDManager.getInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class BuildConfigAdapterImpl implements App.BuildConfigAdapter {

        @Override
        public int getVersionCode() {
            return BuildConfig.VERSION_CODE;
        }

        @Override
        public String getVersionName() {
            return BuildConfig.VERSION_NAME;
        }

        @Override
        public String getLogTag() {
            return BuildConfig.APPLICATION_ID;
        }

        @Override
        public String getPublicSubDirName() {
            return BuildConfig.APPLICATION_ID;
        }

        @Override
        public String getChannel() {
            return "default";
        }

        @Override
        public int getLogLevel() {
            return android.util.Log.VERBOSE;
        }

        @Override
        public boolean isDebug() {
            return BuildConfig.DEBUG;
        }
    }

}
