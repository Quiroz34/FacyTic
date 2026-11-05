package com.tiketefactura.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiketefactura.data.local.AppDatabase
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {
    private val tables = listOf("categorias", "clientes", "productos", "empresa", "documentos", "documento_items", "inventario_movimientos", "gastos", "cajas", "abonos", "reembolsos")
    suspend fun export(db: AppDatabase): String = db.withTransaction {
        JSONObject().apply {
            put("app", "TiketeFactura"); put("version", 8)
            val data = JSONObject()
            tables.forEach { table ->
                val rows = JSONArray()
                db.openHelper.readableDatabase.query("SELECT * FROM $table").use { cursor ->
                    while (cursor.moveToNext()) {
                        val row = JSONObject()
                        cursor.columnNames.forEachIndexed { i, name ->
                            row.put(name, when (cursor.getType(i)) {
                                android.database.Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                                android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i)
                                android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i)
                                else -> cursor.getString(i)
                            })
                        }; rows.put(row)
                    }
                }; data.put(table, rows)
            }; put("tables", data)
            val sequences = JSONObject()
            db.openHelper.readableDatabase.query("SELECT name, seq FROM sqlite_sequence").use { cursor ->
                while (cursor.moveToNext()) if (cursor.getString(0) in tables) sequences.put(cursor.getString(0), cursor.getLong(1))
            }
            put("sequences", sequences)
        }.toString()
    }

    private fun restoreRows(sql: SupportSQLiteDatabase, root: JSONObject) {
        val backupVersion = root.getInt("version")
        require(root.getString("app") == "TiketeFactura" && backupVersion in 7..8) { "El respaldo no es compatible" }
        val data = root.getJSONObject("tables")
        require(data.keys().asSequence().toSet() == tables.toSet()) { "El respaldo está incompleto" }
        val columns = tables.associateWith { table ->
            sql.query("PRAGMA table_info($table)").use { c -> buildSet { while (c.moveToNext()) add(c.getString(1)) } }
        }
        // Validate every row before touching existing data. SQL constraints are checked in the transaction.
        tables.forEach { table ->
            val rows = data.getJSONArray(table)
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                if (backupVersion == 7 && table in setOf("productos", "documento_items") && !row.has("unidad")) row.put("unidad", "PZA")
                require(row.keys().asSequence().toSet() == columns[table]) { "Columnas inválidas: $table" }
            }
        }
        listOf("documento_items", "abonos", "reembolsos", "inventario_movimientos", "documentos", "gastos", "productos", "clientes", "categorias", "empresa", "cajas").forEach { sql.execSQL("DELETE FROM $it") }
        tables.forEach { table ->
            val rows = data.getJSONArray(table)
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val values = ContentValues()
                row.keys().forEach { key -> when (val value = row.get(key)) {
                    JSONObject.NULL -> values.putNull(key)
                    is Number -> { require(value.toDouble().isFinite()); if (value is Int || value is Long) values.put(key, value.toLong()) else values.put(key, value.toDouble()) }
                    is String -> values.put(key, value)
                    else -> error("Dato inválido en $table")
                } }
                sql.insert(table, android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
            }
        }
        root.optJSONObject("sequences")?.let { sequences ->
            sequences.keys().forEach { table ->
                require(table in tables && sequences.getLong(table) >= 0) { "Secuencia de respaldo inválida" }
                val values = ContentValues().apply { put("seq", sequences.getLong(table)) }
                // Never rewind a sequence already used on this installation.
                sql.execSQL("UPDATE sqlite_sequence SET seq = MAX(seq, ?) WHERE name = ?", arrayOf(sequences.getLong(table), table))
                sql.query("SELECT name FROM sqlite_sequence WHERE name = ?", arrayOf(table)).use { cursor ->
                    if (!cursor.moveToFirst()) { values.put("name", table); sql.insert("sqlite_sequence", 0, values) }
                }
            }
        }
        sql.query("PRAGMA foreign_key_check").use { require(!it.moveToFirst()) { "El respaldo tiene relaciones inválidas" } }
        sql.query("SELECT id FROM clientes WHERE deuda < 0 UNION ALL SELECT id FROM productos WHERE precio < 0 OR costo < 0 OR stockMinimo < 0 UNION ALL SELECT id FROM documentos WHERE total < 0 OR abonado < 0 OR abonado > total").use { require(!it.moveToFirst()) { "El respaldo contiene importes inválidos" } }
        sql.query("SELECT COUNT(*) FROM cajas WHERE cierre IS NULL").use { it.moveToFirst(); require(it.getInt(0) <= 1) { "El respaldo contiene varias cajas abiertas" } }
    }

    suspend fun restore(db: AppDatabase, text: String) {
        require(text.length <= 30_000_000) { "El respaldo supera 30 MB" }
        val root = JSONObject(text)
        db.withTransaction { restoreRows(db.openHelper.writableDatabase, root) }
        db.invalidationTracker.refreshVersionsAsync()
    }

    fun read(context: Context, uri: Uri): String = context.contentResolver.openInputStream(uri)!!.use { input ->
        val bytes = input.readNBytesCompat(30_000_001)
        require(bytes.size <= 30_000_000) { "El respaldo supera 30 MB" }; bytes.toString(Charsets.UTF_8)
    }
    private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192)
        while (output.size() < limit) { val n = read(buffer, 0, minOf(buffer.size, limit - output.size())); if (n < 0) break; output.write(buffer, 0, n) }
        return output.toByteArray()
    }
}
