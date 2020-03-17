package org.osmdroid.samplefragments.tilesources;

import android.util.Log;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

/**
 * 为了保密谷歌.cn使用的是GCJ02（火星坐标系），手机gps数据是WGS-84坐标系，需要进行转换。
 */
public class GoogleMapsTileSource extends TileSourceFactory {
    private static final String[] URLS_COM = new String[]{
            "http://mt0.google.com",
            "http://mt1.google.com",
            "http://mt2.google.com",
            "http://mt3.google.com",

    };

    private static final String[] URLS_CN = new String[]{
            "http://mt0.google.com",
            "http://mt1.google.com",
            "http://mt2.google.com",
            "http://mt3.google.com",

    };
    //谷歌卫星混合
    public static final OnlineTileSourceBase GOOGLE_HYBRID = new XYTileSource("Google-Hybrid",
            0, 19, 512, ".png", URLS_COM) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Log.d("url", getBaseUrl() + "/vt/lyrs=y&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex));
            return getBaseUrl() + "/vt/lyrs=y&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌卫星
    public static final OnlineTileSourceBase GOOGLE_SAT = new XYTileSource("Google-Sat",
            0, 19, 512, ".png", URLS_COM) {
        @Override
        public String getTileURLString(long pMapTileIndex) {

            return getBaseUrl() + "/vt/lyrs=s&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);

        }
    };

    //谷歌地图
    public static final OnlineTileSourceBase GOOGLE_ROADS = new XYTileSource("Google-Roads",
            0, 18, 512, ".png", URLS_COM) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=m&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌地形
    public static final OnlineTileSourceBase GOOGLE_TERRAIN = new XYTileSource("Google-Terrain",
            0, 16, 512, ".png", URLS_COM) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=t&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌地形带标注
    public static final OnlineTileSourceBase GOOGLE_TERRAIN_HYBRID = new XYTileSource("Google-Terrain-Hybrid",
            0, 16, 512, ".png", URLS_COM) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "/vt/lyrs=p&scale=2&hl=zh-CN&gl=CN&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };
}
