package com.jbtm.parentschool.network;


import com.jbtm.parentschool.models.CommonModel;
import com.jbtm.parentschool.models.CommonWrapper;
import com.jbtm.parentschool.models.HomeWrapper;
import com.jbtm.parentschool.models.OrderWrapper;
import com.jbtm.parentschool.models.PayModelXiaoMi;
import com.jbtm.parentschool.network.model.DataModel;
import com.jbtm.parentschool.network.model.DataWrapper;
import com.jbtm.parentschool.network.model.ResultModel;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 本类说明：通用网络接口
 */
public interface MyRequestProxy {
    String BASE_URL = UrlRoot.ROOT_URL;

    //测试接口
    @POST("center/send-captcha")
    Observable<ResultModel<DataWrapper<DataModel>>> testPost();

    //测试接口
    @GET("toutiao/index?type=caijing&key=cf2e8c721799bbc8f3c9d639a4d0a9e6")
    Observable<ResultModel<DataWrapper<DataModel>>> testGet();

    //测试接口
    @GET("book/receipts/nav1/getgoodslist")
    Observable<ResultModel<DataWrapper<DataModel>>> testGet(@Query("receipt_id") String id, @Query("shop_id") String shopId);

    //测试接口
    @FormUrlEncoded
    @POST("toutiao/index")
    Observable<ResultModel<DataWrapper<DataModel>>> getBookListByPost(@FieldMap Map<String, Object> params);

    //获取验证码
    @POST("tv/center/send-captcha")
    @FormUrlEncoded
    Observable<ResultModel> sendCaptcha(@FieldMap Map<String, Object> params);

    //登录
    @POST("tv/center/login")
    @FormUrlEncoded
    Observable<ResultModel<CommonModel>> login(@FieldMap Map<String, Object> params);

    //首页数据
    @POST("tv/index/index")
    @FormUrlEncoded
    Observable<ResultModel<HomeWrapper>> getHomeData(@FieldMap Map<String, Object> params);

    //详情页数据
    @POST("tv/index/course")
    @FormUrlEncoded
    Observable<ResultModel<CommonWrapper>> getDetailData(@FieldMap Map<String, Object> params);

    //课程播放地址
    @POST("tv/index/play")
    @FormUrlEncoded
    Observable<ResultModel<CommonModel>> getPlayUrl(@FieldMap Map<String, Object> params);

    //创建订单
    @POST("tv/pay/order")
    @FormUrlEncoded
    Observable<ResultModel<CommonModel>> makeOrder(@FieldMap Map<String, Object> params);

    //创建订单（小米支付）
    @POST("tv/pay/order")
    @FormUrlEncoded
    Observable<ResultModel<PayModelXiaoMi>> makeOrderXiaoMi(@FieldMap Map<String, Object> params);

    //获取支付结果
    @POST("tv/pay/order-qry")
    @FormUrlEncoded
    Observable<ResultModel> getPayResult(@FieldMap Map<String, Object> params);

    //获取历史观看记录
    @POST("tv/center/history")
    @FormUrlEncoded
    Observable<ResultModel<CommonWrapper>> getHistory(@FieldMap Map<String, Object> params);

    //获取我的订购信息
    @POST("tv/center/my-orders")
    @FormUrlEncoded
    Observable<ResultModel<OrderWrapper>> getMyOrders(@FieldMap Map<String, Object> params);

    //检查版本
    @POST("tv/index/check-version")
    @FormUrlEncoded
    Observable<ResultModel<CommonWrapper>> checkVersion(@FieldMap Map<String, Object> params);

    //上传播放进度记录
    @POST("tv/index/progress")
    @FormUrlEncoded
    Observable<ResultModel> updateProgress(@FieldMap Map<String, Object> params);
}
