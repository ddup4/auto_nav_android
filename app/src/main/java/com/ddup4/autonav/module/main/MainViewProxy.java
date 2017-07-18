package com.ddup4.autonav.module.main;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ddup4.autonav.api.entity.GpsInfo;
import com.ddup4.autonav.api.entity.Response;
import com.ddup4.autonav.app.BaseViewProxy;
import com.ddup4.autonav.data.ApiManager;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.AppContext;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.lang.ClassName;
import com.okandroid.boot.lang.Log;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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

        private TelephonyManager mTelephonyManager;
        private PhoneStateListener mPhoneStateListener;

        public GpsInfo gpsInfo;

        private ViewData() {
        }

        private void startTelephonyListen() {
            mTelephonyManager = (TelephonyManager) AppContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    Log.v(CLASS_NAME, "onCallStateChanged", getCallStateString(state), incomingNumber);

                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (!TextUtils.isEmpty(incomingNumber)) {
                            showReadGpsInfoConfirm(incomingNumber);
                        }
                    }
                }
            };
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

    public void showReadGpsInfoConfirm(String phone) {
        MainView view = getView();
        if (view == null) {
            return;
        }

        view.showReadGpsInfoConfirm(phone);
    }

    public void readGpsInfoFromServer(String phone) {
        replaceDefaultRequestHolder(ApiManager.getInstance().getDefaultApi()
                .getGpsInfo(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<GpsInfo>>() {
                    @Override
                    public void accept(@NonNull Response<GpsInfo> response) throws Exception {
                        MainView view = getView();
                        if (view == null) {
                            return;
                        }

                        if (response.status != 0 || response.data == null) {
                            ToastUtil.show("位置信息无效");
                            return;
                        }

                        view.showStartNavConfirm(response.data);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable e) throws Exception {
                        MainView view = getView();
                        if (view == null) {
                            return;
                        }

                        ToastUtil.show("位置信息读取失败");
                        e.printStackTrace();
                    }
                }));
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

        ViewData viewData = (ViewData) getDynamicViewData();
        if (viewData == null) {
            return;
        }

        viewData.startTelephonyListen();

        // TODO
    }

}
