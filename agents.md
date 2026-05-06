# KKMall Agent 协作规范

本文件是 KKMall 仓库的 AI 协作与需求交付规范。后续所有 Agent 在处理本项目需求时，必须优先遵守本文件。

## 1. 主要语言

- 后续回答、需求澄清、设计澄清、spec 文档、任务文档和交付说明，主要语言必须使用中文。
- 代码标识符、API 路径、类名、方法名、配置键、命令、日志、错误码等工程实体保持英文或原始格式。
- 如引用英文技术名词，优先保留英文原词，并在必要时补充中文解释。
- 若用户明确要求使用英文或双语，则按用户当次要求执行。

## 2. 中文优先 Hook

本仓库启用中文优先 Hook：

```text
.kiro/steering/chinese-language-hook.md
```

该 Hook 用于提醒后续 Agent：

- 默认用中文回复用户。
- 默认用中文编写 `.kiro/specs` 下的需求、设计、任务文档。
- 默认用中文更新 `docs/` 下的产品、设计、部署和开发文档。
- 不翻译代码中的英文命名，不把 Java/TypeScript/SQL 标识符改成中文。

## 3. Spec-First 总规则

所有新需求必须先进入 spec 模式，再进入实现：

1. 需求澄清
2. 需求 Spec
3. 设计澄清
4. 设计 Spec
5. 任务拆分
6. 代码实现
7. 验证与文档更新

在需求澄清、设计澄清和 spec 文档完成前，不得直接开始实现。这个规则称为实现前门禁。

## 4. Spec 目录

每个需求必须创建或更新独立特性目录：

```text
.kiro/specs/<feature-name>/
```

目录名使用简短英文短横线命名，例如：

```text
.kiro/specs/product-image-upload/
.kiro/specs/admin-product-editor/
.kiro/specs/chinese-spec-workflow/
```

较大的需求必须先拆分为多个 spec，不要把互不相关的能力混在同一个 spec 中。

## 5. 必需文档

每个需求至少包含：

```text
.kiro/specs/<feature-name>/requirements.md
.kiro/specs/<feature-name>/design.md
.kiro/specs/<feature-name>/tasks.md
```

### 5.1 requirements.md

必须包含：

- 背景与问题
- 目标与成功标准
- 目标用户与使用场景
- 用户故事或用例
- 功能需求
- 非功能需求
- 范围外事项
- 风险、约束与假设
- 验收标准
- 需求澄清记录

### 5.2 design.md

必须包含：

- 架构与模块边界
- 领域模型或数据模型变化
- API 变化与请求/响应结构
- 前端页面、组件与交互设计
- 状态流和核心业务流程
- 异常处理与边界情况
- 兼容性、迁移与发布说明
- 测试策略
- 设计澄清记录

### 5.3 tasks.md

必须包含：

- 后端任务
- 前端任务
- 数据库或迁移任务
- 测试任务
- 联调与部署任务
- 验证清单
- 实现状态

## 6. 需求澄清

编写或更新 `requirements.md` 前，必须澄清：

- 这个需求解决什么业务问题？
- 谁会使用这个能力？
- 哪些用户旅程必须可用？
- 哪些内容在范围内，哪些明确不做？
- 验收标准是什么？
- 有哪些风险、依赖和约束？
- 优先级和交付边界是什么？

澄清后的决策必须记录到 `requirements.md`。

## 7. 设计澄清

进入实现计划前，必须澄清：

- 采用什么技术方案？
- 是否需要数据模型或数据库结构变化？
- 新增或变更哪些 API？
- 前端需要哪些页面、表单、状态和交互？
- 有哪些兼容、迁移或回滚问题？
- 失败模式和边界情况如何处理？
- 哪些自动化测试和人工验收能证明完成？

澄清后的决策必须记录到 `design.md`。

## 8. 实现前门禁

当用户要求“实现”“开发”“修复”“继续”等操作时：

1. 先检查该需求是否已有完整 spec。
2. 若没有 spec，先创建 `requirements.md`、`design.md`、`tasks.md`。
3. 若 spec 不完整，先补齐需求澄清和设计澄清。
4. 只有 spec 和任务拆分完成后，才能进入代码实现。

Bug 修复可以使用较小 spec，但只要影响产品行为、API、数据或 UI，就必须保留可追踪的需求说明、设计说明和任务清单。

## 9. 完成规则

实现完成后必须：

- 更新对应 `tasks.md` 的任务状态。
- 如果实现中发生设计变化，同步更新 `requirements.md` 或 `design.md`。
- 执行相关验证命令。
- 用中文说明变更内容、验证结果和剩余风险。

## 10. 当前项目上下文

KKMall 是单商家自营 B2C 电商项目：

- 后端：Spring Boot、Spring Security、MyBatis-Plus、Flyway、战术 DDD。
- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Ant Design Vue。
- 基础设施：MySQL、RabbitMQ、MinIO，可用时使用 Docker Compose。

后续变更必须尊重现有 DDD 包边界、`/api/v1` API 前缀、统一响应格式和 `.kiro/specs` 文档结构。
