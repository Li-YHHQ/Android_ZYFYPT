# 教学资源平台 (Android)

一个基于Android开发的教学资源移动应用平台，提供文章、课件、视频、案例等多种教学资源的浏览、收藏和管理功能。

## 项目简介

教学资源平台是一款面向教育领域的移动应用，旨在为用户提供便捷的教学资源访问和管理服务。用户可以浏览各类教学资源，关注感兴趣的作者或机构，收藏喜欢的内容，并通过个性化设置优化使用体验。

## 主要功能

### 资源浏览
- **文章浏览**：查看教学相关文章，支持图文混排
- **课件浏览**：在线查看PDF格式的课件资源
- **视频观看**：支持在线视频播放，可自定义播放设置
- **案例学习**：浏览教学案例和项目资源

### 用户功能
- **用户认证**：支持用户注册、登录功能
- **收藏管理**：收藏感兴趣的资源，统一管理
- **关注功能**：关注优质作者和机构，及时获取更新
- **用户详情**：查看作者或机构的详细信息

### 个性化设置
- **主题模式**：支持浅色、深色和跟随系统三种主题
- **字体设置**：提供小、标准、大、特大四种字体大小
- **播放设置**：自定义视频自动播放行为
- **缓存管理**：控制缓存启用状态，清理缓存文件
- **通知设置**：管理推送通知偏好

## 技术架构

### 开发环境
- **开发语言**：Java + Kotlin
- **最低SDK版本**：24 (Android 7.0)
- **目标SDK版本**：36
- **编译SDK版本**：36
- **构建工具**：Gradle (Kotlin DSL)

### 核心技术栈

#### UI框架
- **ViewPager2**：实现主页面滑动切换
- **BottomNavigationView**：底部导航栏
- **RecyclerView**：列表展示
- **SwipeRefreshLayout**：下拉刷新
- **CardView**：卡片式布局
- **CoordinatorLayout**：协调布局

#### 网络层
- **Retrofit 2.11.0**：HTTP客户端封装
- **OkHttp 4.12.0**：底层网络请求
- **Gson Converter**：JSON数据解析
- **Picasso 2.8**：网络图片加载

#### 功能组件
- **Android PDF Viewer 3.2.0**：PDF文件查看
- **Material Design Components**：Material设计组件
- **VideoView**：视频播放支持

### 项目结构

```
app/src/main/java/com/example/zyfypt613lsl/
├── activities/          # Activity页面
│   ├── BaseActivity.java
│   ├── MainActivity.java              # 主页面
│   ├── LoginActivity.java             # 登录页面
│   ├── RegisterActivity.java          # 注册页面
│   ├── DetailActivity.java            # 课件详情
│   ├── ViewArticleActivity.java       # 文章详情
│   ├── UserDetailActivity.java        # 用户详情
│   ├── MyCollectMainActivity.java     # 我的收藏
│   ├── MyFocusMainActivity.java       # 我的关注
│   └── SettingsActivity.java          # 设置页面
├── fragments/           # Fragment模块
│   ├── BaseFragment.java
│   ├── Fragment1.java                 # 文章模块
│   ├── Fragment2.java                 # 课件模块
│   ├── Fragment3.java                 # 视频模块
│   ├── Fragment4.java                 # 案例模块
│   ├── OwnerFragment.java             # 个人中心
│   └── ResourceListFragment.java      # 资源列表基类
├── adapter/             # RecyclerView适配器
│   ├── MyAdapter.java                 # 资源列表适配器
│   ├── CollectAdapter.java            # 收藏列表适配器
│   └── FollowAdapter.java             # 关注列表适配器
├── bean/                # 数据模型
│   ├── ResBean.java                   # 资源实体
│   ├── UserBean.java                  # 用户实体
│   ├── CollectResultBean.java         # 收藏结果
│   └── FollowResultBean.java          # 关注结果
├── service/             # 网络服务接口
│   ├── ResService.java                # 资源服务
│   ├── UserService.java               # 用户服务
│   ├── CollectService.java            # 收藏服务
│   └── FocusService.java              # 关注服务
├── utils/               # 工具类
│   ├── NetworkClient.java             # 网络客户端
│   ├── NetworkUtils.java              # 网络工具
│   ├── CacheManager.java              # 缓存管理
│   ├── PreferenceManager.java         # 偏好设置管理
│   ├── RefreshHelper.java             # 刷新辅助
│   ├── EmptyStateHelper.java          # 空状态处理
│   ├── SkeletonLoader.java            # 骨架屏加载
│   └── VideoPlayerHelper.kt           # 视频播放辅助
├── common/              # 公共常量
│   └── Common.java                    # 全局常量配置
└── MyApplication.java   # Application入口
```

