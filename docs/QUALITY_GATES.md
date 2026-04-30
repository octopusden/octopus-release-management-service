# Quality Gates

Quality gates in this repo follow the unified octopusden standard. The shared
contract (workflows, convention plugin, ruleset) lives in
[octopusden/octopus-base](https://github.com/octopusden/octopus-base) — see
[`docs/Octopus JVM Style Guidelines.md`](https://github.com/octopusden/octopus-base/blob/main/docs/Octopus%20JVM%20Style%20Guidelines.md)
for the source of truth.

Local additions below document ORMS-specific configuration (coverage
thresholds, FT exclusions, overrides) layered on top of the shared standard.

## Local commands

Full local gate (requires Docker daemon for `qualityCoverage`/`qualityCheck`):

```bash
./gradlew qualityStatic
./gradlew qualityCoverage
./gradlew qualityCheck
./gradlew securityReport
```

`quality/tests-coverage` job command in CI:

```bash
./gradlew qualityCoverage -x :release-management-service:test --no-daemon --stacktrace
```

The convention plugin wires `qualityCoverage` → `koverXmlReport` + `koverVerify`
automatically when `coverage.enabled = true` in the `octopusQuality` block
(set in root `build.gradle.kts`). The only exclusion is the heavy Spring Boot
integration test `:release-management-service:test`.

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

PR merge is gated by the **`gate/merge`** check published by
`.github/workflows/merge-gate.yml`. This workflow fans out to three reusable
workflows from `octopusden/octopus-base@v2.3.3` and aggregates their results
via the shared `merge-gate` composite action.

- `.github/workflows/merge-gate.yml` — **PR-time entry point** (triggers on `pull_request`)
  - Calls `build.yml` → `common-java-gradle-build.yml` (compile + unit tests)
  - Calls `quality.yml` → `common-java-gradle-quality-gates.yml`
    - `quality/wrapper-validation`
    - `quality/static` (detekt + ktlint, blocking with baselines)
    - `quality/tests-coverage` (Kover, blocking)
  - Calls `security.yml` → `common-java-gradle-security-reports.yml`
    - `security/codeql` (blocking — findings addressed via GitHub Code Scanning)
    - `security/trivy` (blocking)
    - `security/dependency-check` (currently disabled via `enable-dependency-check: false`)
  - `gate/merge` job — fails unless every dependency reports `success`

- `.github/workflows/quality.yml` — also runs on `push: [main]` and `workflow_dispatch`
- `.github/workflows/security.yml` — also runs on `push: [main]`, nightly `cron`, and `workflow_dispatch`

Branch protection on `main` requires the `gate/merge` check context (applied
via the `jvm-strict` ruleset from `octopusden/octopus-base`).
