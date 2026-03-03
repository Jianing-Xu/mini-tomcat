# mini-tomcat

Phase 1 implements a minimal runnable servlet container with:

- SIMPLE_BIO connector
- Engine / Host / Context / Wrapper hierarchy
- `web.xml` servlet registration
- Servlet `init/service/destroy` lifecycle
- examples plus curl based verification

Quick start:

```bash
mvn clean test
mvn -DskipTests exec:java -Dexec.mainClass=examples.phase1basic.Phase1ExampleMain
```

Detailed run and verification steps live in `examples/phase1-basic/README.md`.
