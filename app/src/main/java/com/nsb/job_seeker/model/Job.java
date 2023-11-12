package com.nsb.job_seeker.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Job implements Serializable, Parcelable {
    private String id, nameJob, companyId, place, salary, deadline, desJob, reqJob,
            typeJob, image, amountRecruitment, workingForm, experience, gender;
    private String companyName, typeId;

    public Job() {
    }

    public Job(String id, String nameJob, String companyId, String companyName, String place,
               String salary, String deadline, String desJob, String reqJob, String typeId,
               String typeJob, String image, String amountRecruitment, String workingForm, String experience, String gender) {
        this.nameJob = nameJob;
        this.companyId = companyId;
        this.place = place;
        this.salary = salary;
        this.deadline = deadline;
        this.id = id;
        this.desJob = desJob;
        this.reqJob = reqJob;
        this.typeJob = typeJob;
        this.image = image;
        this.amountRecruitment = amountRecruitment;
        this.workingForm = workingForm;
        this.experience = experience;
        this.gender = gender;
        this.typeId = typeId;
        this.companyName = companyName;
    }

    protected Job(Parcel in) {
        id = in.readString();
        nameJob = in.readString();
        companyId = in.readString();
        place = in.readString();
        salary = in.readString();
        deadline = in.readString();
        desJob = in.readString();
        reqJob = in.readString();
        typeJob = in.readString();
        image = in.readString();
        amountRecruitment = in.readString();
        workingForm = in.readString();
        experience = in.readString();
        gender = in.readString();
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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String typeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAmountRecruitment() {
        return amountRecruitment;
    }

    public void setAmountRecruitment(String amountRecruitment) {
        this.amountRecruitment = amountRecruitment;
    }

    public String getWorkingForm() {
        return workingForm;
    }

    public void setWorkingForm(String workingForm) {
        this.workingForm = workingForm;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Job{" +
                "nameJob='" + nameJob + '\'' +
                ", company='" + companyId + '\'' +
                ", place='" + place + '\'' +
                ", salary='" + salary + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nameJob);
        dest.writeString(companyId);
        dest.writeString(place);
        dest.writeString(salary);
        dest.writeString(deadline);
        dest.writeString(desJob);
        dest.writeString(reqJob);
        dest.writeString(typeJob);
        dest.writeString(image);
        dest.writeString(amountRecruitment);
        dest.writeString(workingForm);
        dest.writeString(experience);
        dest.writeString(gender);
    }
}
