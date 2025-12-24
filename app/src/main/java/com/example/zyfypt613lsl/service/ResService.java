package com.example.zyfypt613lsl.service;

import com.example.zyfypt613lsl.bean.ResBean;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * 资源浏览模块相关接口。
 */
public interface ResService {

    /**
     * 通用分页列表
     */
    @GET("api.php/lists/mod/{mod}/page/{page}/SessionID/{sessionID}")
    Call<List<ResBean>> getList(
            @Path("mod") String mod,
            @Path("page") int page,
            @Path("sessionID") String sessionID
    );

    /**
     * 专题视频列表
     */
    @GET("api.php/listspecial/mod/video/page/{page}/SessionID/{sessionID}")
    Call<List<ResBean>> getSpecialVideoList(
            @Path("page") int page,
            @Path("sessionID") String sessionID
    );

    /**
     * 资源详情（SessionID在路径中）
     */
    @GET("api.php/get/mod/{mod}/id/{id}/SessionID/{sessionID}")
    Call<ResBean> getDetail(
            @Path("mod") String mod,
            @Path("id") int id,
            @Path("sessionID") String sessionID
    );
    
    /**
     * 资源详情（SessionID在Header中）
     */
    @GET("api.php/get/mod/{mod}/id/{id}")
    Call<ResBean> getDetailWithHeader(
            @Path("mod") String mod,
            @Path("id") int id,
            @Header("SessionID") String sessionID
    );

    /**
     * 指定用户发布的资源
     */
    @GET("api.php/lists/mod/{mod}")
    Call<List<ResBean>> getUserResourceList(
            @Path("mod") String mod,
            @Query("page") int page,
            @Query("userid") int userId,
            @Header("SessionID") String sessionID
    );

    /**
     * 下载 PDF/附件
     */
    @GET
    Call<ResponseBody> getPdf(@Url String url);
}