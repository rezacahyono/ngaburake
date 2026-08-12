package com.rezacah.ngaburake.data.fixture

/**
 * Sample fixture: deliberately covered by a broad `-keep class ... { *; }` rule, so R8 keeps
 * both the class name AND its members unchanged. This demonstrates the `ClassNameChecker`
 * mapping cross-check CRITICAL path — the class is found in `mapping.txt` but was NOT renamed.
 */
class LegacyAuthManager {
    fun getLegacyToken(): String = "legacy"
}
