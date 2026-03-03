# Phase 1 Example

Build:

```bash
mvn clean test
mvn -DskipTests package
```

Run:

```bash
mvn -DskipTests exec:java -Dexec.mainClass=examples.phase1basic.Phase1ExampleMain
```

Verify:

```bash
curl -i http://localhost:8080/app/demo
curl -i http://localhost:8080/unknown/demo
curl -i http://localhost:8080/app/missing
curl -i http://localhost:8080/app/error
curl -i http://localhost:8080/app/partial
```

Conflict check:

```bash
mvn -DskipTests exec:java -Dexec.mainClass=examples.phase1basic.Phase1ConflictCheckMain
```
