package com.tiketefactura.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EmpresaDao_Impl implements EmpresaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Empresa> __insertionAdapterOfEmpresa;

  public EmpresaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmpresa = new EntityInsertionAdapter<Empresa>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `empresa` (`id`,`nombre`,`ruc`,`direccion`,`telefono`,`eslogan`,`mensajePie`,`logoUri`,`impuestoActivo`,`impuestoPorcentaje`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Empresa entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        if (entity.getRuc() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRuc());
        }
        if (entity.getDireccion() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDireccion());
        }
        if (entity.getTelefono() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTelefono());
        }
        if (entity.getEslogan() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEslogan());
        }
        if (entity.getMensajePie() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getMensajePie());
        }
        if (entity.getLogoUri() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getLogoUri());
        }
        final int _tmp = entity.getImpuestoActivo() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindDouble(10, entity.getImpuestoPorcentaje());
      }
    };
  }

  @Override
  public Object insert(final Empresa e, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEmpresa.insert(e);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Empresa> get() {
    final String _sql = "SELECT * FROM empresa WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"empresa"}, new Callable<Empresa>() {
      @Override
      @Nullable
      public Empresa call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfRuc = CursorUtil.getColumnIndexOrThrow(_cursor, "ruc");
          final int _cursorIndexOfDireccion = CursorUtil.getColumnIndexOrThrow(_cursor, "direccion");
          final int _cursorIndexOfTelefono = CursorUtil.getColumnIndexOrThrow(_cursor, "telefono");
          final int _cursorIndexOfEslogan = CursorUtil.getColumnIndexOrThrow(_cursor, "eslogan");
          final int _cursorIndexOfMensajePie = CursorUtil.getColumnIndexOrThrow(_cursor, "mensajePie");
          final int _cursorIndexOfLogoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUri");
          final int _cursorIndexOfImpuestoActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "impuestoActivo");
          final int _cursorIndexOfImpuestoPorcentaje = CursorUtil.getColumnIndexOrThrow(_cursor, "impuestoPorcentaje");
          final Empresa _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpRuc;
            if (_cursor.isNull(_cursorIndexOfRuc)) {
              _tmpRuc = null;
            } else {
              _tmpRuc = _cursor.getString(_cursorIndexOfRuc);
            }
            final String _tmpDireccion;
            if (_cursor.isNull(_cursorIndexOfDireccion)) {
              _tmpDireccion = null;
            } else {
              _tmpDireccion = _cursor.getString(_cursorIndexOfDireccion);
            }
            final String _tmpTelefono;
            if (_cursor.isNull(_cursorIndexOfTelefono)) {
              _tmpTelefono = null;
            } else {
              _tmpTelefono = _cursor.getString(_cursorIndexOfTelefono);
            }
            final String _tmpEslogan;
            if (_cursor.isNull(_cursorIndexOfEslogan)) {
              _tmpEslogan = null;
            } else {
              _tmpEslogan = _cursor.getString(_cursorIndexOfEslogan);
            }
            final String _tmpMensajePie;
            if (_cursor.isNull(_cursorIndexOfMensajePie)) {
              _tmpMensajePie = null;
            } else {
              _tmpMensajePie = _cursor.getString(_cursorIndexOfMensajePie);
            }
            final String _tmpLogoUri;
            if (_cursor.isNull(_cursorIndexOfLogoUri)) {
              _tmpLogoUri = null;
            } else {
              _tmpLogoUri = _cursor.getString(_cursorIndexOfLogoUri);
            }
            final boolean _tmpImpuestoActivo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfImpuestoActivo);
            _tmpImpuestoActivo = _tmp != 0;
            final double _tmpImpuestoPorcentaje;
            _tmpImpuestoPorcentaje = _cursor.getDouble(_cursorIndexOfImpuestoPorcentaje);
            _result = new Empresa(_tmpId,_tmpNombre,_tmpRuc,_tmpDireccion,_tmpTelefono,_tmpEslogan,_tmpMensajePie,_tmpLogoUri,_tmpImpuestoActivo,_tmpImpuestoPorcentaje);
          } else {
            _result = null;
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
