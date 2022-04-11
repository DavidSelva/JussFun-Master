package com.app.jussfun.ui.feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.databinding.ItemReportBinding;
import com.app.jussfun.helper.callback.OnReportListener;
import com.app.jussfun.model.Report;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter {
    private final Context context;
    private List<Report> reportList = new ArrayList<>();
    private RecyclerView.ViewHolder viewHolder;
    private OnReportListener listener;

    public ReportAdapter(Context context, List<Report> reportList, OnReportListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @NonNull ItemReportBinding binding = ItemReportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemViewHolder) holder).binding.txtReport.setText(reportList.get(position).getTitle());
        ((ItemViewHolder) holder).binding.txtReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportList.get(holder.getAdapterPosition()).getTitle();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemReportBinding binding;

        public ItemViewHolder(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}