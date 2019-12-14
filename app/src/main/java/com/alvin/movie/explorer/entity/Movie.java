package com.alvin.movie.explorer.entity;

import android.graphics.drawable.Drawable;

/**
 * Created by Alvin on 2019/12/14.
 */

public class Movie {
    private String ID;
    private String title;
    private String originalTitle;
    private String rating;
    private String resolution;
    private String fileName;
    private String filePath;
    private String format;
    private String year;
    private String doubanID;
    private String photo;
    private String absolutePhoto;
    private String createTime;
    private String lastModified;
    private Drawable photoDrawable;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDoubanID() {
        return doubanID;
    }

    public void setDoubanID(String doubanID) {
        this.doubanID = doubanID;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAbsolutePhoto() {
        return absolutePhoto;
    }

    public void setAbsolutePhoto(String absolutePhoto) {
        this.absolutePhoto = absolutePhoto;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Drawable getPhotoDrawable() {
        return photoDrawable;
    }

    public void setPhotoDrawable(Drawable photoDrawable) {
        this.photoDrawable = photoDrawable;
    }
}
