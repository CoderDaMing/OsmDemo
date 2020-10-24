package com.ming.googlemap;

import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class GoogleMapFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher {
    private static final String TAG = "GoogleMapFragment";

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String PREFS_NAME = "com.ming.googlemap.prefs";
    private static final String PREFS_TILE_SOURCE = "tilesource";
    private static final String PREFS_LATITUDE_STRING = "latitudeString";
    private static final String PREFS_LONGITUDE_STRING = "longitudeString";
    private static final String PREFS_ORIENTATION = "orientation";
    private static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";

    // ===========================================================
    // Fields
    // ===========================================================
    private SharedPreferences mPrefs;
    private MapView mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay = null;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;

    // ===========================================================
    // OfflineMap
    // ===========================================================
    private boolean useDataConnection = true;
    private MaskLayer maskLayer;
    private Button btnChangeSource;
    private Button btnDownLoad;
    private Button executeJob;
    private SeekBar zoom_min;
    private SeekBar zoom_max;
    private EditText cache_north, cache_south, cache_east, cache_west, cache_output;
    private TextView cache_estimate;
    private CacheManager mgr = null;
    private AlertDialog downloadPrompt = null;
    private AlertDialog alertDialog = null;
    private SqliteArchiveTileWriter writer = null;

    public static GoogleMapFragment newInstance(Bundle args) {
        GoogleMapFragment fragment = new GoogleMapFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_google_map, container, false);

        maskLayer = contentView.findViewById(R.id.mask_layer);
        btnChangeSource = contentView.findViewById(R.id.btn_change_source);
        btnChangeSource.setOnClickListener(this);
        btnDownLoad = contentView.findViewById(R.id.btn_download);
        btnDownLoad.setOnClickListener(this);

        mMapView = contentView.findViewById(R.id.map_view);
        mMapView.setDestroyMode(false);

        mMapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            /**
             * mouse wheel zooming ftw
             * http://stackoverflow.com/questions/11024809/how-can-my-view-respond-to-a-mousewheel
             * @param v
             * @param event
             * @return
             */
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_SCROLL:
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMapView.getController().zoomOut();
                            else {
                                //this part just centers the map on the current mouse location before the zoom action occurs
                                IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                                mMapView.getController().animateTo(iGeoPoint);
                                mMapView.getController().zoomIn();
                            }
                            return true;
                    }
                }
                return false;
            }
        });
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMap();

        //接收传递参数
        Bundle arguments = getArguments();
        if (arguments != null) {
            Offline offline = arguments.getParcelable(OfflineMapActivity.PARAMS_OFFLINE);
            if (offline != null) {
                useDataConnection = offline.isUseDataConnection();
                mMapView.setUseDataConnection(useDataConnection);
                if (!useDataConnection) {
                    Log.e(TAG, "useDataConnection is false");
                    btnChangeSource.setVisibility(View.GONE);
                    btnDownLoad.setVisibility(View.GONE);
                    maskLayer.setVisibility(View.GONE);
                    //offline
                    mMapView.setMinZoomLevel(offline.getZoomMin());
                    mMapView.setMaxZoomLevel(offline.getZoomMax());
                    mMapView.getController().setZoom(offline.getZoomMin() + 1);
                    BoundingBox boundingBox = new BoundingBox(offline.getNorth(), offline.getEast(), offline.getSouth(), offline.getWest());
                    mMapView.setExpectedCenter(new GeoPoint(boundingBox.getCenterLatitude(), boundingBox.getCenterLongitude()));
                    offlineShowSqlite(new File(offline.getTxtPath()));
                }
            }
        }
    }

    private void initMap() {

        final Context context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //My Location
        //note you have handle the permissions yourself, the overlay did not do it for you
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
        mLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(this.mLocationOverlay);

        //On screen compass
        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(this.mCompassOverlay);


        //map scale
        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mMapView.getOverlays().add(this.mScaleBarOverlay);


        //support for map rotation
        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(this.mRotationGestureOverlay);


        //needed for pinch zooms
        mMapView.setMultiTouchControls(true);

        //scales tiles to the current screen's DPI, helps with readability of labels
        mMapView.setTilesScaledToDpi(true);

        //禁止自动出现放大，缩小的按钮 osmdroid 6.0以后才有的
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        if (useDataConnection) {
            mMapView.setMinZoomLevel(1.5d);
            mMapView.setMaxZoomLevel(19.0d);
            //the rest of this is restoring the last map location the user looked at
            final float zoomLevel = mPrefs.getFloat(PREFS_ZOOM_LEVEL_DOUBLE, 1);
            mMapView.getController().setZoom(zoomLevel);
//        final float orientation = mPrefs.getFloat(PREFS_ORIENTATION, 0);
//        mMapView.setMapOrientation(orientation, false);
            final String latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, "39.907475d");
            final String longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, "116.391262d");
            final double latitude = Double.parseDouble(latitudeString);
            final double longitude = Double.parseDouble(longitudeString);
            mMapView.setExpectedCenter(new GeoPoint(latitude, longitude));
        }
    }

    @Override
    public void onPause() {
        //save the current location
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
        edit.putFloat(PREFS_ORIENTATION, mMapView.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(mMapView.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(mMapView.getMapCenter().getLongitude()));
        edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, (float) mMapView.getZoomLevelDouble());
        edit.commit();

        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //this part terminates all of the overlays and background threads for osmdroid
        //only needed when you programmatically create the map
        mMapView.onDetach();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (useDataConnection) {
            final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
                    TileSourceFactory.DEFAULT_TILE_SOURCE.name());
            setTileSource(tileSourceName);
        }
        mMapView.onResume();
    }

    public void zoomIn() {
        mMapView.getController().zoomIn();
    }

    public void zoomOut() {
        mMapView.getController().zoomOut();
    }

    public void invalidateMapView() {
        mMapView.invalidate();
    }

    public void setTileSource(String tileSourceName) {
        ITileSource iTileSource = GoogleMapsTileSource.TILE_SOURCE_MAP.get(tileSourceName);
        if (iTileSource != null) {
            IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(getActivity());
            TileWriter tileWriter = new TileWriter();
            MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(registerReceiver, null);
            MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(registerReceiver, null, findArchiveFiles());
            MapTileDownloader downloaderProvider = new MapTileDownloader(null, tileWriter, new NetworkAvailabliltyCheck(getActivity()));
            MapTileProviderBase mapTileProviderArray = new MapTileProviderArray(iTileSource, registerReceiver, new MapTileModuleProviderBase[]{fileSystemProvider, fileArchiveProvider, downloaderProvider});
            //使您能够以编程方式设置磁贴提供程序
            mMapView.setTileProvider(mapTileProviderArray);

            mMapView.setTileSource(iTileSource);
            Log.e(TAG, "setTileSource->" + tileSourceName);
        } else {
            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            Log.e(TAG, "setTileSource->DEFAULT_TILE_SOURCE");
        }
        invalidateMapView();
    }

    private static File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(), "osmdroid");

    private IArchiveFile[] findArchiveFiles() {
        ArrayList<IArchiveFile> mArchiveFiles = new ArrayList();
        if (!checkSdCard()) {
            return null;
        }
        File[] files = OSMDROID_PATH.listFiles();
        if (files != null) {
            for (File file : files) {
                IArchiveFile archiveFile = ArchiveFileFactory.getArchiveFile(file);
                if (archiveFile != null) {
                    mArchiveFiles.add(archiveFile);
                }
            }
        }
        return (IArchiveFile[]) mArchiveFiles.toArray(new IArchiveFile[mArchiveFiles.size()]);
    }

    private boolean checkSdCard() {
        String state = Environment.getExternalStorageState();
        Log.d(TAG, "sdcard state: " + state);
        return "mounted".equals(state);
    }

    //region OfflineMap
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_download:
                showCacheManagerDialog();
                break;

            case R.id.executeJob:
                updateEstimate(true);
                break;

            case R.id.btn_change_source:
                final String[] mapTypes = GoogleMapsTileSource.getTileSourceNames();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("切换图源")
                        .setItems(mapTypes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                setTileSource(mapTypes[i]);
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
                break;
            default:
                break;

        }
    }

    private void showCacheManagerDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());


        // set title
        alertDialogBuilder.setTitle(R.string.cache_manager);
        //.setMessage(R.string.cache_manager_description);

        // set dialog message
        alertDialogBuilder.setItems(new CharSequence[]{
//                        getResources().getString(R.string.cache_current_size),
                        getResources().getString(R.string.cache_download),
                        getResources().getString(R.string.cancel)
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
//                                try {
//                                    mgr = new CacheManager(mMapView);
//                                } catch (TileSourcePolicyException e) {
//                                    Log.e(TAG, e.getMessage());
//                                    dialog.dismiss();
//                                    return;
//                                }
//                                showCurrentCacheInfo();
//                                break;
//                            case 1:
                                downloadJobAlert();
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                }
        );


        // create alert dialog
        alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


        //mgr.possibleTilesInArea(mMapView.getBoundingBox(), 0, 18);
        // mgr.
    }

    private void downloadJobAlert() {
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.sample_cachemgr_input, null);
        view.findViewById(R.id.cache_archival_section).setVisibility(View.VISIBLE);

        BoundingBox boundingBox = MapUtil.getLatLngBoundsByPointList(mMapView, maskLayer.getPoints());
        zoom_max = view.findViewById(R.id.slider_zoom_max);
        zoom_max.setMax((int) mMapView.getMaxZoomLevel());
        zoom_max.setOnSeekBarChangeListener(GoogleMapFragment.this);


        zoom_min = view.findViewById(R.id.slider_zoom_min);
        zoom_min.setMax((int) mMapView.getMaxZoomLevel());
        zoom_min.setProgress((int) mMapView.getMinZoomLevel());
        zoom_min.setOnSeekBarChangeListener(GoogleMapFragment.this);
        cache_east = view.findViewById(R.id.cache_east);
        cache_east.setText(boundingBox.getLonEast() + "");
        cache_north = view.findViewById(R.id.cache_north);
        cache_north.setText(boundingBox.getLatNorth() + "");
        cache_south = view.findViewById(R.id.cache_south);
        cache_south.setText(boundingBox.getLatSouth() + "");
        cache_west = view.findViewById(R.id.cache_west);
        cache_west.setText(boundingBox.getLonWest() + "");
        cache_estimate = view.findViewById(R.id.cache_estimate);
        cache_output = view.findViewById(R.id.cache_output);

        //change listeners for both validation and to trigger the download estimation
        cache_east.addTextChangedListener(this);
        cache_north.addTextChangedListener(this);
        cache_south.addTextChangedListener(this);
        cache_west.addTextChangedListener(this);
        executeJob = view.findViewById(R.id.executeJob);
        executeJob.setOnClickListener(this);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cache_east = null;
                cache_south = null;
                cache_estimate = null;
                cache_north = null;
                cache_west = null;
                executeJob = null;
                zoom_min = null;
                zoom_max = null;
                cache_output = null;
            }
        });
        downloadPrompt = builder.create();
        downloadPrompt.show();

