package com.ming.googlemap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ArrayList<String> list = new ArrayList<>();
        list.add("Sample Google Map");
        ListView lv = findViewById(R.id.activitylist);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission_group.LOCATION,Manifest.permission_group.STORAGE},1);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case 0:
                this.startActivity(new Intent(this, GoogleMapActivity.class));
                break;
            default:
                break;
        }
    }

    public void onResume(){
        super.onResume();
        updateStorageInfo();
    }

    /**
     * refreshes the current osmdroid cache paths with user preferences plus soe logic to work around
     * file system permissions on api23 devices. it's primarily used for out android tests.
     * @param ctx
     * @return current cache size in bytes
     */
    public static long updateStoragePreferences(Context ctx){

        //loads the osmdroid config from the shared preferences object.
        //if this is the first time launching this app, all settings are set defaults with one exception,
        //the tile cache. the default is the largest write storage partition, which could end up being
        //this app's private storage, depending on device config and permissions

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //also note that our preference activity has the corresponding save method on the config object, but it can be called at any time.


        File dbFile = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
        if (dbFile.exists()) {
            return dbFile.length();
        }
        return -1;
    }


    /**
     * gets storage state and current cache size
     */
    private void updateStorageInfo(){

        long cacheSize = updateStoragePreferences(this);
        //cache management ends here

        TextView tv = findViewById(R.id.sdcardstate_value);
        final String state = Environment.getExternalStorageState();

        boolean mSdCardAvailable = Environment.MEDIA_MOUNTED.equals(state);
        tv.setText((mSdCardAvailable ? "Mounted" : "Not Available") );
        if (!mSdCardAvailable) {
            tv.setTextColor(Color.RED);
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv = findViewById(R.id.version_text);
        tv.setText(BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE);

        tv = findViewById(R.id.mainstorageInfo);
        tv.setText(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + "\n" +
                "Cache size: " + Formatter.formatFileSize(this,cacheSize));
    }
}
