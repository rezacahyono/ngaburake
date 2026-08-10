package com.rezacah.ngaburake.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Entry point for the `com.rezacah.ngaburake.obfuscation-verify` Gradle plugin.
 *
 * Registers the `obfuscationVerify { }` extension and the `verifyObfuscation` task. This class
 * only wires things together — the actual check logic lives in `MappingAnalysisTask`.
 *
 * If an Android R8 minify task (`minify<Variant>WithR8`) exists in the project,
 * `verifyObfuscation` is wired as `finalizedBy` that task so the mapping file is guaranteed to
 * exist by the time it runs. Safe to apply without the Android Gradle Plugin — no such task
 * simply means nothing gets auto-wired, and `verifyObfuscation` can still be run manually.
 */
class ObfuscationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "obfuscationVerify",
            ObfuscationExtension::class.java,
        ).apply {
            failOnViolation.convention(false)
            reportFormat.convention(ReportFormat.CONSOLE)
            outputDir.convention(project.layout.buildDirectory.dir("reports/obfuscation"))
        }

        val verifyTask = project.tasks.register(
            "verifyObfuscation",
            MappingAnalysisTask::class.java,
        ) { task ->
            task.mappingFile.set(
                project.layout.buildDirectory.file("outputs/mapping/release/mapping.txt"),
            )
            task.sensitivePackages.set(extension.sensitivePackages)
            task.failOnViolation.set(extension.failOnViolation)
            task.reportFormat.set(extension.reportFormat)
            task.outputDir.set(extension.outputDir)
        }

        // Defensive: only wire up to R8/mapping-producing tasks if they exist — AGP may not be
        // applied, and task names vary by variant (minify<Variant>WithR8,
        // merge<Variant>ComposeMapping also rewrites mapping.txt when Compose is used).
        // mustRunAfter (not just finalizedBy) is required so Gradle's task validation
        // recognizes verifyObfuscation reads a file those tasks produce.
        project.tasks.matching {
            (it.name.startsWith("minify") && it.name.endsWith("WithR8")) ||
                (it.name.startsWith("merge") && it.name.endsWith("ComposeMapping"))
        }.configureEach { mappingProducerTask ->
            mappingProducerTask.finalizedBy(verifyTask)
            verifyTask.get().mustRunAfter(mappingProducerTask)
        }
    }
}
