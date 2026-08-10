package com.jalo.ngaburake.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Configuration DSL for the `obfuscationVerify { }` block.
 *
 * Defaults ([failOnViolation] = `false`, [reportFormat] = [ReportFormat.CONSOLE], [outputDir] =
 * `build/reports/obfuscation`) are set as Gradle property conventions in [ObfuscationPlugin],
 * not here — this class only declares what's configurable.
 */
abstract class ObfuscationExtension {
    /**
     * Fully qualified class names (e.g. `"com.jalo.ngaburake.PaymentManager"`) that must appear
     * renamed in the R8 mapping file. Not validated at configuration time — a typo here simply
     * shows up as a "not found in mapping" warning when `verifyObfuscation` runs.
     */
    abstract val sensitivePackages: ListProperty<String>

    /**
     * Whether `verifyObfuscation` should throw and fail the build when a class in
     * [sensitivePackages] was found in the mapping file but kept its original name.
     *
     * Defaults to `false` (report only). Set to `true` to use this as a CI security gate.
     */
    abstract val failOnViolation: Property<Boolean>

    /**
     * Output format for the generated report. Defaults to [ReportFormat.CONSOLE].
     *
     * @throws IllegalStateException at task execution time if set to [ReportFormat.HTML] —
     *   that format is not implemented yet (planned for v1.1).
     */
    abstract val reportFormat: Property<ReportFormat>

    /** Directory where the report file is written. Defaults to `build/reports/obfuscation`. */
    abstract val outputDir: DirectoryProperty
}
