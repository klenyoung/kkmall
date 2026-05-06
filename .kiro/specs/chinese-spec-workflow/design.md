# 中文 Spec 工作流规范设计

## 架构与模块边界

本需求只影响项目协作规范文档，不涉及前端、后端、数据库或部署架构。

文档边界：

- 根目录 `agents.md`：面向所有 Agent 的总协作规范。
- `.kiro/steering/chinese-language-hook.md`：长期生效的中文优先 Hook。
- `.kiro/specs/chinese-spec-workflow/`：记录本次规范变更的 spec 文档。

## 数据模型变化

无数据库、领域模型或 API 数据结构变化。

## API 变化

无 API 变化。

## 文档设计

### agents.md

改为中文主体，包含：

- 主要语言规则。
- 中文优先 Hook 位置。
- spec-first 总规则。
- spec 目录约定。
- `requirements.md`、`design.md`、`tasks.md` 最小结构。
- 需求澄清规则。
- 设计澄清规则。
- 实现前门禁。
- 完成规则。
- 当前项目上下文。

### chinese-language-hook.md

使用 `.kiro/steering` 的 always inclusion 形式：

```yaml
---
inclusion: always
---
```

Hook 内容定义：

- 默认中文回复。
- 默认中文编写 `.kiro/specs/**`。
- 默认中文更新 `docs/**` 和 `agents.md`。
- 工程标识符、API、配置、命令等保持英文或原始格式。

## 状态流和执行流程

后续新需求流程：

1. 用户提出需求。
2. Agent 使用中文进行需求澄清。
3. Agent 创建或更新中文 `requirements.md`。
4. Agent 使用中文进行设计澄清。
5. Agent 创建或更新中文 `design.md`。
6. Agent 创建或更新中文 `tasks.md`。
7. Agent 实现并验证。
8. Agent 用中文输出交付说明。

## 异常处理与边界情况

- 用户明确要求英文时，以用户当次要求为准。
- 代码、API、命令、错误码不强制翻译。
- 已存在的历史英文内容不强制一次性翻译。

## 兼容性与迁移

- 不影响运行时。
- 不影响已有 `.kiro/specs/b2c-ecommerce-platform`。
- 后续新 spec 使用中文，旧 spec 可在被修改时逐步中文化。

## 测试策略

- 检查新增文件存在。
- 检查 `agents.md` 关键中文规则存在。
- 检查 Hook 包含 `inclusion: always` 和中文优先规则。

## 设计澄清记录

- Hook 采用 `.kiro/steering/chinese-language-hook.md` 实现，因为项目已有 `.kiro/steering` 规则目录。
- 本次不新增自动化校验脚本，先以协作规则方式落地。
