package com.ming.googlemap;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class OsmApplication extends MultiDexApplication {
    private static OsmApplication instance;

    public static OsmApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //https://github.com/osmdroid/osmdroid/issues/366

        //super important. Many tile servers, including open street maps, will BAN applications by user
        //agent. Do not use the sample application's user agent for your app! Use your own setting, such
        //as the app id.
//        Configuration.getInstance().setUserAgentValue(getPackageName());
//        BingMapTileSource.retrieveBingKey(this);
//        final BingMapTileSource source = new BingMapTileSource(null);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                source.initMetaData();
//            }
//        }).start();
//        source.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
//        TileSourceFactory.addTileSource(source);
//
//        final BingMapTileSource source2 = new BingMapTileSource(null);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                source2.initMetaData();
//            }
//        }).start();
//        source2.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
//        TileSourceFactory.addTileSource(source2);
//
//
//
//        //FIXME need a key for this TileSourceFactory.addTileSource(TileSourceFactory.CLOUDMADESMALLTILES);
//
//        //FIXME need a key for this TileSourceFactory.addTileSource(TileSourceFactory.CLOUDMADESTANDARDTILES);
//
//
//        //the sample app a few additional tile sources that we have api keys for, so add them here
//        //this will automatically show up in the tile source list
//        //FIXME this key is expired TileSourceFactory.addTileSource(new HEREWeGoTileSource(getApplicationContext()));
//        TileSourceFactory.addTileSource(new MapBoxTileSource(getApplicationContext()));
//        TileSourceFactory.addTileSource(new MapQuestTileSource(getApplicationContext()));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
