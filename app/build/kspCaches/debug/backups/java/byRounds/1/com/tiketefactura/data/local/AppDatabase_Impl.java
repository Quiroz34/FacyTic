package com.tiketefactura.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ProductoDao _productoDao;

  private volatile EmpresaDao _empresaDao;

  private volatile DocumentoDao _documentoDao;

  private volatile CategoriaDao _categoriaDao;

  private volatile ClienteDao _clienteDao;

  private volatile GastoDao _gastoDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `productos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `precio` REAL NOT NULL, `descripcion` TEXT, `categoriaId` INTEGER, `stock` REAL NOT NULL, `codigoBarras` TEXT, FOREIGN KEY(`categoriaId`) REFERENCES `categorias`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_productos_categoriaId` ON `productos` (`categoriaId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `empresa` (`id` INTEGER NOT NULL, `nombre` TEXT NOT NULL, `ruc` TEXT, `direccion` TEXT, `telefono` TEXT, `eslogan` TEXT, `mensajePie` TEXT, `logoUri` TEXT, `impuestoActivo` INTEGER NOT NULL, `impuestoPorcentaje` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `documentos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tipo` TEXT NOT NULL, `numero` TEXT NOT NULL, `fecha` TEXT NOT NULL, `clienteNombre` TEXT, `clienteId` INTEGER, `subtotal` REAL NOT NULL, `impuesto` REAL NOT NULL, `total` REAL NOT NULL, `estado` TEXT NOT NULL, `metodoPago` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `documento_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `documentoId` INTEGER NOT NULL, `nombre` TEXT NOT NULL, `cantidad` REAL NOT NULL, `precio` REAL NOT NULL, `subtotal` REAL NOT NULL, FOREIGN KEY(`documentoId`) REFERENCES `documentos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documento_items_documentoId` ON `documento_items` (`documentoId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categorias` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `clientes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `telefono` TEXT, `correo` TEXT, `direccion` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `gastos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `descripcion` TEXT NOT NULL, `monto` REAL NOT NULL, `fecha` TEXT NOT NULL, `categoria` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7a59ef4ea44c4c367a1f22d20098fcaf')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `productos`");
        db.execSQL("DROP TABLE IF EXISTS `empresa`");
        db.execSQL("DROP TABLE IF EXISTS `documentos`");
        db.execSQL("DROP TABLE IF EXISTS `documento_items`");
        db.execSQL("DROP TABLE IF EXISTS `categorias`");
        db.execSQL("DROP TABLE IF EXISTS `clientes`");
        db.execSQL("DROP TABLE IF EXISTS `gastos`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProductos = new HashMap<String, TableInfo.Column>(7);
        _columnsProductos.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("precio", new TableInfo.Column("precio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("descripcion", new TableInfo.Column("descripcion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("categoriaId", new TableInfo.Column("categoriaId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("stock", new TableInfo.Column("stock", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductos.put("codigoBarras", new TableInfo.Column("codigoBarras", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProductos = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysProductos.add(new TableInfo.ForeignKey("categorias", "SET NULL", "NO ACTION", Arrays.asList("categoriaId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesProductos = new HashSet<TableInfo.Index>(1);
        _indicesProductos.add(new TableInfo.Index("index_productos_categoriaId", false, Arrays.asList("categoriaId"), Arrays.asList("ASC")));
        final TableInfo _infoProductos = new TableInfo("productos", _columnsProductos, _foreignKeysProductos, _indicesProductos);
        final TableInfo _existingProductos = TableInfo.read(db, "productos");
        if (!_infoProductos.equals(_existingProductos)) {
          return new RoomOpenHelper.ValidationResult(false, "productos(com.tiketefactura.data.local.Producto).\n"
                  + " Expected:\n" + _infoProductos + "\n"
                  + " Found:\n" + _existingProductos);
        }
        final HashMap<String, TableInfo.Column> _columnsEmpresa = new HashMap<String, TableInfo.Column>(10);
        _columnsEmpresa.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("ruc", new TableInfo.Column("ruc", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("direccion", new TableInfo.Column("direccion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("telefono", new TableInfo.Column("telefono", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("eslogan", new TableInfo.Column("eslogan", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("mensajePie", new TableInfo.Column("mensajePie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("logoUri", new TableInfo.Column("logoUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("impuestoActivo", new TableInfo.Column("impuestoActivo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmpresa.put("impuestoPorcentaje", new TableInfo.Column("impuestoPorcentaje", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmpresa = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmpresa = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEmpresa = new TableInfo("empresa", _columnsEmpresa, _foreignKeysEmpresa, _indicesEmpresa);
        final TableInfo _existingEmpresa = TableInfo.read(db, "empresa");
        if (!_infoEmpresa.equals(_existingEmpresa)) {
          return new RoomOpenHelper.ValidationResult(false, "empresa(com.tiketefactura.data.local.Empresa).\n"
                  + " Expected:\n" + _infoEmpresa + "\n"
                  + " Found:\n" + _existingEmpresa);
        }
        final HashMap<String, TableInfo.Column> _columnsDocumentos = new HashMap<String, TableInfo.Column>(11);
        _columnsDocumentos.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("tipo", new TableInfo.Column("tipo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("numero", new TableInfo.Column("numero", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("fecha", new TableInfo.Column("fecha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("clienteNombre", new TableInfo.Column("clienteNombre", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("clienteId", new TableInfo.Column("clienteId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("subtotal", new TableInfo.Column("subtotal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("impuesto", new TableInfo.Column("impuesto", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("total", new TableInfo.Column("total", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("estado", new TableInfo.Column("estado", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentos.put("metodoPago", new TableInfo.Column("metodoPago", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDocumentos = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDocumentos = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDocumentos = new TableInfo("documentos", _columnsDocumentos, _foreignKeysDocumentos, _indicesDocumentos);
        final TableInfo _existingDocumentos = TableInfo.read(db, "documentos");
        if (!_infoDocumentos.equals(_existingDocumentos)) {
          return new RoomOpenHelper.ValidationResult(false, "documentos(com.tiketefactura.data.local.Documento).\n"
                  + " Expected:\n" + _infoDocumentos + "\n"
                  + " Found:\n" + _existingDocumentos);
        }
        final HashMap<String, TableInfo.Column> _columnsDocumentoItems = new HashMap<String, TableInfo.Column>(6);
        _columnsDocumentoItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentoItems.put("documentoId", new TableInfo.Column("documentoId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentoItems.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentoItems.put("cantidad", new TableInfo.Column("cantidad", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentoItems.put("precio", new TableInfo.Column("precio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDocumentoItems.put("subtotal", new TableInfo.Column("subtotal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDocumentoItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysDocumentoItems.add(new TableInfo.ForeignKey("documentos", "CASCADE", "NO ACTION", Arrays.asList("documentoId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesDocumentoItems = new HashSet<TableInfo.Index>(1);
        _indicesDocumentoItems.add(new TableInfo.Index("index_documento_items_documentoId", false, Arrays.asList("documentoId"), Arrays.asList("ASC")));
        final TableInfo _infoDocumentoItems = new TableInfo("documento_items", _columnsDocumentoItems, _foreignKeysDocumentoItems, _indicesDocumentoItems);
        final TableInfo _existingDocumentoItems = TableInfo.read(db, "documento_items");
        if (!_infoDocumentoItems.equals(_existingDocumentoItems)) {
          return new RoomOpenHelper.ValidationResult(false, "documento_items(com.tiketefactura.data.local.DocumentoItem).\n"
                  + " Expected:\n" + _infoDocumentoItems + "\n"
                  + " Found:\n" + _existingDocumentoItems);
        }
        final HashMap<String, TableInfo.Column> _columnsCategorias = new HashMap<String, TableInfo.Column>(2);
        _columnsCategorias.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategorias.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategorias = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategorias = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategorias = new TableInfo("categorias", _columnsCategorias, _foreignKeysCategorias, _indicesCategorias);
        final TableInfo _existingCategorias = TableInfo.read(db, "categorias");
        if (!_infoCategorias.equals(_existingCategorias)) {
          return new RoomOpenHelper.ValidationResult(false, "categorias(com.tiketefactura.data.local.Categoria).\n"
                  + " Expected:\n" + _infoCategorias + "\n"
                  + " Found:\n" + _existingCategorias);
        }
        final HashMap<String, TableInfo.Column> _columnsClientes = new HashMap<String, TableInfo.Column>(5);
        _columnsClientes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("telefono", new TableInfo.Column("telefono", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("correo", new TableInfo.Column("correo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("direccion", new TableInfo.Column("direccion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysClientes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesClientes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoClientes = new TableInfo("clientes", _columnsClientes, _foreignKeysClientes, _indicesClientes);
        final TableInfo _existingClientes = TableInfo.read(db, "clientes");
        if (!_infoClientes.equals(_existingClientes)) {
          return new RoomOpenHelper.ValidationResult(false, "clientes(com.tiketefactura.data.local.Cliente).\n"
                  + " Expected:\n" + _infoClientes + "\n"
                  + " Found:\n" + _existingClientes);
        }
        final HashMap<String, TableInfo.Column> _columnsGastos = new HashMap<String, TableInfo.Column>(5);
        _columnsGastos.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGastos.put("descripcion", new TableInfo.Column("descripcion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGastos.put("monto", new TableInfo.Column("monto", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGastos.put("fecha", new TableInfo.Column("fecha", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGastos.put("categoria", new TableInfo.Column("categoria", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGastos = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGastos = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGastos = new TableInfo("gastos", _columnsGastos, _foreignKeysGastos, _indicesGastos);
        final TableInfo _existingGastos = TableInfo.read(db, "gastos");
        if (!_infoGastos.equals(_existingGastos)) {
          return new RoomOpenHelper.ValidationResult(false, "gastos(com.tiketefactura.data.local.Gasto).\n"
                  + " Expected:\n" + _infoGastos + "\n"
                  + " Found:\n" + _existingGastos);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7a59ef4ea44c4c367a1f22d20098fcaf", "ed0af017c2422d3d45dd90023a36f38b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "productos","empresa","documentos","documento_items","categorias","clientes","gastos");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `productos`");
      _db.execSQL("DELETE FROM `empresa`");
      _db.execSQL("DELETE FROM `documentos`");
      _db.execSQL("DELETE FROM `documento_items`");
      _db.execSQL("DELETE FROM `categorias`");
      _db.execSQL("DELETE FROM `clientes`");
      _db.execSQL("DELETE FROM `gastos`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProductoDao.class, ProductoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmpresaDao.class, EmpresaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DocumentoDao.class, DocumentoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoriaDao.class, CategoriaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ClienteDao.class, ClienteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GastoDao.class, GastoDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProductoDao productoDao() {
    if (_productoDao != null) {
      return _productoDao;
    } else {
      synchronized(this) {
        if(_productoDao == null) {
          _productoDao = new ProductoDao_Impl(this);
        }
        return _productoDao;
      }
    }
  }

  @Override
  public EmpresaDao empresaDao() {
    if (_empresaDao != null) {
      return _empresaDao;
    } else {
      synchronized(this) {
        if(_empresaDao == null) {
          _empresaDao = new EmpresaDao_Impl(this);
        }
        return _empresaDao;
      }
    }
  }

  @Override
  public DocumentoDao documentoDao() {
    if (_documentoDao != null) {
      return _documentoDao;
    } else {
      synchronized(this) {
        if(_documentoDao == null) {
          _documentoDao = new DocumentoDao_Impl(this);
        }
        return _documentoDao;
      }
    }
  }

  @Override
  public CategoriaDao categoriaDao() {
    if (_categoriaDao != null) {
      return _categoriaDao;
    } else {
      synchronized(this) {
        if(_categoriaDao == null) {
          _categoriaDao = new CategoriaDao_Impl(this);
        }
        return _categoriaDao;
      }
    }
  }

  @Override
  public ClienteDao clienteDao() {
    if (_clienteDao != null) {
      return _clienteDao;
    } else {
      synchronized(this) {
        if(_clienteDao == null) {
          _clienteDao = new ClienteDao_Impl(this);
        }
        return _clienteDao;
      }
    }
  }

  @Override
  public GastoDao gastoDao() {
    if (_gastoDao != null) {
      return _gastoDao;
    } else {
      synchronized(this) {
        if(_gastoDao == null) {
          _gastoDao = new GastoDao_Impl(this);
        }
        return _gastoDao;
      }
    }
  }
}
