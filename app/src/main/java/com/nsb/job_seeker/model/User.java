package com.nsb.job_seeker.model;

import java.io.Serializable;

public class User implements Serializable {
    private String name,  email, token, id, image;

    public User() {
    }

    public User(String id, String name, String email, String image) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
