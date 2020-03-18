package com.ming.googlemap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class GoogleMapActivity extends AppCompatActivity {
    private static final String TAG = "GoogleMapActivity";
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.GOOGLE_MAP_FRAGMENT_TAG";
    /**
     * The idea behind that is to force a MapView refresh when switching from offline to online.
     * If you don't do that, the map may display - when online - approximated tiles
     * * that were computed when offline
     * * that could be replaced by downloaded tiles
     * * but as the display is not refreshed there's no try to get better tiles
     *
     * @since 6.0
     */
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.e(TAG, "networkReceiver intent = [" + intent + "]");
                googleMapFragment.invalidateMapView();
            } catch (NullPointerException e) {
                // lazy handling of an improbable NPE
            }
        }
    };

    private GoogleMapFragment googleMapFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        MainActivity.updateStoragePreferences(this);    //needed for unit tests

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
            googleMapFragment = GoogleMapFragment.newInstance();
            fm.beginTransaction().add(R.id.google_map_container, googleMapFragment, MAP_FRAGMENT_TAG).commit();
        }

        findViewById(R.id.btn_change_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapActivity.this)
                        .setTitle("切换google图源")
                        .setItems(GoogleMapsTileSource.NAMES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                googleMapFragment.setTileSource(GoogleMapsTileSource.NAMES[i]);
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * small example of keyboard events on the mapview
     * page up = zoom out
     * page down = zoom in
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_PAGE_DOWN:
                googleMapFragment.zoomIn();
                return true;
            case KeyEvent.KEYCODE_PAGE_UP:
                googleMapFragment.zoomOut();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * @since 6.0
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }
}
