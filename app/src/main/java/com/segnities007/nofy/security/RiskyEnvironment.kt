package com.segnities007.nofy.security

internal data class RiskyEnvironment(
    val reasons: List<RiskyEnvironmentReason>
)

internal enum class RiskyEnvironmentReason {
    DebuggerAttached,
    ProcessTraced,
    FridaServerDetected,
    InjectedHookLibraryDetected,
    HookFrameworkPackageDetected,
    SuspiciousEnvironmentVariable,
    UnexpectedDebuggableApp,
    TestKeysBuild,
    WritableSystemPartition,
    PermissiveSelinux,
    RootArtifactDetected,
    RootManagerDetected
}
