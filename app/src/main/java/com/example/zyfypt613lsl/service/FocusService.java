package com.example.zyfypt613lsl.service;

import com.example.zyfypt613lsl.bean.FollowResultBean;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * 用户关注相关接口。
 */
public interface FocusService {

    @GET("api.php/exists/mod/userfocus")
    Call<String> exists(
            @Query("idolid") int userId,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/create/mod/userfocus")
    Call<String> focus(
            @Query("idolid") int userId,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/delete/mod/userfocus")
    Call<String> unfocus(
            @Query("idolid") int userId,
            @Header("SessionID") String sessionID
    );

    @GET("api.php/listmyfocus/mod/userfocus")
    Call<List<FollowResultBean>> getMyFollowList(
            @Query("page") int page,
            @Header("SessionID") String sessionID
    );
    
    // 获取原始JSON响应用于调试
    @GET("api.php/listmyfocus/mod/userfocus")
    Call<String> getMyFollowListRaw(
            @Query("page") int page,
            @Header("SessionID") String sessionID
    );
}

