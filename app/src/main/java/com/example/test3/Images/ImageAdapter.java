package com.example.test3.Images;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;

import java.util.ArrayList;
import java.util.List;

// ImageAdapter.java
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Image> images;
    private final Context context;
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Image image, View sharedView);
    }

    public ImageAdapter(Context context, OnPhotoClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.images = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = images.get(position);
        ImageManager imageManager = new ImageManager(context);
//        Bitmap bitmap = imageManager.loadImage(image.getUrl());
        holder.imageView.setImageResource(image.getUrl());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(image, holder.imageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setPhotos(List<Image> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image_view);
        }
    }
}
