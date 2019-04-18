package com.jbtm.parentschool.early.edu.network.model;

import java.io.Serializable;
import java.util.List;


public class DataWrapper<T> implements Serializable {
    public List<T> data;
    public String stat;
}
