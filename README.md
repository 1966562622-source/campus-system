# 校园课程管理系统

## 一、项目简介

本项目是一个基于 **Spring Boot 4.1.1** 开发的校园课程管理系统，主要用于管理学生信息、课程信息以及学生选课业务。

系统采用 **Controller → Service → Mapper** 分层架构，集成了 **Spring Security + JWT** 无状态认证、**Redis 缓存**、**MyBatis-Plus** 持久层框架，使用 **MySQL** 存储业务数据。

在选课业务中，系统实现了**重复选课校验**、**课程容量限制**、**事务管理**和基于 `SELECT ... FOR UPDATE` 的**悲观锁并发控制**，有效防止超额选课；同时利用 **Redis 分布式锁**（Lua 脚本）保证课程缓存重建的原子性。

---

## 二、技术栈

| 类别 | 技术 |
|------|------|
| 开发语言 | Java 17 |
| 后端框架 | Spring Boot 4.1.1 |
| 安全框架 | Spring Security + JWT（jjwt 0.12.6） |
| 持久层 | MyBatis-Plus 3.5.17 |
| 数据库 | MySQL（MySQL Connector/J） |
| 缓存 | Spring Data Redis |
| JSON 序列化 | Jackson |
| 密码加密 | BCryptPasswordEncoder |
| 参数校验 | Jakarta Bean Validation |
| 工具库 | Lombok |
| 构建工具 | Maven |

---

## 三、项目结构

```
src/main/java/com/example/campus/
├── CampusSystemApplication.java       # 启动类
├── common/
│   └── Result.java                    # 统一响应封装
├── config/
│   ├── MybatisPlusConfig.java         # MyBatis-Plus 分页插件配置
│   └── SecurityConfig.java            # Spring Security 安全配置
├── controller/
│   ├── AuthController.java            # 认证接口（登录、获取当前用户）
│   ├── StudentController.java         # 学生 CRUD 接口
│   ├── CourseController.java          # 课程 CRUD 及分页查询接口
│   ├── StudentCourseController.java   # 选课 / 退课接口
│   └── RedisTestController.java       # Redis 读写测试接口
├── dto/
│   ├── LoginRequest.java              # 登录请求 DTO
│   ├── CourseCreateDTO.java           # 创建课程请求 DTO
│   └── SelectedCourseDTO.java         # 已选课程响应 DTO
├── entity/
│   ├── Student.java                   # 学生实体（student 表）
│   ├── Course.java                    # 课程实体（course 表）
│   ├── StudentCourse.java             # 选课关联实体（student_course 表）
│   └── SysUser.java                   # 系统用户实体（sys_user 表）
├── exception/
│   ├── BusinessException.java         # 业务异常类
│   └── GlobalExceptionHandler.java    # 全局异常处理器
├── mapper/
│   ├── StudentMapper.java             # 学生 Mapper（含关联查询）
│   ├── CourseMapper.java              # 课程 Mapper（含 FOR UPDATE 查询）
│   ├── StudentCourseMapper.java       # 选课 Mapper
│   └── SysUserMapper.java             # 用户 Mapper
├── security/
│   ├── JwtAuthenticationFilter.java   # JWT 认证过滤器
│   ├── JwtUtil.java                   # JWT 工具类（生成/解析）
│   └── LoginUser.java                 # 登录用户主体
├── service/
│   ├── AuthService.java               # 认证服务（登录验证、Token 生成）
│   ├── StudentService.java            # 学生服务
│   ├── CourseService.java             # 课程服务（含 Redis 缓存策略）
│   └── StudentCourseService.java      # 选课服务（含并发控制）
└── vo/
    ├── LoginVo.java                   # 登录响应 VO
    └── CourseVO.java                  # 课程响应 VO
```

---

## 四、数据库设计

