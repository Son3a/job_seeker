package com.nsb.job_seeker.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Company implements Serializable, Parcelable {
    String id, name, isDelete, link, image, about, address, location, phone;
    int totalEmployee;

    public Company(String id, String name, String isDelete, String link, String image, int totalEmployee,
                   String about, String address, String location, String phone) {
        this.id = id;
        this.name = name;
        this.isDelete = isDelete;
        this.link = link;
        this.image = image;
        this.totalEmployee = totalEmployee;
        this.about = about;
        this.address = address;
        this.location = location;
        this.phone = phone;
    }

    protected Company(Parcel in) {
        id = in.readString();
        name = in.readString();
        isDelete = in.readString();
        link = in.readString();
        image = in.readString();
        totalEmployee = in.readInt();
        about = in.readString();
        address = in.readString();
        location = in.readString();
        phone = in.readString();
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

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

    public int getTotalEmployee() {
        return totalEmployee;
    }

    public void setTotalEmployee(int totalEmployee) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(isDelete);
        parcel.writeString(link);
        parcel.writeString(image);
        parcel.writeInt(totalEmployee);
        parcel.writeString(about);
        parcel.writeString(address);
        parcel.writeString(location);
        parcel.writeString(phone);
    }
}
