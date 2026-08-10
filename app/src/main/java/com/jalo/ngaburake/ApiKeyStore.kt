package com.jalo.ngaburake

/**
 * Dogfooding fixture for `docs/PLAN.md` Phase 6 — deliberately covered by an overly broad
 * `-keep` rule in `proguard-rules.pro`, so R8 keeps its original name (negative test case for
 * `verifyObfuscation`).
 */
class ApiKeyStore {
    fun getApiKey(): String = "dummy-key"
}
