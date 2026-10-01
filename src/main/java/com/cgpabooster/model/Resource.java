package com.cgpabooster.model;

import java.sql.Timestamp;

/**
 * Represents a row in the "resources" table.
 */
public class Resource {

    private int id;
    private String title;
    private String description;
    private String category;
    private String resourceUrl;
    private int uploadedBy;
    private String uploadedByName; // joined from users table, not a DB column
    private Timestamp createdAt;

    public Resource() {
    }

    public Resource(int id, String title, String description, String category,
                     String resourceUrl, int uploadedBy, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.resourceUrl = resourceUrl;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
