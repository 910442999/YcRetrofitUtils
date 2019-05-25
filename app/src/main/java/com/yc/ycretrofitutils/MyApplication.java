package com.yc.ycretrofitutils;

import android.app.Application;
import android.util.Log;

import com.yc.ycrertofitutils.BuildConfig;
import com.yc.ycrertofitutils.YcRetrofitUtils;
import com.yc.ycrertofitutils.service.BaseApiService;
import com.yc.ycretrofitutils.BasicUse.BasicUseService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyApplication extends Application {
    public static BasicUseService basicUseService;

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 设置baseUrl 及 ApiService (必填项)
         * 如果你觉得 BaseApiService默认的不能满足你的需求 可继承BaseApiService后传入你自己定义的BaseApiService
         */

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("114lalala", message);
            }
        });
        if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }

        YcRetrofitUtils builder = YcRetrofitUtils.getInstance()
                .init(this) //初始化
                .setLog(true)
                .setLogTag("ycLog")
                .setBaseUrl("http://v.juhe.cn/")

                //                .setReadTimeOut(100)//默认 60000
                //                .setWriteTimeOut(100)//默认 60000
                //                .setConnectTimeout(100)//默认 60000
                .build()
                .create();
        basicUseService = builder.create(BasicUseService.class);
    }
}
