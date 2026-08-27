package com.tiketefactura.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DocumentoDao_Impl implements DocumentoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Documento> __insertionAdapterOfDocumento;

  private final EntityInsertionAdapter<DocumentoItem> __insertionAdapterOfDocumentoItem;

  public DocumentoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDocumento = new EntityInsertionAdapter<Documento>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `documentos` (`id`,`tipo`,`numero`,`fecha`,`clienteNombre`,`clienteId`,`subtotal`,`impuesto`,`total`,`estado`,`metodoPago`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Documento entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTipo());
        statement.bindString(3, entity.getNumero());
        statement.bindString(4, entity.getFecha());
        if (entity.getClienteNombre() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getClienteNombre());
        }
        if (entity.getClienteId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getClienteId());
        }
        statement.bindDouble(7, entity.getSubtotal());
        statement.bindDouble(8, entity.getImpuesto());
        statement.bindDouble(9, entity.getTotal());
        statement.bindString(10, entity.getEstado());
        statement.bindString(11, entity.getMetodoPago());
      }
    };
    this.__insertionAdapterOfDocumentoItem = new EntityInsertionAdapter<DocumentoItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `documento_items` (`id`,`documentoId`,`nombre`,`cantidad`,`precio`,`subtotal`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DocumentoItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDocumentoId());
        statement.bindString(3, entity.getNombre());
        statement.bindDouble(4, entity.getCantidad());
        statement.bindDouble(5, entity.getPrecio());
        statement.bindDouble(6, entity.getSubtotal());
      }
    };
  }

  @Override
  public Object insertDocumento(final Documento d, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDocumento.insertAndReturnId(d);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItems(final List<DocumentoItem> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDocumentoItem.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object saveFullDocumento(final Documento d, final List<DocumentoItem> items,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DocumentoDao.DefaultImpls.saveFullDocumento(DocumentoDao_Impl.this, d, items, __cont), $completion);
  }

  @Override
  public Flow<List<DocumentoConItems>> getAllWithItems() {
    final String _sql = "SELECT * FROM documentos ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"documento_items",
        "documentos"}, new Callable<List<DocumentoConItems>>() {
      @Override
      @NonNull
      public List<DocumentoConItems> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
            final int _cursorIndexOfNumero = CursorUtil.getColumnIndexOrThrow(_cursor, "numero");
            final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
            final int _cursorIndexOfClienteNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteNombre");
            final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
            final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
            final int _cursorIndexOfImpuesto = CursorUtil.getColumnIndexOrThrow(_cursor, "impuesto");
            final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
            final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
            final int _cursorIndexOfMetodoPago = CursorUtil.getColumnIndexOrThrow(_cursor, "metodoPago");
            final LongSparseArray<ArrayList<DocumentoItem>> _collectionItems = new LongSparseArray<ArrayList<DocumentoItem>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionItems.containsKey(_tmpKey)) {
                _collectionItems.put(_tmpKey, new ArrayList<DocumentoItem>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipdocumentoItemsAscomTiketefacturaDataLocalDocumentoItem(_collectionItems);
            final List<DocumentoConItems> _result = new ArrayList<DocumentoConItems>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final DocumentoConItems _item;
              final Documento _tmpDocumento;
              final int _tmpId;
              _tmpId = _cursor.getInt(_cursorIndexOfId);
              final String _tmpTipo;
              _tmpTipo = _cursor.getString(_cursorIndexOfTipo);
              final String _tmpNumero;
              _tmpNumero = _cursor.getString(_cursorIndexOfNumero);
              final String _tmpFecha;
              _tmpFecha = _cursor.getString(_cursorIndexOfFecha);
              final String _tmpClienteNombre;
              if (_cursor.isNull(_cursorIndexOfClienteNombre)) {
                _tmpClienteNombre = null;
              } else {
                _tmpClienteNombre = _cursor.getString(_cursorIndexOfClienteNombre);
              }
              final Integer _tmpClienteId;
              if (_cursor.isNull(_cursorIndexOfClienteId)) {
                _tmpClienteId = null;
              } else {
                _tmpClienteId = _cursor.getInt(_cursorIndexOfClienteId);
              }
              final double _tmpSubtotal;
              _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
              final double _tmpImpuesto;
              _tmpImpuesto = _cursor.getDouble(_cursorIndexOfImpuesto);
              final double _tmpTotal;
              _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal);
              final String _tmpEstado;
              _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
              final String _tmpMetodoPago;
              _tmpMetodoPago = _cursor.getString(_cursorIndexOfMetodoPago);
              _tmpDocumento = new Documento(_tmpId,_tmpTipo,_tmpNumero,_tmpFecha,_tmpClienteNombre,_tmpClienteId,_tmpSubtotal,_tmpImpuesto,_tmpTotal,_tmpEstado,_tmpMetodoPago);
              final ArrayList<DocumentoItem> _tmpItemsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpItemsCollection = _collectionItems.get(_tmpKey_1);
              _item = new DocumentoConItems(_tmpDocumento,_tmpItemsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipdocumentoItemsAscomTiketefacturaDataLocalDocumentoItem(
      @NonNull final LongSparseArray<ArrayList<DocumentoItem>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipdocumentoItemsAscomTiketefacturaDataLocalDocumentoItem(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`documentoId`,`nombre`,`cantidad`,`precio`,`subtotal` FROM `documento_items` WHERE `documentoId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "documentoId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfDocumentoId = 1;
      final int _cursorIndexOfNombre = 2;
      final int _cursorIndexOfCantidad = 3;
      final int _cursorIndexOfPrecio = 4;
      final int _cursorIndexOfSubtotal = 5;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<DocumentoItem> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final DocumentoItem _item_1;
          final int _tmpId;
          _tmpId = _cursor.getInt(_cursorIndexOfId);
          final int _tmpDocumentoId;
          _tmpDocumentoId = _cursor.getInt(_cursorIndexOfDocumentoId);
          final String _tmpNombre;
          _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
          final double _tmpCantidad;
          _tmpCantidad = _cursor.getDouble(_cursorIndexOfCantidad);
          final double _tmpPrecio;
          _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
          final double _tmpSubtotal;
          _tmpSubtotal = _cursor.getDouble(_cursorIndexOfSubtotal);
          _item_1 = new DocumentoItem(_tmpId,_tmpDocumentoId,_tmpNombre,_tmpCantidad,_tmpPrecio,_tmpSubtotal);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
