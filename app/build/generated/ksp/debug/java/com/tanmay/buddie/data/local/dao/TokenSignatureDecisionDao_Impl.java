package com.tanmay.buddie.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanmay.buddie.data.local.Converters;
import com.tanmay.buddie.data.local.entity.SignatureDecision;
import com.tanmay.buddie.data.local.entity.TokenSignatureDecision;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TokenSignatureDecisionDao_Impl implements TokenSignatureDecisionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TokenSignatureDecision> __insertionAdapterOfTokenSignatureDecision;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteDecision;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDecisionById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForSubject;

  public TokenSignatureDecisionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTokenSignatureDecision = new EntityInsertionAdapter<TokenSignatureDecision>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `token_signature_decisions` (`id`,`subjectId`,`signature`,`decision`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TokenSignatureDecision entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        statement.bindString(3, entity.getSignature());
        final String _tmp = __converters.fromSignatureDecision(entity.getDecision());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
      }
    };
    this.__preparedStmtOfDeleteDecision = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM token_signature_decisions WHERE subjectId = ? AND signature = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDecisionById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM token_signature_decisions WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllForSubject = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM token_signature_decisions WHERE subjectId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object saveDecision(final TokenSignatureDecision decision,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTokenSignatureDecision.insertAndReturnId(decision);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDecision(final String subjectId, final String signature,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDecision.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, subjectId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, signature);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDecision.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDecisionById(final long id, final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDecisionById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDecisionById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllForSubject(final String subjectId,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForSubject.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, subjectId);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllForSubject.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TokenSignatureDecision>> getDecisionsForSubject(final String subjectId) {
    final String _sql = "SELECT * FROM token_signature_decisions WHERE subjectId = ? ORDER BY signature ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, subjectId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"token_signature_decisions"}, new Callable<List<TokenSignatureDecision>>() {
      @Override
      @NonNull
      public List<TokenSignatureDecision> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final List<TokenSignatureDecision> _result = new ArrayList<TokenSignatureDecision>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TokenSignatureDecision _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpSignature;
            _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            final SignatureDecision _tmpDecision;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDecision)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDecision);
            }
            final SignatureDecision _tmp_1 = __converters.toSignatureDecision(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SignatureDecision', but it was NULL.");
            } else {
              _tmpDecision = _tmp_1;
            }
            _item = new TokenSignatureDecision(_tmpId,_tmpSubjectId,_tmpSignature,_tmpDecision);
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

  @Override
  public Flow<List<TokenSignatureDecision>> getAllDecisions() {
    final String _sql = "SELECT * FROM token_signature_decisions ORDER BY subjectId ASC, signature ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"token_signature_decisions"}, new Callable<List<TokenSignatureDecision>>() {
      @Override
      @NonNull
      public List<TokenSignatureDecision> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final List<TokenSignatureDecision> _result = new ArrayList<TokenSignatureDecision>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TokenSignatureDecision _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpSignature;
            _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            final SignatureDecision _tmpDecision;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDecision)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDecision);
            }
            final SignatureDecision _tmp_1 = __converters.toSignatureDecision(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SignatureDecision', but it was NULL.");
            } else {
              _tmpDecision = _tmp_1;
            }
            _item = new TokenSignatureDecision(_tmpId,_tmpSubjectId,_tmpSignature,_tmpDecision);
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

  @Override
  public Object getAllDecisionsSync(
      final Continuation<? super List<TokenSignatureDecision>> $completion) {
    final String _sql = "SELECT * FROM token_signature_decisions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TokenSignatureDecision>>() {
      @Override
      @NonNull
      public List<TokenSignatureDecision> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final List<TokenSignatureDecision> _result = new ArrayList<TokenSignatureDecision>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TokenSignatureDecision _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpSignature;
            _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            final SignatureDecision _tmpDecision;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDecision)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDecision);
            }
            final SignatureDecision _tmp_1 = __converters.toSignatureDecision(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SignatureDecision', but it was NULL.");
            } else {
              _tmpDecision = _tmp_1;
            }
            _item = new TokenSignatureDecision(_tmpId,_tmpSubjectId,_tmpSignature,_tmpDecision);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDecision(final String subjectId, final String signature,
      final Continuation<? super TokenSignatureDecision> $completion) {
    final String _sql = "SELECT * FROM token_signature_decisions WHERE subjectId = ? AND signature = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, subjectId);
    _argIndex = 2;
    _statement.bindString(_argIndex, signature);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TokenSignatureDecision>() {
      @Override
      @Nullable
      public TokenSignatureDecision call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final TokenSignatureDecision _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpSignature;
            _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            final SignatureDecision _tmpDecision;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDecision)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDecision);
            }
            final SignatureDecision _tmp_1 = __converters.toSignatureDecision(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SignatureDecision', but it was NULL.");
            } else {
              _tmpDecision = _tmp_1;
            }
            _result = new TokenSignatureDecision(_tmpId,_tmpSubjectId,_tmpSignature,_tmpDecision);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
