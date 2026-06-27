# 在线考试系统

基于 Spring Boot + Vue.js 的在线考试系统，支持三种角色：超级管理员、教师、学生。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.1.4 |
| 安全框架 | Apache Shiro + JWT |
| ORM | MyBatis-Plus 3.4.1 |
| 数据库 | MySQL 5.7+ |
| 连接池 | Druid 1.2.6 |
| 前端框架 | Vue 2.6 + Element UI |
| 构建工具 | Maven + Vue CLI |
| 开发语言 | Java 8 |

## 项目结构

`
yfexam-exam/
├── exam-api/                  # 后端 Spring Boot 项目
│   ├── src/main/java/com/yy/exam/
│   │   ├── ability/           # 通用能力（Shiro、JWT、文件上传、定时任务）
│   │   ├── config/            # 配置类（CORS、Shiro、SPA路由）
│   │   ├── core/              # 核心工具类
│   │   └── modules/           # 业务模块
│   │       ├── exam/          # 考试管理
│   │       ├── paper/         # 试卷管理
│   │       ├── qu/            # 题库管理
│   │       ├── repo/          # 题库分类
│   │       ├── notice/        # 通知公告
│   │       ├── sys/           # 系统管理（用户、角色、部门、配置）
│   │       └── user/          # 用户相关（考试记录、错题本）
│   └── src/main/resources/
│       ├── application.yml    # 主配置
│       └── application-dev.yml # 开发环境配置
├── exam-vue/                  # 前端 Vue 项目
│   └── src/
│       ├── api/               # API 接口
│       ├── router/            # 路由配置
│       ├── store/             # Vuex 状态管理
│       ├── utils/             # 工具函数
│       └── views/             # 页面组件
│           ├── dashboard/     # 控制台
│           ├── exam/          # 考试管理
│           ├── my/            # 我的考试
│           ├── paper/         # 试卷
│           ├── qu/            # 题库
│           ├── repo/          # 题库分类
│           ├── sys/           # 系统管理
│           └── user/          # 用户管理
└── docs/                      # 文档和SQL脚本
    └── 安装资源/
        ├── yy_exam.sql          # 数据库初始化脚本
        └── notice.sql           # 通知公告表
`

## 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | 推荐 JDK 8 |
| MySQL | 5.7+ | 数据库服务 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 8.9+ | 前端构建 |
| npm | 3.0+ | 前端包管理 |

## 快速开始

### 1. 初始化数据库

创建数据库 yy_exam（字符集 UTF-8），导入 SQL 脚本：

先导入主脚本，再导入通知公告表：

`
mysql -u root -p yy_exam < docs/安装资源/yy_exam.sql
mysql -u root -p yy_exam < docs/安装资源/notice.sql
`

### 2. 配置数据库连接

编辑 exam-api/src/main/resources/application-dev.yml，修改数据库连接信息：

`yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yy_exam?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true
    username: root          # 改为你的数据库用户名
    password: 你的密码       # 改为你的数据库密码
`

### 3. 配置文件上传路径

编辑 exam-api/src/main/resources/application-dev.yml：

`yaml
conf:
  upload:
    # Windows 示例：d:/exam-upload/
    # Linux/Mac 示例：/data/upload/
    dir: /data/upload/
    # 访问地址，注意保留 /upload/file/ 前缀
    url: http://localhost:8080/upload/file/
    # 允许上传的文件后缀
    allow-extensions: jpg,jpeg,png
`

### 4. 启动后端

`ash
cd exam-api
mvn spring-boot:run
`

启动后访问：
- 应用地址：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

### 5. 启动前端（开发模式）

`ash
cd exam-vue
npm install
npm run dev
`

前端开发服务器启动后访问：http://localhost:9527

> 前端开发服务器已配置代理，API 请求会自动转发到 http://localhost:8080。

### 6. 生产部署

构建前端并放入后端 static 目录，然后打包运行：

`ash
# 构建前端
cd exam-vue
npm run build:prod

# 将 dist 目录内容复制到后端 static 目录
cp -r dist/* ../exam-api/src/main/resources/static/

# 构建后端 jar 包
cd ../exam-api
mvn clean package -DskipTests

# 运行
java -jar target/online-exam.jar
`

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | admin | admin | 拥有全部权限 |
| 教师 | teacher1 | teacher1 | 管理考试、题库、试卷 |
| 学生 | person | person | 参加考试、查看成绩 |

## 角色权限说明

| 功能模块 | 超级管理员(sa) | 教师(teacher) | 学生(student) |
|----------|:---:|:---:|:---:|
| 控制台统计 | 全量数据 | 本人数据 | 本人数据 |
| 考试管理 | 全量 | 本人创建 | 仅查看可参加考试 |
| 题库管理 | 全量 | 本人创建 | 不可见 |
| 试卷管理 | 全量 | 本人相关 | 仅本人试卷 |
| 用户管理 | 全部操作 | 仅导入学生 | 不可见 |
| 角色管理 | 全部操作 | 不可见 | 不可见 |
| 部门管理 | 全部操作 | 不可见 | 不可见 |
| 系统配置 | 全部操作 | 不可见 | 不可见 |
| 通知公告 | 全部操作 | 全部操作 | 不可见 |
| 参加考试 | 不可使用 | 不可使用 | 可用 |
| 查看成绩 | 不可使用 | 不可使用 | 本人成绩 |
| 错题本 | 不可使用 | 不可使用 | 可用 |

## 安全特性

- **JWT 认证**：无状态 Token 认证，有效期 7 天
- **Shiro 权限控制**：基于角色的访问控制（RBAC），注解使用 OR 逻辑满足任一角色即可
- **试卷所有权校验**：学生只能操作自己的试卷，防止越权查看/修改他人试卷
- **角色导入限制**：教师批量导入用户时只能导入学生角色，超管可导入全部角色
- **密码加密**：MD5 + 随机盐值加密
- **SPA 路由支持**：Vue History 模式路由刷新不 404

## 常见问题

### 1. 前端访问后端 404

确认后端已启动且端口为 8080。前端开发模式通过 ue.config.js 中的 proxy 转发 API 请求。

### 2. 直接访问页面路由 404

后端已配置 SpaRoutingFilter，会将非 API 的 GET 请求转发到 index.html，由 Vue Router 接管。

### 3. 数据库连接失败

检查 pplication-dev.yml 中的数据库配置，确保 MySQL 服务已启动且 yy_exam 数据库已创建。

### 4. 文件上传失败

检查 conf.upload.dir 配置的目录是否存在且有写入权限。

### 5. 登录报"账号或密码错误"

确认数据库已导入 SQL 脚本，默认账号密码见上方表格。

## 许可证

MIT License
