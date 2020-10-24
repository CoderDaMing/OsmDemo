package com.ming.googlemap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        final ArrayList<String> list = new ArrayList<>();
        list.add("Sample OfflineMap List ");
        ListView lv = findViewById(R.id.activitylist);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case 0:
                this.startActivity(new Intent(this, OfflineListActivity.class));
                break;
            default:
                break;
        }
    }

    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            checkPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
            updateStorageInfo();
        }
    }

    /**
     * 检查路径
     *
     * @param url
     */
    public static void checkPath(String url) {
        Log.e(TAG, "checkPath: url " + url);
        File dir = new File(url);
        if (!dir.exists()) {
            Log.e(TAG, " checkPath: path 文件不存在创建新文件");
            boolean isSuccess = dir.mkdirs();//文件不存在，则创建文件
            if (!isSuccess) {//没有创建成在创建一次
                dir.mkdirs();
            }
        } else {
            Log.e(TAG, "checkPath: path 文件夹存在");
        }
    }

    /**
     * refreshes the current osmdroid cache paths with user preferences plus soe logic to work around
     * file system permissions on api23 devices. it's primarily used for out android tests.
     *
     * @param ctx
     * @return current cache size in bytes
     */
    public static long updateStoragePreferences(Context ctx) {

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
    private void updateStorageInfo() {

        long cacheSize = updateStoragePreferences(this);
        //cache management ends here

        TextView tv = findViewById(R.id.sdcardstate_value);
        final String state = Environment.getExternalStorageState();

        boolean mSdCardAvailable = Environment.MEDIA_MOUNTED.equals(state);
        tv.setText((mSdCardAvailable ? "Mounted" : "Not Available"));
        if (!mSdCardAvailable) {
            tv.setTextColor(Color.RED);
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv = findViewById(R.id.version_text);
        tv.setText(BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE);

        tv = findViewById(R.id.mainstorageInfo);
        tv.setText(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + "\n" +
                "Cache size: " + Formatter.formatFileSize(this, cacheSize));
    }
}
