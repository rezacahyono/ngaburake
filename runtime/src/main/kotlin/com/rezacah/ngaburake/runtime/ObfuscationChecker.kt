package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.report.Finding

/**
 * A single obfuscation check strategy — checks one aspect of whether [className] is obfuscated
 * as expected and returns a [Finding] describing the result.
 *
 * Adding a new kind of check (e.g. a future string-literal scanner) means adding a new
 * implementation of this interface, not modifying [RuntimeCheckUseCase] — see
 * `design-patterns-android` skill, Strategy pattern.
 */
interface ObfuscationChecker {
    /**
     * Checks [className] and returns a [Finding] describing the result.
     *
     * Safe to call from any thread — implementations must not perform blocking I/O. `RuntimeCheckUseCase`
     * (added in a later iteration) runs checkers concurrently on a background dispatcher.
     */
    fun check(className: String): Finding
}
