package com.example.test3.Category;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test3.R;
import com.example.test3.Subcategory.BottomNavActivity;

import java.util.List;

// CategoryAdapter.java
public class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder>{
    List<Category> categories;
    Context context;
    private final SelecteCategoryListener listener;

    public CategoryAdapter(List<Category> categories, Context context, SelecteCategoryListener listener) {
        this.categories = categories;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.nameTextView.setText(category.getName());
        holder.imageView.setImageResource(category.getImageUrl());


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listener.onCategorySelected(category);

            }
        });
//
//        holder.cardView.setOnClickListener(View ->{
//            Intent intent = new Intent(context, BottomNavActivity.class);
//            intent.putExtra("CATEGORY_TYPE", category.getName());
//            context.startActivity(intent);
//        });


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

}
