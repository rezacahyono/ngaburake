package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.mapping.MappingIndex
import com.rezacah.ngaburake.mapping.MappingParser
import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.report.ReportGenerator
import com.rezacah.ngaburake.report.Severity
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
    private val unmatchedWildcards: List<String>,
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
     * a background dispatcher. A wildcard entry that matched no class in the mapping file
     * produces one [Severity.WARNING] finding for that pattern instead of being silently dropped.
     *
     * Suspend function — safe to call from any coroutine context, but must not be called
     * blockingly from `Dispatchers.Main` (use `withContext` or a coroutine scope).
     */
    suspend fun verify(): ObfuscationResult {
        val checkedFindings = useCase.execute(sensitivePackages)
        val unmatchedFindings = unmatchedWildcards.map { pattern ->
            Finding(
                target = pattern,
                type = FindingType.CLASS_NAME,
                severity = Severity.WARNING,
                detail = "wildcard matched no classes in mapping.txt",
            )
        }
        return ObfuscationResult.from(checkedFindings + unmatchedFindings)
    }

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
         * Adds multiple fully qualified class names at once — equivalent to calling
         * [addSensitivePackage] once per entry, for when the list is long enough that one-at-a-time
         * calls get unwieldy. Can be freely mixed with [addSensitivePackage].
         *
         * A `"com.example.payment.*"`-style entry (ending in `.*`) is a package-prefix wildcard —
         * expanded at [build] time into every class under that package found in the mapping file
         * supplied via [withMappingFile]. Wildcards **require** [withMappingFile] — the reflection
         * heuristic alone has no way to enumerate classes in a package.
         */
        fun addSensitivePackages(classNames: List<String>): Builder = apply {
            sensitivePackages.addAll(classNames)
        }

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
         *   nothing is almost always a configuration mistake, not an intentional no-op — or if a
         *   wildcard entry (e.g. `"com.example.payment.*"`) was added without [withMappingFile].
         */
        fun build(): ObfuscationSDK {
            val distinctPackages = sensitivePackages.distinct()
            require(distinctPackages.isNotEmpty()) {
                "At least one sensitive package must be added via addSensitivePackage() before build()."
            }

            val (wildcards, exact) = distinctPackages.partition { it.endsWith(".*") }
            val index = mappingFile?.let { MappingParser.parse(it) }
            require(wildcards.isEmpty() || index != null) {
                "Wildcard sensitive package \"${wildcards.firstOrNull()}\" requires withMappingFile() " +
                    "— wildcard expansion needs a real mapping.txt, the reflection heuristic alone " +
                    "can't enumerate classes in a package."
            }
            val unmatchedWildcards = mutableListOf<String>()
            val expandedWildcards = wildcards.flatMap { pattern ->
                val matches = index!!.classesUnder(pattern.removeSuffix(".*"))
                if (matches.isEmpty()) unmatchedWildcards.add(pattern)
                matches
            }
            val resolvedPackages = (exact + expandedWildcards).distinct()

            return ObfuscationSDK(resolvedPackages, unmatchedWildcards, index, sensitiveKeywords)
        }
    }
}
