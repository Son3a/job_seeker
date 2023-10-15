package com.nsb.job_seeker.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Job implements Serializable, Parcelable {
    private String nameJob, company, place, salary, time_update, id, locationCompany, desJob, reqJob, typeJob;

    public Job() {
    }

    public Job(String nameJob, String company, String place, String salary, String time_update) {
        this.nameJob = nameJob;
        this.company = company;
        this.place = place;
        this.salary = salary;
        this.time_update = time_update;
    }

    public Job(String id, String nameJob, String company, String place, String salary, String time_update) {
        this.id = id;
        this.nameJob = nameJob;
        this.company = company;
        this.place = place;
        this.salary = salary;
        this.time_update = time_update;
    }

    public Job(String id, String nameJob, String company, String place, String salary, String time_update,
               String locationCompany, String desJob, String reqJob, String typeJob) {
        this.id = id;
        this.nameJob = nameJob;
        this.company = company;
        this.place = place;
        this.salary = salary;
        this.time_update = time_update;
        this.locationCompany = locationCompany;
        this.desJob = desJob;
        this.reqJob = reqJob;
        this.typeJob = typeJob;
    }

    protected Job(Parcel in) {
        nameJob = in.readString();
        company = in.readString();
        place = in.readString();
        salary = in.readString();
        time_update = in.readString();
        id = in.readString();
        locationCompany = in.readString();
        desJob = in.readString();
        reqJob = in.readString();
        typeJob = in.readString();
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameJob() {
        return nameJob;
    }

    public void setNameJob(String nameJob) {
        this.nameJob = nameJob;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getTime_update() {
        return time_update;
    }

    public void setTime_update(String time_update) {
        this.time_update = time_update;
    }

    public String getLocationCompany() {
        return locationCompany;
    }

    public void setLocationCompany(String locationCompany) {
        this.locationCompany = locationCompany;
    }

    public String getDesJob() {
        return desJob;
    }

    public void setDesJob(String desJob) {
        this.desJob = desJob;
    }

    public String getReqJob() {
        return reqJob;
    }

    public void setReqJob(String reqJob) {
        this.reqJob = reqJob;
    }

    public String getTypeJob() {
        return typeJob;
    }

    public void setTypeJob(String typeJob) {
        this.typeJob = typeJob;
    }

    @Override
    public String toString() {
        return "Job{" +
                "nameJob='" + nameJob + '\'' +
                ", company='" + company + '\'' +
                ", place='" + place + '\'' +
                ", salary='" + salary + '\'' +
                ", time_update='" + time_update + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(nameJob);
        dest.writeString(company);
        dest.writeString(place);
        dest.writeString(salary);
        dest.writeString(time_update);
        dest.writeString(id);
        dest.writeString(locationCompany);
        dest.writeString(desJob);
        dest.writeString(reqJob);
        dest.writeString(typeJob);
    }
}
