package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.mapping.MappingIndex
import com.rezacah.ngaburake.mapping.MappingParser
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.report.ReportGenerator
import java.io.File

/**
 * Public facade for the runtime obfuscation verification SDK — the entry point most consumers
 * interact with.
 *
 * Use [Builder] to construct an instance, then call [verify] to run every configured checker and
 * produce an [ObfuscationResult]. Instances are lightweight and safe to create per verification
 * run — they are not required to be singletons, since verification is commonly run once per
 * app launch (CI/QA build) or on demand.
 *
 * Example:
 * ```
 * val sdk = ObfuscationSDK.Builder()
 *     .addSensitivePackage("com.example.PaymentManager")
 *     .build()
 * val result = sdk.verify()
 * ```
 */
class ObfuscationSDK private constructor(
    private val sensitivePackages: List<String>,
    private val mappingIndex: MappingIndex?,
    private val sensitiveKeywords: List<String>,
) {
    private val useCase = RuntimeCheckUseCase(
        checkers = listOf(
            ClassNameChecker(mappingIndex),
            ReflectionChecker(sensitiveKeywords),
        ),
    )

    /**
     * Runs every configured checker against every class in [sensitivePackages], concurrently on
     * a background dispatcher.
     *
     * Suspend function — safe to call from any coroutine context, but must not be called
     * blockingly from `Dispatchers.Main` (use `withContext` or a coroutine scope).
     */
    suspend fun verify(): ObfuscationResult = ObfuscationResult.from(useCase.execute(sensitivePackages))

    /**
     * Renders [result] using [format]. Pure function — no I/O side effect; writing the output to
     * a file or log is the caller's responsibility.
     */
    fun generateReport(result: ObfuscationResult, format: ReportFormat): String =
        ReportGenerator.generate(result.findings, format)

    /**
     * Builds an [ObfuscationSDK] instance. Validates configuration once in [build], not
     * repeatedly on every setter call.
     */
    class Builder {
        private val sensitivePackages = mutableListOf<String>()
        private var mappingFile: File? = null
        private var sensitiveKeywords: List<String> = ReflectionChecker.DEFAULT_KEYWORDS

        /** Adds a fully qualified class name that must be verified as obfuscated. */
        fun addSensitivePackage(className: String): Builder = apply { sensitivePackages.add(className) }

        /**
         * Supplies an R8 `mapping.txt` file to cross-check class names against, instead of
         * relying solely on the [ClassNameChecker] reflection heuristic.
         *
         * `mapping.txt` is a build artifact, not bundled into the APK by default — the caller is
         * responsible for making it available at this path (e.g. packaged as an asset for a
         * dedicated CI/QA build variant). Omit this if only the reflection heuristic is needed.
         */
        fun withMappingFile(file: File): Builder = apply { mappingFile = file }

        /**
         * Overrides the keywords [ReflectionChecker] flags in field/method names. Defaults to
         * `apiKey`, `secret`, `token`, `password` (case-insensitive substring match).
         *
         * Call this if the consumer's naming convention uses different terms — e.g.
         * `.withSensitiveKeywords(listOf("credential", "privateKey"))`.
         */
        fun withSensitiveKeywords(keywords: List<String>): Builder = apply {
            sensitiveKeywords = keywords
        }

        /**
         * @throws IllegalArgumentException if no sensitive packages were configured — verifying
         *   nothing is almost always a configuration mistake, not an intentional no-op.
         */
        fun build(): ObfuscationSDK {
            require(sensitivePackages.isNotEmpty()) {
                "At least one sensitive package must be added via addSensitivePackage() before build()."
            }
            val index = mappingFile?.let { MappingParser.parse(it) }
            return ObfuscationSDK(sensitivePackages.toList(), index, sensitiveKeywords)
        }
    }
}
