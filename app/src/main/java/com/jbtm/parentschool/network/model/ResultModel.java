package com.jbtm.parentschool.network.model;

import java.io.Serializable;

public class ResultModel<T> implements Serializable {
    public int code = -1;
    public String msg = "";
    public T result;
}
