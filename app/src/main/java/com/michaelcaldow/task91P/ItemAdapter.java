package com.michaelcaldow.task91P;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{
    private List<Item> itemList;
    private Context context;
    private OnItemClickListener listener;

    // Interface for handling clicks
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    // Constructor
    public ItemAdapter(Context context, List<Item> itemList, OnItemClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list_item layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Get item at current position
        Item currentItem = itemList.get(position);

        // Bind data to the views in the ViewHolder
        String summary = currentItem.getPostType() + ": " + currentItem.getName();
        holder.itemSummaryTextView.setText(summary);

        // Set the click listener for the whole item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Update the list data and refresh the RecyclerView
    public void setItems(List<Item> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }


    // ViewHolder class
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView itemSummaryTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemSummaryTextView = itemView.findViewById(R.id.item_summary_textView);
        }
    }
}
