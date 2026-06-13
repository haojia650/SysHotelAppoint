# SysHotelAppoint

[中文版本](./README.md)

`SysHotelAppoint` is a hotel reservation system that includes three main parts:

- Frontend: `SysHotelAppoint.elementui`
- Backend: `SysHotelAppoint.springboot`
- Database script: `syshotelappoint.sql`

The project uses a separated frontend/backend architecture:

- Frontend: `Vue 3 + Vite + Element Plus + Pinia`
- Backend: `Spring Boot 3 + MyBatis-Plus + MySQL + WebSocket`

## Project Structure

```text
SysHotelAppoint/
├─ SysHotelAppoint.elementui/      # Frontend project
├─ SysHotelAppoint.springboot/     # Backend project
├─ syshotelappoint.sql             # Database initialization script
├─ README.md
└─ README.en.md
```

## Requirements

Recommended environment:

- Node.js 18+
- npm 9+
- JDK 17
- MySQL 8.x
- Maven 3.9+ or the bundled `mvnw`

## Run Locally

### 1. Import the Database

Create a database, for example:

```sql
CREATE DATABASE SysHotelAppoint CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then import `syshotelappoint.sql` from the repository root.

### 2. Configure Local Backend Settings

The public backend config file is:

- `SysHotelAppoint.springboot/src/main/resources/application.yml`

The local sensitive config file is intentionally not committed:

- `SysHotelAppoint.springboot/application-local.yml`

Create this file in the backend directory and fill in your local database and mail settings. Example:

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

### 3. Start the Backend

Go to the backend directory:

```powershell
cd SysHotelAppoint.springboot
.\mvnw.cmd spring-boot:run
```

Default backend URL:

- API: `http://localhost:7245`

### 4. Start the Frontend

Go to the frontend directory:

```powershell
cd SysHotelAppoint.elementui
npm install
npm run dev
```

Default frontend URL:

- Frontend: `http://localhost:8080`

## Build

### Build the Frontend

```powershell
cd SysHotelAppoint.elementui
npm install
npm run build
```

### Package the Backend

```powershell
cd SysHotelAppoint.springboot
.\mvnw.cmd -DskipTests package
```

## Environment Variables

The backend `application.yml` has been refactored to read environment variables first. Main variables:

- `SERVER_PUBLIC_BASE_URL`
- `SERVER_PORT`
- `MAIL_HOST`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

This is suitable for cloud deployment.

## Repository Notes

To avoid pushing local secrets and large runtime assets to GitHub, the following paths are ignored by default:

- `SysHotelAppoint.springboot/application-local.yml`
- `SysHotelAppoint.springboot/external-resources/`
- `SysHotelAppoint.springboot/target/`
- `SysHotelAppoint.elementui/node_modules/`
- IDE folders such as `.idea/` and `.vscode/`

Notes:

- `external-resources` is the runtime upload/output directory used by the backend
- Since it is not committed, some demo image links from the database may need to be re-uploaded or re-initialized in a new environment

## Deployment Notes

The repository can run locally, but additional work is still needed before production deployment:

- Production frontend API base URL
- Production WebSocket URL
- Backend file upload URL generation
- Production database and mail environment variables

## Repository

- GitHub: [haojia650/SysHotelAppoint](https://github.com/haojia650/SysHotelAppoint)

