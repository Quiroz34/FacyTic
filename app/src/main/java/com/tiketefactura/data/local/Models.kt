package com.tiketefactura.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- CATEGORIA ---
@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun getAll(): Flow<List<Categoria>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: Categoria)
    @Update
    suspend fun update(c: Categoria)
    @Delete
    suspend fun delete(c: Categoria)
}

// --- CLIENTE ---
@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val telefono: String? = null,
    val correo: String? = null,
    val direccion: String? = null,
    val deuda: Double = 0.0 // Nuevo: Deuda acumulada
)

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAll(): Flow<List<Cliente>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: Cliente)
    @Update
    suspend fun update(c: Cliente)
    @Delete
    suspend fun delete(c: Cliente)
}

// --- PRODUCTO ---
@Entity(
    tableName = "productos",
    foreignKeys = [ForeignKey(
        entity = Categoria::class,
        parentColumns = ["id"],
        childColumns = ["categoriaId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["categoriaId"])]
)
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val precio: Double,
    val descripcion: String? = null,
    val categoriaId: Int? = null,
    val stock: Double = 0.0,
    val codigoBarras: String? = null,
    val costo: Double = 0.0,
    val stockMinimo: Double = 5.0,
    val proveedor: String = "",
    val unidad: String = "PZA"
)

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAll(): Flow<List<Producto>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: Producto)
    @Update
    suspend fun update(p: Producto)
    @Delete
    suspend fun delete(p: Producto)
}

// --- MOVIMIENTO DE INVENTARIO ---
@Entity(tableName = "inventario_movimientos")
data class InventarioMovimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val productoNombre: String,
    val cantidad: Double,
    val tipo: String, // "ENTRADA", "VENTA", "AJUSTE"
    val fecha: String
)

@Dao
interface InventarioDao {
    @Query("SELECT * FROM inventario_movimientos ORDER BY id DESC")
    fun getRecientes(): Flow<List<InventarioMovimiento>>
    @Insert
    suspend fun insert(m: InventarioMovimiento)
}

// --- GASTO ---
@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoria: String,
    val metodo: String = "EFECTIVO",
    val cajaId: Int? = null
)

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun getAll(): Flow<List<Gasto>>
    @Insert
    suspend fun insert(g: Gasto)
    @Update
    suspend fun update(g: Gasto)
    @Delete
    suspend fun delete(g: Gasto)
}

// --- EMPRESA / CONFIG ---
@Entity(tableName = "empresa")
data class Empresa(
    @PrimaryKey val id: Int = 1,
    val nombre: String = "Mi Negocio",
    val ruc: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val eslogan: String? = null,
    val mensajePie: String? = "¡Gracias por su preferencia!",
    val logoUri: String? = null,
    val impuestoActivo: Boolean = true,
    val impuestoPorcentaje: Double = 15.0,
    val permitirSinStock: Boolean = false,
    val formatoPapel: String = "A4"
)

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresa WHERE id = 1")
    fun get(): Flow<Empresa?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: Empresa)
}

// --- FACTURA / TICKET ---
@Entity(tableName = "documentos")
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val numero: String,
    val fecha: String,
    val clienteNombre: String? = "Consumidor Final",
    val clienteId: Int? = null,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0, // Nuevo: Descuento aplicado
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String = "PAGADA", // "PAGADA" o "PENDIENTE"
    val metodoPago: String = "EFECTIVO",
    val impuestoPorcentaje: Double = 0.0,
    val firma: String? = null,
    val abonado: Double = 0.0,
    val efectivoRecibido: Double = 0.0,
    val costoTotal: Double = 0.0,
    val cajaId: Int? = null,
    val empresaSnapshot: String? = null,
    val motivoAnulacion: String? = null
)

@Entity(
    tableName = "documento_items",
    foreignKeys = [ForeignKey(
        entity = Documento::class,
        parentColumns = ["id"],
        childColumns = ["documentoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["documentoId"])]
)
data class DocumentoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentoId: Int,
    val nombre: String,
    val cantidad: Double,
    val precio: Double,
    val subtotal: Double,
    val productoId: Int? = null,
    val costo: Double = 0.0,
    val unidad: String = "PZA"
)

data class DocumentoConItems(
    @Embedded val documento: Documento,
    @Relation(parentColumn = "id", entityColumn = "documentoId")
    val items: List<DocumentoItem>
)

@Dao
interface DocumentoDao {
    @Transaction
    @Query("SELECT * FROM documentos ORDER BY id DESC")
    fun getAllWithItems(): Flow<List<DocumentoConItems>>

    @Insert
    suspend fun insertDocumento(d: Documento): Long

    @Update
    suspend fun update(d: Documento)

    @Transaction
    @Query("SELECT * FROM documentos WHERE id = :id")
    suspend fun find(id: Int): DocumentoConItems?

    @Insert
    suspend fun insertItems(items: List<DocumentoItem>)

    @Transaction
    suspend fun saveFullDocumento(d: Documento, items: List<DocumentoItem>) {
        val docId = insertDocumento(d).toInt()
        val itemsWithId = items.map { it.copy(documentoId = docId) }
        insertItems(itemsWithId)
    }
}

@Entity(tableName = "abonos")
data class Abono(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clienteId: Int,
    val documentoId: Int?,
    val monto: Double,
    val fecha: String,
    val metodo: String = "EFECTIVO",
    val cajaId: Int? = null
)

@Entity(tableName = "cajas")
data class Caja(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val apertura: String,
    val fondo: Double,
    val cierre: String? = null,
    val contado: Double? = null,
    val esperado: Double? = null
)

@Dao
interface OperacionesDao {
    @Query("SELECT * FROM reembolsos ORDER BY id DESC")
    fun reembolsos(): Flow<List<Reembolso>>
    @Insert suspend fun insertReembolso(r: Reembolso): Long
    @Query("SELECT * FROM abonos ORDER BY id DESC")
    fun abonos(): Flow<List<Abono>>
    @Query("SELECT * FROM cajas ORDER BY id DESC")
    fun cajas(): Flow<List<Caja>>
    @Query("SELECT * FROM cajas WHERE cierre IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun cajaAbierta(): Caja?
    @Insert suspend fun insertAbono(a: Abono): Long
    @Insert suspend fun insertCaja(c: Caja): Long
    @Update suspend fun updateCaja(c: Caja)
}

@Entity(tableName = "reembolsos")
data class Reembolso(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentoId: Int,
    val monto: Double,
    val metodo: String,
    val fecha: String,
    val cajaId: Int?,
    val motivo: String
)

// --- DATABASE ---
@Database(
    entities = [Producto::class, Empresa::class, Documento::class, DocumentoItem::class, Categoria::class, Cliente::class, Gasto::class, InventarioMovimiento::class, Abono::class, Caja::class, Reembolso::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun operacionesDao(): OperacionesDao
    abstract fun productoDao(): ProductoDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun gastoDao(): GastoDao
    abstract fun inventarioDao(): InventarioDao
}
