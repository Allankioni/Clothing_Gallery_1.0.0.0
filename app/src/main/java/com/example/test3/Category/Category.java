package com.example.test3.Category;

import com.example.test3.Subcategory.ClothingItem;

import java.util.ArrayList;
import java.util.List;

// Category.java
public class Category {
     String id;
    String name;
     List<ClothingItem> subcategories;
     int imageUrl;

    public Category(Category category) {
        // Required empty constructor for Firebase
    }

    public Category(String id, String name, int imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.subcategories = new ArrayList<>();
    }

    public Category(String id, String name, String imageUrl) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClothingItem> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<ClothingItem> subcategories) {
        this.subcategories = subcategories;
    }

    public int getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }

}
