---
inclusion: fileMatch
fileMatchPattern: "backend/src/main/java/**"
---

# DDD 分层架构规范

本项目后端采用战术 DDD 分层架构，所有新增或修改的代码必须遵守以下规范。

## 分层职责

```
interfaces（接口层）
    ↓ DTO
application（应用服务层）
    ↓ Domain Model
domain（领域层）
    ↑ Domain Model
infrastructure（基础设施层）
```

### interfaces 层
- 负责 HTTP 请求接收、参数校验、响应组装
- 只操作 DTO 对象，不直接操作 PO 或领域对象
- Controller 返回 `ApiResponse<XxxDto>`，禁止返回 `Map<String, Object>`
- Request 类使用 Lombok `@Data` + JSR-303 校验注解

### application 层
- 编排领域服务，处理事务
- 只操作领域对象（Domain Model），不直接操作 PO
- 通过 Repository 接口获取和保存领域对象
- 通过 MapStruct Mapper 将领域对象转为 DTO 返回给接口层

### domain 层
- 核心业务逻辑，聚合根、值对象、领域服务、领域事件
- 不依赖基础设施层（不 import PO、Mapper、Spring 注解）
- Repository 接口定义在领域层
- 业务规则校验在领域对象内部完成

### infrastructure 层
- 数据持久化实现
- PO 类（`@Data` + `@TableName`）与数据库表一一对应
- Repository 实现类负责 PO ↔ Domain Model 转换
- MyBatis-Plus Mapper 接口

## 对象命名规范

| 对象类型 | 命名 | 位置 | 说明 |
|---------|------|------|------|
| 领域对象 | `User`、`Order` | domain 包 | 聚合根，包含业务规则 |
| 值对象 | `PhoneNumber`、`Money` | domain 包 | 不可变，自校验 |
| 持久化对象 | `UserPo`、`OrderPo` | infrastructure 包 | 与数据库表对应 |
| 数据传输对象 | `UserProfileDto`、`LoginResultDto` | interfaces.dto 包 | 接口层响应 |
| 请求对象 | `ProfileRequest`、`LoginRequest` | application 或 interfaces 包 | 接口层入参 |

## 转换规范

使用 MapStruct 框架进行对象转换：

- **PO ↔ Domain Model**：在 infrastructure 层定义 `XxxConverter`（`@Mapper(componentModel = "spring")`）
- **Domain Model → DTO**：在 interfaces.dto 包定义 `XxxDtoMapper`（`@Mapper(componentModel = "spring")`）
- 禁止手动 new DTO 并逐字段 set（除非字段极少或有特殊逻辑）
- MapStruct 编译时生成实现类，运行时零反射开销

## Repository 规范

- 接口定义在 domain 层：`public interface UserRepository`
- 实现在 infrastructure 层：`@Repository public class UserRepositoryImpl implements UserRepository`
- Repository 方法返回领域对象，不返回 PO
- Repository 内部使用 Converter 完成 PO ↔ Domain Model 转换

## 示范模块

`account` 模块已完成完整的 DDD 分层改造，可作为其他模块的参考：

```
account/
├── domain/
│   ├── User.java              # 聚合根
│   ├── Gender.java            # 枚举
│   ├── PhoneNumber.java       # 值对象
│   ├── Role.java              # 枚举
│   └── UserRepository.java    # 仓储接口
├── infrastructure/
│   ├── UserPo.java            # 持久化对象
│   ├── UserMapper.java        # MyBatis-Plus Mapper
│   ├── UserConverter.java     # PO ↔ Domain Model（MapStruct）
│   └── UserRepositoryImpl.java # 仓储实现
├── application/
│   ├── AccountProfileApplicationService.java
│   └── AuthApplicationService.java
└── interfaces/
    ├── AccountProfileController.java
    ├── AuthController.java
    └── dto/
        ├── UserProfileDto.java
        ├── UserProfileDtoMapper.java  # Domain → DTO（MapStruct）
        ├── LoginResultDto.java
        ├── MockCodeResultDto.java
        └── UploadResultDto.java
```
