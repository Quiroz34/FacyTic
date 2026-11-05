package com.tiketefactura.di

import android.content.Context
import androidx.room.Room
import com.tiketefactura.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /** Preserves the last publicly shipped v5 database while introducing the v6 features. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            listOf(
                "ALTER TABLE clientes ADD COLUMN deuda REAL NOT NULL DEFAULT 0",
                "ALTER TABLE documentos ADD COLUMN descuento REAL NOT NULL DEFAULT 0",
                "CREATE TABLE IF NOT EXISTS inventario_movimientos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, productoId INTEGER NOT NULL, productoNombre TEXT NOT NULL, cantidad REAL NOT NULL, tipo TEXT NOT NULL, fecha TEXT NOT NULL)"
            ).forEach(db::execSQL)
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            listOf(
                "ALTER TABLE gastos ADD COLUMN metodo TEXT NOT NULL DEFAULT 'EFECTIVO'",
                "ALTER TABLE gastos ADD COLUMN cajaId INTEGER",
                "ALTER TABLE productos ADD COLUMN costo REAL NOT NULL DEFAULT 0",
                "ALTER TABLE productos ADD COLUMN stockMinimo REAL NOT NULL DEFAULT 5",
                "ALTER TABLE productos ADD COLUMN proveedor TEXT NOT NULL DEFAULT ''",
                "ALTER TABLE empresa ADD COLUMN permitirSinStock INTEGER NOT NULL DEFAULT 0",
                "ALTER TABLE empresa ADD COLUMN formatoPapel TEXT NOT NULL DEFAULT 'A4'",
                "ALTER TABLE documentos ADD COLUMN impuestoPorcentaje REAL NOT NULL DEFAULT 0",
                "ALTER TABLE documentos ADD COLUMN firma TEXT",
                "ALTER TABLE documentos ADD COLUMN abonado REAL NOT NULL DEFAULT 0",
                "ALTER TABLE documentos ADD COLUMN efectivoRecibido REAL NOT NULL DEFAULT 0",
                "ALTER TABLE documentos ADD COLUMN costoTotal REAL NOT NULL DEFAULT 0",
                "ALTER TABLE documentos ADD COLUMN cajaId INTEGER",
                "ALTER TABLE documentos ADD COLUMN empresaSnapshot TEXT",
                "ALTER TABLE documentos ADD COLUMN motivoAnulacion TEXT",
                "ALTER TABLE documento_items ADD COLUMN productoId INTEGER",
                "ALTER TABLE documento_items ADD COLUMN costo REAL NOT NULL DEFAULT 0",
                "CREATE TABLE IF NOT EXISTS abonos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, clienteId INTEGER NOT NULL, documentoId INTEGER, monto REAL NOT NULL, fecha TEXT NOT NULL, metodo TEXT NOT NULL, cajaId INTEGER)",
                "CREATE TABLE IF NOT EXISTS cajas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, apertura TEXT NOT NULL, fondo REAL NOT NULL, cierre TEXT, contado REAL, esperado REAL)",
                "CREATE TABLE IF NOT EXISTS reembolsos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, documentoId INTEGER NOT NULL, monto REAL NOT NULL, metodo TEXT NOT NULL, fecha TEXT NOT NULL, cajaId INTEGER, motivo TEXT NOT NULL)",
                "UPDATE documentos SET abonado = total WHERE estado = 'PAGADA'",
                "UPDATE documentos SET impuestoPorcentaje = CASE WHEN subtotal - descuento > 0 THEN ROUND(impuesto * 100.0 / (subtotal - descuento), 2) ELSE 0 END",
                // Old versions kept payments only in the aggregate client balance. Reconcile that
                // balance oldest-first without fabricating individual receipts or payment dates.
                "UPDATE documentos SET abonado = MIN(total, MAX(0, (SELECT COALESCE(SUM(d.total), 0) FROM documentos d WHERE d.clienteId = documentos.clienteId AND d.metodoPago = 'CRÉDITO') - (SELECT c.deuda FROM clientes c WHERE c.id = documentos.clienteId) - (SELECT COALESCE(SUM(p.total), 0) FROM documentos p WHERE p.clienteId = documentos.clienteId AND p.metodoPago = 'CRÉDITO' AND p.id < documentos.id))) WHERE metodoPago = 'CRÉDITO' AND clienteId IN (SELECT id FROM clientes) AND total >= 0",
                "UPDATE documentos SET estado = CASE WHEN abonado >= total THEN 'PAGADA' ELSE 'PENDIENTE' END WHERE metodoPago = 'CRÉDITO' AND clienteId IN (SELECT id FROM clientes) AND total >= 0"
            ).forEach(db::execSQL)
        }
    }
    /** Adds the selling unit while preserving all existing inventory as pieces. */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE productos ADD COLUMN unidad TEXT NOT NULL DEFAULT 'PZA'")
            db.execSQL("ALTER TABLE documento_items ADD COLUMN unidad TEXT NOT NULL DEFAULT 'PZA'")
        }
    }
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "tikete_v2.db")
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
    }

    @Provides
    fun provideProductoDao(db: AppDatabase): ProductoDao = db.productoDao()

    @Provides
    fun provideEmpresaDao(db: AppDatabase): EmpresaDao = db.empresaDao()

    @Provides
    fun provideDocumentoDao(db: AppDatabase): DocumentoDao = db.documentoDao()

    @Provides
    fun provideCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()

    @Provides
    fun provideClienteDao(db: AppDatabase): ClienteDao = db.clienteDao()

    @Provides
    fun provideGastoDao(db: AppDatabase): GastoDao = db.gastoDao()

    @Provides
    fun provideInventarioDao(db: AppDatabase): InventarioDao = db.inventarioDao()
}
