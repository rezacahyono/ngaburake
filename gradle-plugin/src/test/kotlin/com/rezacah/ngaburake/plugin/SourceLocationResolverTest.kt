package com.rezacah.ngaburake.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SourceLocationResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `resolves a kt file under a source root`() {
        val root = tempFolder.newFolder("src", "main", "kotlin")
        val packageDir = File(root, "com/example")
        packageDir.mkdirs()
        val sourceFile = File(packageDir, "PaymentManager.kt")
        sourceFile.writeText("class PaymentManager")

        val resolver = SourceLocationResolver(listOf(root))

        assertThat(resolver.resolve("com.example.PaymentManager")).isEqualTo(sourceFile)
    }

    @Test
    fun `resolves a java file under a source root`() {
        val root = tempFolder.newFolder("src", "main", "java")
        val packageDir = File(root, "com/example")
        packageDir.mkdirs()
        val sourceFile = File(packageDir, "ApiKeyStore.java")
        sourceFile.writeText("class ApiKeyStore {}")

        val resolver = SourceLocationResolver(listOf(root))

        assertThat(resolver.resolve("com.example.ApiKeyStore")).isEqualTo(sourceFile)
    }

    @Test
    fun `returns null when no matching file exists under any root`() {
        val kotlinRoot = tempFolder.newFolder("src", "main", "kotlin")
        val javaRoot = tempFolder.newFolder("src", "main", "java")

        val resolver = SourceLocationResolver(listOf(kotlinRoot, javaRoot))

        assertThat(resolver.resolve("com.example.DoesNotExist")).isNull()
    }
}
