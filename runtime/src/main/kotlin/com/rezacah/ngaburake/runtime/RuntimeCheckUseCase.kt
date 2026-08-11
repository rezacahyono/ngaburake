package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.report.Finding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Runs a list of [ObfuscationChecker]s against every configured sensitive class, in parallel.
 *
 * @param checkers strategies to run for each class (see `design-patterns-android` — Strategy
 *   pattern; add a new kind of check by adding a new [ObfuscationChecker], not by changing this
 *   class).
 * @param dispatcher where checks run — defaults to [Dispatchers.Default] because checking is
 *   CPU-bound (reflection, string matching), not I/O. Injectable for testing with a
 *   `TestDispatcher`.
 */
class RuntimeCheckUseCase(
    private val checkers: List<ObfuscationChecker>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Runs every checker against every class in [sensitivePackages], concurrently, and returns
     * the flattened list of findings (`checkers.size * sensitivePackages.size` entries).
     */
    suspend fun execute(sensitivePackages: List<String>): List<Finding> = coroutineScope {
        sensitivePackages.flatMap { className ->
            checkers.map { checker ->
                async(dispatcher) { checker.check(className) }
            }
        }.map { it.await() }
    }
}
