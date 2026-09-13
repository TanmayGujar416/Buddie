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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanmay.buddie.data.local.Converters;
import com.tanmay.buddie.data.local.entity.ExamEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.LocalDateTime;
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
public final class ExamDao_Impl implements ExamDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExamEntity> __insertionAdapterOfExamEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ExamEntity> __deletionAdapterOfExamEntity;

  private final EntityDeletionOrUpdateAdapter<ExamEntity> __updateAdapterOfExamEntity;

  public ExamDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExamEntity = new EntityInsertionAdapter<ExamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `exams` (`id`,`subjectId`,`title`,`datetime`,`durationMinutes`,`hall`,`seatNumber`,`syllabusScope`,`allowedMaterials`,`roomOverride`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExamEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        statement.bindString(3, entity.getTitle());
        final String _tmp = __converters.fromLocalDateTime(entity.getDatetime());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        statement.bindLong(5, entity.getDurationMinutes());
        statement.bindString(6, entity.getHall());
        if (entity.getSeatNumber() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSeatNumber());
        }
        if (entity.getSyllabusScope() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSyllabusScope());
        }
        if (entity.getAllowedMaterials() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAllowedMaterials());
        }
        if (entity.getRoomOverride() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRoomOverride());
        }
      }
    };
    this.__deletionAdapterOfExamEntity = new EntityDeletionOrUpdateAdapter<ExamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `exams` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExamEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfExamEntity = new EntityDeletionOrUpdateAdapter<ExamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `exams` SET `id` = ?,`subjectId` = ?,`title` = ?,`datetime` = ?,`durationMinutes` = ?,`hall` = ?,`seatNumber` = ?,`syllabusScope` = ?,`allowedMaterials` = ?,`roomOverride` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExamEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        statement.bindString(3, entity.getTitle());
        final String _tmp = __converters.fromLocalDateTime(entity.getDatetime());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        statement.bindLong(5, entity.getDurationMinutes());
        statement.bindString(6, entity.getHall());
        if (entity.getSeatNumber() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSeatNumber());
        }
        if (entity.getSyllabusScope() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSyllabusScope());
        }
        if (entity.getAllowedMaterials() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAllowedMaterials());
        }
        if (entity.getRoomOverride() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getRoomOverride());
        }
        statement.bindString(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertExam(final ExamEntity exam, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExamEntity.insert(exam);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExam(final ExamEntity exam, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExamEntity.handle(exam);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExam(final ExamEntity exam, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExamEntity.handle(exam);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExamEntity>> getAllExams() {
    final String _sql = "SELECT * FROM exams ORDER BY datetime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exams"}, new Callable<List<ExamEntity>>() {
      @Override
      @NonNull
      public List<ExamEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDatetime = CursorUtil.getColumnIndexOrThrow(_cursor, "datetime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfHall = CursorUtil.getColumnIndexOrThrow(_cursor, "hall");
          final int _cursorIndexOfSeatNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seatNumber");
          final int _cursorIndexOfSyllabusScope = CursorUtil.getColumnIndexOrThrow(_cursor, "syllabusScope");
          final int _cursorIndexOfAllowedMaterials = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedMaterials");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final List<ExamEntity> _result = new ArrayList<ExamEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExamEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDatetime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDatetime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDatetime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDatetime = _tmp_1;
            }
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final String _tmpHall;
            _tmpHall = _cursor.getString(_cursorIndexOfHall);
            final String _tmpSeatNumber;
            if (_cursor.isNull(_cursorIndexOfSeatNumber)) {
              _tmpSeatNumber = null;
            } else {
              _tmpSeatNumber = _cursor.getString(_cursorIndexOfSeatNumber);
            }
            final String _tmpSyllabusScope;
            if (_cursor.isNull(_cursorIndexOfSyllabusScope)) {
              _tmpSyllabusScope = null;
            } else {
              _tmpSyllabusScope = _cursor.getString(_cursorIndexOfSyllabusScope);
            }
            final String _tmpAllowedMaterials;
            if (_cursor.isNull(_cursorIndexOfAllowedMaterials)) {
              _tmpAllowedMaterials = null;
            } else {
              _tmpAllowedMaterials = _cursor.getString(_cursorIndexOfAllowedMaterials);
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            _item = new ExamEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpDatetime,_tmpDurationMinutes,_tmpHall,_tmpSeatNumber,_tmpSyllabusScope,_tmpAllowedMaterials,_tmpRoomOverride);
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
  public Object getExamById(final String id, final Continuation<? super ExamEntity> $completion) {
    final String _sql = "SELECT * FROM exams WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExamEntity>() {
      @Override
      @Nullable
      public ExamEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDatetime = CursorUtil.getColumnIndexOrThrow(_cursor, "datetime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfHall = CursorUtil.getColumnIndexOrThrow(_cursor, "hall");
          final int _cursorIndexOfSeatNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seatNumber");
          final int _cursorIndexOfSyllabusScope = CursorUtil.getColumnIndexOrThrow(_cursor, "syllabusScope");
          final int _cursorIndexOfAllowedMaterials = CursorUtil.getColumnIndexOrThrow(_cursor, "allowedMaterials");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final ExamEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSubjectId;
            _tmpSubjectId = _cursor.getString(_cursorIndexOfSubjectId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDatetime;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDatetime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDatetime);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDatetime = _tmp_1;
            }
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final String _tmpHall;
            _tmpHall = _cursor.getString(_cursorIndexOfHall);
            final String _tmpSeatNumber;
            if (_cursor.isNull(_cursorIndexOfSeatNumber)) {
              _tmpSeatNumber = null;
            } else {
              _tmpSeatNumber = _cursor.getString(_cursorIndexOfSeatNumber);
            }
            final String _tmpSyllabusScope;
            if (_cursor.isNull(_cursorIndexOfSyllabusScope)) {
              _tmpSyllabusScope = null;
            } else {
              _tmpSyllabusScope = _cursor.getString(_cursorIndexOfSyllabusScope);
            }
            final String _tmpAllowedMaterials;
            if (_cursor.isNull(_cursorIndexOfAllowedMaterials)) {
              _tmpAllowedMaterials = null;
            } else {
              _tmpAllowedMaterials = _cursor.getString(_cursorIndexOfAllowedMaterials);
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            _result = new ExamEntity(_tmpId,_tmpSubjectId,_tmpTitle,_tmpDatetime,_tmpDurationMinutes,_tmpHall,_tmpSeatNumber,_tmpSyllabusScope,_tmpAllowedMaterials,_tmpRoomOverride);
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
