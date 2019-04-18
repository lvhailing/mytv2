package com.jbtm.parentschool.models;

import java.util.List;

/**
 * Created by lvhailing on 2018/12/21.
 * 首页精选及下方课程对象
 */

public class HomeColumnModel {
    public int column_id;
    public String title;
    public int type;
    public List<HomeCourseModel> items;
}
