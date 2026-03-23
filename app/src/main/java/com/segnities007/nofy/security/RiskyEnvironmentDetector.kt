package com.segnities007.nofy.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.segnities007.nofy.BuildConfig
import java.io.File
import java.util.Locale

/**
 * リリースビルド向けの実行環境ヒューリスティック（デバッグビルドでは常に「安全」）。
 * いずれかの条件に該当すると [RiskyEnvironment] を返す。
 */
internal class RiskyEnvironmentDetector(
    private val context: Context
) {
    /** 該当理由が無ければ `null`。 */
    fun detect(): RiskyEnvironment? {
        if (BuildConfig.DEBUG) {
            return null
        }

        val reasons = buildList {
            if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
                add(RiskyEnvironmentReason.DebuggerAttached)
            }
            if (hasTracerPid()) {
                add(RiskyEnvironmentReason.ProcessTraced)
            }
            if (hasFridaServerPort()) {
                add(RiskyEnvironmentReason.FridaServerDetected)
            }
            if (hasInjectedHookLibrary()) {
                add(RiskyEnvironmentReason.InjectedHookLibraryDetected)
            }
            if (hasHookFrameworkPackage()) {
                add(RiskyEnvironmentReason.HookFrameworkPackageDetected)
            }
            if (hasSuspiciousEnvironmentVariable()) {
                add(RiskyEnvironmentReason.SuspiciousEnvironmentVariable)
            }
            if (isUnexpectedlyDebuggableApp()) {
                add(RiskyEnvironmentReason.UnexpectedDebuggableApp)
            }
            if (Build.TAGS?.contains("test-keys") == true) {
                add(RiskyEnvironmentReason.TestKeysBuild)
            }
            if (hasWritableSystemPartition()) {
                add(RiskyEnvironmentReason.WritableSystemPartition)
            }
            if (isSelinuxPermissive()) {
                add(RiskyEnvironmentReason.PermissiveSelinux)
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
        return RiskyEnvironmentHeuristics.knownRootArtifacts.any(::existsSafely)
    }

    private fun hasKnownRootManager(): Boolean {
        return RiskyEnvironmentHeuristics.knownRootPackages.any(::isPackageInstalled)
    }

    private fun hasHookFrameworkPackage(): Boolean {
        return RiskyEnvironmentHeuristics.knownHookPackages.any(::isPackageInstalled)
    }

    private fun existsSafely(path: String): Boolean {
        return runCatching { File(path).exists() }.getOrDefault(false)
    }

    private fun hasTracerPid(): Boolean {
        val status = runCatching {
            File(RiskyEnvironmentHeuristics.PROC_SELF_STATUS_PATH).readText()
        }.getOrNull() ?: return false
        return parseTracerPid(status)?.let { it > 0 } == true
    }

    private fun hasInjectedHookLibrary(): Boolean {
        return fileContainsAnyIndicator(
            path = RiskyEnvironmentHeuristics.PROC_SELF_MAPS_PATH,
            indicators = RiskyEnvironmentHeuristics.suspiciousMapIndicators
        )
    }

    private fun hasFridaServerPort(): Boolean {
        return RiskyEnvironmentHeuristics.knownFridaPorts.any { port ->
            parseListeningTcpPorts(readTextSafely(RiskyEnvironmentHeuristics.PROC_NET_TCP_PATH)).contains(port) ||
                parseListeningTcpPorts(readTextSafely(RiskyEnvironmentHeuristics.PROC_NET_TCP6_PATH)).contains(port)
        }
    }

    private fun hasSuspiciousEnvironmentVariable(): Boolean {
        return !System.getenv(RiskyEnvironmentHeuristics.LD_PRELOAD_ENV).isNullOrBlank()
    }

    private fun hasWritableSystemPartition(): Boolean {
        return readLinesSafely(RiskyEnvironmentHeuristics.PROC_MOUNTS_PATH).any(::isWritableSensitiveMountLine)
    }

    private fun isSelinuxPermissive(): Boolean {
        return readTextSafely(RiskyEnvironmentHeuristics.SELINUX_ENFORCE_PATH)
            .trim()
            .equals("0")
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

    private fun readTextSafely(path: String): String {
        return runCatching { File(path).readText() }.getOrDefault("")
    }

    private fun readLinesSafely(path: String): List<String> {
        return runCatching { File(path).readLines() }.getOrDefault(emptyList())
    }

    private fun fileContainsAnyIndicator(
        path: String,
        indicators: List<String>
    ): Boolean {
        return readLinesSafely(path).any { line ->
            val normalizedLine = line.lowercase(Locale.US)
            indicators.any(normalizedLine::contains)
        }
    }
}
