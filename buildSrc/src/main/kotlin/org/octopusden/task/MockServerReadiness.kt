package org.octopusden.task

import org.gradle.api.GradleException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Blocks until MockServer answers its own status endpoint with 200.
 *
 * On OKD the route is created together with the pod, so for a few seconds after the pod reports
 * ready the router still answers with its own 503 page. That is not a transport error, and the
 * MockServer client does not reject it either - `reset()` returns normally against a 503 and only
 * the later expectation upload fails, so readiness has to be decided on the HTTP status code.
 *
 * The probe is issued directly rather than through `MockServerClient.hasStarted`, whose delay
 * argument is the sleep between attempts and not a request timeout: its requests are bounded only
 * by a 20s socket timeout, which overruns the readiness budget on a route that accepts connections
 * but never answers. Each probe here is capped by the budget it has left, so the wait overruns
 * [timeoutMs] by at most one connection attempt.
 */
internal object MockServerReadiness {
    private const val PROBE_TIMEOUT_MS = 2_000L
    private const val STATUS_PATH = "/mockserver/status"

    fun await(
        host: String,
        port: Int,
        timeoutMs: Long,
        periodMs: Long,
        onNotReady: (String) -> Unit = {},
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        val statusUri = URI.create("http://$host:$port$STATUS_PATH")
        val client =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofMillis(PROBE_TIMEOUT_MS))
                .build()
        var lastOutcome = "no probe completed"
        while (true) {
            val budget = deadline - System.currentTimeMillis()
            if (budget <= 0) {
                throw GradleException(
                    "MockServer at $host:$port did not start serving within $timeoutMs ms, last probe: $lastOutcome",
                )
            }
            lastOutcome = probe(client, statusUri, minOf(budget, PROBE_TIMEOUT_MS)) ?: return
            onNotReady(lastOutcome)
            val untilDeadline = (deadline - System.currentTimeMillis()).coerceAtLeast(0)
            Thread.sleep(minOf(periodMs, untilDeadline))
        }
    }

    /** Returns null once the status endpoint answers 200, otherwise why it did not. */
    private fun probe(
        client: HttpClient,
        statusUri: URI,
        timeoutMs: Long,
    ): String? {
        val request =
            HttpRequest
                .newBuilder(statusUri)
                .timeout(Duration.ofMillis(timeoutMs))
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build()
        return try {
            val statusCode = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
            if (statusCode == 200) null else "HTTP $statusCode"
        } catch (e: IOException) {
            e.toString()
        }
    }
}
