package com.jalo.ngaburake

/**
 * Dogfooding fixture for `docs/PLAN.md` Phase 6 — no `-keep` rule targets this class, so R8
 * should rename it in `mapping.txt` (positive test case for `verifyObfuscation`).
 */
class PaymentManager {
    fun processPayment(amount: Double): Boolean {
        return amount > 0
    }
}
