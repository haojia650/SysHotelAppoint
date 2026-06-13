# SysHotelAppoint

[English Version](./README.en.md)

`SysHotelAppoint` 是一个酒店预订系统，包含前端、后端和数据库脚本三部分：

- 前端：`SysHotelAppoint.elementui`
- 后端：`SysHotelAppoint.springboot`
- 数据库脚本：`syshotelappoint.sql`

项目当前采用前后端分离架构：

- 前端：`Vue 3 + Vite + Element Plus + Pinia`
- 后端：`Spring Boot 3 + MyBatis-Plus + MySQL + WebSocket`

## 目录结构

```text
SysHotelAppoint/
├─ SysHotelAppoint.elementui/      # 前端项目
├─ SysHotelAppoint.springboot/     # 后端项目
├─ syshotelappoint.sql             # 数据库初始化脚本
├─ README.md
└─ README.en.md
```

## 运行环境

建议使用以下环境：

- Node.js 18+
- npm 9+
- JDK 17
- MySQL 8.x
- Maven 3.9+ 或项目自带 `mvnw`

## 本地启动

### 1. 导入数据库

创建数据库，例如：

```sql
CREATE DATABASE SysHotelAppoint CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后导入根目录下的 `syshotelappoint.sql`。

### 2. 配置后端本地参数

后端公开配置文件为：

- `SysHotelAppoint.springboot/src/main/resources/application.yml`

本地敏感配置文件不会提交到 Git：

- `SysHotelAppoint.springboot/application-local.yml`

你需要在后端目录创建这个文件，并填写本地数据库和邮件配置。示例：

```yml
server:
  ip: http://localhost:7245
  port: 7245

spring:
  mail:
    host: smtp.qq.com
    username: your_mail@example.com
    password: your_mail_password
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/SysHotelAppoint?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

### 3. 启动后端

进入后端目录：

```powershell
cd SysHotelAppoint.springboot
.\mvnw.cmd spring-boot:run
```

默认端口：

- API：`http://localhost:7245`

### 4. 启动前端

进入前端目录：

```powershell
cd SysHotelAppoint.elementui
npm install
npm run dev
```

默认开发端口：

- Frontend：`http://localhost:8080`

## 构建

### 前端构建

```powershell
cd SysHotelAppoint.elementui
npm install
npm run build
```

### 后端打包

```powershell
cd SysHotelAppoint.springboot
.\mvnw.cmd -DskipTests package
```

## 环境变量

后端 `application.yml` 已改为优先读取环境变量，主要变量如下：

- `SERVER_PUBLIC_BASE_URL`
- `SERVER_PORT`
- `MAIL_HOST`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

适合部署到云平台时使用。

## 当前仓库说明

为了避免将本地敏感信息和大体积运行时资源提交到 GitHub，以下内容默认不纳入版本控制：

- `SysHotelAppoint.springboot/application-local.yml`
- `SysHotelAppoint.springboot/external-resources/`
- `SysHotelAppoint.springboot/target/`
- `SysHotelAppoint.elementui/node_modules/`
- IDE 配置目录，如 `.idea/`、`.vscode/`

其中：

- `external-resources` 是后端运行过程中上传生成的资源目录
- 该目录未上传到仓库，因此数据库中的部分演示图片链接在新环境中可能需要重新上传或重新初始化

## 部署提示

当前代码库可以本地运行，但如果要部署到线上，还需要继续处理以下内容：

- 前端生产环境接口地址
- 前端 WebSocket 地址
- 后端文件上传 URL 生成逻辑
- 数据库、邮件等生产环境变量

## 仓库地址

- GitHub: [haojia650/SysHotelAppoint](https://github.com/haojia650/SysHotelAppoint)

