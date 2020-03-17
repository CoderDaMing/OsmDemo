package org.osmdroid;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class CustomsOsmdroidMapConfig {
    //地图旋转
    private RotationGestureOverlay mRotationGestureOverlay;
    //比例尺
    private ScaleBarOverlay mScaleBarOverlay;
    //指南针方向
    private CompassOverlay mCompassOverlay = null;
    //设置导航图标的位置
    protected MyLocationNewOverlay mLocationOverlay;
    //是否地图旋转
    protected boolean mbRotation = false;

    public CustomsOsmdroidMapConfig() {
    }

    public void InitMapOverlays(MapView mapView, Activity context) {
        mapView.setDrawingCacheEnabled(true);
        mapView.setMaxZoomLevel(29.0);
        mapView.setMinZoomLevel(5.0);
        mapView.getController().setZoom(12.0);
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);// 触控放大缩小
        mapView.getOverlayManager().getTilesOverlay().setEnabled(true);
        //禁止自动出现放大，缩小的按钮 osmdroid 6.0以后才有的
        mapView.setBuiltInZoomControls(false);

        if (mbRotation) {
            //地图自由旋转
            mRotationGestureOverlay = new RotationGestureOverlay(mapView);
            mRotationGestureOverlay.setEnabled(true);
            mapView.getOverlays().add(this.mRotationGestureOverlay);
        }

        //比例尺配置
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setAlignBottom(true); //底部显示
        mScaleBarOverlay.setLineWidth(2);
        mScaleBarOverlay.setMaxLength(1.5F);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, dip2px(context, 60));
        mapView.getOverlays().add(this.mScaleBarOverlay);

        //指南针方向
        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(this.mCompassOverlay);

        //设置导航图标
        //自已重写了MyLocationNewOverlay
//        this.mLocationOverlay = new CustomsMyLocationOverlay(new GpsMyLocationProvider(context), mapView);
        mapView.getOverlays().add(this.mLocationOverlay);
        mLocationOverlay.enableMyLocation();
    }

    public MyLocationNewOverlay getmLocationOverlay() {
        return mLocationOverlay;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void onPause() {
        mCompassOverlay.disableCompass();
        mLocationOverlay.disableFollowLocation();
        mLocationOverlay.disableMyLocation();
        mScaleBarOverlay.enableScaleBar();
    }

    public void onResume() {
        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mScaleBarOverlay.disableScaleBar();
    }
}
