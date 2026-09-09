package org.octopusden.task

import org.gradle.api.GradleException
import org.mockserver.client.MockServerClient
import java.util.concurrent.TimeUnit

/**
 * Blocks until MockServer answers its own status endpoint with 200.
 *
 * On OKD the route is created together with the pod, so for a few seconds after the pod reports
 * ready the router still answers with its own 503 page. That is not a transport error, and the
 * MockServer client does not reject it either - `reset()` returns normally against a 503 and only
 * the later expectation upload fails, so readiness has to be decided on the HTTP status code.
 */
internal object MockServerReadiness {
    private const val PROBE_TIMEOUT_MS = 500L

    fun await(
        host: String,
        port: Int,
        timeoutMs: Long,
        periodMs: Long,
        onNotReady: () -> Unit = {},
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        // Never close this client: MockServerClient.close() stops the remote server.
        val client = MockServerClient(host, port)
        while (!client.hasStarted(1, PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            if (System.currentTimeMillis() >= deadline) {
                throw GradleException("MockServer at $host:$port did not start serving within $timeoutMs ms")
            }
            onNotReady()
            Thread.sleep(periodMs)
        }
    }
}
