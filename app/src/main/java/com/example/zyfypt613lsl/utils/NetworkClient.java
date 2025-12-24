package com.example.zyfypt613lsl.utils;

import com.example.zyfypt613lsl.common.Common;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 全局网络客户端单例 - 优化网络请求性能
 * 复用OkHttpClient和Retrofit实例，避免重复创建
 */
public class NetworkClient {
    
    private static volatile NetworkClient instance;
    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;
    private final Retrofit rawRetrofit;
    private final Retrofit scalarsRetrofit;
    
    private NetworkClient() {
        // 创建优化的OkHttpClient - 10秒超时，启用连接池
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                // 启用连接池复用
                .connectionPool(new okhttp3.ConnectionPool(5, 30, TimeUnit.SECONDS))
                .build();
        
        // 创建Gson Retrofit（用于解析JSON对象）
        retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // 创建原始字符串 Retrofit（用于调试和返回String的API）
        rawRetrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        
        // 创建Scalars + Gson Retrofit（用于返回String的API，同时支持Gson）
        scalarsRetrofit = new Retrofit.Builder()
                .baseUrl(Common.BASEURL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    
    public static NetworkClient getInstance() {
        if (instance == null) {
            synchronized (NetworkClient.class) {
                if (instance == null) {
                    instance = new NetworkClient();
                }
            }
        }
        return instance;
    }
    
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
    
    public Retrofit getRetrofit() {
        return retrofit;
    }
    
    public Retrofit getRawRetrofit() {
        return rawRetrofit;
    }
    
    /**
     * 获取服务接口
     */
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
    
    /**
     * 获取原始字符串服务接口（用于调试）
     */
    public <T> T createRawService(Class<T> serviceClass) {
        return rawRetrofit.create(serviceClass);
    }
    
    /**
     * 获取Scalars服务接口（用于返回String的API）
     */
    public <T> T createScalarsService(Class<T> serviceClass) {
        return scalarsRetrofit.create(serviceClass);
    }
    
    public Retrofit getScalarsRetrofit() {
        return scalarsRetrofit;
    }
}
