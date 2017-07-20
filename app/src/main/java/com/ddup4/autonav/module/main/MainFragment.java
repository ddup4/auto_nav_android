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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.ddup4.autonav.R;
import com.ddup4.autonav.api.entity.GpsInfo;
import com.ddup4.autonav.app.BaseFragment;
import com.ddup4.autonav.ext.HostSettingsDialog;
import com.ddup4.autonav.util.ToastUtil;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.lang.ClassName;
import com.okandroid.boot.lang.Log;
import com.okandroid.boot.lang.ResumedViewClickListener;
import com.okandroid.boot.util.IOUtil;
import com.okandroid.boot.util.ViewUtil;

import java.io.IOException;

import overlay.DrivingRouteOverlay;

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

    private class Content extends ContentViewHelper implements LocationSource, AMapLocationListener, RouteSearch.OnRouteSearchListener {

        protected FrameLayout mMapViewPan;
        protected MapView mMapView;
        private AMap mAMap;

        protected AMapLocationClient mAMapLocationClient;
        private RouteSearch mRouteSearch;
        private DriveRouteResult mDriveRouteResult;

        @Nullable
        private GpsInfo mGpsInfo;

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

            mMapViewPan = ViewUtil.findViewByID(mRootView, R.id.map_view_pan);

            AMapOptions options = new AMapOptions();
            options.camera(new CameraPosition(getInitLocation(), 10f, 0, 0));
            mMapView = new MapView(activity, options);
            mMapView.onCreate(null);

            mAMap = mMapView.getMap();
            mAMap.setLocationSource(this);
            mAMap.getUiSettings().setMyLocationButtonEnabled(true);
            mAMap.setMyLocationEnabled(true);

            mMapViewPan.addView(mMapView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            mAMapLocationClient = new AMapLocationClient(activity);
            AMapLocationClientOption locationClientOption = new AMapLocationClientOption();
            locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mAMapLocationClient.setLocationOption(locationClientOption);
            mAMapLocationClient.setLocationListener(this);
            mAMapLocationClient.startLocation();

            mRouteSearch = new RouteSearch(activity);
            mRouteSearch.setRouteSearchListener(this);
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
            mGpsInfo = gpsInfo;
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
            mMapView.onResume();
        }

        protected void onPause() {
            mMapView.onPause();
        }

        @Override
        public void close() throws IOException {
            super.close();

            mMapView.onDestroy();
        }

        private void calculateWithCurrentGpsInfo() {
            if (mGpsInfo == null) {
                Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo gps info is null");
                return;
            }

            ToastUtil.show("开始计算导航路径");

            Log.v(CLASS_NAME, "calculateWithCurrentGpsInfo", mGpsInfo.phone, mGpsInfo.latitude, mGpsInfo.longtitude);

            LatLng startLocation = getRecentLocation();
            LatLng endLocation = new LatLng(mGpsInfo.latitude, mGpsInfo.longtitude);
            RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(startLocation.latitude, startLocation.longitude),
                    new LatLonPoint(endLocation.latitude, endLocation.longitude));

            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_SINGLE_DEFAULT, null, null, "");
            mRouteSearch.calculateDriveRouteAsyn(query);
        }

        private LatLng getRecentLocation() {
            if (mLastLocation != null) {
                return new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }
            return getInitLocation();
        }

        private LatLng getInitLocation() {
            AMapLocation location = ((MainActivity) getActivity()).getLastLocation();
            return new LatLng(location.getLatitude(), location.getLongitude());
        }


        private AMapLocation mLastLocation;
        private OnLocationChangedListener mLocationChangedListener;

        @Override
        public synchronized void activate(OnLocationChangedListener onLocationChangedListener) {
            mLocationChangedListener = onLocationChangedListener;
            syncLocation();
        }

        @Override
        public synchronized void deactivate() {
            mLocationChangedListener = null;
        }

        @Override
        public synchronized void onLocationChanged(AMapLocation aMapLocation) {
            mLastLocation = aMapLocation;
            syncLocation();
        }

        private synchronized void syncLocation() {
            if (mLocationChangedListener != null && mLastLocation != null) {
                mLocationChangedListener.onLocationChanged(mLastLocation);
            }
        }

        @Override
        public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

        }

        @Override
        public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
            mAMap.clear();// 清理地图上的所有覆盖物

            if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                if (result != null && result.getPaths() != null) {
                    if (result.getPaths().size() > 0) {
                        mDriveRouteResult = result;
                        final DrivePath drivePath = mDriveRouteResult.getPaths()
                                .get(0);
                        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                                getActivity(), mAMap, drivePath,
                                mDriveRouteResult.getStartPos(),
                                mDriveRouteResult.getTargetPos(), null);
                        drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                        drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                        drivingRouteOverlay.removeFromMap();
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                        // mBottomLayout.setVisibility(View.VISIBLE);
                        int dis = (int) drivePath.getDistance();
                        int dur = (int) drivePath.getDuration();

                        syncCurrentGpsInfoView();

                        // String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                        // mRotueTimeDes.setText(des);
                        // mRouteDetailDes.setVisibility(View.VISIBLE);
                        // int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                        // mRouteDetailDes.setText("打车约" + taxiCost + "元");
                        /*
                        mBottomLayout.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext,
                                        DriveRouteDetailActivity.class);
                                intent.putExtra("drive_path", drivePath);
                                intent.putExtra("drive_result",
                                        mDriveRouteResult);
                                startActivity(intent);
                            }
                        });
                        */

                    } else if (result != null && result.getPaths() == null) {
                        ToastUtil.show("没有可用的路线");
                    }
                } else {
                    ToastUtil.show("没有可用的导航路线");
                }
            } else {
                ToastUtil.show("导航路线计算失败，请稍后再试");
            }
        }

        @Override
        public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

        }

        @Override
        public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

        }
    }

}
