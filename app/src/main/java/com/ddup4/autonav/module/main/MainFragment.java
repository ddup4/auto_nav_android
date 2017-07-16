package com.ddup4.autonav.module.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    @Override
    public void updateGpsInfo() {
        if (mContent != null) {
            mContent.updateGpsInfo();
        }
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

        public void updateGpsInfo() {
            mAMapNavi.stopNavi();

            MainViewProxy proxy = getDefaultViewProxy();
            if (proxy == null) {
                return;
            }

            MainViewProxy.ViewData viewData = (MainViewProxy.ViewData) proxy.getDynamicViewData();
            if (viewData == null) {
                return;
            }

            mGpsInfo = viewData.gpsInfo;
            if (!mNaviInitSuccess) {
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
            calculateWithCurrentGpsInfo();
        }

        private void calculateWithCurrentGpsInfo() {
            if (mGpsInfo == null) {
                Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo gps info is null");
                return;
            }

            Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo", mGpsInfo.phone, mGpsInfo.latitude, mGpsInfo.longtitude);
            mAMapNavi.calculateRideRoute(new NaviLatLng(mGpsInfo.latitude, mGpsInfo.longtitude));
        }

        @Override
        public void onStartNavi(int i) {

        }

        @Override
        public void onTrafficStatusUpdate() {

        }

        @Override
        public void onLocationChange(AMapNaviLocation location) {
            Log.v(CLASS_NAME, "onLocationChange", location.getCoord());
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
            syncCurrentGpsInfoView();

            // TODO
            // mAMapNavi.startNavi(NaviType.GPS);
            mAMapNavi.startNavi(NaviType.EMULATOR);
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
