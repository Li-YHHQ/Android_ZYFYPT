package com.example.zyfypt613lsl.service;

import com.example.zyfypt613lsl.bean.CollectResultBean;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 收藏相关接口。
 */
public interface CollectService {

    @GET("api.php/exists/mod/collect{mod}")
    Call<String> exists(
            @Path("mod") String mod,
            @Query("resid") int resid,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/create/mod/collect{mod}")
    Call<String> collect(
            @Path("mod") String mod,
            @Query("resid") int resid,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/delete/mod/collect{mod}")
    Call<String> uncollect(
            @Path("mod") String mod,
            @Query("resid") int resid,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/listmycollect/mod/collect{mod}")
    Call<List<CollectResultBean>> getMyCollectList(
            @Path("mod") String mod,
            @Query("page") int page,
            @Header("SessionID") String sessionID
    );
    
    // 获取原始JSON响应用于调试
    @GET("api.php/listmycollect/mod/collect{mod}")
    Call<String> getMyCollectListRaw(
            @Path("mod") String mod,
            @Query("page") int page,
            @Header("SessionID") String sessionID
    );
}

