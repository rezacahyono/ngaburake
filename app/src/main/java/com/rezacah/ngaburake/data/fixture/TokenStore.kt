package com.rezacah.ngaburake.data.fixture

/**
 * Sample fixture: a class whose *class name* gets renamed by R8, but whose `secretToken` field
 * name is preserved via a `-keepclassmembers` rule in `proguard-rules.pro`. This demonstrates
 * the `ReflectionChecker` FIELD_NAME detection path — the class looks obfuscated, but a
 * sensitive member name still leaks through reflection.
 */
class TokenStore {
    @Suppress("unused")
    val secretToken: String = "dummy-token"
}
