package com.example.zyfypt613lsl.service;

import com.example.zyfypt613lsl.bean.UserBean;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * 用户认证相关接口。
 */
public interface UserService {

    @GET("api.php/login")
    Call<UserBean> login(
            @Query("username") String username,
            @Query("password") String password
    );

    @GET("api.php/reg")
    Call<String> register(
            @Query("username") String username,
            @Query("password") String password,
            @Query("tel") String tel,
            @Query("roleid") int roleid,
            @Query("email") String email
    );

    @GET("api.php/logout")
    Call<String> logout(@Header("SessionID") String sessionID);
    
    // 获取用户信息
    @GET("api.php/get/mod/user")
    Call<UserBean> getUserInfo(
            @Query("id") int userId,
            @Header("SessionID") String sessionID
    );
}
