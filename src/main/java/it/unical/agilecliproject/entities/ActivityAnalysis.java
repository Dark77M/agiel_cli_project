package it.unical.agilecliproject.entities;

public class ActivityAnalysis {
    private String activityName;
    private long duration;

    private String date;
    private String weekDays;

    public ActivityAnalysis() {
    }


    public ActivityAnalysis(String activityName, long duration, String date, String weekDays) {
        this.activityName = activityName;
        this.duration = duration;
        this.date = date;
        this.weekDays = weekDays;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(String weekDays) {
        this.weekDays = weekDays;
    }
}
