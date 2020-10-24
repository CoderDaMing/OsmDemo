package com.ming.googlemap;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        MapType.MAPNIK,
        MapType.GOOGLE_HYBRID,
        MapType.GOOGLE_ROADS,
        MapType.GOOGLE_TERRAIN_HYBRID,
})

@Retention(RetentionPolicy.SOURCE)
public @interface MapType {
    String MAPNIK = "Mapnik";
    String GOOGLE_HYBRID = "Google-Hybrid";
    String GOOGLE_ROADS = "Google-Roads";
    String GOOGLE_TERRAIN_HYBRID = "Google-Terrain-Hybrid";
}
