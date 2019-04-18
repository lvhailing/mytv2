package com.jbtm.parentschool.models;

/**
 * Created by lvhailing on 2018/12/15.
 */

public class WatchHistoryModel {
    public int course_id;
    public String title;
    public String photo;
    public int progress;

    public WatchHistoryModel(int course_id, String title, String photo, int progress) {
        this.course_id = course_id;
        this.title = title;
        this.photo = photo;
        this.progress = progress;
    }
}
