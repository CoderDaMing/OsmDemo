package com.ming.googlemap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;


/**
 * Gps相关工具类
 */

public class GpsUtils {
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isOPen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    /**
     * 此为跳转到位置设置
     */
    public static void openGpsSwitch(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //在安卓系统中，在非Acitivity中启动Activity，使用context.startAcitivityt()需要给Intent意图添加此标志：
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, 66);
        Toast.makeText(activity, activity.getString(R.string.change_phone_location_mode), Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查经纬度合法性
     *
     * @param latitude  [-90,90]
     * @param longitude [-180,180]
     * @return boolean
     */
    public static boolean checkLatLng(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * 经纬度都是0即定位失败
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public static boolean isLocationFailed(double latitude, double longitude) {
        return latitude == 0 && longitude == 0;
    }
}
