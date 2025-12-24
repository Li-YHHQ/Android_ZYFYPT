package com.example.zyfypt613lsl.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 收藏记录返回数据。
 * 支持多种API字段名映射
 */
public class CollectResultBean {

    private String id;
    
    @SerializedName(value = "resid", alternate = {"res_id", "resourceId", "resource_id"})
    private String resid;
    
    @SerializedName(value = "userid", alternate = {"user_id", "userId", "uid", "authorid", "author_id"})
    private String userid;
    
    @SerializedName(value = "username", alternate = {"user_name", "userName", "authorname", "author_name", "nickname"})
    private String username;
    
    // 支持多种可能的字段名
    @SerializedName(value = "name", alternate = {"title", "resname", "res_name", "resourceName", "resource_name"})
    private String name;
    
    @SerializedName(value = "thumb", alternate = {"thumbnail", "cover", "image", "img", "pic", "picture"})
    private String thumb;
    
    @SerializedName(value = "description", alternate = {"desc", "intro", "content", "summary"})
    private String description;
    
    @SerializedName(value = "updatetime", alternate = {"update_time", "create_time", "createtime", "time", "date"})
    private String updatetime;
    
    // 额外字段
    @SerializedName(value = "author", alternate = {"creator", "uploader"})
    private String author;
    
    // 资源类型
    @SerializedName(value = "type", alternate = {"module", "mod", "category"})
    private String type;
    
    // 浏览量
    @SerializedName(value = "views", alternate = {"view_count", "viewCount", "hits"})
    private String views;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResid() {
        return resid;
    }

    public void setResid(String resid) {
        this.resid = resid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }
    
    /**
     * 获取显示作者名，优先使用username，其次使用author
     */
    public String getDisplayAuthor() {
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        if (author != null && !author.trim().isEmpty()) {
            return author;
        }
        return null;
    }
}
