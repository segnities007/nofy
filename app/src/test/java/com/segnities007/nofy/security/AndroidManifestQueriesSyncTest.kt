package com.segnities007.nofy.security

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AndroidManifestQueriesSyncTest {

    @Test
    fun manifestIncludesAllRiskyEnvironmentPackageQueries() {
        val manifestText = readAppManifestText()
        RiskyEnvironmentHeuristics.allManifestQueryPackageNames().forEach { packageName ->
            assertTrue(
                "Missing <queries> entry for $packageName in AndroidManifest.xml",
                manifestText.contains("""android:name="$packageName"""")
            )
        }
    }

    private fun readAppManifestText(): String {
        val candidates = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml")
        )
        val manifestFile = candidates.firstOrNull { it.exists() }
            ?: error(
                "AndroidManifest.xml not found (tried ${candidates.map { it.path }}); cwd=${File("").absoluteFile}"
            )
        return manifestFile.readText()
    }
}
