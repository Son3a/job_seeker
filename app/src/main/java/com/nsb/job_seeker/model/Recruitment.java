package com.nsb.job_seeker.model;

public class Recruitment {
    private String id, nameJob, place, timeCreated, amountCV, deadline;

    public Recruitment() {
    }

    public Recruitment(String nameJob, String place, String timeCreated, String amountCV, String deadline) {
        this.nameJob = nameJob;
        this.place = place;
        this.timeCreated = timeCreated;
        this.amountCV = amountCV;
        this.deadline = deadline;
    }

    public Recruitment(String id, String nameJob, String place, String timeCreated, String amountCV, String deadline) {
        this.id = id;
        this.nameJob = nameJob;
        this.place = place;
        this.timeCreated = timeCreated;
        this.amountCV = amountCV;
        this.deadline = deadline;
    }

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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getAmountCV() {
        return amountCV;
    }

    public void setAmountCV(String amountCV) {
        this.amountCV = amountCV;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "Recruitment{" +
                "nameJob='" + nameJob + '\'' +
                ", place='" + place + '\'' +
                ", timeCreated='" + timeCreated + '\'' +
                ", amountCV='" + amountCV + '\'' +
                ", deadline='" + deadline + '\'' +
                '}';
    }
}
