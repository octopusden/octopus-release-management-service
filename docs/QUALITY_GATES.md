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
./gradlew qualityCoverage -x :release-management-service:test -x koverVerify -x koverCachedVerify
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
  - `quality/tests-coverage` (required, currently runs `qualityCoverage` with exclusions from CI workflow)
- `.github/workflows/security.yml`
  - `security/dependency-check` (report-only, temporarily disabled via `if: ${{ false }}`)
  - `security/codeql` (report-only)
  - `security/trivy` (report-only)
