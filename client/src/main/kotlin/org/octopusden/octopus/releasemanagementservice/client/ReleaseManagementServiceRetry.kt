package org.octopusden.octopus.releasemanagementservice.client

import feign.RetryableException
import feign.Retryer
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.concurrent.TimeUnit

const val attempts: Int = 5
const val timeDelayAttempt: Int = 300
const val iterations: Int = 5
class ReleaseManagementServiceRetry(private val attemptIntervalMillis: Int = 60000) : Retryer {
    private val timeDelayIteration: Int = attemptIntervalMillis / iterations - (attempts * timeDelayAttempt)
    private val stopTime = System.currentTimeMillis() + attemptIntervalMillis

    private var attempt: Int = attempts
    private var iteration: Int = iterations

    override fun continueOrPropagate(e: RetryableException) {

        strategies.getOrDefault(e.status()) {
            if (stopTime < System.currentTimeMillis()) {
                throw e.cause!!
            }

            log.debug("Retry: iteration=${iterations - iteration + 1}, attempt=${attempts - attempt + 1}")

            if (attempt-- > 0) {
                TimeUnit.MILLISECONDS.sleep(timeDelayAttempt.toLong())
            } else if (iteration-- > 0) {
                attempt = attempts

                TimeUnit.MILLISECONDS.sleep(timeDelayIteration.toLong())
            } else {
                throw e.cause!!
            }

        }
            .invoke(e)
    }

    override fun clone(): Retryer {
        return ReleaseManagementServiceRetry(attemptIntervalMillis)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ReleaseManagementServiceRetry::class.java)
        private val strategies = mapOf<Int, (e: RetryableException) -> Unit>(
            HttpStatus.SC_ACCEPTED to { e ->
                val currentDate = Date()
                val retryAfterDate = Date(e.retryAfter())

                log.debug("Deferred result retry after {}", retryAfterDate)

                if (retryAfterDate.after(currentDate)) {
                    val sleepingTime = retryAfterDate.time - currentDate.time
                    log.trace("Sleeping time ${sleepingTime}ms")
                    Thread.sleep(sleepingTime)
                }
            }
        )
    }
}
