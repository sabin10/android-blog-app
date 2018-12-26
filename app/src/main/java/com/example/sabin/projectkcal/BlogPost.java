package com.example.sabin.projectkcal;

import java.security.Timestamp;
import java.util.Date;

public class BlogPost extends BlogPostId {
    public String
            user_id
            , title
            , description
            , content
            , image
            , thumb;

    public Date timestamp;

    public BlogPost() {}

    public BlogPost(String user_id, String title, String description, String content, String image, String thumb, Date timestamp) {
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.image = image;
        this.thumb = thumb;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


}
