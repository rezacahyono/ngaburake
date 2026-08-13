package com.rezacah.ngaburake.plugin

import com.rezacah.ngaburake.mapping.MappingParser
import com.rezacah.ngaburake.mapping.ObfuscationCheckResult
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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

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

    // @Internal, not @InputFiles: this only affects the best-effort SARIF filePath/line —
    // never severity/pass-fail — so under-tracking it doesn't risk a stale wrong-verdict cache
    // hit, only a stale (or missing) file location on an otherwise identical report.
    @get:Internal
    abstract val sourceRoots: ListProperty<File>

    // Captured at configuration time (set by ObfuscationPlugin from project.rootDir) instead of
    // read via `project` inside @TaskAction — the Project instance isn't Configuration-Cache
    // safe to reference during task execution.
    @get:Internal
    abstract val rootDir: Property<File>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val index = MappingParser.parse(mappingFile.get().asFile)
        val resolver = SourceLocationResolver(sourceRoots.get())

        // "com.foo.payment.*" entries are a package-prefix wildcard, expanded against the parsed
        // mapping file into every concrete class under that package — safe to detect via a plain
        // endsWith(".*") because "*" is never a valid identifier character, so no real FQCN can
        // end with it.
        val (wildcards, exact) = sensitivePackages.get().partition { it.endsWith(".*") }
        val wildcardFindings = wildcards.flatMap { pattern ->
            val prefix = pattern.removeSuffix(".*")
            val matches = index.classesUnder(prefix)
            if (matches.isEmpty()) {
                listOf(
                    Finding(
                        target = pattern,
                        type = FindingType.CLASS_NAME,
                        severity = Severity.WARNING,
                        detail = "wildcard matched no classes in mapping.txt",
                    ),
                )
            } else {
                matches.map { className -> toFinding(className, index.check(className), resolver) }
            }
        }
        val exactFindings = exact.distinct().map { pkg -> toFinding(pkg, index.check(pkg), resolver) }
        val findings = (exactFindings + wildcardFindings).distinct()

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

    private fun toFinding(
        className: String,
        result: ObfuscationCheckResult,
        resolver: SourceLocationResolver,
    ): Finding {
        val sourceFile = resolver.resolve(className)
        // Relative to the project root so SARIF gets a repo-relative path, not an absolute one
        // tied to this machine.
        val filePath = sourceFile?.let { rootDir.get().toPath().relativize(it.toPath()).toString() }

        return when (result) {
            ObfuscationCheckResult.Obfuscated -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.OK,
                detail = "renamed as expected",
                filePath = filePath,
            )

            ObfuscationCheckResult.NotObfuscated -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.CRITICAL,
                detail = "kept its original name — check for an overly broad -keep rule",
                filePath = filePath,
            )

            ObfuscationCheckResult.NotFoundInMapping -> Finding(
                target = className,
                type = FindingType.CLASS_NAME,
                severity = Severity.WARNING,
                detail = "not found in mapping.txt — not verified",
                filePath = filePath,
            )
        }
    }
}
