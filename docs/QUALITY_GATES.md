# Quality Gates

## Local commands

```bash
./gradlew qualityStatic
./gradlew qualityCoverage
./gradlew qualityCheck
./gradlew securityReport
```

Style guide for enforced rules:
- `docs/STYLE_GUIDELINES.md`

Technical debt register for code references (`TD-xxx`):
- `docs/TECH_DEBT.md`

## Coverage thresholds

- `coverage.line.min` in `gradle.properties`
- `coverage.branch.min` in `gradle.properties`

## Test runtime settings

- `test.mockserver.port` in `gradle.properties` (can be overridden by `TEST_MOCKSERVER_PORT`)

## CI workflows

- `.github/workflows/quality.yml`
  - `quality/static` (required)
  - `quality/tests-coverage` (required)
- `.github/workflows/security.yml`
  - `security/dependency-check` (report-only)
  - `security/codeql` (report-only)
  - `security/trivy` (report-only)
