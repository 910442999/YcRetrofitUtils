package com.yc.ycretrofitutils;

import android.app.Application;

import com.yc.ycrertofitutils.YcRetrofitUtils;
import com.yc.ycrertofitutils.service.BaseApiService;
import com.yc.ycretrofitutils.BasicUse.BasicUseService;


public class MyApplication extends Application {
    public static BasicUseService basicUseService;

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 设置baseUrl 及 ApiService (必填项)
         * 如果你觉得 BaseApiService默认的不能满足你的需求 可继承BaseApiService后传入你自己定义的BaseApiService
         */
        YcRetrofitUtils builder = YcRetrofitUtils.getInstance()
                .init(this) //初始化
                .setBaseUrl("http://v.juhe.cn/")
//                .setReadTimeOut(100)//默认 60000
//                .setWriteTimeOut(100)//默认 60000
//                .setConnectTimeout(100)//默认 60000
                .build()
                .create();
        basicUseService = builder.create(BasicUseService.class);

    }
}
