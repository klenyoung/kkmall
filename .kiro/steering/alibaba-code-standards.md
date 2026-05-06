---
inclusion: fileMatch
fileMatchPattern: "**/*.{java,ts,tsx,js,jsx,css,scss,less,sql,xml,yml,yaml,json}"
---

# 阿里巴巴前后端代码规范

所有生成的代码必须严格遵守本规范。本规范基于《阿里巴巴 Java 开发手册（泰山版）》及阿里前端代码规范整理。

---

## 一、Java 后端规范

### 1. 命名规约

- **【强制】** 命名不能以下划线或美元符号开始或结束。反例：`_name`、`name_`、`$name`
- **【强制】** 严禁使用拼音与英文混合命名，禁止直接使用中文。反例：`DaZhePromotion`、`getPingfenByName()`
- **【强制】** 类名使用 `UpperCamelCase` 风格，以下例外：`DO / BO / DTO / VO / AO / PO / UID`
  - 正例：`UserDO`、`XmlService`、`TcpUdpDeal`
- **【强制】** 方法名、参数名、成员变量、局部变量统一使用 `lowerCamelCase` 风格
  - 正例：`localValue`、`getHttpMessage()`、`inputUserId`
- **【强制】** 常量命名全部大写，单词间用下划线隔开
  - 正例：`MAX_STOCK_COUNT`，反例：`MAX_COUNT`
- **【强制】** 抽象类命名使用 `Abstract` 或 `Base` 开头；异常类命名使用 `Exception` 结尾；测试类以被测类名开始，以 `Test` 结尾
- **【强制】** POJO 类中布尔类型变量不要加 `is` 前缀，否则部分框架解析会引起序列化错误
- **【强制】** 包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词，使用单数形式
- **【强制】** Service/DAO 类，暴露的服务必须是接口，实现类用 `Impl` 后缀
  - 正例：`CacheServiceImpl` 实现 `CacheService`
- **【强制】** 枚举类名建议带 `Enum` 后缀，枚举成员名称全大写，单词间用下划线
  - 正例：`ProcessStatusEnum`，成员：`SUCCESS / UNKNOWN_REASON`

**各层命名规约：**
- Service/DAO 层方法：`get`（单个）、`list`（多个）、`count`（统计）、`save/insert`（插入）、`remove/delete`（删除）、`update`（修改）
- 领域模型：`xxxDO`（数据对象）、`xxxDTO`（数据传输）、`xxxVO`（展示对象）、禁止命名 `xxxPOJO`

---

### 2. 常量定义

- **【强制】** 不允许任何魔法值（未经预先定义的常量）直接出现在代码中
- **【强制】** `long` 或 `Long` 赋值时，数值后使用大写 `L`，不能是小写 `l`
- **【推荐】** 不要使用一个常量类维护所有常量，按功能归类分开维护
  - 正例：缓存相关常量放 `CacheConsts`，系统配置放 `ConfigConsts`
- **【推荐】** 变量值仅在固定范围内变化时，用 `enum` 类型定义

---

### 3. 代码格式

- **【强制】** 采用 **4 个空格**缩进，禁止使用 tab 字符
- **【强制】** 单行字符数限制不超过 **120 个**，超出需换行
- **【强制】** `if/for/while/switch/do` 等保留字与括号之间必须加空格
- **【强制】** 任何二目、三目运算符的左右两边都需要加一个空格
- **【强制】** 左大括号前不换行，左大括号后换行；右大括号前换行
- **【强制】** 注释的双斜线与注释内容之间有且仅有一个空格：`// 注释内容`
- **【强制】** 文件编码设置为 UTF-8，换行符使用 Unix 格式（LF）
- **【推荐】** 单个方法总行数不超过 **80 行**
- **【推荐】** 不同逻辑、不同语义的代码之间插入一个空行分隔

---

### 4. OOP 规约

- **【强制】** 所有覆写方法必须加 `@Override` 注解
- **【强制】** 不能使用过时的类或方法
- **【强制】** `Object` 的 `equals` 方法容易抛空指针，应使用常量或确定有值的对象调用
  - 正例：`"test".equals(object)`，反例：`object.equals("test")`