//        updateEstimate(false);
    }

    /**
     * if true, start the job
     * if false, just update the dialog box
     */
    private void updateEstimate(boolean startJob) {
        try {
            if (cache_east != null &&
                    cache_west != null &&
                    cache_north != null &&
                    cache_south != null &&
                    zoom_max != null &&
                    zoom_min != null &&
                    cache_output != null) {
                double n = Double.parseDouble(cache_north.getText().toString());
                double s = Double.parseDouble(cache_south.getText().toString());
                double e = Double.parseDouble(cache_east.getText().toString());
                double w = Double.parseDouble(cache_west.getText().toString());
                int zoommin = (int) mMapView.getZoomLevelDouble();
                int zoommax = 19;
                if (startJob) {
                    if (zoommin > 15){
                        zoommin = 15;
                    }
                    String name = this.cache_output.getText().toString();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getActivity(), "please input the name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String outputName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator + (((((name + "_" + this.mMapView.getTileProvider().getTileSource().name() + "_" + zoommin + "_" + zoommax + "_") + n + "_") + e + "_") + s + "_") + w) + ".sqlite";
                    writer = new SqliteArchiveTileWriter(outputName);
                    try {
                        mgr = new CacheManager(mMapView, writer);
                    } catch (TileSourcePolicyException ex) {
                        Log.e(TAG, ex.getMessage());
                        return;
                    }
                } else {
                    if (mgr == null) {
                        try {
                            mgr = new CacheManager(mMapView);
                        } catch (TileSourcePolicyException ex) {
                            Log.e(TAG, ex.getMessage());
                            return;
                        }
                    }
                }
                //nesw
                BoundingBox bb = new BoundingBox(n, e, s, w);
                int tilecount = mgr.possibleTilesInArea(bb, zoommin, zoommax);
                cache_estimate.setText(tilecount + " tiles");
                if (startJob) {
                    if (downloadPrompt != null) {
                        downloadPrompt.dismiss();
                        downloadPrompt = null;
                    }

                    //this triggers the download
                    mgr.downloadAreaAsync(getActivity(), bb, zoommin, zoommax, new CacheManager.CacheManagerCallback() {
                        @Override
                        public void onTaskComplete() {
                            Toast.makeText(getActivity(), "Download complete!", Toast.LENGTH_LONG).show();
                            if (writer != null) {
                                writer.onDetach();
                            }
                            getActivity().finish();
                        }

                        @Override
                        public void onTaskFailed(int errors) {
                            Toast.makeText(getActivity(), "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();
                            if (writer != null)
                                writer.onDetach();
                        }

                        @Override
                        public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                            //NOOP since we are using the build in UI
                        }

                        @Override
                        public void downloadStarted() {
                            //NOOP since we are using the build in UI
                        }

                        @Override
                        public void setPossibleTilesInArea(int total) {
                            //NOOP since we are using the build in UI
                        }
                    });
                }

            }
        } catch (Exception ex) {
            //TODO something better?
            ex.printStackTrace();
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showCurrentCacheInfo() {
        Toast.makeText(getActivity(), "Calculating...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());


                // set title
                alertDialogBuilder.setTitle(R.string.cache_manager)
                        .setMessage("Cache Capacity (bytes): " + mgr.cacheCapacity() + "\n" +
                                "Cache Usage (bytes): " + mgr.currentCacheUsage());

                // set dialog message
                alertDialogBuilder.setItems(new CharSequence[]{

                                getResources().getString(R.string.cancel)
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // show it
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            }
        }).start();


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateEstimate(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateEstimate(false);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void offlineShowSqlite(File sqliteFile) {
        //narrow it down to only sqlite tiles
        try {

            //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one

            //create the offline tile provider, it will only do offline file archives
            //again using the first file
            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()),
                    new File[]{sqliteFile});
            //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
            mMapView.setTileProvider(tileProvider);

            //this bit enables us to find out what tiles sources are available. note, that this action may take some time to run
            //and should be ran asynchronously. we've put it inline for simplicity

            String source = "";
            IArchiveFile[] archives = tileProvider.getArchives();
            if (archives.length > 0) {
                //cheating a bit here, get the first archive file and ask for the tile sources names it contains
                Set<String> tileSources = archives[0].getTileSources();
                //presumably, this would be a great place to tell your users which tiles sources are available
                if (!tileSources.isEmpty()) {
                    //ok good, we found at least one tile source, create a basic file based tile source using that name
                    //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
                    //which probably won't match your offline tile source, unless it's MAPNIK
                    source = tileSources.iterator().next();
                    this.mMapView.setTileSource(FileBasedTileSource.getSource(source));
                } else {
                    this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                }

            } else {
                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
            }

            Toast.makeText(getActivity(), "Using " + sqliteFile.getAbsolutePath() + " " + source, Toast.LENGTH_SHORT).show();
            invalidateMapView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 只加载sqlite数据
     */
    public void onlyShowSqlite() {
        this.mMapView.setUseDataConnection(false);

        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {

            File[] list = f.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue;
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }
                    //narrow it down to only sqlite tiles
                    if (ArchiveFileFactory.isFileExtensionRegistered(name) &&
                            name.equals("sqlite")) {
                        try {

                            //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one

                            //create the offline tile provider, it will only do offline file archives
                            //again using the first file
                            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()),
                                    new File[]{list[i]});
                            //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
                            mMapView.setTileProvider(tileProvider);

                            //this bit enables us to find out what tiles sources are available. note, that this action may take some time to run
                            //and should be ran asynchronously. we've put it inline for simplicity

                            String source = "";
                            IArchiveFile[] archives = tileProvider.getArchives();
                            if (archives.length > 0) {
                                //cheating a bit here, get the first archive file and ask for the tile sources names it contains
                                Set<String> tileSources = archives[0].getTileSources();
                                //presumably, this would be a great place to tell your users which tiles sources are available
                                if (!tileSources.isEmpty()) {
                                    //ok good, we found at least one tile source, create a basic file based tile source using that name
                                    //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
                                    //which probably won't match your offline tile source, unless it's MAPNIK
                                    source = tileSources.iterator().next();
                                    this.mMapView.setTileSource(FileBasedTileSource.getSource(source));
                                } else {
                                    this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                }

                            } else {
                                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                            }

                            Toast.makeText(getActivity(), "Using " + list[i].getAbsolutePath() + " " + source, Toast.LENGTH_SHORT).show();
                            this.mMapView.invalidate();
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            Toast.makeText(getActivity(), f.getAbsolutePath() + " did not have any files I can open! Try using default", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), f.getAbsolutePath() + " dir not found!", Toast.LENGTH_SHORT).show();
        }


    }
    //endregion
}
