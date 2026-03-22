package com.segnities007.nofy.security

/**
 * [RiskyEnvironmentDetector] が参照する定数・パッケージ一覧。
 * `app` の AndroidManifest `<queries>` と同期すること（[allManifestQueryPackageNames]）。
 */
internal object RiskyEnvironmentHeuristics {
    const val PROC_SELF_STATUS_PATH: String = "/proc/self/status"
    const val PROC_SELF_MAPS_PATH: String = "/proc/self/maps"
    const val PROC_NET_TCP_PATH: String = "/proc/net/tcp"
    const val PROC_NET_TCP6_PATH: String = "/proc/net/tcp6"
    const val PROC_MOUNTS_PATH: String = "/proc/mounts"
    const val SELINUX_ENFORCE_PATH: String = "/sys/fs/selinux/enforce"
    const val LD_PRELOAD_ENV: String = "LD_PRELOAD"

    val knownRootArtifacts: List<String> = listOf(
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

    /**
     * Package names must also appear in `app` manifest `<queries>` so
     * [android.content.pm.PackageManager.getPackageInfo] works on API 30+.
     */
    val knownRootPackages: List<String> = listOf(
        "com.topjohnwu.magisk",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.kingroot.kinguser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine"
    )

    /** See [knownRootPackages] re: manifest `<queries>`. */
    val knownHookPackages: List<String> = listOf(
        "de.robv.android.xposed.installer",
        "org.lsposed.manager",
        "io.github.libxposed.manager",
        "com.saurik.substrate",
        "me.weishu.exp"
    )

    val suspiciousMapIndicators: List<String> = listOf(
        "frida",
        "xposed",
        "substrate",
        "lsposed",
        "edxp",
        "riru",
        "zygisk"
    )

    val knownFridaPorts: Set<Int> = setOf(27042, 27043)

    /** `app` モジュール `AndroidManifest.xml` の `<queries>` と一致させること。 */
    fun allManifestQueryPackageNames(): List<String> =
        knownRootPackages + knownHookPackages
}
