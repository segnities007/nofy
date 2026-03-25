package com.segnities007.nofy.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskyEnvironmentDetectorTest {
    @Test
    fun `parseTracerPid returns traced pid when present`() {
        val status = """
            Name:   nofy
            State:  R (running)
            TracerPid:      4821
        """.trimIndent()

        assertEquals(4821, parseTracerPid(status))
    }

    @Test
    fun `parseTracerPid returns zero when not traced`() {
        val status = """
            Name:   nofy
            TracerPid:      0
        """.trimIndent()

        assertEquals(0, parseTracerPid(status))
    }

    @Test
    fun `parseTracerPid returns null when line is missing`() {
        val status = """
            Name:   nofy
            State:  S (sleeping)
        """.trimIndent()

        assertNull(parseTracerPid(status))
    }

    @Test
    fun `parseListeningTcpPorts returns only listening local ports`() {
        val tcp = """
              sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
               0: 0100007F:69A2 00000000:0000 0A 00000000:00000000 00:00000000 00000000  1000        0 1
               1: 0100007F:0050 00000000:0000 01 00000000:00000000 00:00000000 00000000  1000        0 2
               2: 0100007F:69A3 00000000:0000 0A 00000000:00000000 00:00000000 00000000  1000        0 3
        """.trimIndent()

        assertEquals(setOf(27042, 27043), parseListeningTcpPorts(tcp))
    }

    @Test
    fun `isWritableSensitiveMountLine detects rw system mount`() {
        assertTrue(
            isWritableSensitiveMountLine(
                "/dev/block/dm-0 /system ext4 rw,seclabel,relatime 0 0"
            )
        )
        assertFalse(
            isWritableSensitiveMountLine(
                "/dev/block/dm-0 /system ext4 ro,seclabel,relatime 0 0"
            )
        )
    }
}
