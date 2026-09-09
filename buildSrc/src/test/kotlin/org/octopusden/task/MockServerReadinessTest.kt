package org.octopusden.task

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

class MockServerReadinessTest {
    @Test
    fun `retries while the router answers 503 and returns once the route serves`() {
        val router = RouterStub(readyAfterProbes = 2)
        router.start()
        try {
            val retries = AtomicInteger()
            MockServerReadiness.await(HOST, router.port, TIMEOUT_MS, PERIOD_MS) { retries.incrementAndGet() }
            assertTrue(retries.get() >= 1, "expected at least one retry, got ${retries.get()}")
            assertTrue(router.probes.get() >= 3, "expected the 503 probes to be repeated, got ${router.probes.get()}")
        } finally {
            router.stop()
        }
    }

    @Test
    fun `fails at the deadline while the router keeps answering 503`() {
        val router = RouterStub(readyAfterProbes = Int.MAX_VALUE)
        router.start()
        try {
            val exception =
                assertThrows<GradleException> {
                    MockServerReadiness.await(HOST, router.port, FAIL_TIMEOUT_MS, PERIOD_MS)
                }
            assertEquals(
                "MockServer at $HOST:${router.port} did not start serving within $FAIL_TIMEOUT_MS ms",
                exception.message,
            )
            assertTrue(router.probes.get() >= 1, "expected the readiness probe to be attempted")
        } finally {
            router.stop()
        }
    }

    @Test
    fun `recovers when the route only starts accepting connections after the first attempt`() {
        val router = RouterStub(readyAfterProbes = 0)
        try {
            MockServerReadiness.await(HOST, router.port, TIMEOUT_MS, PERIOD_MS) { router.start() }
            assertTrue(router.probes.get() >= 1, "expected a probe once the route was up")
        } finally {
            router.stop()
        }
    }

    /** Serves the OKD router's 503 page until [readyAfterProbes] status probes have been answered. */
    private class RouterStub(
        private val readyAfterProbes: Int,
    ) {
        val port = ServerSocket(0).use { it.localPort }
        val probes = AtomicInteger()
        private var server: HttpServer? = null

        fun start() {
            if (server != null) return
            server =
                HttpServer.create(InetSocketAddress(HOST, port), 0).apply {
                    createContext("/") { respond(it) }
                    start()
                }
        }

        fun stop() {
            server?.stop(0)
        }

        private fun respond(exchange: HttpExchange) {
            val ready = probes.incrementAndGet() > readyAfterProbes
            val body = if (ready) """{"status":"RUNNING"}""" else ROUTER_503_PAGE
            exchange.responseHeaders.add("Content-Type", if (ready) "application/json" else "text/html")
            exchange.sendResponseHeaders(if (ready) 200 else 503, body.length.toLong())
            exchange.responseBody.use { it.write(body.toByteArray()) }
        }
    }

    companion object {
        private const val HOST = "127.0.0.1"
        private const val TIMEOUT_MS = 5_000L
        private const val FAIL_TIMEOUT_MS = 200L
        private const val PERIOD_MS = 50L
        private const val ROUTER_503_PAGE = "<html><body><h1>Application is not available</h1></body></html>"
    }
}
