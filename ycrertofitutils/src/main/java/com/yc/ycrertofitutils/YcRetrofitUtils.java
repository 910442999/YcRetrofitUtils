package com.yc.ycrertofitutils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.yc.ycrertofitutils.interfaces.OnRequestCallBackListener;
import com.yc.ycrertofitutils.service.BaseApiService;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class YcRetrofitUtils {

    private static Application sContext;
    public static final int DEFAULT_MILLISECONDS = 60000;             //默认的超时时间
    private static final int DEFAULT_RETRY_COUNT = 3;                 //默认重试次数
    private static final int DEFAULT_RETRY_INCREASEDELAY = 0;         //默认重试叠加时间
    private static final int DEFAULT_RETRY_DELAY = 500;               //默认重试延时
    public static final int DEFAULT_CACHE_NEVER_EXPIRE = -1;          //缓存过期时间，默认永久缓存
    private String mBaseUrl;                                          //全局BaseUrl
    private long mConnectTimeout;                                      //链接超时
    private long mReadTimeOut;                                         //读超时
    private long mWriteTimeOut;                                        //写超时
    private static int mRetryCount = DEFAULT_RETRY_COUNT;                    //重试次数默认3次
    private static int mRetryDelay = DEFAULT_RETRY_DELAY;                    //延迟xxms重试
    private static int mRetryIncreaseDelay = DEFAULT_RETRY_INCREASEDELAY;    //叠加延迟
    private OkHttpClient.Builder okHttpClientBuilder;                 //okhttp请求的客户端
    private Retrofit.Builder retrofitBuilder;                         //Retrofit请求Builder
    private Retrofit retrofit;
    private Interceptor mInterceptor;                                 //okhttp的拦截器
    private Converter.Factory mConverterFactory;                      //Retrofit全局设置Converter.Factory
    private CallAdapter.Factory mCallAdapterFactory;                  //Retrofit全局设置CallAdapter.Factory

    private static BaseApiService mBaseApiService;                         //通用的的api接口
    //定义公共的参数
    private static Map<String, RequestBody> params;
    private volatile static YcRetrofitUtils sInstance = null;
    private static DisposableSubscriber sDisposableSubscriber;

    private YcRetrofitUtils() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        retrofitBuilder = new Retrofit.Builder();

    }

    public static YcRetrofitUtils getInstance() {
        if (sInstance == null) {
            synchronized (YcRetrofitUtils.class) {
                if (sInstance == null) {
                    sInstance = new YcRetrofitUtils();
                    params = new HashMap<String, RequestBody>();
                }
            }
        }
        return sInstance;
    }

    public static BaseApiService getBaseApiService() {
        return mBaseApiService;
    }

    /**
     * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
     */
    public YcRetrofitUtils init(Application app) {
        sContext = app;
        return this;
    }

    /**
     * 获取全局上下文
     */
    public static Context getContext() {
        initialize();
        return sContext;
    }

    private static void initialize() {
        if (sContext == null)
            throw new ExceptionInInitializerError("请先在全局Application中调用 YcRetrofitinit() 初始化！");
    }

    //okhttp的实例
    public static OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    public static Retrofit getRetrofit() {
        return getRetrofitBuilder().build();
    }

    /**
     * 对外暴露 OkHttpClient,方便自定义
     */
    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        return getInstance().okHttpClientBuilder;
    }

    /**
     * 对外暴露 Retrofit,方便自定义
     */
    public static Retrofit.Builder getRetrofitBuilder() {
        return getInstance().retrofitBuilder;
    }

    /**
     * 全局为Retrofit设置自定义的OkHttpClient
     */
    public YcRetrofitUtils setOkclient(OkHttpClient client) {
        getRetrofitBuilder().client(checkNotNull(client, "OkClient == null"));
        return this;
    }

    /**
     * 全局设置Interceptor,默认HttpLoggingInterceptor.Level.BODY
     */
    public YcRetrofitUtils addOkHttpInterceptor(Interceptor interceptor) {
        mInterceptor = checkNotNull(interceptor, "OkHttpAddInterceptor == null");
        getOkHttpClientBuilder().addInterceptor(mInterceptor);
        return this;
    }

    /**
     * 全局设置addNetworkInterceptor,默认 无
     */
    public YcRetrofitUtils addNetworkInterceptor(Interceptor interceptor) {
        mInterceptor = checkNotNull(interceptor, "addNetworkInterceptor == null");
        getOkHttpClientBuilder().addNetworkInterceptor(mInterceptor);
        return this;
    }

    /**
     * 全局设置addNetworkInterceptor,默认 无
     */
    public YcRetrofitUtils cache(Cache cache) {
        Cache cache1 = checkNotNull(cache, "OkHttpCache == null");
        getOkHttpClientBuilder().cache(cache1);
        return this;
    }

    /**
     * 全局设置Converter.Factory,默认GsonConverterFactory.create()
     */
    public YcRetrofitUtils addConverterFactory(Converter.Factory factory) {
        mConverterFactory = checkNotNull(factory, "RetrofitConverterFactory == null");
        getRetrofitBuilder().addConverterFactory(mConverterFactory);
        return this;
    }

    /**
     * 全局设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    public YcRetrofitUtils addCallAdapterFactory(CallAdapter.Factory factory) {
        mCallAdapterFactory = checkNotNull(factory, "RetrofitCallAdapterFactory == null");
        getRetrofitBuilder().addCallAdapterFactory(mCallAdapterFactory);
        return this;
    }

    /**
     * 全局读取超时时间
     */
    public YcRetrofitUtils setReadTimeOut(long readTimeOut) {
        if (readTimeOut < 0)
            throw new IllegalArgumentException("retryCount must > 0");
        mReadTimeOut = readTimeOut;
        getOkHttpClientBuilder().readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局写入超时时间
     */
    public YcRetrofitUtils setWriteTimeOut(long writeTimeout) {
        if (writeTimeout < 0)
            throw new IllegalArgumentException("retryCount must > 0");
        mWriteTimeOut = writeTimeout;
        getOkHttpClientBuilder().writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局连接超时时间
     */
    public YcRetrofitUtils setConnectTimeout(long connectTimeout) {
        if (connectTimeout < 0)
            throw new IllegalArgumentException("retryCount must > 0");
        mConnectTimeout = connectTimeout;
        getOkHttpClientBuilder().connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 超时重试次数
     */
    public YcRetrofitUtils setRetryCount(int retryCount) {
        if (retryCount < 0)
            throw new IllegalArgumentException("retryCount must > 0");
        mRetryCount = retryCount;
        return this;
    }

    /**
     * 超时重试次数
     */
    public static int getRetryCount() {
        return getInstance().mRetryCount;
    }

    /**
     * 超时重试延迟时间
     */
    public YcRetrofitUtils setRetryDelay(int retryDelay) {
        if (retryDelay < 0)
            throw new IllegalArgumentException("retryDelay must > 0");
        mRetryDelay = retryDelay;
        return this;
    }

    /**
     * 超时重试延迟时间
     */
    public static int getRetryDelay() {
        return getInstance().mRetryDelay;
    }

    /**
     * 超时重试延迟叠加时间
     */
    public YcRetrofitUtils setRetryIncreaseDelay(int retryIncreaseDelay) {
        if (retryIncreaseDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        mRetryIncreaseDelay = retryIncreaseDelay;
        return this;
    }

    /**
     * 超时重试延迟叠加时间
     */
    public static int getRetryIncreaseDelay() {
        return getInstance().mRetryIncreaseDelay;
    }

    /**
     * 全局设置baseurl
     */
    public YcRetrofitUtils setBaseUrl(String baseUrl) {
        mBaseUrl = checkNotNull(baseUrl, "baseUrl == null");
        return this;
    }

    /**
     * 获取全局baseurl
     */
    public static String getBaseUrl() {
        return getInstance().mBaseUrl;
    }

    /**
     * 根据当前的请求参数，生成对应的OkClient
     */
    private OkHttpClient.Builder generateOkClient() {
        if (mConnectTimeout <= 0)
            setConnectTimeout(DEFAULT_MILLISECONDS);
        if (mReadTimeOut <= 0)
            setReadTimeOut(DEFAULT_MILLISECONDS);
        if (mWriteTimeOut <= 0)
            setWriteTimeOut(DEFAULT_MILLISECONDS);

        if (mInterceptor == null) {
            //由于Retrofit是基于okhttp的所以，要先初始化okhttp相关配置
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d("YcRetrofitUtils", message);
                }
            });
            // BASIC，BODY，HEADERS
            if (BuildConfig.DEBUG) {
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            }
            //添加拦截器
            addOkHttpInterceptor(interceptor);
        }
        return getOkHttpClientBuilder();
    }

    /**
     * 根据当前的请求参数，生成对应的Retrofit
     */
    private Retrofit.Builder generateRetrofit() {

        final Retrofit.Builder retrofitBuilder = getRetrofitBuilder().baseUrl(checkNotNull(getBaseUrl(), "baseUrl == null"));
        if (mConverterFactory == null)
            addConverterFactory(GsonConverterFactory.create());

        if (mCallAdapterFactory == null)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        return retrofitBuilder;
    }


    /**
     * 生成网络请求build请求体
     *
     * @return
     */
    public YcRetrofitUtils build() {

        OkHttpClient.Builder okHttpClientBuilder = generateOkClient();

        final Retrofit.Builder retrofitBuilder = generateRetrofit();
        retrofitBuilder.client(okHttpClientBuilder.build());
        if (retrofit == null) {
            retrofit = retrofitBuilder.build();
        }
        return this;
    }

    public YcRetrofitUtils create() {
        mBaseApiService = create(BaseApiService.class);
        return this;
    }

    /**
     * 创建api服务  可以支持自定义的api，默认使用BaseApiService,上层不用关心
     *
     * @param service 自定义的apiservice class
     */
    public <T> T create(final Class<T> service) {
        retrofit = checkNotNull(retrofit, "请先在调用build()才能使用");
        return retrofit.create(service);
    }

    public static <T> T checkNotNull(T t, String message) {
        if (t == null) {
            throw new NullPointerException(message);
        }
        return t;
    }

    /**
     * 添加参数
     * 根据传进来的Object对象来判断是String还是File类型的参数
     */
    public YcRetrofitUtils addParameter(String key, Object o) {
        if (params != null && params.size() > 0)
            clear();
        if (o instanceof String) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain;charset=UTF-8"), (String) o);
            params.put(key, body);
        } else if (o instanceof File) {
            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data;charset=UTF-8"), (File) o);
            params.put(key + "\"; filename=\"" + ((File) o).getName() + "", body);
        }
        return this;
    }

    /**
     * 构建RequestBody
     */
    public Map<String, RequestBody> bulider() {

        return params;
    }

    public void clear() {
        params.clear();
    }


    /**
     * get
     *
     * @param url              链接
     * @param callBackListener 回调监听
     */
    public static void get(String url, final OnRequestCallBackListener callBackListener) {
        get(url, "", callBackListener);
    }

    /**
     * get
     *
     * @param url              链接
     * @param tag              类型
     * @param callBackListener 回调监听
     */
    public static void get(String url, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.get(url);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * get
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     */
    public static void get(String url, Map map, final OnRequestCallBackListener callBackListener) {
        get(url, map, "", callBackListener);
    }

    /**
     * get
     *
     * @param url              链接
     * @param map              参数
     * @param tag              类型
     * @param callBackListener 回调监听
     */
    public static void get(String url, Map map, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.get(url, map);
        requestCallBack(flowable, tag, callBackListener);
    }


    /**
     * post
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     */
    public static void post(String url, Map map, final OnRequestCallBackListener callBackListener) {
        post(url, map, "", callBackListener);
    }

    /**
     * post
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void post(String url, Map map, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.post(url, map);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * postBody
     *
     * @param url              链接
     * @param object           参数
     * @param callBackListener 回调监听
     */
    public static void postBody(String url, Object object, final OnRequestCallBackListener callBackListener) {
        postBody(url, object, "", callBackListener);
    }

    /**
     * postBody
     *
     * @param url              链接
     * @param object           参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void postBody(String url, Object object, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.postBody(url, object);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * postBody
     *
     * @param url              链接
     * @param requestBody      参数
     * @param callBackListener 回调监听
     */
    public static void postBody(String url, RequestBody requestBody, final OnRequestCallBackListener callBackListener) {
        postBody(url, requestBody, "", callBackListener);
    }

    /**
     * postBody
     *
     * @param url              链接
     * @param requestBody      参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void postBody(String url, RequestBody requestBody, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.postBody(url, requestBody);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * postJson
     *
     * @param url              链接
     * @param requestBody      参数
     * @param callBackListener 回调监听
     */
    public static void postJson(String url, RequestBody requestBody, final OnRequestCallBackListener callBackListener) {
        postJson(url, requestBody, "", callBackListener);
    }

    /**
     * postBody
     *
     * @param url              链接
     * @param requestBody      参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void postJson(String url, RequestBody requestBody, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.postJson(url, requestBody);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * delete
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     */
    public static void delete(String url, Map map, final OnRequestCallBackListener callBackListener) {
        delete(url, map, "", callBackListener);
    }

    /**
     * delete
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void delete(String url, Map map, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.delete(url, map);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * put
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     */
    public static void put(String url, Map map, final OnRequestCallBackListener callBackListener) {
        put(url, map, "", callBackListener);
    }

    /**
     * put
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void put(String url, Map map, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.put(url, map);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * uploadFlie
     *
     * @param url              链接
     * @param requestBody      描述
     * @param part             文件
     * @param callBackListener 回调监听
     */
    public static void uploadFlie(String url, RequestBody requestBody, MultipartBody.Part part, final OnRequestCallBackListener callBackListener) {
        uploadFlie(url, requestBody, part, "", callBackListener);
    }

    /**
     * uploadFlie
     *
     * @param url              链接
     * @param requestBody      描述
     * @param part             文件
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void uploadFlie(String url, RequestBody requestBody, MultipartBody.Part part, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.uploadFlie(url, requestBody, part);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * uploadFiles
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     */
    public static void uploadFiles(String url, Map map, final OnRequestCallBackListener callBackListener) {
        uploadFiles(url, map, "", callBackListener);
    }

    /**
     * uploadFiles
     *
     * @param url              链接
     * @param map              参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void uploadFiles(String url, Map map, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.uploadFiles(url, map);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * uploadFiles
     *
     * @param url              链接
     * @param parts            参数
     * @param callBackListener 回调监听
     */
    public static void uploadFiles(String url, List<MultipartBody.Part> parts, final OnRequestCallBackListener callBackListener) {
        uploadFiles(url, parts, "", callBackListener);
    }

    /**
     * uploadFiles
     *
     * @param url              链接
     * @param parts            参数
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void uploadFiles(String url, List<MultipartBody.Part> parts, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.uploadFiles(url, parts);
        requestCallBack(flowable, tag, callBackListener);
    }

    /**
     * downloadFile
     *
     * @param url              下载链接
     * @param callBackListener 回调监听
     */
    public static void downloadFile(String url, final OnRequestCallBackListener callBackListener) {
        downloadFile(url, "", callBackListener);
    }

    /**
     * downloadFile
     *
     * @param url              下载链接
     * @param callBackListener 回调监听
     * @param tag              调用的方法类型(区分调用的方法的回调参数)
     */
    public static void downloadFile(String url, String tag, final OnRequestCallBackListener callBackListener) {
        Flowable flowable = mBaseApiService.downloadFile(url);
        requestCallBack(flowable, tag, callBackListener);
    }


    //===============================================================

    /**
     * 处理数据请求相关功能，将flowable加入队列,通过接口回调的方式将rxjava返回的数据返回给调用者
     *
     * @param flowable         调入的flowable
     * @param callBackListener 回调
     * @param <T>              泛型参数
     */

    public static <T> void requestCallBack(Flowable<T> flowable, final OnRequestCallBackListener callBackListener) {
        requestCallBack(flowable, "", callBackListener);
    }

    /**
     * 处理数据请求相关功能，将flowable加入队列,通过接口回调的方式将rxjava返回的数据返回给调用者
     *
     * @param callBackListener 回调
     * @param tag              调用方法标志，回调用
     * @param <T>              泛型参数
     */

    public static <T> void requestCallBack(Flowable<T> flowable, final String tag, final OnRequestCallBackListener callBackListener) {
        sDisposableSubscriber = requestCallBack(tag, callBackListener);
        onSubscribe(flowable, sDisposableSubscriber);
    }

    public static <T> DisposableSubscriber requestCallBack(final String tag, final OnRequestCallBackListener callBackListener) {
        sDisposableSubscriber = new DisposableSubscriber<T>() {
            @Override
            public void onNext(T body) {
                try {
                    if (body instanceof ResponseBody) {
                        String response = ((ResponseBody) body).string();
                        callBackListener.onSuccess(response, tag);
                    } else {
                        callBackListener.onSuccess(body, tag);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callBackListener.onFailed(e.getMessage().toString(), tag);
                }
            }

            @Override
            public void onError(Throwable t) {
                callBackListener.onFailed(t.getMessage().toString(), tag);
            }

            @Override
            public void onComplete() {

            }
        };

        return sDisposableSubscriber;
    }


    public static <T> void onSubscribe(Flowable<T> flowable) {
        onSubscribe(flowable, sDisposableSubscriber);
    }

    public static <T> void onSubscribe(Flowable<T> flowable, DisposableSubscriber<T> disposableSubscriber) {
        Flowable<T> beanFlowable = flowable.subscribeOn(Schedulers.io());
        beanFlowable.observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .subscribeWith(disposableSubscriber);
    }

    /**
     * 取消订阅
     */
    public static void cancelSubscription() {
        cancelSubscription(sDisposableSubscriber);
    }

    /**
     * 取消订阅
     */
    public static void cancelSubscription(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
