package com.ddup4.autonav.module.splash;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.ddup4.autonav.R;
import com.ddup4.autonav.app.BaseFragment;
import com.ddup4.autonav.module.main.MainActivity;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.App;
import com.okandroid.boot.AppContext;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.lang.ClassName;
import com.okandroid.boot.lang.Log;
import com.okandroid.boot.thread.Threads;
import com.okandroid.boot.util.GrantResultUtil;
import com.okandroid.boot.util.IOUtil;
import com.okandroid.boot.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idonans on 2017/2/3.
 */

public class SplashFragment extends BaseFragment<SplashViewProxy> implements SplashView {

    private final String CLASS_NAME = ClassName.valueOf(this);
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
        List<String> missPermission = new ArrayList<>();
        for (String permission : permissions) {
            if (GrantResultUtil.isGranted(ContextCompat.checkSelfPermission(AppContext.getContext(), permission))) {
                continue;
            }

            missPermission.add(permission);
        }

        if (missPermission.size() > 0) {
            requestPermissions(missPermission.toArray(new String[missPermission.size()]), PERMISSION_CODE_ALL);
        } else {
            // all permission granted
            SplashViewProxy proxy = getDefaultViewProxy();
            if (proxy != null) {
                proxy.onAllPermissionReady();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!GrantResultUtil.isAllGranted(grantResults)) {
            ToastUtil.show("权限不足, 请到系统权限设置中允许所有需要权限才能正常使用");
            finishActivity();
        } else {
            SplashViewProxy proxy = getDefaultViewProxy();
            if (proxy != null) {
                proxy.onAllPermissionReady();
            }
        }
    }

    @Override
    public void requestLocation() {
        if (mContent != null) {
            mContent.startRequestLocation();
        }
    }

    @Override
    public boolean directToMain(AMapLocation aMapLocation) {
        Activity activity = getAvailableActivity();
        if (activity == null) {
            return false;
        }

        ToastUtil.show("位置定位成功");

        activity.startActivity(MainActivity.startIntent(activity, aMapLocation));
        finishActivity();
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

        //声明AMapLocationClient类对象
        public AMapLocationClient mLocationClient;
        //声明定位回调监听器
        public AMapLocationListener mLocationListener;
        //声明AMapLocationClientOption对象
        public AMapLocationClientOption mLocationOption;

        private TextView mAppVersionName;

        private Content(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView) {
            super(activity, inflater, contentView, R.layout.ddup4_autonav_module_splash_view);

            mAppVersionName = ViewUtil.findViewByID(mRootView, R.id.app_version_name);
            mAppVersionName.setText("v " + App.getBuildConfigAdapter().getVersionName());

        }

        public void startRequestLocation() {
            ToastUtil.show("正在定位您的位置");
            //初始化定位
            mLocationClient = new AMapLocationClient(AppContext.getContext());
            mLocationListener = new AMapLocationListener() {
                @Override
                public void onLocationChanged(final AMapLocation aMapLocation) {
                    Log.v(CLASS_NAME, "onLocationChanged", aMapLocation);
                    if (aMapLocation == null) {
                        return;
                    }
                    Threads.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            SplashViewProxy proxy = getDefaultViewProxy();
                            if (proxy != null) {
                                proxy.onCurrentLocationFound(aMapLocation);
                            }
                        }
                    });
                }
            };
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);

            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //启动定位
            mLocationClient.startLocation();
        }

    }

}
