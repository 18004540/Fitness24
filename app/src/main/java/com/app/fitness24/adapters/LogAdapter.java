package com.app.fitness24.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.fitness24.R;
import com.app.fitness24.models.Log;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.Holder> {
    Context context;
    List<Log> list;

    public LogAdapter(Context context, List<Log> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvDate.setText(list.get(position).getDate());
        holder.tvWeight.setText(list.get(position).getWeight());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeight;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvWeight = itemView.findViewById(R.id.tv_weight);

        }
    }
}
