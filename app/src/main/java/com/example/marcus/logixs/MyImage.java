package com.example.marcus.logixs;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Marcus on 13.02.2017.
 */

public class MyImage {

    private String title, description, path;
    private Calendar datetime;
    private long datetimeLong;
    protected SimpleDateFormat df = new SimpleDateFormat("MMMM d, yy  h:mm");

    public String getTitle() { return title; }

    public Calendar getDatetime() { return datetime; }

    public void setDatetime(long datetimeLong) {
        this.datetimeLong = datetimeLong;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(datetimeLong);
        this.datetime = cal;
    }

    public void setDatetime(Calendar datetime) { this.datetime = datetime; }

    public String getDescription() { return description; }

    public void setTitle(String title) { this.title = title; }

    public long getDatetimeLong() { return datetimeLong; }

    public void setDescription(String description) { this.description = description; }

    public void setPath(String path) { this.path = path; }

    public String getPath() { return path; }

    @Override public String toString() {
        return "Title:" + title + "   " + df.format(datetime.getTime()) +
                "\nDescription:" + description + "\nPath:" + path;
    }
}