### 4.1 学生表（student）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO) | 学生 ID |
| name | VARCHAR | 姓名 |
| age | INT | 年龄 |
| email | VARCHAR | 邮箱 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.2 课程表（course）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO) | 课程 ID |
| name | VARCHAR | 课程名称 |
| teacher | VARCHAR | 授课教师 |
| credit | DECIMAL | 学分 |
| capacity | INT | 课程容量（选课人数上限） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.3 选课关联表（student_course）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO) | 记录 ID |
| student_id | BIGINT | 学生 ID（外键） |
| course_id | BIGINT | 课程 ID（外键） |
| create_time | DATETIME | 选课时间 |

### 4.4 系统用户表（sys_user）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO) | 用户 ID |
| username | VARCHAR | 用户名 |
| password | VARCHAR | 密码（BCrypt 加密） |
| role | VARCHAR | 角色（如 ADMIN） |
| created_at | DATETIME | 创建时间 |

### 4.5 表关系

- 学生与课程为 **多对多** 关系，通过 `student_course` 关联表维护。
- 一个学生可选多门课程，一门课程可被多个学生选择。

---

## 五、API 接口说明

> 除 `/api/auth/login` 外，所有接口需要在请求头携带 JWT Token：
> ```
> Authorization: Bearer <token>
> ```

### 5.1 认证接口

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录，返回 JWT Token | 无 |
| GET | `/api/auth/me` | 获取当前登录用户信息 | 已登录 |

**登录请求示例：**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "123456"
}
```

**登录响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

---

### 5.2 学生管理接口

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/students/{id}` | 根据 ID 查询学生 | 已登录 |
| GET | `/students` | 查询全部学生列表 | 已登录 |
| POST | `/students` | 新增学生 | 已登录 |
| PUT | `/students/{id}` | 更新学生信息 | 已登录 |
| DELETE | `/students/{id}` | 删除学生（含关联选课记录） | **ADMIN** |
| GET | `/students/{studentId}/courses` | 查询某学生的已选课程 | 已登录 |

---

### 5.3 课程管理接口

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/courses/{id}` | 查询课程详情（带 Redis 缓存） | 已登录 |
| GET | `/courses` | 查询全部课程列表 | 已登录 |
| POST | `/courses` | 新增课程 | 已登录 |
| PUT | `/courses/{id}` | 更新课程（带分布式锁 + 缓存删除） | 已登录 |
| DELETE | `/courses/{id}` | 删除课程（同步删除缓存） | 已登录 |
| GET | `/courses/page` | 课程分页查询（支持按名称/教师筛选） | 已登录 |
| GET | `/courses/{courseId}/students` | 查询某课程的选课学生名单 | 已登录 |
| GET | `/courses/{courseId}/students/page` | 某课程选课学生分页查询 | 已登录 |

**分页查询参数：**

| 参数 | 默认值 | 说明 |
|------|--------|------|
| current | 1 | 当前页码（≥1） |
| size | 10 | 每页条数（1~100） |
| name | 无 | 课程名称模糊匹配（可选） |
| teacher | 无 | 教师精确匹配（可选） |

**创建课程请求示例：**
```json
POST /courses
{
  "name": "高等数学",
  "teacher": "张教授",
  "credit": 4.0,
  "capacity": 50
}
```

---

### 5.4 选课 / 退课接口

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/students/{studentId}/courses/{courseId}` | 学生选课 | 已登录 |
| DELETE | `/students/{studentId}/courses/{courseId}` | 学生退课 | 已登录 |

---

### 5.5 Redis 测试接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/test/redis/set` | 写入测试值到 Redis |
| GET | `/test/redis/get` | 从 Redis 读取测试值 |

---

## 六、核心机制详解

### 6.1 认证与鉴权

