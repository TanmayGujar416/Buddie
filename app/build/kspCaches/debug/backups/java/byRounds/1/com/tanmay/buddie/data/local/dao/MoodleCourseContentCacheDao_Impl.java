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
import com.tanmay.buddie.data.local.entity.MoodleCourseContentCacheEntity;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MoodleCourseContentCacheDao_Impl implements MoodleCourseContentCacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MoodleCourseContentCacheEntity> __insertionAdapterOfMoodleCourseContentCacheEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByCourseId;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public MoodleCourseContentCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMoodleCourseContentCacheEntity = new EntityInsertionAdapter<MoodleCourseContentCacheEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `moodle_course_content_cache` (`courseId`,`contentJson`,`fetchedAtMillis`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoodleCourseContentCacheEntity entity) {
        statement.bindLong(1, entity.getCourseId());
        statement.bindString(2, entity.getContentJson());
        statement.bindLong(3, entity.getFetchedAtMillis());
      }
    };
    this.__preparedStmtOfDeleteByCourseId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM moodle_course_content_cache WHERE courseId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM moodle_course_content_cache";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final MoodleCourseContentCacheEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMoodleCourseContentCacheEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByCourseId(final long courseId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByCourseId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, courseId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByCourseId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getCachedContent(final long courseId,
      final Continuation<? super MoodleCourseContentCacheEntity> $completion) {
    final String _sql = "SELECT * FROM moodle_course_content_cache WHERE courseId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, courseId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MoodleCourseContentCacheEntity>() {
      @Override
      @Nullable
      public MoodleCourseContentCacheEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "courseId");
          final int _cursorIndexOfContentJson = CursorUtil.getColumnIndexOrThrow(_cursor, "contentJson");
          final int _cursorIndexOfFetchedAtMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAtMillis");
          final MoodleCourseContentCacheEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpCourseId;
            _tmpCourseId = _cursor.getLong(_cursorIndexOfCourseId);
            final String _tmpContentJson;
            _tmpContentJson = _cursor.getString(_cursorIndexOfContentJson);
            final long _tmpFetchedAtMillis;
            _tmpFetchedAtMillis = _cursor.getLong(_cursorIndexOfFetchedAtMillis);
            _result = new MoodleCourseContentCacheEntity(_tmpCourseId,_tmpContentJson,_tmpFetchedAtMillis);
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
