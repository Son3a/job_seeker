package com.nsb.job_seeker.model;

import java.io.Serializable;

public class Company implements Serializable {
    String id, name, isDelete, link, image, totalEmployee, about, address, location, idUser, phone;

    public Company(String id, String name, String isDelete, String link, String image, String totalEmployee,
                   String about, String address, String location, String idUser, String phone) {
        this.id = id;
        this.name = name;
        this.isDelete = isDelete;
        this.link = link;
        this.image = image;
        this.totalEmployee = totalEmployee;
        this.about = about;
        this.address = address;
        this.location = location;
        this.idUser = idUser;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTotalEmployee() {
        return totalEmployee;
    }

    public void setTotalEmployee(String totalEmployee) {
        this.totalEmployee = totalEmployee;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
