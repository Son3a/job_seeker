package com.nsb.job_seeker.message.model;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
