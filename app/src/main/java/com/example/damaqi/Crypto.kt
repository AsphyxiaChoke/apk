package com.example.damaqi

import kotlin.random.Random

private const val MASK32: Long = 0xFFFFFFFFL
private const val MASK16: Long = 0xFFFFL

fun toUInt32Signed(v: Long): Long {
    var x = v
    if (x < 0) x = (1L shl 32) + x
    return x and MASK32
}

private fun u32(v: Long): Long = v and MASK32
private fun u16(v: Long): Long = v and MASK16

fun c51Encrypt32(v: Long, machineId: Long, lineNum: Long): Long {
    val k = longArrayOf(
        u32(0x12345678L xor machineId xor lineNum),
        u32(0x23456789L xor machineId xor lineNum),
        u32(0x3456789aL xor machineId xor lineNum),
        u32(0x456789abL xor machineId xor lineNum)
    )

    var v0 = u16(v)
    var v1 = u16(v shr 16)

    var n = 32
    var sum = 0L
    val delta = 0x9e3779b9L

    while (n > 0) {
        n--
        sum = u32(sum + delta)

        var p1 = u32(u16(v1 shl 4) + k[0])
        var p2 = u32(v1 + sum)
        var p3 = u32(u16(v1 shr 5) + k[1])
        var x = p1 xor p2 xor p3
        v0 = u16(v0 + x)

        p1 = u32(u16(v0 shl 4) + k[2])
        p2 = u32(v0 + sum)
        p3 = u32(u16(v0 shr 5) + k[3])
        x = p1 xor p2 xor p3
        v1 = u16(v1 + x)
    }

    return u32((v1 shl 16) or v0)
}

fun encrypt32CustomDelta(v: Long, deltaVal: Long): Long {
    val lineNum = 11111111L
    val k = longArrayOf(
        u32(0x12345678L xor v xor lineNum),
        u32(0x23456789L xor v xor lineNum),
        u32(0x3456789aL xor v xor lineNum),
        u32(0x456789abL xor v xor lineNum)
    )

    var v0 = u16(v)
    var v1 = u16(v shr 16)

    var n = 32
    var sum = 0L

    while (n > 0) {
        n--
        sum = u32(sum + deltaVal)

        var p1 = u32(u16(v1 shl 4) + k[0])
        var p2 = u32(v1 + sum)
        var p3 = u32(u16(v1 shr 5) + k[1])
        var x = p1 xor p2 xor p3
        v0 = u16(v0 + x)

        p1 = u32(u16(v0 shl 4) + k[2])
        p2 = u32(v0 + sum)
        p3 = u32(u16(v0 shr 5) + k[3])
        x = p1 xor p2 xor p3
        v1 = u16(v1 + x)
    }

    return u32((v1 shl 16) or v0)
}

data class AuditInput(
    val line: Long,
    val machine: Long,
    val serial: Long,
    val lastMoney: Long,
    val currMoney: Long,
    val allow: Long
)

object DaMaQi {

    fun generateAudit(input: AuditInput): String {
        val line = toUInt32Signed(input.line)
        val machine = toUInt32Signed(input.machine)
        val serial = toUInt32Signed(input.serial)
        val last = toUInt32Signed(input.lastMoney)
        val curr = toUInt32Signed(input.currMoney)
        val allow = (if (input.allow > 99) 99L else input.allow) and 0xFFL

        val xorRes = serial xor machine xor line xor last xor curr
        val splice = (xorRes and 0xFFFFFF00L) or allow

        return c51Encrypt32(splice, machine, line).toString()
    }

    fun generateConfig(line: Long, machine: Long, serial: Long): String {
        val l = toUInt32Signed(line)
        val m = toUInt32Signed(machine)
        val s = toUInt32Signed(serial)
        return c51Encrypt32(s, m, l).toString()
    }

    fun generateBackground(machine: Long): String {
        var m = toUInt32Signed(machine)
        if (m > 99999999L) m = 99999999L
        val randomSerial = Random.nextInt(0, 100000)
        val signature = encrypt32CustomDelta(m, randomSerial.toLong())
        return "%010d%05d".format(signature, randomSerial)
    }
}
