package com.app.jussfun.ui.feed;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.bumptech.glide.Glide;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

/**
 * Created by ravi on 23/10/17.
 */

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.MyViewHolder> {

    private List<ThumbnailItem> thumbnailItemList;
    private ThumbnailsAdapterListener listener;
    private Context mContext;
    private int selectedIndex = 0, displayWidth, displayHeight;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView filterName;
        private RelativeLayout itemLay;
        private LinearLayout contentLay;

        public MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            filterName = (TextView) view.findViewById(R.id.filter_name);
            itemLay = view.findViewById(R.id.itemLay);
            contentLay = view.findViewById(R.id.contentLay);
        }
    }

    public ThumbnailsAdapter(Context context, List<ThumbnailItem> thumbnailItemList, ThumbnailsAdapterListener listener,
                             int displayWidth, int displayHeight) {
        mContext = context;
        this.thumbnailItemList = thumbnailItemList;
        this.listener = listener;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_filter, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ThumbnailItem thumbnailItem = thumbnailItemList.get(holder.getAdapterPosition());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = (int) (displayWidth / 3);
        params.height = (int) (displayHeight / 4);
        holder.contentLay.setLayoutParams(params);

//        holder.thumbnail.setImageBitmap(thumbnailItem.filter.processFilter(thumbnailItem.image));
        Glide.with(mContext)
                .load(thumbnailItem.image)
                .centerCrop()
                .into(holder.thumbnail);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preventMultipleClick(view);
                listener.onFilterSelected(thumbnailItem.filter);
                selectedIndex = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });

//        Log.i("TAG", "filteronBindViewHolder: "+thumbnailItem.filterName);
        holder.filterName.setText(thumbnailItem.filterName);

        if (selectedIndex == holder.getAdapterPosition()) {
            holder.filterName.setTextColor(ContextCompat.getColor(mContext, R.color.filter_label_selected));
        } else {
            holder.filterName.setTextColor(ContextCompat.getColor(mContext, R.color.filter_label_normal));
        }
    }

    private void preventMultipleClick(View view) {
        view.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 2000);
    }

    @Override
    public int getItemCount() {
        return thumbnailItemList.size();
    }

    public interface ThumbnailsAdapterListener {
        void onFilterSelected(Filter filter);
    }

}
