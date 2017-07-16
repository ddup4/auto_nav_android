package com.ddup4.autonav.module.main;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ddup4.autonav.app.BaseViewProxy;
import com.okandroid.boot.AppContext;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.lang.ClassName;
import com.okandroid.boot.lang.Log;

import java.io.IOException;

/**
 * Created by idonans on 2017/2/3.
 */

public class MainViewProxy extends BaseViewProxy<MainView> {

    private final String CLASS_NAME = ClassName.valueOf(this);

    public MainViewProxy(MainView mainView) {
        super(mainView);
    }

    @Override
    protected DynamicViewData onInitBackground() {
        return new ViewData();
    }

    class ViewData implements DynamicViewData {

        private final TelephonyManager mTelephonyManager = (TelephonyManager) AppContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.v(CLASS_NAME, "onCallStateChanged", getCallStateString(state), incomingNumber);
            }
        };

        private ViewData() {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        private void close() {
        }

        private String getCallStateString(int state) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    return "CALL_STATE_IDLE" + state;
                case TelephonyManager.CALL_STATE_RINGING:
                    return "CALL_STATE_RINGING" + state;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    return "CALL_STATE_OFFHOOK" + state;
                default:
                    return "unknown" + state;
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        ViewData viewData = (ViewData) getDynamicViewData();
        if (viewData != null) {
            viewData.close();
        }
    }

    @Override
    public void onReady() {
        MainView view = getView();
        if (view == null) {
            return;
        }

        // TODO
    }

}
