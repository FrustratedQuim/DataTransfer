package com.ratger.datatransfer

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

object DataUtils {
    // Чтение строки из пакета
    fun readString(byteBuf: ByteBuf): String {
        val length = readVarInt(byteBuf)
        if (length < 0 || length > 32767) throw IllegalArgumentException("Invalid string length: $length")

        val bytes = ByteArray(length)
        byteBuf.readBytes(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    // Запись длины содержимого + самих данных
    fun writeString(byteBuf: ByteBuf, content: String) {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(byteBuf, bytes.size)
        byteBuf.writeBytes(bytes)
    }

    // Кастомная функция записи числа
    fun writeVarInt(buf: ByteBuf, value: Int) {
        var temp = value
        while (true) {
            if ((temp and 0x7F.inv()) == 0) {
                buf.writeByte(temp)
                return
            }
            buf.writeByte((temp and 0x7F) or 0x80)
            temp = temp ushr 7
        }
    }

    // Логика просчёта длины
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
}