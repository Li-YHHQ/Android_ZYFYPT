package com.example.zyfypt613lsl.common;

/**
 * 汇总服务端常量，方便在不同模块中统一复用。
 */
public final class Common {

    private Common() {
        // no-op
    }

    /** 业务接口根地址 */
    public static final String BASEURL = "http://43.143.162.173:9001/";

    /** 资源文件统一存储目录 */
    public static final String UPLOAD_BASE_URL = BASEURL + "Uploads/";

    /** 图片/缩略图等静态资源前缀 */
    public static final String IMAGEURL = UPLOAD_BASE_URL;

    /** 文章详情页根地址（ARTICLEURL + id） */
    public static final String ARTICLEURL = BASEURL + "article.php/show/index/id/";

    /** Header 中 SessionID 的键名，避免硬编码 */
    public static final String SESSION_HEADER = "SessionID";

    /**
     * 拼接上传资源的完整地址，屏蔽调用侧的字符串拼接细节。
     */
    public static String buildUploadUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return IMAGEURL;
        }
        if (relativePath.startsWith("http")) {
            return relativePath;
        }
        return UPLOAD_BASE_URL + relativePath;
    }

    /**
     * 视频文件完整地址。
     * 根据文档示例：
     * 完整地址形如 http://服务器地址/Uploads/video/video/目录/文件名.mp4
     * 接口中的 videopath 只给出最后一段目录和文件名，例如：thinkphpuml/0.mp4
     */
    public static String buildVideoUrl(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) {
            return "";
        }
        if (videoPath.startsWith("http")) {
            return videoPath;
        }
        // 始终按照文档的规则拼接：Uploads/video/video/ + videopath
        return BASEURL + "Uploads/video/video/" + videoPath;
    }
}
