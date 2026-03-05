# Style Guidelines

This document describes enabled and review-candidate style-related checks and current violation statistics.

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
| `detekt:style:MaxLineLength` | 4 | baseline |
| `detekt:style:NewLineAtEndOfFile` | 21 | baseline |
| `detekt:style:ProtectedMemberInFinalClass` | 8 | baseline |
| `detekt:style:SwallowedException` | 3 | baseline |
| `detekt:exceptions:TooGenericExceptionCaught` | 6 | baseline |
| `ktlint:standard:chain-method-continuation` | 49 | baseline |

For enabled checks, code links are listed only for not-yet-fixed cases.

### `ktlint` numeric criteria (active)

- `max_line_length = 140` (from `.editorconfig`, for `*.kt` and `*.kts`)

### `ktlint:standard:chain-method-continuation`

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

### `ktlint` Checks With Baseline Violations

All checks in this section are enabled `ktlint` checks (same category as `chain-method-continuation`).

| Rule | Violations |
| --- | ---: |
| `standard:class-signature` | 71 |
| `standard:final-newline` | 25 |
| `standard:function-expression-body` | 23 |
| `standard:binary-expression-wrapping` | 4 |
| `standard:string-template-indent` | 10 |
| `standard:max-line-length` | 3 |
| `standard:argument-list-wrapping` | 65 |

Total baseline violations for checks in this section: `201`.

### `ktlint:standard:class-signature`

Meaning:
- Wrap multiline class/constructor signatures consistently.

Wrong:
```kotlin
data class CommitDTO(val id: String, val author: String, val message: String)
```

Right:
```kotlin
data class CommitDTO(
    val id: String,
    val author: String,
    val message: String,
)
```

### `ktlint:standard:final-newline`

Criteria:
- File must end with a newline (`insert_final_newline = true`).

Wrong:
```kotlin
class Example
```
File ends immediately after `Example` with no trailing newline.

Right:
```kotlin
class Example
```
File ends with one trailing newline.

### `ktlint:standard:function-expression-body`

Meaning:
- Prefer expression body for single-expression functions.

Wrong:
```kotlin
fun isEmpty(value: String): Boolean {
    return value.isEmpty()
}
```

Right:
```kotlin
fun isEmpty(value: String): Boolean = value.isEmpty()
```

### `ktlint:standard:binary-expression-wrapping`

Meaning:
- Wrap long binary expressions (`&&`, `||`, `+`, etc.) consistently.

Wrong:
```kotlin
val allowed = isEnabled &&
user.isActive && user.hasRole("ADMIN")
```

Right:
```kotlin
val allowed =
    isEnabled &&
        user.isActive &&
        user.hasRole("ADMIN")
```

### `ktlint:standard:string-template-indent`

Meaning:
- Keep indentation in multiline string templates consistent.

Wrong:
```kotlin
val message = """
    Build: ${buildId}
  Status: ${status}
""".trimIndent()
```

Right:
```kotlin
val message = """
    Build: ${buildId}
    Status: ${status}
""".trimIndent()
```

### `ktlint:standard:max-line-length`

Criteria:
- `max_line_length = 140`.

Wrong:
```kotlin
val longMessage = "This line is intentionally too long ... (more than 140 characters) ... to illustrate max-line-length violation"
```

Right:
```kotlin
val longMessage =
    "This line is split " +
        "to keep each line length below 140 characters."
```

### `ktlint:standard:function-literal`

Meaning:
- Keep lambda literal formatting consistent in multiline expressions.

Wrong:
```kotlin
val names = values.map({
    it.name
})
```

Right:
```kotlin
val names =
    values.map {
        it.name
    }
```

### `ktlint:standard:argument-list-wrapping`

Meaning:
- Wrap long argument lists consistently when call spans multiple lines.

Wrong:
```kotlin
client.createMandatoryUpdate(dryRun, component, version, projectKey, epicName, dueDate, notice, customer)
```

Right:
```kotlin
client.createMandatoryUpdate(
    dryRun,
    component,
    version,
    projectKey,
    epicName,
    dueDate,
    notice,
    customer,
)
```

### `ktlint:standard:block-comment-initial-star-alignment`

Meaning:
- Align leading `*` in block comments.

Wrong:
```kotlin
/*
* first line
  * second line
 */
```

Right:
```kotlin
/*
 * first line
 * second line
 */
```

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

### `detekt:style:UseCheckOrError`

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

### `detekt:style:UnusedPrivateMember`

Do not keep private functions or properties that are never used.

Wrong:
```kotlin
private fun build(): Stream<Arguments> = Stream.empty()
```

Right:
```kotlin
private fun buildArguments(): Stream<Arguments> = Stream.empty()

@MethodSource("buildArguments")
fun shouldBuildCorrectly(arguments: Arguments) { /* ... */ }
```

### `detekt:style:UnusedPrivateProperty`

Do not keep unused private properties.

Wrong:
```kotlin
private val mapper = ObjectMapper()
```

Right:
```kotlin
private val mapper = ObjectMapper()

fun serialize(value: Any): String = mapper.writeValueAsString(value)
```

### `detekt:style:SwallowedException` (open)

Do not catch an exception and silently ignore it. Either handle it explicitly, log it with context, or rethrow a more specific exception.

Wrong:
```kotlin
try {
    update()
} catch (e: Exception) {
}
```

Right:
```kotlin
try {
    update()
} catch (e: IOException) {
    log.warn("Mandatory update failed for component {}", component, e)
}
```

Open code references:
- `automation/src/main/kotlin/org/octopusden/octopus/automation/releasemanagement/command/MandatoryUpdate.kt:116`
- `client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/ReleaseManagementServiceErrorDecoder.kt:29`
- `teamcity-plugin/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/teamcity/plugin/ReleaseManagementBuildTriggerService.kt:95`

### `detekt:exceptions:TooGenericExceptionCaught` (open)

Catch the narrowest exception type that the code can actually handle.

Wrong:
```kotlin
try {
    loadVersion()
} catch (e: Exception) {
    return null
}
```

Right:
```kotlin
try {
    loadVersion()
} catch (e: FeignException.NotFound) {
    return null
}
```

Open code references:
- `automation/src/main/kotlin/org/octopusden/octopus/automation/releasemanagement/command/MandatoryUpdate.kt:116`
- `client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/ReleaseManagementServiceErrorDecoder.kt:29`
- `legacy-releng-client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/client/LegacyRelengErrorDecoder.kt:18`
- `legacy-releng-client/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/legacy/LegacyRelengErrorDecoder.kt:15`
- `server/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/actuator/LegacyRelengIndicator.kt:16`
- `teamcity-plugin/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/teamcity/plugin/ReleaseManagementBuildTriggerService.kt:95`

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

## Disabled Checks (Review Candidates)

These rules are currently `active: false` in detekt.

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

Rule status:
- `active: false` (disabled globally), guidance below is informational.

How to write:
- Use `*array` only when API expects `vararg` and you only have an `Array`.
- Do not use `*` when API expects `Array`.
- Do not create temporary arrays only to unpack them with `*`.

Case 1: API expects `vararg`, source is `Array` -> use `*`:
```kotlin
fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}
```

Case 2: API expects `Array` -> pass as is (without `*`):
```kotlin
fun execute(arguments: Array<String>) = run(arguments)
```

Case 3: Known fixed arguments -> pass directly (without temporary array):
```kotlin
run("--spring.profiles.active=test", "--debug")
```

Example in code:
- `server/src/main/kotlin/org/octopusden/octopus/releasemanagementservice/ReleaseManagementServiceApplication.kt:12`
