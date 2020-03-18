package com.ming.googlemap;

import android.os.Build;
import android.os.LocaleList;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

import java.util.Locale;

public class GoogleMapsTileSource extends TileSourceFactory {
    /**
     * url
     */
    private static final String[] URLS = new String[]{"http://mt0.google.com", "http://mt1.google.com", "http://mt2.google.com", "http://mt3.google.com"};

    /**
     * Names
     */
    public static final String[] NAMES = {"Google-Hybrid", "Google-Roads", "Google-Terrain-Hybrid"};

    /**
     * Params
     */
    private static final String HL_ZH = "zh-CN";
    private static final String HL_EN = "en-US";
    private static final String GL_CN = "cn";
    private static final String GL_EN = "en";

    private static Pair<String, String> getLocalePair() {
        //Android--获取当前系统的语言环境其代码如下：
        try {
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = LocaleList.getDefault().get(0);
            } else {
                locale = Locale.getDefault();
            }
            String language = locale.getLanguage();
            Log.d("SystemLanguageUtil ", language);
            if (language.startsWith("zh")) {
                //中文
                return new Pair<>(HL_ZH, GL_CN);
            } else {
                //英文
                return new Pair<>(HL_EN, GL_EN);
            }
        } catch (Exception e) {
            //英文
            return new Pair<>(HL_EN, GL_EN);
        }
    }

    /**
     * DisplayMetrics
     */
    private static final DisplayMetrics dm = OsmApplication.getInstance().getResources().getDisplayMetrics();

    //谷歌卫星混合
    private static final OnlineTileSourceBase GOOGLE_HYBRID = new XYTileSource(NAMES[0],
            0, 19, (int) (256.0f * dm.density), ".png", URLS) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Pair<String, String> localePair = getLocalePair();
            String hl = localePair.first;
            String gl = localePair.second;
            return getBaseUrl() + "/vt/lyrs=y&scale=" + dm.density + "&hl=" + hl + "&gl=" + gl + "&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌地图
    private static final OnlineTileSourceBase GOOGLE_ROADS = new XYTileSource(NAMES[1],
            0, 19, (int) (256.0f * dm.density), ".png", URLS) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Pair<String, String> localePair = getLocalePair();
            String hl = localePair.first;
            String gl = localePair.second;
            return getBaseUrl() + "/vt/lyrs=m&scale=" + dm.density + "&hl=" + hl + "&gl=" + gl + "&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    //谷歌地形带标注
    private static final OnlineTileSourceBase GOOGLE_TERRAIN_HYBRID = new XYTileSource(NAMES[2],
            0, 19, (int) (256.0f * dm.density), ".png", URLS) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            Pair<String, String> localePair = getLocalePair();
            String hl = localePair.first;
            String gl = localePair.second;
            return getBaseUrl() + "/vt/lyrs=p&scale=" + dm.density + "&hl=" + hl + "&gl=" + gl + "&src=app&x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex);
        }
    };

    /**
     * Sources
     */
    public static final ArrayMap<String, ITileSource> TILE_SOURCE_MAP = new ArrayMap<>();

    /**
     * Last Put Source in Map
     */
    static {
        TILE_SOURCE_MAP.put(TileSourceFactory.DEFAULT_TILE_SOURCE.name(), TileSourceFactory.DEFAULT_TILE_SOURCE);
        TILE_SOURCE_MAP.put(NAMES[0], GOOGLE_HYBRID);
        TILE_SOURCE_MAP.put(NAMES[1], GOOGLE_ROADS);
        TILE_SOURCE_MAP.put(NAMES[2], GOOGLE_TERRAIN_HYBRID);
    }
}
