package com.jbtm.parentschool.models;

/**
 * Created by lvhailing on 2018/12/15.
 * 用户点击购买按钮后 客户端请求后台接口，接口返回的支付对象
 */

public class PayModelXiaoMi {
    public long order_id;  //订单ID
    public MiParam mi_param;

    public class MiParam {
        public long app_id;  //在小米开发者网站上注册时获取的应用appId
        public String cust_order_id;  //客户订单号
        public String product_name;  //客户产品名称,用于支付商品名称展示
        public long price;    //订单金额，单位 分
        public String order_desc;    //包月、包年描述
        public String extra_data;    //额外信息，用于app跟小米之间的一些特殊约定
    }
}
