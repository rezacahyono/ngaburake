package com.rezacah.ngaburake.plugin

import com.rezacah.ngaburake.report.ReportFormat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObfuscationPluginTest {

    @Test
    fun `apply plugin registers the obfuscationVerify extension`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val extension = project.extensions.findByName("obfuscationVerify")
        assertNotNull(extension)
        assertTrue(extension is ObfuscationExtension)
    }

    @Test
    fun `apply plugin registers the verifyObfuscation task`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val task = project.tasks.findByName("verifyObfuscation")
        assertNotNull(task)
        assertTrue(task is MappingAnalysisTask)
    }

    @Test
    fun `extension default values match the configured convention`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val extension = project.extensions.getByType(ObfuscationExtension::class.java)

        assertEquals(false, extension.failOnViolation.get())
        assertEquals(ReportFormat.CONSOLE, extension.reportFormat.get())
    }

    @Test
    fun `apply plugin does not error even when AGP has not been applied`() {
        val project = ProjectBuilder.builder().build()

        // No minify*WithR8 task exists at all because AGP isn't applied — the plugin must
        // still apply successfully without an UnknownTaskException.
        project.pluginManager.apply(ObfuscationPlugin::class.java)

        assertNotNull(project.tasks.findByName("verifyObfuscation"))
    }
}
