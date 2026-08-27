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
    val direccion: String? = null
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
    val codigoBarras: String? = null
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

// --- GASTO ---
@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoria: String // Renta, Luz, Mercancia, etc.
)

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun getAll(): Flow<List<Gasto>>
    @Insert
    suspend fun insert(g: Gasto)
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
    val impuestoPorcentaje: Double = 15.0
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
    val tipo: String, // "FACTURA" o "TICKET"
    val numero: String,
    val fecha: String,
    val clienteNombre: String? = "Consumidor Final",
    val clienteId: Int? = null,
    val subtotal: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String = "PAGADA",
    val metodoPago: String = "EFECTIVO" // Efectivo, Tarjeta, Transferencia
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
    val subtotal: Double
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

    @Insert
    suspend fun insertItems(items: List<DocumentoItem>)

    @Transaction
    suspend fun saveFullDocumento(d: Documento, items: List<DocumentoItem>) {
        val docId = insertDocumento(d).toInt()
        val itemsWithId = items.map { it.copy(documentoId = docId) }
        insertItems(itemsWithId)
    }
}

// --- DATABASE ---
@Database(
    entities = [Producto::class, Empresa::class, Documento::class, DocumentoItem::class, Categoria::class, Cliente::class, Gasto::class], 
    version = 5, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun gastoDao(): GastoDao
}
