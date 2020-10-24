package com.ming.googlemap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;

import androidx.annotation.NonNull;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapUtil {
    public static double calculateLineDistance(GeoPoint latLng1, GeoPoint latLng2) {
        return latLng1.distanceToAsDouble(latLng2);
    }

    public static Bitmap crateBitmapByView(View view) {
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(measureSpec, measureSpec);

        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();

        view.layout(0, 0, measuredWidth, measuredHeight);
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 根据中心点和自定义内容获取缩放bounds
     *
     * @param centerpoint
     * @param pointList
     * @return
     */
    public static BoundingBox getLatLngBounds(GeoPoint centerpoint, List<GeoPoint> pointList) {
        if (centerpoint != null) {
            for (int i = 0; i < pointList.size(); i++) {
                GeoPoint p = pointList.get(i);
                double boungsLat = (centerpoint.getLatitude() * 2) - p.getLatitude();
                double boungsLon = (centerpoint.getLongitude() * 2) - p.getLongitude();
                if (GpsUtils.checkLatLng(boungsLat, boungsLon)) {
                    GeoPoint p1 = new GeoPoint(boungsLat, boungsLon);
                    pointList.add(p1);
                }
            }
        }
        return BoundingBox.fromGeoPointsSafe(pointList);
    }

    /**
     * 根据自定义内容获取缩放bounds
     */
    public static BoundingBox getLatLngBounds(List<GeoPoint> pointList) {

        return BoundingBox.fromGeoPointsSafe(pointList);
    }


    public static BoundingBox getLatLngBoundsByPointList(MapView mapView, List<Point> pointList) {

        IGeoPoint gp1 = fromPixels(mapView, pointList.get(0));
        IGeoPoint gp2 = fromPixels(mapView, pointList.get(1));
        return new BoundingBox(gp1.getLatitude(), gp2.getLongitude(), gp2.getLatitude(), gp1.getLongitude());
    }

    /**
     * 获得自己周围随机位置
     *
     * @param lat   纬度
     * @param lng   经度
     * @param meter 距离
     */
    public static GeoPoint getRandomNeaybyLatLng(double lat, double lng, int meter) {
        double distance = new Random().nextInt(meter);
        double angle = new Random().nextInt(360);
        return getLatLngByDistanceAngle(lng, lat, distance, angle);
    }

    /**
     * 根据提供经纬度返回目标经纬度
     *
     * @param GLON
     * @param GLAT
     * @param distance 距离
     * @param angle    顺时针角度
     * @return
     */
    private static GeoPoint getLatLngByDistanceAngle(double GLON, double GLAT, double distance, double angle) {
        double Ea = 6378137;     //   赤道半径
        double Eb = 6356725;     //   极半径
        double dx = distance * Math.sin(angle * Math.PI / 180.0);
        double dy = distance * Math.cos(angle * Math.PI / 180.0);
        //double ec = 6356725 + 21412 * (90.0 - GLAT) / 90.0;
        // 21412 是赤道半径与极半径的差
        double ec = Eb + (Ea - Eb) * (90.0 - GLAT) / 90.0;
        double ed = ec * Math.cos(GLAT * Math.PI / 180);
        double newLon = (dx / ed + GLON * Math.PI / 180.0) * 180.0 / Math.PI;
        double newLat = (dy / ec + GLAT * Math.PI / 180.0) * 180.0 / Math.PI;
        return new GeoPoint(newLat, newLon);
    }

    /**
     * 将经纬度格式换一下显示在banner上
     *
     * @param latitude
     * @param longitude
     * @return 39°59'38" N   116°28'30" E
     */
    public static String locationToNesw(double longitude, double latitude) {
        String lat;
        String lon;
        if (latitude >= 0) {
            lat = "N";
        } else {
            lat = "S";
            latitude = -latitude;
        }
        if (longitude >= 0) {
            lon = "E";
        } else {
            lon = "W";
            longitude = -longitude;
        }
        int degree1 = (int) latitude;
        int min1 = (int) ((latitude - degree1) * 60);
        int sec1 = (int) ((latitude - degree1) * 3600 - min1 * 60);
        String end1 = degree1 + "°" + min1 + "′" + sec1 + "″ " + lat;

        int degree2 = (int) longitude;
        int min2 = (int) ((longitude - degree2) * 60);
        int sec2 = (int) ((longitude - degree2) * 3600 - min2 * 60);
        String end2 = degree2 + "°" + min2 + "′" + sec2 + "″ " + lon;

        return end1 + "  " + end2;
    }

    public static IGeoPoint fromPixels(MapView mapView, @NonNull Point point) {
        return mapView.getProjection().fromPixels(point.x, point.y);
    }

    public static Point toPixels(MapView mapView, @NonNull IGeoPoint point) {
        return mapView.getProjection().toPixels(point, null);
    }


}
