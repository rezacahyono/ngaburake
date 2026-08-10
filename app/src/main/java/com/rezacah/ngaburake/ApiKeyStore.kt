package com.rezacah.ngaburake

/**
 * Dogfooding fixture for the `obfuscationVerify {}` config in this module's `build.gradle.kts` —
 * configured as a `sensitivePackages` entry, checked by `verifyObfuscation` against
 * `proguard-rules.pro`'s `-keep,allowobfuscation` rule for this class.
 */
class ApiKeyStore {
    fun getApiKey(): String = "dummy-key"
}
