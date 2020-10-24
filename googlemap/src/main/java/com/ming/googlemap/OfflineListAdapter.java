package com.ming.googlemap;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class OfflineListAdapter extends RecyclerView.Adapter<OfflineListAdapter.MyVH> {
    private List<Offline> offlineList;
    private OnClickRvItemListener onClickRvItemListener;

    public OfflineListAdapter(List<Offline> offlineList) {
        this.offlineList = offlineList;
    }

    @NonNull
    @Override
    public MyVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_offline_list = View.inflate(OsmApplication.getInstance(), R.layout.item_offline_list, null);
        return new MyVH(item_offline_list);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVH holder, int position) {
        if (offlineList.isEmpty()) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.itemView.setLayoutParams(layoutParams);

        Offline offline = offlineList.get(position);
        holder.txtTime.setText(offline.getTxtTime());
        holder.txtSize.setText(offline.getTxtSize());
        holder.txtLevel.setText(offline.getZoomMin()+"-"+offline.getZoomMax());
        holder.txtContent.setText(offline.getTxtContent());
        holder.txtPath.setText(offline.getTxtPath());

        final int layoutPosition = holder.getLayoutPosition();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickRvItemListener != null) {
                    onClickRvItemListener.onClickRvItem(layoutPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return offlineList.size();
    }

    class MyVH extends RecyclerView.ViewHolder {
        private TextView txtTime, txtSize, txtLevel, txtContent, txtPath;

        public MyVH(@NonNull View itemView) {
            super(itemView);
            this.txtTime = itemView.findViewById(R.id.txtTime);
            this.txtSize = itemView.findViewById(R.id.txtSize);
            this.txtLevel = itemView.findViewById(R.id.txtLevel);
            this.txtContent = itemView.findViewById(R.id.txtContent);
            this.txtPath = itemView.findViewById(R.id.txtPath);
        }
    }

    public interface OnClickRvItemListener {
        void onClickRvItem(int layoutPosition);
    }

    public void setOnClickRvItemListener(OnClickRvItemListener onClickRvItemListener) {
        this.onClickRvItemListener = onClickRvItemListener;
    }
}
