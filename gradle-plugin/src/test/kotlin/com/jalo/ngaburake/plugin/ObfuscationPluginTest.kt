package com.jalo.ngaburake.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObfuscationPluginTest {

    @Test
    fun `apply plugin mendaftarkan extension obfuscationVerify`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val extension = project.extensions.findByName("obfuscationVerify")
        assertNotNull(extension)
        assertTrue(extension is ObfuscationExtension)
    }

    @Test
    fun `apply plugin mendaftarkan task verifyObfuscation`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val task = project.tasks.findByName("verifyObfuscation")
        assertNotNull(task)
        assertTrue(task is MappingAnalysisTask)
    }

    @Test
    fun `extension default value sesuai convention`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(ObfuscationPlugin::class.java)

        val extension = project.extensions.getByType(ObfuscationExtension::class.java)

        assertEquals(false, extension.failOnViolation.get())
        assertEquals(ReportFormat.CONSOLE, extension.reportFormat.get())
    }

    @Test
    fun `apply plugin tidak error walau AGP belum di-apply`() {
        val project = ProjectBuilder.builder().build()

        // Tidak ada task minify*WithR8 sama sekali karena AGP tidak di-apply — plugin harus
        // tetap berhasil apply tanpa UnknownTaskException.
        project.pluginManager.apply(ObfuscationPlugin::class.java)

        assertNotNull(project.tasks.findByName("verifyObfuscation"))
    }
}
