## BaoTao 电商示例项目

基于 Spring Boot + Vue3 + MySQL 的简单电商网站，用于完成《网络应用开发》课程实验。

- **后端**：Spring Boot 2.7.x / Java 8 / Spring Security / JPA / MySQL / JWT
- **前端**：Vue 3 + Vite + Element Plus + Pinia
- **部署**：Docker / docker-compose（后端 + 前端 + MySQL）

### 学生信息

- 姓名：潘昊
- 学号：20233045141
- 课程：网络应用开发

---

### 功能概览

- 顾客：
  - 用户注册、登录、注销（基于 JWT）
  - 浏览和搜索商品列表、查看商品详情
  - 购物车管理：添加、修改数量、删除
  - 下单与模拟付款：从购物车结算生成订单
  - 查看订单列表和历史订单详情
  - 下单后发送邮件确认（若配置了 SMTP）
- 管理员（销售）：
  - 商品目录管理：增删改商品信息
  - 订单管理：查看所有订单、按状态筛选、更新订单状态（发货 / 取消）
  - 销售统计报表：按日销售额统计、热销商品 Top 列表
  - 客户管理：查看所有用户及其浏览 / 购买日志

默认管理员账号（启动时自动初始化）：

- 用户名：`admin`
- 密码：`admin123`

---

### 本地运行后端

#### 1. 环境要求

- JDK 8
- Maven 3.x
- MySQL 8（本地或远程均可）

#### 2. 数据库准备

1. 创建数据库：

```sql
CREATE DATABASE baotaodb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 根据实际环境修改 `src/main/resources/application.properties` 中的：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

#### 3. 启动后端

```bash
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

---

### 本地运行前端

1. 安装 Node.js（建议 18+）。
2. 在 `frontend` 目录下安装依赖并启动开发服务器：

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，通过 Vite 代理访问后端 `/api`。

---

### 使用 Docker 本地运行（与服务器部署方式相同）

确保本机已安装 Docker 与 docker-compose。

```bash
# 在项目根目录
docker-compose build
docker-compose up -d
```

容器说明：

- `baotao-mysql`：MySQL 数据库（端口 `3306`）
- `baotao-backend`：Spring Boot 后端（端口 `8080`）
- `baotao-frontend`：前端 Nginx 容器（端口 `80`）

启动成功后，通过浏览器访问：

- `http://localhost/`：前端页面
- 使用管理员账号 `admin/admin123` 登录后台

---

### 在华为云 CentOS 8 上的部署步骤（简要）

1. **安装 Docker（若尚未安装）**  
   按官方文档安装 Docker Engine，并配置开机自启。

2. **安装 docker-compose（插件或独立二进制）**

3. **获取代码**

```bash
git clone <你的仓库地址> baotao
cd baotao
```

4. **构建并启动**

```bash
docker-compose build
docker-compose up -d
```

5. **在华为云控制台开放端口**  
   在安全组中放通 `80`（HTTP）与需要的端口（如 `8080` 或 `3306` 仅限内网）。

6. **通过公网访问**  
   在浏览器中访问：`http://你的云服务器公网IP/`，使用测试账号登录并验证功能。

以上步骤和结果可以整理到实验报告的“应用部署”部分，并附上关键命令和截图。


