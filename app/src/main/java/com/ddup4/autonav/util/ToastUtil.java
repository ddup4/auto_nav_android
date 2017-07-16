package com.ddup4.autonav.util;

import android.widget.Toast;

import com.okandroid.boot.AppContext;
import com.okandroid.boot.thread.Threads;

/**
 * Created by idonans on 2017/7/16.
 */

public class ToastUtil {

    private ToastUtil() {
    }

    private static Toast sLastToast;

    public static void show(final CharSequence message) {
        Threads.runOnUi(new Runnable() {
            @Override
            public void run() {
                cancelLastToast();
                sLastToast = Toast.makeText(AppContext.getContext(), String.valueOf(message), Toast.LENGTH_LONG);
                sLastToast.show();
            }
        });
    }

    private static void cancelLastToast() {
        if (sLastToast != null) {
            sLastToast.cancel();
            sLastToast = null;
        }
    }

}
