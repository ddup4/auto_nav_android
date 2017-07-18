package com.ddup4.autonav.module.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.ddup4.autonav.R;
import com.ddup4.autonav.api.entity.GpsInfo;
import com.ddup4.autonav.app.BaseFragment;
import com.ddup4.autonav.ext.HostSettingsDialog;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.AppContext;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.lang.ClassName;
import com.okandroid.boot.lang.Log;
import com.okandroid.boot.lang.ResumedViewClickListener;
import com.okandroid.boot.util.IOUtil;
import com.okandroid.boot.util.ViewUtil;

import java.io.IOException;

/**
 * Created by idonans on 2017/2/3.
 */

public class MainFragment extends BaseFragment<MainViewProxy> implements MainView {

    private final String CLASS_NAME = ClassName.valueOf(this);

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected MainViewProxy newDefaultViewProxy() {
        return new MainViewProxy(this);
    }

    private Content mContent;

    @Override
    protected void showInitSuccessContentView(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView, @NonNull DynamicViewData dynamicViewData) {
        IOUtil.closeQuietly(mContent);
        mContent = new Content(activity, inflater, contentView);
    }

    @Override
    public void showStartNavConfirm(final GpsInfo gpsInfo) {
        new AlertDialog.Builder(getActivity())
                .setMessage("读取到目标(" + gpsInfo.phone + ")位置信息" + "\n"
                        + "(" + gpsInfo.latitude + "," + gpsInfo.longtitude + ")\n"
                        + "是否开始导航?")
                .setPositiveButton("开始导航", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mContent != null) {
                            mContent.updateGpsInfo(gpsInfo);
                        }
                    }
                })
                .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ignore
                    }
                })
                .show();
    }

    @Override
    public void showReadGpsInfoConfirm(final String phone) {
        new AlertDialog.Builder(getActivity())
                .setMessage("监听到来电 " + phone + "\n" + "是否读取该电话的位置信息?")
                .setPositiveButton("读取信息", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainViewProxy proxy = getDefaultViewProxy();
                        if (proxy != null) {
                            proxy.readGpsInfoFromServer(phone);
                        }
                    }
                })
                .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ignore
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mContent != null) {
            mContent.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mContent != null) {
            mContent.onPause();
        }
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

    private class Content extends ContentViewHelper implements AMapNaviListener, AMapNaviViewListener {

        protected AMapNaviView mAMapNaviView;
        protected AMapNavi mAMapNavi;

        @Nullable
        private GpsInfo mGpsInfo;
        private boolean mNaviInitSuccess;

        private View mHostSetting;
        private TextView mGpsInfoNaviView;

        private Content(@NonNull Activity activity, @NonNull LayoutInflater inflater, @NonNull ViewGroup contentView) {
            super(activity, inflater, contentView, R.layout.ddup4_autonav_module_main_view);
            mGpsInfoNaviView = ViewUtil.findViewByID(mRootView, R.id.current_gps_info);
            mHostSetting = ViewUtil.findViewByID(mRootView, R.id.host_setting);
            mHostSetting.setOnClickListener(new ResumedViewClickListener(MainFragment.this) {
                @Override
                public void onClick(View v, ResumedViewClickListener listener) {
                    showHostSettingsDialog();
                }
            });

            mAMapNavi = AMapNavi.getInstance(AppContext.getContext());
            mAMapNavi.addAMapNaviListener(this);

            mAMapNaviView = ViewUtil.findViewByID(mRootView, R.id.navi_view);
            mAMapNaviView.onCreate(null);
            mAMapNaviView.setAMapNaviViewListener(this);

            AMapNaviViewOptions options = mAMapNaviView.getViewOptions();
            options.setSettingMenuEnabled(true);
            mAMapNaviView.setViewOptions(options);
        }

        private void syncCurrentGpsInfoView() {
            String text = null;
            if (mGpsInfo != null) {
                if (!TextUtils.isEmpty(mGpsInfo.phone)) {
                    text = "目标用户\n" + mGpsInfo.phone;
                }
            }
            mGpsInfoNaviView.setText(text);
        }

        public void updateGpsInfo(GpsInfo gpsInfo) {
            mAMapNavi.stopNavi();

            mGpsInfo = gpsInfo;
            if (!mNaviInitSuccess) {
                ToastUtil.show("等待导航地图初始化完成");
                return;
            }

            calculateWithCurrentGpsInfo();
        }

        private void showHostSettingsDialog() {
            Activity activity = getAvailableActivity();
            if (activity == null) {
                return;
            }

            new HostSettingsDialog(activity).show();
        }

        protected void onResume() {
            mAMapNaviView.onResume();
        }

        protected void onPause() {
            mAMapNaviView.onPause();
        }

        @Override
        public void close() throws IOException {
            super.close();

            mAMapNaviView.onDestroy();
            mAMapNavi.stopNavi();
            mAMapNavi.destroy();
        }

        @Override
        public void onInitNaviFailure() {

        }

        @Override
        public void onInitNaviSuccess() {
            Log.v(CLASS_NAME, "onInitNaviSuccess");
            mNaviInitSuccess = true;

            // 切换到当前位置
            NaviLatLng lastLocation = getLastLocation();
            if (lastLocation != null) {
                // TODO
            }

            calculateWithCurrentGpsInfo();
        }

        private void calculateWithCurrentGpsInfo() {
            if (mGpsInfo == null) {
                Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo gps info is null");
                return;
            }

            ToastUtil.show("开始计算导航路径");

            Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo", mGpsInfo.phone, mGpsInfo.latitude, mGpsInfo.longtitude);

            NaviLatLng currentLocation = getLastLocation();
            if (currentLocation == null) {
                ToastUtil.show("当前位置信息不足, 使用系统默认设置");
                mAMapNavi.calculateRideRoute(new NaviLatLng(mGpsInfo.latitude, mGpsInfo.longtitude));
            } else {
                mAMapNavi.calculateRideRoute(currentLocation, new NaviLatLng(mGpsInfo.latitude, mGpsInfo.longtitude));
            }
        }

        private NaviLatLng getLastLocation() {
            if (mLastNaviLocation != null) {
                return mLastNaviLocation.getCoord();
            }
            AMapLocation location = ((MainActivity) getActivity()).getLastLocation();
            return new NaviLatLng(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStartNavi(int i) {

        }

        @Override
        public void onTrafficStatusUpdate() {

        }

        private AMapNaviLocation mLastNaviLocation;

        @Override
        public void onLocationChange(AMapNaviLocation location) {
            Log.v(CLASS_NAME, "onLocationChange", location.getCoord());
            mLastNaviLocation = location;
        }

        @Override
        public void onGetNavigationText(int i, String s) {

        }

        @Override
        public void onEndEmulatorNavi() {
            Log.v(CLASS_NAME, "onEndEmulatorNavi");
        }

        @Override
        public void onArriveDestination() {
            Log.v(CLASS_NAME, "onArriveDestination");
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            ToastUtil.show("导航路径计算失败(" + i + ")");
        }

        @Override
        public void onReCalculateRouteForYaw() {

        }

        @Override
        public void onReCalculateRouteForTrafficJam() {

        }

        @Override
        public void onArrivedWayPoint(int i) {
            Log.v(CLASS_NAME, "onArrivedWayPoint", i);
        }

        @Override
        public void onGpsOpenStatus(boolean b) {

        }

        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {

        }

        @Override
        public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
            Log.v(CLASS_NAME, "onNaviInfoUpdated");
        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

        }

        @Override
        public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {

        }

        @Override
        public void hideCross() {

        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

        }

        @Override
        public void hideLaneInfo() {

        }

        @Override
        public void onCalculateRouteSuccess(int[] ints) {
            ToastUtil.show("导航路径计算成功, 开始 gps 导航");
            Log.v(CLASS_NAME, "onCalculateRouteSuccess");
            syncCurrentGpsInfoView();

            mAMapNavi.startNavi(NaviType.GPS);
        }

        @Override
        public void notifyParallelRoad(int i) {

        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

        }

        @Override
        public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

        }

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

        }

        @Override
        public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

        }

        @Override
        public void onPlayRing(int i) {

        }

        @Override
        public void onNaviSetting() {
        }

        @Override
        public void onNaviCancel() {
            finishActivity();
        }

        @Override
        public boolean onNaviBackClick() {
            return false;
        }

        @Override
        public void onNaviMapMode(int i) {
        }

        @Override
        public void onNaviTurnClick() {
        }

        @Override
        public void onNextRoadClick() {
        }

        @Override
        public void onScanViewButtonClick() {
        }

        @Override
        public void onLockMap(boolean b) {
        }

        @Override
        public void onNaviViewLoaded() {
        }

    }

}