- **【强制】** 所有相同类型的包装类对象之间值的比较，全部使用 `equals` 方法
- **【强制】** POJO 类属性必须使用包装数据类型；RPC 方法返回值和参数必须使用包装数据类型；局部变量推荐使用基本数据类型
- **【强制】** 定义 DO/DTO/VO 等 POJO 类时，不要设定任何属性默认值
- **【强制】** 构造方法里禁止加入任何业务逻辑，初始化逻辑放在 `init` 方法中
- **【强制】** POJO 类必须写 `toString` 方法
- **【推荐】** 类内方法定义顺序：公有方法或保护方法 > 私有方法 > getter/setter 方法
- **【推荐】** 循环体内字符串连接使用 `StringBuilder.append()`，禁止使用 `+` 拼接
- **【推荐】** 类成员与方法访问控制从严，能 `private` 就不用 `protected`，能 `protected` 就不用 `public`

---

### 5. 集合处理

- **【强制】** 重写 `equals` 就必须重写 `hashCode`
- **【强制】** 不要在 `foreach` 循环里进行元素的 `remove/add` 操作，`remove` 请使用 `Iterator` 方式
- **【强制】** 使用 `entrySet` 遍历 Map，而不是 `keySet`
- **【推荐】** 集合初始化时指定集合初始值大小
  - 正例：`new HashMap<>(16)`
- **【推荐】** 高度注意 Map 类集合 K/V 能否存储 null 值：`ConcurrentHashMap` 的 key 和 value 均不允许为 null

---

### 6. 并发处理

- **【强制】** 线程资源必须通过线程池提供，不允许在应用中自行显式创建线程
- **【强制】** 线程池不允许使用 `Executors` 创建，必须通过 `ThreadPoolExecutor` 方式，并指定有意义的线程名称
- **【强制】** `SimpleDateFormat` 是线程不安全的类，不要定义为 `static` 变量；JDK 8 推荐使用 `DateTimeFormatter`
- **【强制】** 并发修改同一记录时，避免更新丢失，需要加锁（应用层/缓存/数据库乐观锁）
- **【推荐】** 使用 `ThreadLocalRandom` 代替 `Random` 实例在多线程场景下的使用

---

### 7. 控制语句

- **【强制】** `switch` 块内每个 `case` 要么通过 `break/return` 终止，要么注释说明；必须包含 `default` 语句
- **【强制】** `if/else/for/while/do` 语句中必须使用大括号，即使只有一行代码
- **【推荐】** 表达异常分支时，少用 `if-else`，使用卫语句（Guard Clause）提前返回；`if-else` 嵌套不超过 **3 层**
- **【推荐】** 避免采用取反逻辑运算符，使用正向逻辑表达

---

### 8. 注释规约

- **【强制】** 类、类属性、类方法的注释必须使用 Javadoc 规范：`/** 内容 */`，不得使用 `// xxx` 方式
- **【强制】** 所有抽象方法（包括接口中的方法）必须用 Javadoc 注释，说明做什么、实现什么功能
- **【强制】** 所有类必须添加创建者和创建日期
- **【强制】** 方法内部单行注释在被注释语句上方另起一行，使用 `//` 注释
- **【强制】** 所有枚举类型字段必须有注释，说明每个数据项的用途
- **【推荐】** 代码修改的同时，注释也要进行相应修改
- **【推荐】** 谨慎注释掉代码，无用代码直接删除（代码仓库保存历史）

---

### 9. 异常处理

- **【强制】** 不要用异常做流程控制、条件控制
- **【强制】** 捕获异常是为了处理它，不要捕获了却什么都不处理
- **【强制】** `finally` 块必须对资源对象、流对象进行关闭；JDK 7+ 使用 `try-with-resources`
- **【强制】** 不要在 `finally` 块中使用 `return`
- **【强制】** 有 `try` 块放到了事务代码中，`catch` 异常后如果需要回滚事务，必须手动回滚
- **【推荐】** 避免直接抛出 `new RuntimeException()`，使用有业务含义的自定义异常，如 `ServiceException`
- **【推荐】** 对外 HTTP/API 接口使用错误码方式返回；应用内部推荐异常抛出

