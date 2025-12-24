package com.example.zyfypt613lsl.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 关注列表返回数据。
 * 支持多种API字段名映射
 */
public class FollowResultBean {

    private String id;
    
    @SerializedName(value = "idolid", alternate = {"idol_id", "userId", "user_id", "uid", "focusid", "focus_id", "targetid", "target_id"})
    private String idolid;
    
    // 用户名字段 - 支持多种可能的字段名
    @SerializedName(value = "idolname", alternate = {"idol_name", "name", "nickname", "displayname", "display_name", "uname"})
    private String idolname;
    
    // username字段 - API可能返回这个字段
    @SerializedName(value = "username", alternate = {"userName", "user_name"})
    private String username;
    
    // 真实姓名字段 - 可能API返回的是这个字段
    @SerializedName(value = "realname", alternate = {"real_name", "truename", "true_name", "fullname", "full_name"})
    private String realname;
    
    @SerializedName(value = "avatar", alternate = {"head", "headimg", "head_img", "photo", "userAvatar", "user_avatar", "img", "image", "pic"})
    private String avatar;
    
    @SerializedName(value = "rolename", alternate = {"role_name", "role", "userRole", "user_role", "type", "userType", "usertype"})
    private String rolename;
    
    @SerializedName(value = "description", alternate = {"desc", "intro", "introduction", "bio", "signature", "sign", "remark"})
    private String description;
    
    // 额外字段用于兼容 - 可能API返回的是这些字段
    @SerializedName(value = "email", alternate = {"mail", "useremail", "user_email"})
    private String email;
    
    @SerializedName(value = "phone", alternate = {"mobile", "tel", "telephone", "userphone", "user_phone"})
    private String phone;
    
    // 可能API返回的是这个字段作为用户名
    @SerializedName(value = "account", alternate = {"loginname", "login_name", "loginid"})
    private String account;
    
    // author字段 - 可能API返回的是这个字段
    @SerializedName(value = "author", alternate = {"authorname", "author_name"})
    private String author;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdolid() {
        return idolid;
    }

    public void setIdolid(String idolid) {
        this.idolid = idolid;
    }

    public String getIdolname() {
        return idolname;
    }

    public void setIdolname(String idolname) {
        this.idolname = idolname;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    
    /**
     * 获取显示名称，按优先级尝试多个字段
     */
    public String getDisplayName() {
        // 1. 优先使用realname（真实姓名）
        if (realname != null && !realname.trim().isEmpty()) {
            return realname.trim();
        }
        // 2. 尝试使用idolname
        if (idolname != null && !idolname.trim().isEmpty()) {
            return idolname.trim();
        }
        // 3. 尝试使用username
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        // 4. 尝试使用author
        if (author != null && !author.trim().isEmpty()) {
            return author.trim();
        }
        // 5. 尝试使用account
        if (account != null && !account.trim().isEmpty()) {
            return account.trim();
        }
        // 6. 尝试使用email
        if (email != null && !email.trim().isEmpty()) {
            // 从邮箱提取用户名
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                return email.substring(0, atIndex);
            }
            return email;
        }
        // 7. 尝试使用phone
        if (phone != null && !phone.trim().isEmpty()) {
            // 隐藏部分手机号
            if (phone.length() >= 7) {
                return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
            }
            return phone;
        }
        // 8. 使用idolid作为最后备选
        if (idolid != null && !idolid.trim().isEmpty()) {
            return "用户#" + idolid;
        }
        return null;
    }
}