## 核心功能说明

### 资源管理
应用通过统一的`ResBean`数据模型管理各类资源，支持：
- 资源ID、名称、描述
- 作者信息（姓名、ID）
- 缩略图、更新时间
- PDF课件附件
- 视频文件路径

### 网络请求
- 基于Retrofit构建的RESTful API客户端
- 统一的网络拦截器处理Session认证
- 支持请求日志记录和调试
- 完善的错误处理和重试机制

### 缓存机制
- 图片缓存：通过Picasso自动管理
- 数据缓存：`CacheManager`提供本地缓存支持
- 用户偏好：`PreferenceManager`持久化用户设置

### UI优化
- **骨架屏**：资源加载时展示占位动画，提升体验
- **空状态**：数据为空时展示友好提示界面
- **下拉刷新**：支持手势刷新数据
- **错误处理**：网络异常时提供重试选项

## 服务器配置

### 后端接口
- **基础URL**：`http://43.143.162.173:9001/`
- **资源存储**：`/Uploads/`
- **视频路径**：`/Uploads/video/video/`
- **文章页面**：`/article.php/show/index/id/{id}`

### 认证方式
通过HTTP Header中的`SessionID`字段进行用户认证

## 构建和运行

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11
- Android SDK 36
- Gradle 8.x

### 构建步骤

1. 克隆项目
```bash
git clone <repository-url>
cd Android_ZYFYPT
```

2. 在Android Studio中打开项目

3. 同步Gradle依赖
```bash
./gradlew build
```

4. 运行应用
- 连接Android设备或启动模拟器
- 点击运行按钮或执行：
```bash
./gradlew installDebug
```

### 权限说明

应用需要以下权限：
- `INTERNET`：访问网络获取资源
- `ACCESS_NETWORK_STATE`：检测网络连接状态
- `WRITE_SETTINGS`：调整系统亮度（可选）

## 应用截图

应用包含以下主要界面：
- 登录注册页面
- 资源浏览页面（文章/课件/视频/案例）
- 资源详情页面
- 用户详情页面
- 我的收藏和关注页面
- 个人中心
- 设置页面

## 特色功能

### 1. 多资源类型支持
应用支持多种教学资源格式，包括文章、PDF课件、视频和案例，满足不同学习场景需求。

### 2. 智能缓存
自动缓存已访问的资源，离线也可查看部分内容，节省流量。

### 3. 个性化体验
提供丰富的个性化设置选项，包括主题、字体、播放行为等，满足不同用户偏好。

### 4. 优雅的加载体验
采用骨架屏加载动画和空状态设计，提供流畅的用户体验。

## 待开发功能

- [ ] 离线下载功能
- [ ] 搜索和筛选
- [ ] 评论和互动
- [ ] 学习进度跟踪
- [ ] 推荐算法优化

## 版本信息

- **当前版本**：1.0
- **版本代码**：1
- **包名**：`com.example.zyfypt613lsl`

## 开发说明

### 代码规范
- 遵循Android开发最佳实践
- 使用Material Design设计规范
- 统一的命名规范和注释

### 日志调试
项目包含详细的日志输出，标签格式为类名，便于调试和问题排查。

### 网络安全
配置了网络安全策略（`network_security_config.xml`），确保数据传输安全。

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，欢迎提交Issue。