---

### 10. 日志规约

- **【强制】** 应用中不可直接使用 Log4j、Logback 的 API，必须依赖 SLF4J 门面：
  ```java
  private static final Logger logger = LoggerFactory.getLogger(Xxx.class);
  ```
- **【强制】** `trace/debug/info` 级别的日志输出，必须使用占位符方式：
  ```java
  logger.debug("Processing trade with id: {} and symbol: {}", id, symbol);
  ```
- **【强制】** 异常信息应包括案发现场信息和异常堆栈信息：
  ```java
  logger.error(params.toString() + "_" + e.getMessage(), e);
  ```
- **【强制】** 生产环境禁止输出 `debug` 日志
- **【推荐】** `error` 级别只记录系统逻辑出错、异常或重要错误信息；用户输入参数错误使用 `warn` 级别

---

### 11. MySQL 数据库规约

- **【强制】** 表达是与否概念的字段，必须使用 `is_xxx` 命名，数据类型为 `unsigned tinyint`
- **【强制】** 表名、字段名必须使用小写字母或数字，禁止出现大写字母
- **【强制】** 表名不使用复数名词
- **【强制】** 主键索引名为 `pk_字段名`；唯一索引名为 `uk_字段名`；普通索引名为 `idx_字段名`
- **【强制】** 小数类型为 `decimal`，禁止使用 `float` 和 `double`
- **【强制】** 表必备三字段：`id`（bigint unsigned 主键）、`gmt_create`（datetime）、`gmt_modified`（datetime）
- **【强制】** 禁止使用外键与级联，外键概念在应用层解决
- **【强制】** 禁止使用存储过程
- **【强制】** SQL 参数严格使用参数绑定，禁止字符串拼接 SQL（防 SQL 注入）
- **【强制】** 查询禁止使用 `SELECT *`，必须明确写出字段列表
- **【强制】** 超过三个表禁止 JOIN
- **【强制】** 页面搜索严禁左模糊或全模糊，需要模糊搜索走搜索引擎
- **【推荐】** 单表行数超过 500 万行或容量超过 2GB 才考虑分库分表
- **【推荐】** 字符集统一使用 `utf8mb4`

---

### 12. 应用分层（DDD 适配）

本项目采用 DDD 架构，各层职责如下：

```
Controller（Web 层）
    ↓
Application Service（应用服务层）
    ↓
Domain Service + Domain Model（领域层）
    ↓
Repository / DAO（基础设施层）
```

- **Controller 层**：参数校验、请求转发，不包含业务逻辑，返回 VO
- **Application Service 层**：编排领域服务，处理事务，输入 DTO，输出 DTO
- **Domain 层**：核心业务逻辑，聚合根、领域服务、领域事件，不依赖基础设施
- **Repository 层**：数据持久化，返回 DO，禁止跨领域直接 JOIN

**对象命名规范：**
- `XxxDO`：数据对象，与数据库表一一对应
- `XxxDTO`：数据传输对象，Service 层向外传输
- `XxxVO`：展示对象，Controller 层向前端传输
- `XxxCommand`：命令对象，前端向后端传入的写操作请求
- `XxxQuery`：查询对象，前端向后端传入的查询请求

---

### 13. 安全规约

- **【强制】** 用户个人页面或功能必须进行权限控制校验，防止水平越权
- **【强制】** 用户敏感数据禁止直接展示，必须脱敏（如手机号中间 4 位隐藏）
- **【强制】** 用户输入的 SQL 参数严格使用参数绑定，禁止字符串拼接 SQL
- **【强制】** 用户请求传入的任何参数必须做有效性验证
- **【强制】** 禁止向 HTML 页面输出未经安全过滤或未正确转义的用户数据（防 XSS）
- **【强制】** 表单、AJAX 提交必须执行 CSRF 安全验证
- **【强制】** 短信、支付等平台资源必须实现防重放机制（数量限制、验证码校验）

---

## 二、React + TypeScript 前端规范

### 1. 命名规约

