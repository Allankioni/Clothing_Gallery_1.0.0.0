package com.example.test3.Images;


// Image.java
public class Image {
    private String id;
    private int url;
    private String subcategoryId;
    private long timestamp;

    public Image(String id, String url, String subcategoryId) {
        // Required empty constructor for Firebase
    }

    public Image(String id, int url, String subcategoryId) {
        this.id = id;
        this.url = url;
        this.subcategoryId = subcategoryId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUrl() {
        return url;
    }

    public void setUrl(int url) {
        this.url = url;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
