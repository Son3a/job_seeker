package com.nsb.job_seeker.employer;

public class Recruitment {
    private String nameJob, place, timeCreated,amountCV,deadline;

    public Recruitment() {
    }

    public Recruitment(String nameJob, String place, String timeCreated, String amountCV, String deadline) {
        this.nameJob = nameJob;
        this.place = place;
        this.timeCreated = timeCreated;
        this.amountCV = amountCV;
        this.deadline = deadline;
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
}
