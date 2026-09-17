package org.octopusden.task

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class MockServerReadinessTest {
    @Test
    fun `retries while the router answers 503 and returns once the route serves`() {
        val router = RouterStub(readyAfterProbes = 2)
        router.start()
        try {
            val outcomes = mutableListOf<String>()
            MockServerReadiness.await(HOST, router.port, TIMEOUT_MS, PERIOD_MS) { outcomes += it }
            assertEquals(listOf("HTTP 503", "HTTP 503"), outcomes)
            assertEquals(3, router.probes.get())
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
                "MockServer at $HOST:${router.port} did not start serving within $FAIL_TIMEOUT_MS ms, " +
                    "last probe: HTTP 503",
                exception.message,
            )
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

    @Test
    fun `gives up within the budget when the route accepts the connection but never answers`() {
        val route = SilentRoute()
        route.start()
        try {
            val started = System.currentTimeMillis()
            val exception =
                assertThrows<GradleException> {
                    MockServerReadiness.await(HOST, route.port, FAIL_TIMEOUT_MS, PERIOD_MS)
                }
            val elapsed = System.currentTimeMillis() - started
            assertTrue(
                elapsed < SOCKET_TIMEOUT_MS,
                "expected the probe to be cut off by the readiness budget, but it waited $elapsed ms",
            )
            assertTrue(
                exception.message!!.contains("HttpTimeoutException"),
                "expected the timeout to be reported, got: ${exception.message}",
            )
        } finally {
            route.stop()
        }
    }

    /** Serves the OKD router's 503 page until [readyAfterProbes] probes have been answered, then 200. */
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

    /** A route that completes the TCP handshake and then leaves the request unanswered. */
    private class SilentRoute {
        private val serverSocket = ServerSocket(0)
        private val held = mutableListOf<Socket>()

        val port: Int get() = serverSocket.localPort

        fun start() {
            thread(isDaemon = true, name = "silent-route") {
                while (!serverSocket.isClosed) {
                    try {
                        held += serverSocket.accept()
                    } catch (e: IOException) {
                        return@thread
                    }
                }
            }
        }

        fun stop() {
            serverSocket.close()
            held.forEach { runCatching { it.close() } }
        }
    }

    companion object {
        private const val HOST = "127.0.0.1"
        private const val TIMEOUT_MS = 5_000L
        private const val FAIL_TIMEOUT_MS = 300L
        private const val PERIOD_MS = 50L
        private const val SOCKET_TIMEOUT_MS = 20_000L
        private const val ROUTER_503_PAGE = "<html><body><h1>Application is not available</h1></body></html>"
    }
}
