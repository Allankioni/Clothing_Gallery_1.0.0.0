package com.example.test3.Subcategory;


// ClothingItem.java
public class ClothingItem {
    private long id;
    private String type;
    private String category;
    private String imageUri;
    private boolean hasValidImage;
    private boolean isPlaceholder;

    public ClothingItem() {
        // Required empty constructor for Firebase
    }

    public ClothingItem(int id, String type, String imageUri, boolean hasValidImage, boolean isPlaceholder) {
        this.id = id;
        this.type = type;
        this.imageUri = imageUri;
        this.hasValidImage = hasValidImage;
        this.isPlaceholder = isPlaceholder;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public boolean isHasValidImage() {
        return hasValidImage;
    }

    public void setHasValidImage(boolean hasValidImage) {
        this.hasValidImage = hasValidImage;
    }
// Getters and setters for type and category

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    // Getters and setters for id, type, and imageUri
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