- **组件文件**：使用 `PascalCase`，如 `UserProfile.tsx`、`OrderList.tsx`
- **非组件文件**：使用 `camelCase`，如 `useOrderList.ts`、`orderService.ts`
- **组件名**：与文件名保持一致，使用 `PascalCase`
- **变量/函数**：使用 `camelCase`，如 `getUserInfo`、`orderList`
- **常量**：使用 `UPPER_SNAKE_CASE`，如 `MAX_PAGE_SIZE`
- **类型/接口**：使用 `PascalCase`，接口不加 `I` 前缀，如 `UserInfo`、`OrderDetail`
- **枚举**：使用 `PascalCase`，枚举值使用 `UPPER_SNAKE_CASE`
- **CSS 类名**：使用 `kebab-case`，如 `user-profile`、`order-list`
- **事件处理函数**：以 `handle` 开头，如 `handleSubmit`、`handleOrderClick`
- **布尔变量**：以 `is`、`has`、`can`、`should` 开头，如 `isLoading`、`hasError`

---

### 2. 组件规范

- **【强制】** 使用函数组件 + Hooks，禁止使用 Class 组件（新代码）
- **【强制】** 组件 Props 必须定义 TypeScript 类型，使用 `interface` 或 `type`
  ```tsx
  interface UserCardProps {
    userId: string;
    userName: string;
    onSelect?: (userId: string) => void;
  }
  ```
- **【强制】** 组件必须有明确的返回类型声明（`React.FC` 或显式 `JSX.Element`）
- **【强制】** 禁止使用 `any` 类型，使用 `unknown` 替代不确定类型
- **【强制】** Props 解构时提供默认值
  ```tsx
  function UserCard({ userId, userName, onSelect = () => {} }: UserCardProps) {}
  ```
- **【推荐】** 单个组件文件不超过 **300 行**，超出则拆分子组件
- **【推荐】** 组件内部代码顺序：Hooks → 派生状态/计算值 → 事件处理函数 → 渲染逻辑
- **【推荐】** 避免在 JSX 中写复杂逻辑，提取到变量或函数中

---

### 3. Hooks 规范

- **【强制】** 自定义 Hook 必须以 `use` 开头，如 `useOrderList`、`useUserInfo`
- **【强制】** 遵守 Hooks 使用规则：只在函数组件顶层调用，不在条件/循环中调用
- **【强制】** `useEffect` 必须声明完整的依赖数组，不允许遗漏依赖
- **【强制】** `useEffect` 中有副作用（订阅、定时器）必须返回清理函数
  ```tsx
  useEffect(() => {
    const timer = setInterval(fetchData, 5000);
    return () => clearInterval(timer);
  }, [fetchData]);
  ```
- **【推荐】** 将复杂的状态逻辑提取到自定义 Hook 中，保持组件简洁
- **【推荐】** 使用 `useCallback` 缓存传递给子组件的回调函数，避免不必要的重渲染
- **【推荐】** 使用 `useMemo` 缓存计算开销大的值

---

### 4. TypeScript 规范

- **【强制】** 禁止使用 `any`，必要时使用 `unknown` 并做类型收窄
- **【强制】** 函数必须声明参数类型和返回值类型
  ```ts
  function calculateTotal(items: OrderItem[]): number {}
  ```
- **【强制】** 使用 `interface` 定义对象类型，使用 `type` 定义联合类型、交叉类型
- **【强制】** 枚举值必须显式赋值
  ```ts
  enum OrderStatus {
    PENDING = 'PENDING',
    PAID = 'PAID',
    SHIPPED = 'SHIPPED',
  }
  ```
- **【强制】** 使用可选链 `?.` 和空值合并 `??` 代替手动 null 检查
- **【推荐】** 优先使用 `const` 断言和 `as const` 而非类型断言 `as Type`
- **【推荐】** 使用泛型提高代码复用性，避免重复类型定义
- **【推荐】** API 响应类型统一定义，放在 `types/` 目录下

---

### 5. 状态管理规范

