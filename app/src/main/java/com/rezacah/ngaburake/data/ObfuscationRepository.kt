package com.rezacah.ngaburake.data

import android.content.Context
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.runtime.ObfuscationResult
import com.rezacah.ngaburake.runtime.ObfuscationSDK
import java.io.File

/**
 * Data-layer seam between the UI layer and the [ObfuscationSDK]. The ViewModel depends on this
 * interface (not on the SDK directly), which keeps SDK construction and asset handling out of
 * the presentation layer and makes the ViewModel testable with a fake.
 */
interface ObfuscationRepository {
    /**
     * Runs the runtime obfuscation check. [useMappingFile] enables cross-checking against a
     * bundled mapping.txt; [sensitiveKeywords] overrides the ReflectionChecker keyword list.
     */
    suspend fun verify(
        sensitivePackages: List<String>,
        useMappingFile: Boolean,
        sensitiveKeywords: List<String>,
    ): ObfuscationResult

    /** Renders [result] in the requested [format]. Pure function, no I/O. */
    fun generateReport(result: ObfuscationResult, format: ReportFormat): String
}

/**
 * Default repository backed by [ObfuscationSDK]. Reads the bundled `mapping.txt` asset (copied to
 * cache, since the SDK needs a real [File]) when cross-checking is enabled.
 */
class ObfuscationRepositoryImpl(
    private val context: Context,
    private val mappingFileProvider: (() -> File?)? = null,
    private val sdkFactory: (List<String>, File?, List<String>) -> ObfuscationSDK = { packages, mapping, keywords ->
        val builder = ObfuscationSDK.Builder()
            .withSensitiveKeywords(keywords)
        packages.forEach(builder::addSensitivePackage)
        mapping?.let(builder::withMappingFile)
        builder.build()
    },
) : ObfuscationRepository {

    @Volatile
    private var lastSdk: ObfuscationSDK? = null

    override suspend fun verify(
        sensitivePackages: List<String>,
        useMappingFile: Boolean,
        sensitiveKeywords: List<String>,
    ): ObfuscationResult {
        val provider = mappingFileProvider ?: ::defaultBundledMappingFile
        val mappingFile = if (useMappingFile) provider() else null
        return sdkFactory(sensitivePackages, mappingFile, sensitiveKeywords)
            .also { lastSdk = it }
            .verify()
    }

    override fun generateReport(result: ObfuscationResult, format: ReportFormat): String {
        // Reuse the SDK built during verify(). Building a fresh one with an empty package list
        // would throw in ObfuscationSDK.Builder.build().
        val sdk = checkNotNull(lastSdk) { "verify() must run before generateReport()" }
        return sdk.generateReport(result, format)
    }

    /**
     * Reads the mapping.txt bundled as a static asset and materializes it to a cache [File],
     * since [ObfuscationSDK.Builder.withMappingFile] needs a real file path.
     */
    private fun defaultBundledMappingFile(): File? {
        val assetDir = context.assets
        return try {
            val entries = assetDir.list("")
            if (entries != null && entries.any { it == "mapping.txt" }) {
                val target = File(context.cacheDir, "mapping.txt")
                assetDir.open("mapping.txt").use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                target
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
