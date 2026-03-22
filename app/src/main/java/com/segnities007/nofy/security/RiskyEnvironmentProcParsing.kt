package com.segnities007.nofy.security

import java.util.Locale

private const val TCP_LISTEN_STATE = "0A"
private const val HEXADECIMAL_RADIX = 16

private val sensitiveMountPoints = setOf("/system", "/vendor", "/product", "/system_ext")

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