- 用户登录成功后，服务端生成 JWT Token（包含 `userId`、`username`、`role`），有效期 **2 小时**（可通过 `jwt.expiration` 配置）。
- 请求到达后由 [JwtAuthenticationFilter](file:///Users/xiaokeai/Downloads/campus-system/src/main/java/com/example/campus/security/JwtAuthenticationFilter.java) 解析 Token 并设置 `SecurityContext`。
- 角色权限控制：删除学生接口仅 `ADMIN` 角色可访问，通过 `@PreAuthorize("hasRole('ADMIN')")` 注解实现。

### 6.2 Redis 缓存策略（Cache-Aside）

课程查询使用 **Cache-Aside** 模式：

1. **读流程**：先查 Redis 缓存 → 命中直接返回；未命中则查数据库，回写缓存（过期时间 600~900 秒随机）。
2. **防缓存击穿**：使用 Redis `SETNX` 实现**分布式锁**，锁的释放通过 **Lua 脚本**保证原子性；未获取锁的请求自旋重试（最多 20 次，每次间隔 100ms）。
3. **防缓存穿透**：查询不存在的课程时，写入 `__NULL__` 空值缓存（60 秒过期）。
4. **写流程**：更新课程时获取分布式锁 → 更新数据库 → 删除缓存；删除课程时同步删除缓存。

### 6.3 并发选课控制

选课操作 [StudentCourseService.selectCourse](file:///Users/xiaokeai/Downloads/campus-system/src/main/java/com/example/campus/service/StudentCourseService.java) 使用 **悲观锁**：

1. 通过 `SELECT ... FOR UPDATE` 对课程记录加行锁。
2. 在事务内完成：校验学生/课程是否存在 → 校验重复选课 → 校验课程容量 → 插入选课记录。
3. 成功选课后删除相关课程的学生缓存。

### 6.4 统一异常处理

[GlobalExceptionHandler](file:///Users/xiaokeai/Downloads/campus-system/src/main/java/com/example/campus/exception/GlobalExceptionHandler.java) 统一拦截以下异常并返回标准 JSON 响应：

| 异常类型 | HTTP 状态码 | 说明 |
|----------|-------------|------|
| `MethodArgumentNotValidException` | 400 | 请求体参数校验失败 |
| `ConstraintViolationException` | 400 | 路径/查询参数校验失败 |
| `IllegalArgumentException` | 400 | 非法参数（如分页 size > 100） |
| `BadCredentialsException` | 401 | 用户名或密码错误 |
| `BusinessException` | 自定义 | 业务异常（如课程不存在、满员等） |
| `Exception` | 500 | 未知系统异常 |

### 6.5 统一响应格式

所有接口返回统一格式 [Result](file:///Users/xiaokeai/Downloads/campus-system/src/main/java/com/example/campus/common/Result.java)：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

## 七、运行环境

| 环境 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |

---

## 八、项目启动步骤

### 1. 克隆项目

```bash
git clone <repository-url>
cd campus-system
```

### 2. 创建数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE campus_system DEFAULT CHARACTER SET utf8mb4;
```

创建数据表（参考第四节"数据库设计"中的表结构）。

### 3. 配置环境变量

项目通过环境变量注入敏感配置，启动前需设置：

```bash
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=your_jwt_secret_key_at_least_32_bytes
```

> `JWT_SECRET` 至少需要 **32 字节**（32 个字符）。

配置文件 [application.yaml](file:///Users/xiaokeai/Downloads/campus-system/src/main/resources/application.yaml) 中其他可调参数：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/campus_system` | 数据库连接 |
| `spring.data.redis.host` | `localhost` | Redis 地址 |
| `spring.data.redis.port` | `6379` | Redis 端口 |
| `jwt.expiration` | `7200000`（2 小时） | Token 有效期（毫秒） |

### 4. 构建与启动

```bash
# 编译打包
mvn clean package -DskipTests

# 启动
java -jar target/campus-system-0.0.1-SNAPSHOT.jar
```

或在 IDE 中直接运行 [CampusSystemApplication](file:///Users/xiaokeai/Downloads/campus-system/src/main/java/com/example/campus/CampusSystemApplication.java) 的 `main` 方法。

### 5. 验证

```bash
# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 使用返回的 Token 访问受保护接口
curl http://localhost:8080/students \
  -H "Authorization: Bearer <your-token>"
```