package com.ddup4.autonav.ext;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ddup4.autonav.R;
import com.ddup4.autonav.data.ApiManager;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.lang.ResumedViewClickListener;
import com.okandroid.boot.util.ViewUtil;
import com.okandroid.boot.widget.ContentDialog;

/**
 * Created by idonans on 2017/7/16.
 */

public class HostSettingsDialog extends ContentDialog {

    public HostSettingsDialog(@NonNull Activity activity) {
        super(activity);
    }

    private View mRootView;
    private TextView mCurrentHost;
    private EditText mNewHost;
    private View mSubmit;

    @Override
    public void onCreate() {
        super.onCreate();
        setContentView(R.layout.ddup4_autonav_ext_host_settings_dialog);
        mRootView = ViewUtil.findViewByID(getContentView(), R.id.root_view);
        mCurrentHost = ViewUtil.findViewByID(mRootView, R.id.current_host);
        mNewHost = ViewUtil.findViewByID(mRootView, R.id.new_host);
        mSubmit = ViewUtil.findViewByID(mRootView, R.id.submit);
        mSubmit.setOnClickListener(new ResumedViewClickListener(HostSettingsDialog.this) {
            @Override
            public void onClick(View v, ResumedViewClickListener listener) {
                submit();
            }
        });

        setDimBackground(true);
        setDimAmount(0.5f);
        setAnimationStyle(R.style.OKAndroid_Animation_ContentLoadingTop);

        mCurrentHost.setText("当前服务器地址(" + ApiManager.getInstance().getHost() + ")");
    }

    private void submit() {
        String host = mNewHost.getText().toString();
        if (host != null) {
            host = host.trim();
        }

        if (TextUtils.isEmpty(host)) {
            ToastUtil.show("服务器地址不能为空");
            return;
        }

        try {
            if (!host.endsWith("/")) {
                throw new IllegalStateException();
            }

            ApiManager.getInstance().setHost(host);
            dismiss();
        } catch (Throwable e) {
            e.printStackTrace();
            ToastUtil.show("服务器地址格式不正确, 请参考示例地址");
        }
    }

}
