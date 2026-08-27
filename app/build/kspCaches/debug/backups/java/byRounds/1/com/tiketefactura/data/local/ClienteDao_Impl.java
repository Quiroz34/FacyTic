package com.tiketefactura.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class ClienteDao_Impl implements ClienteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Cliente> __insertionAdapterOfCliente;

  private final EntityDeletionOrUpdateAdapter<Cliente> __deletionAdapterOfCliente;

  private final EntityDeletionOrUpdateAdapter<Cliente> __updateAdapterOfCliente;

  public ClienteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCliente = new EntityInsertionAdapter<Cliente>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `clientes` (`id`,`nombre`,`telefono`,`correo`,`direccion`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cliente entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        if (entity.getTelefono() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTelefono());
        }
        if (entity.getCorreo() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCorreo());
        }
        if (entity.getDireccion() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDireccion());
        }
      }
    };
    this.__deletionAdapterOfCliente = new EntityDeletionOrUpdateAdapter<Cliente>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `clientes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cliente entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCliente = new EntityDeletionOrUpdateAdapter<Cliente>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `clientes` SET `id` = ?,`nombre` = ?,`telefono` = ?,`correo` = ?,`direccion` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cliente entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        if (entity.getTelefono() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTelefono());
        }
        if (entity.getCorreo() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCorreo());
        }
        if (entity.getDireccion() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDireccion());
        }
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Cliente c, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCliente.insert(c);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Cliente c, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCliente.handle(c);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Cliente c, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCliente.handle(c);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Cliente>> getAll() {
    final String _sql = "SELECT * FROM clientes ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"clientes"}, new Callable<List<Cliente>>() {
      @Override
      @NonNull
      public List<Cliente> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfTelefono = CursorUtil.getColumnIndexOrThrow(_cursor, "telefono");
          final int _cursorIndexOfCorreo = CursorUtil.getColumnIndexOrThrow(_cursor, "correo");
          final int _cursorIndexOfDireccion = CursorUtil.getColumnIndexOrThrow(_cursor, "direccion");
          final List<Cliente> _result = new ArrayList<Cliente>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cliente _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpTelefono;
            if (_cursor.isNull(_cursorIndexOfTelefono)) {
              _tmpTelefono = null;
            } else {
              _tmpTelefono = _cursor.getString(_cursorIndexOfTelefono);
            }
            final String _tmpCorreo;
            if (_cursor.isNull(_cursorIndexOfCorreo)) {
              _tmpCorreo = null;
            } else {
              _tmpCorreo = _cursor.getString(_cursorIndexOfCorreo);
            }
            final String _tmpDireccion;
            if (_cursor.isNull(_cursorIndexOfDireccion)) {
              _tmpDireccion = null;
            } else {
              _tmpDireccion = _cursor.getString(_cursorIndexOfDireccion);
            }
            _item = new Cliente(_tmpId,_tmpNombre,_tmpTelefono,_tmpCorreo,_tmpDireccion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
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
}
