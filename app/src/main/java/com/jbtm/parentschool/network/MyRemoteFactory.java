package com.jbtm.parentschool.network;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jbtm.parentschool.BuildConfig;
import com.jbtm.parentschool.utils.SPUtil;
import com.jbtm.parentschool.utils.Util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyRemoteFactory {
    private static final String TAG = MyRemoteFactory.class.getSimpleName();
    private static final String HTTP_USER_AGENT = "User-Agent";
    private static MyRemoteFactory sInstance;
    private final Gson mGsonDateFormat;
    private static Retrofit retrofit;
    private static HashMap<Class, Object> proxyMap = new HashMap<>();

    //连接超时 、读写超时
    private final static long DEFAULT_TIMEOUT = 20;

    public static MyRemoteFactory getInstance() {
        if (sInstance == null) {
            synchronized (MyRemoteFactory.class) {
                if (sInstance == null) {
                    sInstance = new MyRemoteFactory();
                }
            }
        }
        return sInstance;
    }

    public MyRemoteFactory() {
        mGsonDateFormat = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setPrettyPrinting()
                .setVersion(1.0)
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
    }

    /**
     * 创建retrofit服务
     */
    public <T> T getProxy(final Class<T> someClass) {
        if (proxyMap.containsKey(someClass)) {
            return (T) proxyMap.get(someClass);
        }
        String baseUrl = "";
        try {
            Field field = someClass.getField("BASE_URL");
            baseUrl = (String) field.get(someClass);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(mGsonDateFormat))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        T proxyClass = retrofit.create(someClass);
        proxyMap.put(someClass, proxyClass);
        return proxyClass;
    }


    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //设置超时时间
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        //清空所有拦截
        if (builder.interceptors() != null) {
            builder.interceptors().clear();
        }

        //添加公共参数
        builder.addInterceptor(commonParams);

        //添加日志打印
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        builder.addInterceptor(log);

        return builder.build();
    }

    Interceptor commonParams = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (request.method().equals("POST")) {
                //post请求 基础参数放在body里
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                bodyBuilder
                        .add("device_type", "4")
                        .add("api_version", Util.getVersionName())
                        .add("accept_token", SPUtil.getToken());
                if (request.body() instanceof FormBody) {
                    FormBody body = (FormBody) request.body();
                    for (int i = 0; i < body.size(); i++) {
                        bodyBuilder.addEncoded(body.encodedName(i), body.encodedValue(i));
                    }
                }
                FormBody formBody = bodyBuilder.build();
                request = request.newBuilder().post(formBody).build();
            } else if (request.method().equals("GET")) {
                //get请求 基础参数放在头信息里
                Request.Builder newBuilder = request.newBuilder();
                newBuilder.addHeader("version", "1.0.0");
                newBuilder.addHeader("from", "android");
                request = newBuilder.build();
            }
            return chain.proceed(request);
        }
    };
}
