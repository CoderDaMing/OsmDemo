package com.ming.googlemap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OfflineListActivity extends AppCompatActivity implements View.OnClickListener, OfflineListAdapter.OnClickRvItemListener {
    private static final String TAG = "OfflineListActivity";

    private RecyclerView rvOffline;
    private OfflineListAdapter offlineListAdapter;
    private List<Offline> offlineList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_list);
        //title
        TextView textTitle = findViewById(R.id.tv_title_bar_title);
        textTitle.setText(getString(R.string.activity_offline_list));
        findViewById(R.id.fl_title_bar_left).setOnClickListener(this);
        findViewById(R.id.fl_title_bar_right).setOnClickListener(this);
        //list
        rvOffline = findViewById(R.id.rv_offline);
        rvOffline.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        offlineListAdapter = new OfflineListAdapter(offlineList);
        rvOffline.setAdapter(offlineListAdapter);
        offlineListAdapter.setOnClickRvItemListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initOfflineData();
        Log.e(TAG, "offlineList size: " + offlineList.size());
    }

    private static SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void initOfflineData() {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {
            File[] list = f.listFiles();
            if (list != null) {
                List<Offline> fileList = new ArrayList<>();
                for (int i = 0; i < list.length; i++) {
                    if (!list[i].isDirectory()) {
                        String name = list[i].getName().toLowerCase();
                        if (name.contains(".")) {
                            name = name.substring(name.lastIndexOf(".") + 1);
                            if (name.length() != 0 && ArchiveFileFactory.isFileExtensionRegistered(name)) {
                                File file = list[i];
                                Offline offline = new Offline();
                                String[] names = file.getName().replace(".sqlite", "").split("_");
                                if (names.length >= 8) {
                                    offline.setTxtContent(names[0]);
                                    offline.setTxtTime(sdf_full.format(new Date(file.lastModified())));
                                    offline.setTxtSize(String.format(Locale.ROOT, "%.2f MB", (((double) file.length()) / 1024.0d) / 1024.0d));
                                    offline.setTxtMapType(names[1]);
                                    offline.setZoomMin(Double.parseDouble(names[2]));
                                    offline.setZoomMax(Double.parseDouble(names[3]));
                                    offline.setNorth(Double.parseDouble(names[4]));
                                    offline.setEast(Double.parseDouble(names[5]));
                                    offline.setSouth(Double.parseDouble(names[6]));
                                    offline.setWest(Double.parseDouble(names[7]));
                                    offline.setTxtPath(file.getAbsolutePath());
                                    fileList.add(offline);
                                }
                            }
                        }
                    }
                }
                this.offlineList.clear();
                this.offlineList.addAll(fileList);
                this.offlineListAdapter.notifyDataSetChanged();
                return;
            }
            return;
        }
        Toast.makeText(this, f.getAbsolutePath() + " dir not found!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickRvItem(int layoutPosition) {
        if (offlineList.isEmpty()) {
            return;
        }
        Offline offline = offlineList.get(layoutPosition);
        Intent intent = new Intent(OfflineListActivity.this, OfflineMapActivity.class);
        intent.putExtra(OfflineMapActivity.PARAMS_FUNCTION, OfflineMapActivity.FUNCTION_SHOW);
        intent.putExtra(OfflineMapActivity.PARAMS_OFFLINE, offline);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_title_bar_left:
                finish();
                break;
            case R.id.fl_title_bar_right:
                Intent intent = new Intent(OfflineListActivity.this, OfflineMapActivity.class);
                intent.putExtra(OfflineMapActivity.PARAMS_FUNCTION, OfflineMapActivity.FUNCTION_ADD);
                startActivity(intent);
                break;
            default:
                break;

        }
    }
}
