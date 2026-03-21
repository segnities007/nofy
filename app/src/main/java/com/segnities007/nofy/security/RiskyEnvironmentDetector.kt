package com.segnities007.nofy.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.segnities007.nofy.BuildConfig
import java.io.File

internal class RiskyEnvironmentDetector(
    private val context: Context
) {
    fun detect(): RiskyEnvironment? {
        if (BuildConfig.DEBUG) {
            return null
        }

        val reasons = buildList {
            if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
                add(RiskyEnvironmentReason.DebuggerAttached)
            }
            if (isUnexpectedlyDebuggableApp()) {
                add(RiskyEnvironmentReason.UnexpectedDebuggableApp)
            }
            if (Build.TAGS?.contains("test-keys") == true) {
                add(RiskyEnvironmentReason.TestKeysBuild)
            }
            if (hasRootArtifact()) {
                add(RiskyEnvironmentReason.RootArtifactDetected)
            }
            if (hasKnownRootManager()) {
                add(RiskyEnvironmentReason.RootManagerDetected)
            }
        }

        return reasons.takeIf(List<RiskyEnvironmentReason>::isNotEmpty)
            ?.let(::RiskyEnvironment)
    }

    private fun isUnexpectedlyDebuggableApp(): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private fun hasRootArtifact(): Boolean {
        return knownRootArtifacts.any(::existsSafely)
    }

    private fun hasKnownRootManager(): Boolean {
        return knownRootPackages.any(::isPackageInstalled)
    }

    private fun existsSafely(path: String): Boolean {
        return runCatching { File(path).exists() }.getOrDefault(false)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return runCatching {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        }.getOrDefault(false)
    }

    private companion object {
        val knownRootArtifacts = listOf(
            "/system/app/Superuser.apk",
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/cache/su",
            "/data/adb/magisk",
            "/init.magisk.rc"
        )

        val knownRootPackages = listOf(
            "com.topjohnwu.magisk",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.kingroot.kinguser",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine"
        )
    }
}