- **【强制】** 组件内部状态使用 `useState`，跨组件共享状态使用全局状态管理（Redux Toolkit / Zustand）
- **【强制】** 禁止直接修改 state，必须通过 setter 函数或 dispatch
- **【推荐】** 服务端数据（API 请求结果）使用专门的数据获取库管理（如 React Query / SWR），不要手动管理 loading/error/data 三态
- **【推荐】** 全局状态按领域模块拆分，避免单一巨型 store

---

### 6. 样式规范

- **【强制】** 使用 CSS Modules 或 CSS-in-JS，禁止使用全局 CSS 类名（避免样式污染）
- **【强制】** 禁止使用内联样式（`style={{}}`），除非是动态计算的样式值
- **【强制】** 颜色、字体、间距等设计 token 使用 CSS 变量或主题变量，禁止硬编码
- **【推荐】** 响应式布局优先使用 Flexbox 和 Grid，避免使用绝对定位
- **【推荐】** 组件样式文件与组件文件同目录，命名为 `ComponentName.module.css`

---

### 7. API 请求规范

- **【强制】** 所有 API 请求封装在 `services/` 目录下，禁止在组件中直接调用 `fetch/axios`
- **【强制】** API 请求必须处理 loading、success、error 三种状态
- **【强制】** 统一的错误处理：网络错误、业务错误码统一拦截处理
- **【强制】** 请求参数和响应数据必须有 TypeScript 类型定义
- **【推荐】** 使用请求拦截器统一添加 Token、处理 401 跳转登录

---

### 8. 国际化（i18n）规范

- **【强制】** 所有用户可见的文案必须通过 i18n 函数处理，禁止硬编码中文或英文字符串
  ```tsx
  // 正例
  const title = t('order.list.title');
  // 反例
  const title = '订单列表';
  ```
- **【强制】** i18n key 使用点分隔的命名空间格式：`模块.页面.元素`
  - 正例：`order.detail.status.pending`、`user.profile.edit.save`
- **【强制】** 语言包文件按模块拆分，放在 `locales/` 目录下
- **【推荐】** 日期、数字、货币格式化使用 `Intl` API 或 i18n 库的格式化功能

---

### 9. 性能规范

- **【强制】** 列表渲染必须提供唯一且稳定的 `key`，禁止使用数组 index 作为 key（列表有增删时）
- **【强制】** 图片必须设置 `width` 和 `height` 属性，避免布局抖动（CLS）
- **【推荐】** 路由级别的代码分割（`React.lazy` + `Suspense`）
- **【推荐】** 大列表使用虚拟滚动（如 `react-virtual`）
- **【推荐】** 避免在渲染函数中创建新的对象/数组/函数，使用 `useMemo`/`useCallback` 缓存

---

### 10. 可访问性（Accessibility）规范

- **【强制】** 交互元素（按钮、链接）必须有可访问的文本标签
- **【强制】** 图片必须有 `alt` 属性（装饰性图片使用 `alt=""`）
- **【强制】** 表单元素必须有关联的 `label`
- **【推荐】** 使用语义化 HTML 标签（`nav`、`main`、`article`、`section`）
- **【推荐】** 确保键盘可导航，焦点顺序合理

---

## 三、通用规范

### 代码提交规范（Conventional Commits）

提交信息格式：`<type>(<scope>): <subject>`

- `feat`: 新功能
- `fix`: Bug 修复
- `refactor`: 重构（不影响功能）
- `style`: 代码格式调整
- `test`: 测试相关
- `docs`: 文档更新
- `chore`: 构建/工具链变更

正例：`feat(order): 新增订单取消功能`、`fix(payment): 修复微信支付回调幂等问题`

### DRY 原则

- **【强制】** 避免重复代码（Don't Repeat Yourself），相同逻辑必须抽取为公共方法/组件/Hook
- **【强制】** 公共工具函数放在 `utils/` 目录，公共组件放在 `components/common/` 目录

### 安全编码

- **【强制】** 所有用户输入必须做校验和转义，防止 XSS、SQL 注入
- **【强制】** 敏感信息（密码、Token、密钥）禁止打印到日志
- **【强制】** 接口必须做权限校验，防止越权访问

---

> 参考来源：[阿里巴巴 Java 开发手册（泰山版）](https://github.com/alibaba/p3c) | 阿里前端代码规范
