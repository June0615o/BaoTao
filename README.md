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

如果你所在网络访问 Docker Hub 不稳定/被限制，可以先在 `.env` 中把：

- `BASE_REGISTRY` 改成可用的镜像站（例如 `registry.cn-hangzhou.aliyuncs.com`）
- `MYSQL_IMAGE` 改成对应镜像（例如 `registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0`）

```bash
# 在项目根目录
copy env.example .env
docker compose -f docker-compose.yml -f docker-compose.build.yml build
docker compose -f docker-compose.yml -f docker-compose.build.yml up -d
```

容器说明：

- `baotao-mysql`：MySQL 数据库（容器内网端口 `3306`，默认不对宿主机暴露）
- `baotao-backend`：Spring Boot 后端（端口 `8080`）
- `baotao-frontend`：前端 Nginx 容器（端口 `80`）

启动成功后，通过浏览器访问：

- `http://localhost/`：前端页面
- 使用管理员账号 `admin/admin123` 登录后台

---

### 在华为云 CentOS 8 上的部署步骤（推荐：镜像仓库 pull 部署）

核心思路是：**你在 Win11 本地构建并 push 镜像**，云服务器只负责 **pull 镜像并 docker compose up**，这样部署速度更快、也不需要服务器具备完整的构建环境。

#### 0）前置：准备镜像仓库

- 任选一个镜像仓库：
  - Docker Hub（最简单）
  - GHCR（GitHub Container Registry）
- 在 `env.example` 里把下面两项改成你的仓库地址，然后复制成 `.env`：
  - `BAOTAO_BACKEND_IMAGE=...`
  - `BAOTAO_FRONTEND_IMAGE=...`

#### 1）Win11 本地：build + push 镜像

在项目根目录执行（PowerShell / CMD 都可以）：

```bash
copy env.example .env
docker login
docker compose -f docker-compose.yml -f docker-compose.build.yml build
docker compose -f docker-compose.yml -f docker-compose.build.yml push
```

> 说明：`docker-compose.yml` 负责“运行参数”（端口、环境变量、依赖关系），`docker-compose.build.yml` 只负责 “build 指令”。  
> push 完成后，你的云服务器就能直接 `pull` 到同样的镜像。

#### 2）CentOS 8 服务器：安装 Docker + Compose 插件

下面命令按官方仓库安装（CentOS 8）：

```bash
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
docker compose version
```

#### 3）CentOS 8 服务器：拉代码（或仅拷贝 compose 文件）并启动

```bash
git clone <你的仓库地址> baotao
cd baotao
cp env.example .env
vi .env   # 把镜像名、MYSQL_ROOT_PASSWORD 等改成与你本地 push 的一致

docker login
docker compose pull
docker compose up -d
docker compose ps
```

#### 4）华为云安全组/防火墙放行端口（公网可访问）

- 必开：`80/tcp`（访问前端入口，Nginx 会反代 `/api` 到后端）
- 可选：`8080/tcp`（一般不建议对公网开放；调试时才临时开放）
- 不建议公网开放：`3306/tcp`（数据库应仅容器内网访问）

CentOS 防火墙（若开启了 firewalld）：

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload
```

#### 5）公网访问验证

- 浏览器访问：`http://你的云服务器公网IP/`
- API（可选）：`http://你的云服务器公网IP/api/products`

> 如果你后续要绑定域名并上 HTTPS，建议在前端容器前再加一层反向代理（例如 Caddy / Nginx）自动签发证书；当前项目用 `80` 直出也能满足“公网可访问”的课程要求。

以上步骤和结果可以整理到实验报告的“应用部署”部分，并附上关键命令和截图。


