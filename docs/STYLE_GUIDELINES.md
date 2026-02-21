# Style Guidelines

This document describes non-obvious style-related checks and current violation statistics.
Obvious checks like `unused*` are intentionally omitted.

## Scope

- `detekt` config: `config/detekt/detekt.yml`
- `ktlint` config: `.editorconfig`
- Technical debt tracking: `*/detekt-baseline.xml`, `*/ktlint-baseline.xml`, `docs/TECH_DEBT.md`
- Detekt default values referenced below are from `detekt-core-1.23.5` (`default-detekt-config.yml`).

## Enabled Checks (CI Gate)

Statistics below are based on:
- current detekt baselines;
- current ktlint baselines.

| Rule | Open violations | Source |
| --- | --- | --- |
| `detekt:style:ClassOrdering` | 0 | baseline |
| `detekt:style:ForbiddenComment` | 0 | baseline |
| `detekt:style:MaxLineLength` | 12 | baseline |
| `detekt:style:NewLineAtEndOfFile` | 64 | baseline |
| `detekt:style:ProtectedMemberInFinalClass` | 16 | baseline |
| `detekt:style:WildcardImport` | 0 | baseline |
| `detekt:style:UseCheckOrError` | 2 | baseline |
| `detekt:naming:TopLevelPropertyNaming` | 0 | baseline |
| `ktlint:standard:chain-method-continuation` | 50 | baseline |

For enabled checks, code links are listed only for not-yet-fixed cases.

### `ktlint` numeric criteria (active)

- `max_line_length = 140` (from `.editorconfig`, for `*.kt` and `*.kts`)

### `ktlint:standard:chain-method-continuation` (newly enforced after ktlint upgrade)

Meaning:
- In multiline call chains, place a newline before `.` (dot starts the next line).

Wrong:
```kotlin
val value = source.map { it.id }.filter { it > 0 }
```

Right:
```kotlin
val value =
    source
        .map { it.id }
        .filter { it > 0 }
```

### Other Newly Baselined `ktlint` Checks (after upgrade)

These checks are enabled and currently have temporary baseline exceptions.

| Rule | New baseline entries | What it enforces | Criteria |
| --- | ---: | --- | --- |
| `standard:class-signature` | 71 | multiline class/constructor signatures must be wrapped consistently | n/a |
| `standard:final-newline` | 28 | file must end with newline | `insert_final_newline = true` |
| `standard:function-expression-body` | 22 | single-expression functions should use `=` expression body | n/a |
| `standard:binary-expression-wrapping` | 5 | long binary expressions should be wrapped consistently | n/a |
| `standard:string-template-indent` | 4 | multiline string template interpolation must be correctly indented | n/a |
| `standard:max-line-length` | 4 | line length limit | `max_line_length = 140` |
| `standard:function-literal` | 2 | lambda formatting in complex expressions | n/a |
| `standard:argument-list-wrapping` | 2 | long argument lists should be wrapped consistently | n/a |
| `standard:block-comment-initial-star-alignment` | 1 | `*` alignment in block comments | n/a |

Total new baseline entries for these checks: `139`  
(`50` for `chain-method-continuation` is tracked separately above; total new ktlint baseline entries in upgrade commit: `189`).

### `detekt:style:ClassOrdering`

`companion object` should be placed at the end of class body.

Wrong:
```kotlin
class VelocityEngine {
    companion object { ... }
    fun generate(...) = ...
}
```

Right:
```kotlin
class VelocityEngine {
    fun generate(...) = ...

    companion object { ... }
}
```

### `detekt:style:WildcardImport`

Do not use wildcard imports.

Wrong:
```kotlin
import org.example.dto.*
```

Right:
```kotlin
import org.example.dto.BuildDTO
import org.example.dto.BuildFilterDTO
```

### `detekt:style:ForbiddenComment`

Do not add `TODO/FIXME/STOPSHIP` comments. Use `TD-xxx` + `docs/TECH_DEBT.md`.

Wrong:
```kotlin
// TODO: remove after release
```

Right:
```kotlin
// TD-002: switch to TeamcityClient builds API once supported (see docs/TECH_DEBT.md).
```

