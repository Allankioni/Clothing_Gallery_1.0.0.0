package com.example.test3.Category;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameTextView;
    public CardView cardView;


    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.category_image);
        nameTextView = itemView.findViewById(R.id.category_name);
        cardView = itemView.findViewById(R.id.Category_card);


    }
}
