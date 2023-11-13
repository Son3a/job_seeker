package com.nsb.job_seeker.model;

import java.io.Serializable;

public class User implements Serializable {
    public String name,  email, token, id;

    public User() {
    }

    public User(String id, String name, String email, String token) {
        this.name = name;
        this.email = email;
        this.token = token;
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
