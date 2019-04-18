package com.jbtm.parentschool.models;

import java.util.List;

/**
 * Created by lvhailing on 2018/12/21.
 * 详情页整体对象
 */

public class CourseModel {
    public int course_id;
    public String title;
    public String expert_name;
    public String photo;
    public String price;
    public int charge;  //免费是0
    public List<String> tags;
    public String summary;
    public int type;
    public String course_time;
    public boolean is_order;
    public int progress;
    public String publish_y;
    public List<MaterModel> maters;
}
