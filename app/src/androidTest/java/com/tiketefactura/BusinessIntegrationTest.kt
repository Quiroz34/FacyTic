package com.tiketefactura

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiketefactura.data.*
import com.tiketefactura.data.local.*
import com.tiketefactura.di.DatabaseModule
import com.tiketefactura.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusinessIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: BusinessRepository
    private val context: Context = ApplicationProvider.getApplicationContext()
    @Before fun setup() { db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build(); repo = BusinessRepository(db) }
    @After fun cleanup() { db.close() }
    private suspend fun seed(): Producto {
        repo.company(Empresa(nombre = "Prueba", impuestoActivo = false, impuestoPorcentaje = 16.0))
        repo.product(Producto(nombre = "Café", precio = 10.0, costo = 4.0, stock = 10.0, codigoBarras = "123"))
        return db.productoDao().getAll().first().single()
    }
    private suspend fun sale(p: Producto, qty: Double = 1.0, method: String = "EFECTIVO", client: Int? = null) = repo.sale(listOf(SaleLine(p.id, qty)), "Cliente", client, "TICKET", method, 0.0, false, 1000.0, null)

    @Test fun saleUsesSavedReceiptAndSnapshotsCompany() = runBlocking {
        val p = seed(); repo.openBox(100.0)
        val d = sale(p, 1.5)
        assertEquals("T-00000001", d.documento.numero); assertEquals(15.0, d.documento.subtotal, 0.001)
        assertEquals(8.5, db.productoDao().getAll().first().single().stock, 0.001)
        assertEquals(6.0, d.documento.costoTotal, 0.001)
        assertEquals(115.0, BusinessRepository.expectedCash(db.operacionesDao().cajaAbierta()!!, db.documentoDao().getAllWithItems().first(), emptyList(), emptyList()), 0.001)
        repo.company(Empresa(nombre = "Nombre nuevo"))
        assertEquals("Prueba", JSONObject(d.documento.empresaSnapshot!!).getString("nombre"))
        val pdf = PdfGenerator.create(context, d, Empresa())
        assertTrue(pdf.length() > 0)
        android.graphics.pdf.PdfRenderer(android.os.ParcelFileDescriptor.open(pdf, android.os.ParcelFileDescriptor.MODE_READ_ONLY)).use { assertTrue(it.pageCount > 0) }
    }
    @Test fun failureRollsBackStockDebtAndReceipt() = runBlocking {
        val p = seed(); repo.client(Cliente(nombre = "Ana")); val c = db.clienteDao().getAll().first().single()
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_items BEFORE INSERT ON documento_items BEGIN SELECT RAISE(ABORT, 'test failure'); END")
        try { sale(p, 1.0, "CRÉDITO", c.id); fail("Expected failure") } catch (_: android.database.sqlite.SQLiteException) { }
        assertTrue(db.documentoDao().getAllWithItems().first().isEmpty())
        assertEquals(10.0, db.productoDao().getAll().first().single().stock, 0.001)
        assertEquals(0.0, db.clienteDao().getAll().first().single().deuda, 0.001)
    }
    @Test fun concurrentSalesCannotOversell() = runBlocking {
        val p = seed()
        val outcomes = coroutineScope { listOf(async(Dispatchers.IO) { runCatching { sale(p, 7.0) }.isSuccess }, async(Dispatchers.IO) { runCatching { sale(p, 7.0) }.isSuccess }).awaitAll() }
        assertEquals(1, outcomes.count { it }); assertEquals(3.0, db.productoDao().getAll().first().single().stock, 0.001)
    }
    @Test fun creditPaymentUpdatesInvoiceWithoutNegativeExpenses() = runBlocking {
        val p = seed(); repo.client(Cliente(nombre = "Ana")); val c = db.clienteDao().getAll().first().single()
        val d = sale(p, 2.0, "CRÉDITO", c.id)
        repo.payment(c.id, 5.0, "EFECTIVO", d.documento.id)
        assertEquals(15.0, db.clienteDao().getAll().first().single().deuda, 0.001)
        assertEquals("PENDIENTE", db.documentoDao().find(d.documento.id)!!.documento.estado)
        repo.payment(c.id, 15.0, "TARJETA")
        assertEquals("PAGADA", db.documentoDao().find(d.documento.id)!!.documento.estado)
        assertTrue(db.gastoDao().getAll().first().isEmpty())
        try { repo.payment(c.id, -1.0, "EFECTIVO"); fail("Negative payment") } catch (_: IllegalArgumentException) { }
    }
    @Test fun cancellationRestoresInventoryAndRejectsRepeatedReturn() = runBlocking {
        val p = seed(); repo.openBox(0.0); val d = sale(p, 2.0)
        repo.cancel(d.documento.id, "Devolución completa")
        assertEquals(10.0, db.productoDao().getAll().first().single().stock, 0.001)
        assertEquals("ANULADA", db.documentoDao().find(d.documento.id)!!.documento.estado)
        assertEquals(0.0, BusinessRepository.expectedCash(db.operacionesDao().cajaAbierta()!!, db.documentoDao().getAllWithItems().first(), emptyList(), emptyList(), db.operacionesDao().reembolsos().first()), 0.001)
        try { repo.cancel(d.documento.id, "Otra vez"); fail("Repeated return") } catch (_: IllegalArgumentException) { }
    }
    @Test fun refundFromClosedBoxIsChargedToCurrentBox() = runBlocking {
        val p = seed(); repo.openBox(50.0); val d = sale(p, 2.0); repo.closeBox(70.0)
        repo.openBox(100.0); repo.cancel(d.documento.id, "Reembolso entregado")
        val boxes = db.operacionesDao().cajas().first()
        assertEquals(70.0, boxes.find { it.id == 1 }!!.esperado!!, 0.001)
        assertEquals(80.0, BusinessRepository.expectedCash(boxes.first(), db.documentoDao().getAllWithItems().first(), emptyList(), emptyList(), db.operacionesDao().reembolsos().first()), 0.001)
    }
    @Test fun paidCreditReturnRefundsPaymentsAndClearsOnlyOutstandingDebt() = runBlocking {
        val p = seed(); repo.client(Cliente(nombre = "Ana")); val c = db.clienteDao().getAll().first().single()
        repo.openBox(100.0); val d = sale(p, 2.0, "CRÉDITO", c.id)
        repo.payment(c.id, 5.0, "EFECTIVO", d.documento.id)
        repo.cancel(d.documento.id, "Devolución con abono reembolsado")
        assertEquals(0.0, db.clienteDao().getAll().first().single().deuda, 0.001)
        assertEquals(5.0, db.operacionesDao().reembolsos().first().single().monto, 0.001)
        assertEquals(100.0, BusinessRepository.expectedCash(db.operacionesDao().cajaAbierta()!!, db.documentoDao().getAllWithItems().first(), db.operacionesDao().abonos().first(), emptyList(), db.operacionesDao().reembolsos().first()), 0.001)
    }
    @Test fun closedBoxRemainsImmutable() = runBlocking {
        val p = seed(); repo.openBox(100.0); sale(p); repo.expense("Renta", 5.0, "Renta", "EFECTIVO")
        repo.closeBox(105.0)
        val c = db.operacionesDao().cajas().first().single()
        assertEquals(105.0, c.esperado!!, 0.001)
        try { repo.expense("Editar cierre", 9.0, "Renta", "EFECTIVO", db.gastoDao().getAll().first().single().id); fail("Closed box edit") } catch (_: IllegalArgumentException) { }
        try { repo.deleteExpense(db.gastoDao().getAll().first().single()); fail("Closed box expense") } catch (_: IllegalArgumentException) { }
    }
    @Test fun backupRoundTripAndMalformedRestoreRollback() = runBlocking {
        val p = seed(); sale(p)
        val backup = BackupManager.export(db)
        repo.adjust(p.id, 5.0, "Compra")
        BackupManager.restore(db, backup)
        assertEquals(9.0, db.productoDao().getAll().first().single().stock, 0.001)
        val broken = JSONObject(backup)
        broken.getJSONObject("tables").getJSONArray("documento_items").getJSONObject(0).put("documentoId", 9999)
        try { BackupManager.restore(db, broken.toString()); fail("Invalid foreign key") } catch (_: Exception) { }
        assertEquals(1, db.documentoDao().getAllWithItems().first().size)
        assertEquals(9.0, db.productoDao().getAll().first().single().stock, 0.001)
    }
    @Test fun rejectsDuplicatesAndKeepsStockWhenEditingProduct() = runBlocking {
        val p = seed()
        try { repo.product(Producto(nombre = "  CAFÉ  ", precio = 2.0)); fail("Duplicate name") } catch (_: IllegalArgumentException) { }
        try { repo.product(Producto(nombre = "Otro", precio = 2.0, codigoBarras = "123")); fail("Duplicate barcode") } catch (_: IllegalArgumentException) { }
        repo.adjust(p.id, 2.0, "Compra")
        repo.product(p.copy(nombre = "Café nuevo", precio = 12.0, stock = 1.0))
        assertEquals(12.0, db.productoDao().getAll().first().single().stock, 0.001)
        repo.client(Cliente(nombre = "Ana", telefono = "5512345678"))
        try { repo.client(Cliente(nombre = "Otra", telefono = "55 1234-5678")); fail("Duplicate phone") } catch (_: IllegalArgumentException) { }
        repo.category(Categoria(nombre = "Bebidas"))
        try { repo.category(Categoria(nombre = " BEBIDAS ")); fail("Duplicate category") } catch (_: IllegalArgumentException) { }
    }

    @Test fun migrationPreservesV6DataAndReconcilesLegacyBalance() = runBlocking {
        val name = "migration-test.db"; context.deleteDatabase(name)
        val file = context.getDatabasePath(name); file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { sql ->
            listOf(
                "CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL)",
                "CREATE TABLE clientes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, telefono TEXT, correo TEXT, direccion TEXT, deuda REAL NOT NULL)",
                "CREATE TABLE productos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, precio REAL NOT NULL, descripcion TEXT, categoriaId INTEGER, stock REAL NOT NULL, codigoBarras TEXT, FOREIGN KEY(categoriaId) REFERENCES categorias(id) ON UPDATE NO ACTION ON DELETE SET NULL)",
                "CREATE INDEX index_productos_categoriaId ON productos(categoriaId)",
                "CREATE TABLE empresa (id INTEGER PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, ruc TEXT, direccion TEXT, telefono TEXT, eslogan TEXT, mensajePie TEXT, logoUri TEXT, impuestoActivo INTEGER NOT NULL, impuestoPorcentaje REAL NOT NULL)",
                "CREATE TABLE documentos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tipo TEXT NOT NULL, numero TEXT NOT NULL, fecha TEXT NOT NULL, clienteNombre TEXT, clienteId INTEGER, subtotal REAL NOT NULL, descuento REAL NOT NULL, impuesto REAL NOT NULL, total REAL NOT NULL, estado TEXT NOT NULL, metodoPago TEXT NOT NULL)",
                "CREATE TABLE documento_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, documentoId INTEGER NOT NULL, nombre TEXT NOT NULL, cantidad REAL NOT NULL, precio REAL NOT NULL, subtotal REAL NOT NULL, FOREIGN KEY(documentoId) REFERENCES documentos(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
                "CREATE INDEX index_documento_items_documentoId ON documento_items(documentoId)",
                "CREATE TABLE gastos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, descripcion TEXT NOT NULL, monto REAL NOT NULL, fecha TEXT NOT NULL, categoria TEXT NOT NULL)",
                "CREATE TABLE inventario_movimientos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, productoId INTEGER NOT NULL, productoNombre TEXT NOT NULL, cantidad REAL NOT NULL, tipo TEXT NOT NULL, fecha TEXT NOT NULL)",
                "INSERT INTO clientes VALUES (1, 'Cliente anterior', NULL, NULL, NULL, 60)",
                "INSERT INTO productos VALUES (1, 'Anterior', 100, NULL, NULL, 8, NULL)",
                "INSERT INTO documentos VALUES (1, 'TICKET', 'T-12345', '27 ago, 12:00', 'Cliente anterior', 1, 100, 0, 0, 100, 'PENDIENTE', 'CRÉDITO')",
                "INSERT INTO documento_items VALUES (1, 1, 'Anterior', 1, 100, 100)"
            ).forEach(sql::execSQL)
            sql.version = 6
        }
        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(DatabaseModule.MIGRATION_6_7).build()
        try {
            assertEquals(60.0, migrated.clienteDao().getAll().first().single().deuda, 0.001)
            assertEquals(40.0, migrated.documentoDao().find(1)!!.documento.abonado, 0.001)
            assertEquals(8.0, migrated.productoDao().getAll().first().single().stock, 0.001)
            assertEquals("T-12345", migrated.documentoDao().find(1)!!.documento.numero)
        } finally { migrated.close(); context.deleteDatabase(name) }
    }

    @Test fun migrationPreservesV5DataThroughV7() = runBlocking {
        val name = "migration-v5-test.db"; context.deleteDatabase(name)
        val file = context.getDatabasePath(name); file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { sql ->
            listOf(
                "CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL)",
                "CREATE TABLE clientes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, telefono TEXT, correo TEXT, direccion TEXT)",
                "CREATE TABLE productos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, precio REAL NOT NULL, descripcion TEXT, categoriaId INTEGER, stock REAL NOT NULL, codigoBarras TEXT, FOREIGN KEY(categoriaId) REFERENCES categorias(id) ON UPDATE NO ACTION ON DELETE SET NULL)",
                "CREATE INDEX index_productos_categoriaId ON productos(categoriaId)",
                "CREATE TABLE empresa (id INTEGER PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, ruc TEXT, direccion TEXT, telefono TEXT, eslogan TEXT, mensajePie TEXT, logoUri TEXT, impuestoActivo INTEGER NOT NULL, impuestoPorcentaje REAL NOT NULL)",
                "CREATE TABLE documentos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tipo TEXT NOT NULL, numero TEXT NOT NULL, fecha TEXT NOT NULL, clienteNombre TEXT, clienteId INTEGER, subtotal REAL NOT NULL, impuesto REAL NOT NULL, total REAL NOT NULL, estado TEXT NOT NULL, metodoPago TEXT NOT NULL)",
                "CREATE TABLE documento_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, documentoId INTEGER NOT NULL, nombre TEXT NOT NULL, cantidad REAL NOT NULL, precio REAL NOT NULL, subtotal REAL NOT NULL, FOREIGN KEY(documentoId) REFERENCES documentos(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
                "CREATE INDEX index_documento_items_documentoId ON documento_items(documentoId)",
                "CREATE TABLE gastos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, descripcion TEXT NOT NULL, monto REAL NOT NULL, fecha TEXT NOT NULL, categoria TEXT NOT NULL)",
                "INSERT INTO clientes VALUES (1, 'Cliente v5', '555', NULL, NULL)",
                "INSERT INTO productos VALUES (1, 'Producto v5', 25, NULL, NULL, 3, 'V5')",
                "INSERT INTO documentos VALUES (1, 'TICKET', 'T-V5', '2026-01-02 10:00:00', 'Cliente v5', 1, 25, 0, 25, 'PAGADA', 'EFECTIVO')",
                "INSERT INTO documento_items VALUES (1, 1, 'Producto v5', 1, 25, 25)"
            ).forEach(sql::execSQL)
            sql.version = 5
        }
        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(DatabaseModule.MIGRATION_5_6, DatabaseModule.MIGRATION_6_7).build()
        try {
            assertEquals("Cliente v5", migrated.clienteDao().getAll().first().single().nombre)
            assertEquals(0.0, migrated.clienteDao().getAll().first().single().deuda, 0.001)
            assertEquals("T-V5", migrated.documentoDao().find(1)!!.documento.numero)
            assertEquals(3.0, migrated.productoDao().getAll().first().single().stock, 0.001)
        } finally { migrated.close(); context.deleteDatabase(name) }
    }
}
