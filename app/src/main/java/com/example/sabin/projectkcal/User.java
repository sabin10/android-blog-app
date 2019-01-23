package com.example.sabin.projectkcal;

public class User {

    private String first, last, image, bio;

    public User() {
    }

    public User(String first, String last, String image, String bio) {
        this.first = first;
        this.last = last;
        this.image = image;
        this.bio = bio;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
