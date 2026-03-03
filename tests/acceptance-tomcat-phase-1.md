# Tomcat Phase 1 Acceptance

## 1. 验收范围

- 范围：`SIMPLE_BIO` Connector、Container 四层路由、`WEB_XML` 静态映射、Servlet 生命周期、shutdown 资源释放。
- 不含：Session、ClassLoader、Filter、Listener、注解部署、Valve 执行链。

## 2. Given / When / Then 验收用例

### 2.1 正常路径

#### 用例 1：启动最小容器成功

- Given
  - 存在 1 个 Engine、1 个 Host、1 个 Context。
  - `web.xml` 中声明 1 个 Servlet 和 1 条唯一 URL mapping。
- When
  - 执行 `Bootstrap.start()`。
- Then
  - 容器树启动成功。
  - Connector 成功绑定顶层 Engine。
  - Wrapper 完成 ServletDefinition 注册。
  - 系统进入可接收请求状态。

#### 用例 2：首次请求触发 Servlet 初始化并完成响应

- Given
  - Context 中存在 `/demo` 到 `DemoServlet` 的唯一映射。
  - `DemoServlet` 尚未初始化。
- When
  - 客户端向 `/demo` 发起一次 HTTP 请求。
- Then
  - Connector 成功解析请求并构造 Request/Response。
  - Engine、Host、Context、Wrapper 按顺序完成路由。
  - Wrapper 先调用 `init`，再调用 `service`。
  - Response 返回业务内容且状态码为 200。

#### 用例 3：后续请求复用已初始化 Servlet

- Given
  - `DemoServlet` 已完成一次初始化。
- When
  - 客户端再次向 `/demo` 发起请求。
- Then
  - Wrapper 不再重复调用 `init`。
  - 仅调用 `service`。
  - 请求处理成功并返回 200。

### 2.2 失败路径

#### 用例 4：Context 未命中返回 404

- Given
  - Host 下仅部署 Context `/app`。
- When
  - 客户端请求 `/unknown/demo`。
- Then
  - Host 路由后无法命中 Context。
  - 容器直接返回 404。
  - 不触发任何 Wrapper 与 Servlet 调用。

#### 用例 5：Wrapper 未命中返回 404

- Given
  - Context `/app` 已存在。
  - `web.xml` 未注册 `/app/missing` 对应的 Servlet。
- When
  - 客户端请求 `/app/missing`。
- Then
  - Context 路由成功。
  - Wrapper 映射失败。
  - 返回 404。

#### 用例 6：映射冲突导致 startup 失败

- Given
  - 同一 Context 的 `web.xml` 中，两个不同 Servlet 注册了同一条精确 URL pattern。
- When
  - 执行 `Bootstrap.start()`。
- Then
  - Context 在注册映射阶段识别冲突。
  - Context 启动失败。
  - Connector 不得进入可接收请求状态。

#### 用例 7：Servlet 抛出异常且响应未提交时返回 500

- Given
  - `/error` 已映射到 `ErrorServlet`。
  - `ErrorServlet.service` 在写响应前抛出异常。
- When
  - 客户端请求 `/error`。
- Then
  - Wrapper 捕获或上传异常到容器错误处理边界。
  - Response 状态码为 500。
  - 响应体为容器错误输出或空响应体。

#### 用例 8：响应已提交后抛异常不得覆写响应

- Given
  - `/partial` 已映射到 `PartialServlet`。
  - `PartialServlet.service` 先写出响应并提交，再抛出异常。
- When
  - 客户端请求 `/partial`。
- Then
  - 原响应状态码与响应体保持不变。
  - 容器只记录异常，不覆写已提交响应。

### 2.3 shutdown 与资源释放

#### 用例 9：shutdown 反序销毁已初始化 Servlet

- Given
  - 至少 2 个 Wrapper 已分别初始化各自 Servlet。
- When
  - 执行 `Bootstrap.stop()`。
- Then
  - Connector 先停止接收新请求。
  - Wrapper、Context、Host、Engine 按反序停止。
  - 每个已初始化 Servlet 的 `destroy` 恰好执行一次。

#### 用例 10：未初始化 Servlet 在 shutdown 不执行 destroy

- Given
  - 某 Wrapper 已注册但从未收到请求，Servlet 尚未初始化。
- When
  - 执行 `Bootstrap.stop()`。
- Then
  - 该 Wrapper 不调用 `destroy`。
  - 停机流程继续完成。

## 3. 验收判定矩阵

| 场景 | 期望状态码 | 关键判定 |
| --- | --- | --- |
| 正常命中 Servlet | `200` | 完整经过 Connector 与四层容器路由，首次请求触发 `init` |
| Context 未命中 | `404` | Host 已确定，Context 未命中，终止于 Context 路由 |
| Wrapper 未命中 | `404` | Context 已命中，Wrapper 未命中，Servlet 不执行 |
| Servlet 未提交前异常 | `500` | 异常转换为容器错误响应 |
| 响应已提交后异常 | 保持原值 | 不覆写响应，只记录异常 |
| 映射冲突 | 启动失败 | Connector 不进入运行态 |

## 4. 资源释放与关闭验证策略（shutdown）

- 验证 Connector 停止后不再接受新请求。
- 验证 shutdown 顺序固定为 `Connector -> Wrapper -> Context -> Host -> Engine` 的停止入口与 `destroy` 释放责任。
- 验证所有已初始化 Servlet 的 `destroy` 调用次数为 1。
- 验证未初始化 Servlet 不触发 `destroy`。
- 验证映射表、部署定义、线程池引用在 stop 后进入不可用状态。
