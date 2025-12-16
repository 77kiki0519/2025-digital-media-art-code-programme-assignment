## 📦 项目分享说明

### 1. 包含的关键文件
- `README.md` - 详细的项目说明文档
- `start.sh` - 一键启动脚本
- `backend/` - 后端代码（Spring Boot）
- `frontend/` - 前端代码（Vue 3）

### 2. 他人运行步骤

#### 环境要求
确保安装了以下软件：
- Java 17+
- Node.js 16+
- MySQL 8.0+

#### 启动流程

1. **数据库配置**
   ```sql
   CREATE DATABASE online_course_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **修改数据库连接信息**
   编辑 `backend/src/main/resources/application.properties` 中的数据库用户名和密码

3. **运行启动脚本**
   ```bash
   cd 280
   ./start.sh
   ```

4. **访问系统**
   - 本地访问：`http://localhost:3000`
   - 网络访问：`http://[本机IP]:3000`

### 3. 核心功能支持

✅ **视频播放功能**：
- 支持原生视频播放
- 支持B站视频嵌入播放（通过iframe）
- 自动检测视频类型

✅ **课程管理功能**：
- 课程、章节、视频的创建和管理
- 教师权限控制
- 支持添加外部视频链接

### 4. 默认账号

```
用户名：admin
密码：admin123
```
```
用户名：student1
密码：student123
```

```
用户名：teacher1
密码：teacher123
```

## 📖 详细文档

已创建的 `README.md` 包含：
- 完整的技术栈说明
- 详细的环境配置步骤
- 前后端启动指南
- 常见问题解答
- 开发说明

## 🚀 一键启动

`start.sh` 脚本会：
1. 检查环境版本（Java/Node.js）
2. 启动后端服务
3. 安装前端依赖（如果需要）
4. 启动前端服务
5. 显示访问地址
