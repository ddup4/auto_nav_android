package com.ddup4.autonav.module.splash;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddup4.autonav.R;
import com.ddup4.autonav.app.BaseFragment;
import com.ddup4.autonav.module.main.MainActivity;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.App;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.util.GrantResultUtil;
import com.okandroid.boot.util.IOUtil;
import com.okandroid.boot.util.ViewUtil;

/**
 * Created by idonans on 2017/2/3.
 */

public class SplashFragment extends BaseFragment<SplashViewProxy> implements SplashView {

    private static final int PERMISSION_CODE_ALL = 1;

    public static SplashFragment newInstance() {
        Bundle args = new Bundle();
        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected SplashViewProxy newDefaultViewProxy() {
        return new SplashViewProxy(this);
    }

    @Override
    public void requestAllPermission(String[] permissions) {
        requestPermissions(permissions, PERMISSION_CODE_ALL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (GrantResultUtil.isAllGranted(grantResults)) {
            ToastUtil.show("权限不足, 请到系统权限设置中允许所有需要权限才能正常使用");
        } else {
            SplashViewProxy proxy = getDefaultViewProxy();
            if (proxy != null) {
                proxy.onAllPermissionReady();
            }
        }
    }

    @Override
    public boolean directToMain() {
        Activity activity = getAvailableActivity();
        if (activity == null) {
            return false;
        }

        activity.startActivity(MainActivity.startIntent(activity));
        activity.finish();
        return true;
    }

    @Override
    protected void showInitLoadingContentView(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView) {
        // ignore
    }

    private Content mContent;

    @Override
    protected void showInitSuccessContentView(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView, @NonNull DynamicViewData dynamicViewData) {
        IOUtil.closeQuietly(mContent);
        mContent = new Content(activity, inflater, contentView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IOUtil.closeQuietly(mContent);
        mContent = null;
    }

    @Override
    public void onUpdateContentViewIfChanged() {
    }

    private class Content extends ContentViewHelper {

        private TextView mAppVersionName;

        private Content(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView) {
            super(activity, inflater, contentView, R.layout.ddup4_autonav_module_splash_view);

            mAppVersionName = ViewUtil.findViewByID(mRootView, R.id.app_version_name);
            mAppVersionName.setText("v " + App.getBuildConfigAdapter().getVersionName());
        }

    }

}
