package com.example.zyfypt613lsl.bean;

import com.google.gson.annotations.SerializedName;

public class ResBean {
    private String id; // 资源id
    
    @SerializedName(value = "name", alternate = {"title", "resname"})
    private String name; // 资源名称
    
    @SerializedName(value = "thumb", alternate = {"thumbnail", "cover", "image", "img", "pic"})
    private String thumb; // 资源图片地址后缀
    
    @SerializedName(value = "description", alternate = {"desc", "intro", "content"})
    private String description; // 资源描述
    
    @SerializedName(value = "author", alternate = {"username", "authorname"})
    private String author; // 作者
    
    @SerializedName(value = "userId", alternate = {"userid", "user_id", "authorid"})
    private String userId; // 作者id
    
    @SerializedName(value = "update_time", alternate = {"updatetime", "create_time", "createtime"})
    private String update_time; // 发表时间
    
    @SerializedName(value = "pdfattach", alternate = {"pdf", "pdfpath", "pdf_attach", "attachment"})
    private String pdfattach; // 课件地址
    
    @SerializedName(value = "videopath", alternate = {"video", "video_path", "videourl"})
    private String videopath; // 视频地址

    // 根据截图显示，specialname 字段被标记为要删除，所以这里不包含

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getPdfattach() {
        return pdfattach;
    }

    public void setPdfattach(String pdfattach) {
        this.pdfattach = pdfattach;
    }

    public String getVideopath() {
        return videopath;
    }

    public void setVideopath(String videopath) {
        this.videopath = videopath;
    }
}