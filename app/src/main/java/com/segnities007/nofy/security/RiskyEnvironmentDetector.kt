package com.segnities007.nofy.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.segnities007.nofy.BuildConfig
import java.io.File
import java.util.Locale

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
        return knownRootArtifacts.any(::existsSafely)
    }

    private fun hasKnownRootManager(): Boolean {
        return knownRootPackages.any(::isPackageInstalled)
    }

    private fun hasHookFrameworkPackage(): Boolean {
        return knownHookPackages.any(::isPackageInstalled)
    }

    private fun existsSafely(path: String): Boolean {
        return runCatching { File(path).exists() }.getOrDefault(false)
    }

    private fun hasTracerPid(): Boolean {
        val status = runCatching {
            File(PROC_SELF_STATUS_PATH).readText()
        }.getOrNull() ?: return false
        return parseTracerPid(status)?.let { it > 0 } == true
    }

    private fun hasInjectedHookLibrary(): Boolean {
        return fileContainsAnyIndicator(
            path = PROC_SELF_MAPS_PATH,
            indicators = suspiciousMapIndicators
        )
    }

    private fun hasFridaServerPort(): Boolean {
        return knownFridaPorts.any { port ->
            parseListeningTcpPorts(readTextSafely(PROC_NET_TCP_PATH)).contains(port) ||
                parseListeningTcpPorts(readTextSafely(PROC_NET_TCP6_PATH)).contains(port)
        }
    }

    private fun hasSuspiciousEnvironmentVariable(): Boolean {
        return !System.getenv(LD_PRELOAD_ENV).isNullOrBlank()
    }

    private fun hasWritableSystemPartition(): Boolean {
        return readLinesSafely(PROC_MOUNTS_PATH).any(::isWritableSensitiveMountLine)
    }

    private fun isSelinuxPermissive(): Boolean {
        return readTextSafely(SELINUX_ENFORCE_PATH)
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

    private companion object {
        const val PROC_SELF_STATUS_PATH = "/proc/self/status"
        const val PROC_SELF_MAPS_PATH = "/proc/self/maps"
        const val PROC_NET_TCP_PATH = "/proc/net/tcp"
        const val PROC_NET_TCP6_PATH = "/proc/net/tcp6"
        const val PROC_MOUNTS_PATH = "/proc/mounts"
        const val SELINUX_ENFORCE_PATH = "/sys/fs/selinux/enforce"
        const val LD_PRELOAD_ENV = "LD_PRELOAD"

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

        val knownHookPackages = listOf(
            "de.robv.android.xposed.installer",
            "org.lsposed.manager",
            "io.github.libxposed.manager",
            "com.saurik.substrate",
            "me.weishu.exp"
        )

        val suspiciousMapIndicators = listOf(
            "frida",
            "xposed",
            "substrate",
            "lsposed",
            "edxp",
            "riru",
            "zygisk"
        )

        val knownFridaPorts = setOf(27042, 27043)
    }
}

internal fun parseTracerPid(statusContent: String): Int? {
    return statusContent.lineSequence()
        .firstOrNull { it.startsWith("TracerPid:") }
        ?.substringAfter(':')
        ?.trim()
        ?.toIntOrNull()
}

internal fun parseListeningTcpPorts(tcpContent: String): Set<Int> {
    return tcpContent.lineSequence()
        .drop(1)
        .mapNotNull { line ->
            val columns = line.trim().split(Regex("\\s+"))
            if (columns.size < 4 || columns[3] != TCP_LISTEN_STATE) {
                return@mapNotNull null
            }

            columns[1]
                .substringAfter(':', "")
                .takeIf(String::isNotEmpty)
                ?.toIntOrNull(HEXADECIMAL_RADIX)
        }
        .toSet()
}

internal fun isWritableSensitiveMountLine(line: String): Boolean {
    val columns = line.trim().split(Regex("\\s+"))
    if (columns.size < 4) {
        return false
    }

    val mountPoint = columns[1]
    val mountOptions = columns[3].split(',')
    return mountPoint in sensitiveMountPoints && "rw" in mountOptions
}

private const val TCP_LISTEN_STATE = "0A"
private const val HEXADECIMAL_RADIX = 16

private val sensitiveMountPoints = setOf("/system", "/vendor", "/product", "/system_ext")
