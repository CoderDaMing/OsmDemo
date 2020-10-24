package com.ming.googlemap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class OfflineMapActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "OfflineMapActivity";
    private static final String MAP_FRAGMENT_TAG = "org.osmdroid.offline_map_fragment_tag";

    public static final String PARAMS_OFFLINE = "offline";
    public static final String PARAMS_FUNCTION = "function";
    public static final int FUNCTION_SHOW = 0;
    public static final int FUNCTION_ADD = 1;

    private Offline offline = null;
    private int mapFunction = FUNCTION_SHOW;
    private GoogleMapFragment googleMapFragment;
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
                if (mapFunction == FUNCTION_ADD) {
                    Log.e(TAG, "networkReceiver intent = [" + intent + "]");
                    googleMapFragment.invalidateMapView();
                }
            } catch (NullPointerException e) {
                // lazy handling of an improbable NPE
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Intent intent = getIntent();
        if (intent != null) {
            offline = intent.getParcelableExtra(PARAMS_OFFLINE);
            mapFunction = intent.getIntExtra(PARAMS_FUNCTION, FUNCTION_SHOW);
        }

        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
            Bundle args = new Bundle();
            if (offline != null) {
                offline.setUseDataConnection(mapFunction == FUNCTION_ADD);
            }
            args.putParcelable(PARAMS_OFFLINE, offline);
            googleMapFragment = GoogleMapFragment.newInstance(args);
            fm.beginTransaction().add(R.id.offline_map_container, googleMapFragment, MAP_FRAGMENT_TAG).commit();
        }

        //title
        TextView textTitle = findViewById(R.id.tv_back_title);
        textTitle.setText(getString(R.string.activity_offline_map));
        findViewById(R.id.fl_back_title).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_back_title:
                finish();
                break;
            default:
                break;
        }
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
