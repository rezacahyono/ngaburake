package com.rezacah.ngaburake.plugin

import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.report.ReportGenerator
import com.rezacah.ngaburake.report.Severity
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Parses the R8 mapping file and checks that every configured sensitive class was actually
 * renamed. Registered by [ObfuscationPlugin] as the `verifyObfuscation` task.
 */
@CacheableTask
internal abstract class MappingAnalysisTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    @get:Input
    abstract val sensitivePackages: ListProperty<String>

    @get:Input
    abstract val failOnViolation: Property<Boolean>

    @get:Input
    abstract val reportFormat: Property<ReportFormat>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val index = MappingParser.parse(mappingFile.get().asFile)
        val findings = sensitivePackages.get().map { pkg -> toFinding(pkg, index.check(pkg)) }

        val format = reportFormat.get()
        val report = ReportGenerator.generate(findings, format)
        logger.lifecycle(report)

        val reportFile = outputDir.get().asFile.resolve("report.${format.name.lowercase()}")
        reportFile.parentFile.mkdirs()
        reportFile.writeText(report)

        val critical = findings.filter { it.severity == Severity.CRITICAL }
        if (failOnViolation.get() && critical.isNotEmpty()) {
            throw GradleException(
                "Obfuscation violation found for: ${critical.joinToString(", ") { it.target }}",
            )
        }
    }

    private fun toFinding(className: String, result: ObfuscationCheckResult): Finding =
        when (result) {
            ObfuscationCheckResult.Obfuscated -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.OK,
                detail = "renamed as expected",
            )

            ObfuscationCheckResult.NotObfuscated -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.CRITICAL,
                detail = "kept its original name — check for an overly broad -keep rule",
            )

            ObfuscationCheckResult.NotFoundInMapping -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.WARNING,
                detail = "not found in mapping.txt — not verified",
            )
        }
}
