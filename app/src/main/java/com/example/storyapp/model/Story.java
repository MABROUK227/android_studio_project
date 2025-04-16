package com.example.storyapp.model;

import java.util.ArrayList;
import java.util.List;

public class Story {
    private String id;
    private String title;
    private String childName;
    private List<String> storyParts;
    private String imageUrl;
    private long creationDate;

    public Story() {
        this.storyParts = new ArrayList<>();
        this.creationDate = System.currentTimeMillis();
    }

    public Story(String id, String title, String childName, List<String> storyParts, String imageUrl) {
        this.id = id;
        this.title = title;
        this.childName = childName;
        this.storyParts = storyParts;
        this.imageUrl = imageUrl;
        this.creationDate = System.currentTimeMillis();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public List<String> getStoryParts() {
        return storyParts;
    }

    public void setStoryParts(List<String> storyParts) {
        this.storyParts = storyParts;
    }

    public void addStoryPart(String part) {
        this.storyParts.add(part);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
