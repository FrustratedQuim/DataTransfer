/*
 * Класс может быть удалён. Создан для примера работы - получение данных из SQLite.
 * Передаваемые пакетом данные могут также быть вытянуты из иных API или базовых параметров игрока.
 */

package com.ratger.datatransfer

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

class SQLiteManager(plugin: DataTransfer) {

    private val connection: Connection

    init {
        val dbFile = File(plugin.dataFolder, "players.db")
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()

        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        connection.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY," +
                    "code TEXT NOT NULL)"
        )
    }

    fun addPlayer(uuid: UUID) {
        val code = generateRandomString()
        val stmt = connection.prepareStatement("INSERT OR IGNORE INTO players(uuid, code) VALUES(?, ?)")
        stmt.setString(1, uuid.toString())
        stmt.setString(2, code)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getCode(uuid: UUID): String? {
        val selectStmt: PreparedStatement = connection.prepareStatement("SELECT code FROM players WHERE uuid = ?")
        selectStmt.setString(1, uuid.toString())
        val rs: ResultSet = selectStmt.executeQuery()
        val code = if (rs.next()) rs.getString("code") else null

        rs.close()
        selectStmt.close()
        return code
    }

    private fun generateRandomString(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }

    fun close() {
        connection.close()
    }
}