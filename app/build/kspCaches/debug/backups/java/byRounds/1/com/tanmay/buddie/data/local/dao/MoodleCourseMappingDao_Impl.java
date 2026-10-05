package com.tanmay.buddie.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanmay.buddie.data.local.entity.MoodleCourseMappingEntity;
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
public final class MoodleCourseMappingDao_Impl implements MoodleCourseMappingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MoodleCourseMappingEntity> __insertionAdapterOfMoodleCourseMappingEntity;

  private final EntityDeletionOrUpdateAdapter<MoodleCourseMappingEntity> __deletionAdapterOfMoodleCourseMappingEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMappingByCourseId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllMappings;

  public MoodleCourseMappingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMoodleCourseMappingEntity = new EntityInsertionAdapter<MoodleCourseMappingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `moodle_course_mappings` (`moodleCourseId`,`moodleCourseName`,`subjectId`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoodleCourseMappingEntity entity) {
        statement.bindLong(1, entity.getMoodleCourseId());
        statement.bindString(2, entity.getMoodleCourseName());
        if (entity.getSubjectId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSubjectId());
        }
      }
    };
    this.__deletionAdapterOfMoodleCourseMappingEntity = new EntityDeletionOrUpdateAdapter<MoodleCourseMappingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `moodle_course_mappings` WHERE `moodleCourseId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MoodleCourseMappingEntity entity) {
        statement.bindLong(1, entity.getMoodleCourseId());
      }
    };
    this.__preparedStmtOfDeleteMappingByCourseId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM moodle_course_mappings WHERE moodleCourseId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllMappings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM moodle_course_mappings";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateMapping(final MoodleCourseMappingEntity mapping,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMoodleCourseMappingEntity.insert(mapping);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdateMappings(final List<MoodleCourseMappingEntity> mappings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMoodleCourseMappingEntity.insert(mappings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMapping(final MoodleCourseMappingEntity mapping,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMoodleCourseMappingEntity.handle(mapping);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMappingByCourseId(final long courseId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMappingByCourseId.acquire();
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
          __preparedStmtOfDeleteMappingByCourseId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllMappings(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllMappings.acquire();
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
          __preparedStmtOfDeleteAllMappings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MoodleCourseMappingEntity>> getAllMappings() {
    final String _sql = "SELECT * FROM moodle_course_mappings";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"moodle_course_mappings"}, new Callable<List<MoodleCourseMappingEntity>>() {
      @Override
      @NonNull
      public List<MoodleCourseMappingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfMoodleCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseName");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final List<MoodleCourseMappingEntity> _result = new ArrayList<MoodleCourseMappingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoodleCourseMappingEntity _item;
            final long _tmpMoodleCourseId;
            _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            final String _tmpMoodleCourseName;
            _tmpMoodleCourseName = _cursor.getString(_cursorIndexOfMoodleCourseName);
            final String _tmpSubjectId;
            if (_cursor.isNull(_cursorIndexOfSubjectId)) {
              _tmpSubjectId = null;
            } else {
              _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            }
            _item = new MoodleCourseMappingEntity(_tmpMoodleCourseId,_tmpMoodleCourseName,_tmpSubjectId);
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
  public Object getMappingByCourseId(final long courseId,
      final Continuation<? super MoodleCourseMappingEntity> $completion) {
    final String _sql = "SELECT * FROM moodle_course_mappings WHERE moodleCourseId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, courseId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MoodleCourseMappingEntity>() {
      @Override
      @Nullable
      public MoodleCourseMappingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfMoodleCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseName");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final MoodleCourseMappingEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpMoodleCourseId;
            _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            final String _tmpMoodleCourseName;
            _tmpMoodleCourseName = _cursor.getString(_cursorIndexOfMoodleCourseName);
            final String _tmpSubjectId;
            if (_cursor.isNull(_cursorIndexOfSubjectId)) {
              _tmpSubjectId = null;
            } else {
              _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            }
            _result = new MoodleCourseMappingEntity(_tmpMoodleCourseId,_tmpMoodleCourseName,_tmpSubjectId);
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

  @Override
  public Object getMappingsBySubjectId(final String subjectId,
      final Continuation<? super List<MoodleCourseMappingEntity>> $completion) {
    final String _sql = "SELECT * FROM moodle_course_mappings WHERE subjectId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, subjectId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MoodleCourseMappingEntity>>() {
      @Override
      @NonNull
      public List<MoodleCourseMappingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfMoodleCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseName");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final List<MoodleCourseMappingEntity> _result = new ArrayList<MoodleCourseMappingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MoodleCourseMappingEntity _item;
            final long _tmpMoodleCourseId;
            _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            final String _tmpMoodleCourseName;
            _tmpMoodleCourseName = _cursor.getString(_cursorIndexOfMoodleCourseName);
            final String _tmpSubjectId;
            if (_cursor.isNull(_cursorIndexOfSubjectId)) {
              _tmpSubjectId = null;
            } else {
              _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            }
            _item = new MoodleCourseMappingEntity(_tmpMoodleCourseId,_tmpMoodleCourseName,_tmpSubjectId);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
