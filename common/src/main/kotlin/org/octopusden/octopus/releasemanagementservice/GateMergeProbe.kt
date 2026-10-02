package org.octopusden.octopus.releasemanagementservice

/**
 * NOT PRODUCTION CODE. Deliberately defective in two independent ways — a ktlint violation that
 * fails the merge gate, and Sonar issues that fail the quality gate — to observe whether either
 * blocks a merge. Delete this file; never merge it.
 *
 * The blank line below is the ktlint violation, and is load-bearing. Do not tidy it.
 */
object GateMergeProbe {

    fun alwaysTrue(value: Int): Boolean = value == value

    fun weakToken(): Int = java.util.Random().nextInt()
}
