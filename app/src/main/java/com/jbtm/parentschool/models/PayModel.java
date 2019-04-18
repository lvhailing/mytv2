package com.jbtm.parentschool.models;

/**
 * Created by lvhailing on 2018/12/15.
 * 已支付和售卖套餐的对象
 */

public class PayModel {
    public String name;
    public String price;
    public String original_price;
    public String valid_time;   //剩余有效时间
    public Integer package_id;
    public int discount;    //是否特价优惠

    public PayModel(String name, String price, String original_price, int discount) {
        this.name = name;
        this.price = price;
        this.original_price = original_price;
        this.discount = discount;
    }

    public PayModel(String name, String valid_time) {
        this.name = name;
        this.valid_time = valid_time;
    }
}