### `detekt:style:UseCheckOrError` (open)

Use `require/check/error` instead of manual `throw IllegalStateException(...)` for condition checks.

Wrong:
```kotlin
if (prop == null) {
    throw IllegalStateException("Property must be provided")
}
```

Right:
```kotlin
check(prop != null) { "Property must be provided" }
```

Open code reference:
- `test-common/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/BaseActuatorTest.kt:9`

### `detekt:naming:TopLevelPropertyNaming`

Criteria:
- `constantPattern = [A-Z][_A-Z0-9]*` (default)
- `propertyPattern = [A-Za-z][_A-Za-z0-9]*` (default)

Wrong:
```kotlin
const val attempts: Int = 5
const val timeDelayAttempt: Int = 300
```

Right:
```kotlin
const val ATTEMPTS: Int = 5
const val TIME_DELAY_ATTEMPT: Int = 300
```

Reference in current code:
- `client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/ReleaseManagementServiceRetry.kt:10`

## Disabled Checks (Review Candidates)

These rules are currently `active: false` in detekt and are tracked in baselines.

| Rule | Violations |
| --- | --- |
| `MagicNumber` | 20 |
| `LongParameterList` | 6 |
| `LongMethod` | 4 |
| `NestedBlockDepth` | 2 |
| `ReturnCount` | 2 |
| `SpreadOperator` | 2 |
| **Total** | **36** |

### Disabled But To Be Discussed First

- `detekt:complexity:LongParameterList`
- `detekt:complexity:LongMethod`
- `detekt:complexity:NestedBlockDepth`

### `detekt:style:MagicNumber`

Criteria:
- no single threshold; numeric literals are checked with default allow-list
- allowed numbers by default: `-1`, `0`, `1`, `2`

Wrong:
```kotlin
@Order(100)
```

Right:
```kotlin
private const val LOWEST_ORDER = 100
@Order(LOWEST_ORDER)
```

Example in code:
- `server/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/controller/ExceptionInfoHandler.kt:42`

### `detekt:style:ReturnCount`

Criteria:
- `max = 2` returns per function (default)

Wrong:
```kotlin
fun expand(value: Any?): String {
    if (value == null) return ""
    return value.toString()
}
```

Right:
```kotlin
fun expand(value: Any?): String =
    when (value) {
        null -> ""
        is Date -> FORMATTER.format(value.toInstant())
        else -> error("...")
    }
```

Example in code:
- `client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/DateToISOExpander.kt:11`

### `detekt:complexity:LongMethod`

Criteria:
- `threshold = 60` lines (default)

Wrong:
```kotlin
private fun builds(): Stream<Arguments> {
    // very long method body
}
```

Right:
```kotlin
private fun builds(): Stream<Arguments> =
    Stream.of(case1(), case2(), case3())
```

Example in code:
- `test-common/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/BaseBuildControllerTest.kt:42`

### `detekt:complexity:LongParameterList`

Criteria:
- `functionThreshold = 6` (default)
- `constructorThreshold = 7` (default)

Wrong:
```kotlin
fun post(code: Int, typeReference: TypeReference<T>, path: String, params: Map<String, Any>, body: Any?, vararg uriVars: String): T
```

Right:
```kotlin
data class RequestParams(...)
fun post(request: RequestParams): T
```

Example in code:
- `server/src/test/kotlin/org/octopusden/octopus/releasemanagementservice/controller/BaseControllerTest.kt:43`

### `detekt:complexity:NestedBlockDepth`

Criteria:
- `threshold = 4` nested blocks (default)

Wrong:
```kotlin
res.headers()["content-type"]?.find { ... }?.let { try { ... } catch (...) { ... } }
```

Right:
```kotlin
val contentType = res.headers()["content-type"] ?: return null
if (contentType.none { it.contains("application/json") }) return null
return parseBody(res)
```

Example in code:
- `client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/ReleaseManagementServiceErrorDecoder.kt:20`

### `detekt:performance:SpreadOperator`

Wrong:
```kotlin
SpringApplication.run(App::class.java, *args)
```

Right:
```kotlin
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}
```

Example in code:
- `server/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/ReleaseManagementServiceApplication.kt:12`
