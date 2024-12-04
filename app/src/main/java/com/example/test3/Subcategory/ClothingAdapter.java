package com.example.test3.Subcategory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.test3.Category.Category;
import com.example.test3.Fullscreen.FullscreenPhotoActivity;
import com.example.test3.R;

import java.util.ArrayList;
import java.util.List;

public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ViewHolder> {
    private final List<ClothingItem> clothingList;
    private final Context context;
    private final SparseBooleanArray selectedItems;
    private boolean isSelectionMode = false;

    private SelectionListener selectionListener;

    public ClothingAdapter(Context context) {
        this.context = context;
        this.clothingList = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingAdapter.ViewHolder holder, int position) {
        ClothingItem clothingItem = clothingList.get(position);

        if (clothingItem.isHasValidImage()){
            Glide.with(context)
                    .load(Uri.parse(clothingItem.getImageUri()))
                    .placeholder(R.drawable.place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }else {
            holder.imageView.setImageResource(R.drawable.child_default);
        }
        // Show/hide selection UI
        holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedItems.get(position, false));
        holder.selectionOverlay.setVisibility(selectedItems.get(position, false) ? View.VISIBLE : View.GONE);

        // Set click listener for the image
        //click listener for full screen photo
        holder.imageView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
            }else {
                Intent intent = new Intent(context, FullscreenPhotoActivity.class);
                intent.putExtra("PHOTO_URL", clothingItem.getImageUri());
                context.startActivity(intent);
            }
        });
        // Set long click listener for image_delete
        holder.imageView.setOnLongClickListener(v -> {
            Toast.makeText(context, "Long click", Toast.LENGTH_SHORT).show();
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(position);
                if (context instanceof BottomNavActivity) {
                    ((BottomNavActivity) context).startSelectionMode();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return clothingList.size();
    }

    public void selectAll() {
        for (int i = 0; i < clothingList.size(); i++) {
            selectedItems.put(i, true);
        }
        notifyDataSetChanged();
        if (context instanceof BottomNavActivity) {
            ((BottomNavActivity) context).updateSelectionCount(getSelectedItemCount());
        }
    }

    public void deselectAll() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (context instanceof BottomNavActivity) {
            ((BottomNavActivity) context).updateSelectionCount(0);
        }
    }

    public boolean areAllItemsSelected() {
        return selectedItems.size() == clothingList.size();
    }


    public void updateData(List<ClothingItem> newList) {
        clothingList.clear();
        clothingList.addAll(newList);
        notifyDataSetChanged();
    }
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);

        if (selectionListener != null) {
            selectionListener.onSelectionChanged(getSelectedItemCount());
        }
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Long> getSelectedItemIds() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            if (position < clothingList.size()) {
                ClothingItem item = clothingList.get(position);
                ids.add(item.getId());
            }
        }
        return ids;
    }

    public void clearSelection() {
        selectedItems.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        View selectionOverlay;


        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image_view);
            checkBox = itemView.findViewById(R.id.checkBox);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }
    }
}
