package com.ratger.datatransfer

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

object DataUtils {
    fun readString(byteBuf: ByteBuf): String {
        val length = readVarInt(byteBuf)
        if (length < 0 || length > 32767) throw IllegalArgumentException("Invalid string length: $length")

        val bytes = ByteArray(length)
        byteBuf.readBytes(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    // Прямой функции для записи строки нет. Используем кастомную
    fun writeString(byteBuf: ByteBuf, value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(byteBuf, bytes.size)
        byteBuf.writeBytes(bytes)
    }

    fun readVarInt(byteBuf: ByteBuf): Int {
        var value = 0
        var position = 0
        var currentByte: Byte
        while (true) {
            currentByte = byteBuf.readByte()
            value = value or ((currentByte.toInt() and 0x7F) shl position)
            if (currentByte.toInt() and 0x80 == 0) break
            position += 7
            if (position >= 32) throw RuntimeException("VarInt is too big")
        }
        return value
    }

    fun writeVarInt(byteBuf: ByteBuf, value: Int) {
        var v = value
        while (true) {
            if (v and 0x7F.inv() == 0) {
                byteBuf.writeByte(v)
                return
            }
            byteBuf.writeByte((v and 0x7F) or 0x80)
            v = v ushr 7
        }
    }
}
