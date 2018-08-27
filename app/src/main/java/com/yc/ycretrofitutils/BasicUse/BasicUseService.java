package com.yc.ycretrofitutils.BasicUse;


import com.yc.ycretrofitutils.model.NewsBean;

import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface BasicUseService {

    //方式一
    @GET("{path}")
    Flowable<NewsBean> login(@Path("path") String path, @QueryMap Map<String, String> maps);

    //方式二
    @GET()
    Flowable<NewsBean> login1(@Url String url, @QueryMap Map<String, String> maps);

    //方式三
    @GET("toutiao/index")
    Flowable<NewsBean> login2(@QueryMap Map<String, String> maps);

}
