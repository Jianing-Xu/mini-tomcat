# mini-tomcat

`mini-tomcat` 是一个用于学习 Tomcat 核心机制的最小可运行 Servlet 容器实现。当前已完成 Phase 1，范围严格限定在“HTTP 请求进入后，到 Servlet 执行并返回响应”的最小闭环。

## 当前实现范围

- `SIMPLE_BIO` Connector
- `Engine -> Host -> Context -> Wrapper` 四层容器树
- 基于 `web.xml` 的静态 Servlet 注册与 URL 映射
- Servlet `init / service / destroy` 生命周期
- Phase 1 examples、curl 手工验收与最小 JUnit 测试

当前不包含：

- Session
- ClassLoader 隔离
- Filter / Listener
- 注解部署
- 完整 Pipeline / Valve 执行链
- keep-alive / chunked / HTTP2 / TLS

## 目录说明

```text
docs/                     设计文档与阶段文档
tests/                    验收文档
conf/phase1-basic/        Phase 1 示例配置
examples/phase1-basic/    可运行示例与手工验收入口
src/main/java/            容器实现代码
src/test/java/            Phase 1 自动化测试
```

## 快速开始

编译并运行测试：

```bash
mvn clean test
```

打包：

```bash
mvn -DskipTests package
```

启动 Phase 1 示例：

```bash
mvn -DskipTests exec:java -Dexec.mainClass=examples.phase1basic.Phase1ExampleMain
```

默认启动地址：

```text
http://localhost:8080/app
```

## 手工验证

正常请求：

```bash
curl -i http://localhost:8080/app/demo
```

Context 未命中：

```bash
curl -i http://localhost:8080/unknown/demo
```

Servlet 未命中：

```bash
curl -i http://localhost:8080/app/missing
```

Servlet 异常返回 500：

```bash
curl -i http://localhost:8080/app/error
```

响应已提交后异常不覆写：

```bash
curl -i http://localhost:8080/app/partial
```

映射冲突校验：

```bash
mvn -DskipTests exec:java -Dexec.mainClass=examples.phase1basic.Phase1ConflictCheckMain
```

## 文档入口

- 总体蓝图：[docs/architecture-tomcat.md](/Users/xjn/Develop/projects/java/mini-tomcat/docs/architecture-tomcat.md)
- Phase 1 设计：[docs/tomcat-phase-1.md](/Users/xjn/Develop/projects/java/mini-tomcat/docs/tomcat-phase-1.md)
- Phase 1 验收：[tests/acceptance-tomcat-phase-1.md](/Users/xjn/Develop/projects/java/mini-tomcat/tests/acceptance-tomcat-phase-1.md)
- 示例运行说明：[examples/phase1-basic/README.md](/Users/xjn/Develop/projects/java/mini-tomcat/examples/phase1-basic/README.md)
