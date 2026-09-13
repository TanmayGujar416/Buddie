package com.tanmay.buddie.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanmay.buddie.data.local.Converters;
import com.tanmay.buddie.data.local.entity.SessionType;
import com.tanmay.buddie.data.local.entity.TimetableSlotEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.DayOfWeek;
import java.time.LocalTime;
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
public final class TimetableSlotDao_Impl implements TimetableSlotDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimetableSlotEntity> __insertionAdapterOfTimetableSlotEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<TimetableSlotEntity> __deletionAdapterOfTimetableSlotEntity;

  private final EntityDeletionOrUpdateAdapter<TimetableSlotEntity> __updateAdapterOfTimetableSlotEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSlotById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSlotsForSubject;

  public TimetableSlotDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimetableSlotEntity = new EntityInsertionAdapter<TimetableSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `timetable_slots` (`id`,`subjectId`,`sessionType`,`room`,`dayOfWeek`,`startTime`,`endTime`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimetableSlotEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        final String _tmp = __converters.fromSessionType(entity.getSessionType());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindString(4, entity.getRoom());
        final String _tmp_1 = __converters.fromDayOfWeek(entity.getDayOfWeek());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __converters.fromLocalTime(entity.getStartTime());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        final String _tmp_3 = __converters.fromLocalTime(entity.getEndTime());
        if (_tmp_3 == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp_3);
        }
      }
    };
    this.__deletionAdapterOfTimetableSlotEntity = new EntityDeletionOrUpdateAdapter<TimetableSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `timetable_slots` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimetableSlotEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfTimetableSlotEntity = new EntityDeletionOrUpdateAdapter<TimetableSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `timetable_slots` SET `id` = ?,`subjectId` = ?,`sessionType` = ?,`room` = ?,`dayOfWeek` = ?,`startTime` = ?,`endTime` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimetableSlotEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        final String _tmp = __converters.fromSessionType(entity.getSessionType());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindString(4, entity.getRoom());
        final String _tmp_1 = __converters.fromDayOfWeek(entity.getDayOfWeek());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __converters.fromLocalTime(entity.getStartTime());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        final String _tmp_3 = __converters.fromLocalTime(entity.getEndTime());
        if (_tmp_3 == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp_3);
        }
        statement.bindString(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSlotById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM timetable_slots WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSlotsForSubject = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM timetable_slots WHERE subjectId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSlot(final TimetableSlotEntity slot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTimetableSlotEntity.insert(slot);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSlot(final TimetableSlotEntity slot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTimetableSlotEntity.handle(slot);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSlot(final TimetableSlotEntity slot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTimetableSlotEntity.handle(slot);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSlotById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSlotById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteSlotById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSlotsForSubject(final String subjectId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSlotsForSubject.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, subjectId);
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
          __preparedStmtOfDeleteSlotsForSubject.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TimetableSlotEntity>> getAllSlots() {
    final String _sql = "SELECT * FROM timetable_slots ORDER BY dayOfWeek, startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"timetable_slots"}, new Callable<List<TimetableSlotEntity>>() {
      @Override
      @NonNull
      public List<TimetableSlotEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<TimetableSlotEntity> _result = new ArrayList<TimetableSlotEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimetableSlotEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final SessionType _tmpSessionType;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSessionType)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSessionType);
            }
            final SessionType _tmp_1 = __converters.toSessionType(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SessionType', but it was NULL.");
            } else {
              _tmpSessionType = _tmp_1;
            }
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final DayOfWeek _tmpDayOfWeek;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfDayOfWeek);
            }
            final DayOfWeek _tmp_3 = __converters.toDayOfWeek(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.DayOfWeek', but it was NULL.");
            } else {
              _tmpDayOfWeek = _tmp_3;
            }
            final LocalTime _tmpStartTime;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalTime _tmp_5 = __converters.toLocalTime(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_5;
            }
            final LocalTime _tmpEndTime;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfEndTime);
            }
            final LocalTime _tmp_7 = __converters.toLocalTime(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalTime', but it was NULL.");
            } else {
              _tmpEndTime = _tmp_7;
            }
            _item = new TimetableSlotEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpRoom,_tmpDayOfWeek,_tmpStartTime,_tmpEndTime);
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
  public Flow<List<TimetableSlotEntity>> getSlotsForDay(final DayOfWeek day) {
    final String _sql = "SELECT * FROM timetable_slots WHERE dayOfWeek = ? ORDER BY startTime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromDayOfWeek(day);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"timetable_slots"}, new Callable<List<TimetableSlotEntity>>() {
      @Override
      @NonNull
      public List<TimetableSlotEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfRoom = CursorUtil.getColumnIndexOrThrow(_cursor, "room");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<TimetableSlotEntity> _result = new ArrayList<TimetableSlotEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimetableSlotEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final SessionType _tmpSessionType;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfSessionType)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfSessionType);
            }
            final SessionType _tmp_2 = __converters.toSessionType(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SessionType', but it was NULL.");
            } else {
              _tmpSessionType = _tmp_2;
            }
            final String _tmpRoom;
            _tmpRoom = _cursor.getString(_cursorIndexOfRoom);
            final DayOfWeek _tmpDayOfWeek;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfDayOfWeek);
            }
            final DayOfWeek _tmp_4 = __converters.toDayOfWeek(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.DayOfWeek', but it was NULL.");
            } else {
              _tmpDayOfWeek = _tmp_4;
            }
            final LocalTime _tmpStartTime;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfStartTime);
            }
            final LocalTime _tmp_6 = __converters.toLocalTime(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalTime', but it was NULL.");
            } else {
              _tmpStartTime = _tmp_6;
            }
            final LocalTime _tmpEndTime;
            final String _tmp_7;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getString(_cursorIndexOfEndTime);
            }
            final LocalTime _tmp_8 = __converters.toLocalTime(_tmp_7);
            if (_tmp_8 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalTime', but it was NULL.");
            } else {
              _tmpEndTime = _tmp_8;
            }
            _item = new TimetableSlotEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpRoom,_tmpDayOfWeek,_tmpStartTime,_tmpEndTime);
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
