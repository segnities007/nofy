package com.segnities007.nofy.security

/** リリースビルドで検出された「危険な実行環境」の理由の集合。 */
internal data class RiskyEnvironment(
    val reasons: List<RiskyEnvironmentReason>
)

/** デバッガ・ルート・フック等、機密操作をブロックする契機となる要因。 */
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
