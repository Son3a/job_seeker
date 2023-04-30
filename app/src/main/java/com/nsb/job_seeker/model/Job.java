package com.nsb.job_seeker.model;

public class Job {
    private String nameJob, company, place, salary, time_update, id;

    public Job() {
    }

    public Job(String nameJob, String company, String place, String salary, String time_update) {
        this.nameJob = nameJob;
        this.company = company;
        this.place = place;
        this.salary = salary;
        this.time_update = time_update;
    }

    public Job(String id,String nameJob, String company, String place, String salary, String time_update) {
        this.id = id;
        this.nameJob = nameJob;
        this.company = company;
        this.place = place;
        this.salary = salary;
        this.time_update = time_update;
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
}
