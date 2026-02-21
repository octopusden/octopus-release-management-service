# Tech Debt Register

This file tracks known technical debt items with stable IDs referenced from code comments.

## Open Items

| ID | Area | Location | Description | Next step |
| --- | --- | --- | --- | --- |
| TD-001 | automation CLI | `automation/src/main/kotlin/org/octopusden/octopus/automation/releasemanagement/Application.kt` | Only part of commands is registered in `subcommands(...)`. | Add missing commands and integration tests for command routing. |
| TD-002 | FT TeamCity integration | `ft/src/ft/kotlin/org/octopusden/octopus/releasemanagementservice/TriggerTest.kt` | Builds are read via raw HTTP (`readBuilds(...)`) because current Teamcity client wrapper does not expose required builds API in tests. | Extend client wrapper with builds endpoint and migrate tests to typed client calls. |
| TD-003 | FT TeamCity integration | `ft/src/ft/kotlin/org/octopusden/octopus/releasemanagementservice/TriggerTest.kt` | Agent authorization is done via raw HTTP in `beforeAll()`. | Extend client wrapper with agent authorization endpoint and migrate test bootstrap. |

## How To Reference In Code

Use `TD-xxx` in comments and point to this file.

Example:

```kotlin
// TD-002: switch to TeamcityClient builds API once supported (see docs/TECH_DEBT.md).
```
