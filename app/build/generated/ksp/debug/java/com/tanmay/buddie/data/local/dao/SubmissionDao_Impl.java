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
import com.tanmay.buddie.data.local.Converters;
import com.tanmay.buddie.data.local.entity.SessionType;
import com.tanmay.buddie.data.local.entity.SubmissionEntity;
import com.tanmay.buddie.data.local.entity.SubmissionSource;
import com.tanmay.buddie.data.local.entity.SubmissionStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
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
public final class SubmissionDao_Impl implements SubmissionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SubmissionEntity> __insertionAdapterOfSubmissionEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<SubmissionEntity> __deletionAdapterOfSubmissionEntity;

  private final EntityDeletionOrUpdateAdapter<SubmissionEntity> __updateAdapterOfSubmissionEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMoodleSubmission;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFilteredOut;

  private final SharedSQLiteStatement __preparedStmtOfMarkMoodleVanished;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMoodleSubmissionsByCourseId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMoodleSubmissionsBySubjectId;

  public SubmissionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSubmissionEntity = new EntityInsertionAdapter<SubmissionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `submissions` (`id`,`subjectId`,`sessionType`,`title`,`deadline`,`status`,`roomOverride`,`source`,`moodleAssignmentId`,`moodleCourseId`,`filteredOut`,`needsReview`,`moodleVanished`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SubmissionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        final String _tmp = __converters.fromSessionType(entity.getSessionType());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindString(4, entity.getTitle());
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getDeadline());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __converters.fromSubmissionStatus(entity.getStatus());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        if (entity.getRoomOverride() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRoomOverride());
        }
        final String _tmp_3 = __converters.fromSubmissionSource(entity.getSource());
        if (_tmp_3 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_3);
        }
        if (entity.getMoodleAssignmentId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getMoodleAssignmentId());
        }
        if (entity.getMoodleCourseId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getMoodleCourseId());
        }
        final int _tmp_4 = entity.getFilteredOut() ? 1 : 0;
        statement.bindLong(11, _tmp_4);
        final int _tmp_5 = entity.getNeedsReview() ? 1 : 0;
        statement.bindLong(12, _tmp_5);
        final int _tmp_6 = entity.getMoodleVanished() ? 1 : 0;
        statement.bindLong(13, _tmp_6);
      }
    };
    this.__deletionAdapterOfSubmissionEntity = new EntityDeletionOrUpdateAdapter<SubmissionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `submissions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SubmissionEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfSubmissionEntity = new EntityDeletionOrUpdateAdapter<SubmissionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `submissions` SET `id` = ?,`subjectId` = ?,`sessionType` = ?,`title` = ?,`deadline` = ?,`status` = ?,`roomOverride` = ?,`source` = ?,`moodleAssignmentId` = ?,`moodleCourseId` = ?,`filteredOut` = ?,`needsReview` = ?,`moodleVanished` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SubmissionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSubjectId());
        final String _tmp = __converters.fromSessionType(entity.getSessionType());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        statement.bindString(4, entity.getTitle());
        final String _tmp_1 = __converters.fromLocalDateTime(entity.getDeadline());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __converters.fromSubmissionStatus(entity.getStatus());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        if (entity.getRoomOverride() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRoomOverride());
        }
        final String _tmp_3 = __converters.fromSubmissionSource(entity.getSource());
        if (_tmp_3 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_3);
        }
        if (entity.getMoodleAssignmentId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getMoodleAssignmentId());
        }
        if (entity.getMoodleCourseId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getMoodleCourseId());
        }
        final int _tmp_4 = entity.getFilteredOut() ? 1 : 0;
        statement.bindLong(11, _tmp_4);
        final int _tmp_5 = entity.getNeedsReview() ? 1 : 0;
        statement.bindLong(12, _tmp_5);
        final int _tmp_6 = entity.getMoodleVanished() ? 1 : 0;
        statement.bindLong(13, _tmp_6);
        statement.bindString(14, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateMoodleSubmission = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE submissions SET title = ?, deadline = ?, moodleCourseId = ?, filteredOut = ?, needsReview = ?, moodleVanished = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateFilteredOut = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE submissions SET filteredOut = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkMoodleVanished = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE submissions SET moodleVanished = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMoodleSubmissionsByCourseId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM submissions WHERE source = 'MOODLE' AND moodleCourseId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMoodleSubmissionsBySubjectId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM submissions WHERE source = 'MOODLE' AND subjectId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSubmission(final SubmissionEntity submission,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSubmissionEntity.insert(submission);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSubmissions(final List<SubmissionEntity> submissions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSubmissionEntity.insert(submissions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSubmission(final SubmissionEntity submission,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSubmissionEntity.handle(submission);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSubmission(final SubmissionEntity submission,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSubmissionEntity.handle(submission);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMoodleSubmission(final String id, final String title,
      final LocalDateTime deadline, final long courseId, final boolean filteredOut,
      final boolean needsReview, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMoodleSubmission.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, title);
        _argIndex = 2;
        final String _tmp = __converters.fromLocalDateTime(deadline);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, courseId);
        _argIndex = 4;
        final int _tmp_1 = filteredOut ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 5;
        final int _tmp_2 = needsReview ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 6;
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
          __preparedStmtOfUpdateMoodleSubmission.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFilteredOut(final String id, final boolean filteredOut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFilteredOut.acquire();
        int _argIndex = 1;
        final int _tmp = filteredOut ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
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
          __preparedStmtOfUpdateFilteredOut.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markMoodleVanished(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkMoodleVanished.acquire();
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
          __preparedStmtOfMarkMoodleVanished.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMoodleSubmissionsByCourseId(final long courseId,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMoodleSubmissionsByCourseId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, courseId);
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
          __preparedStmtOfDeleteMoodleSubmissionsByCourseId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMoodleSubmissionsBySubjectId(final String subjectId,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMoodleSubmissionsBySubjectId.acquire();
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
          __preparedStmtOfDeleteMoodleSubmissionsBySubjectId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SubmissionEntity>> getAllSubmissions() {
    final String _sql = "SELECT * FROM submissions ORDER BY deadline ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"submissions"}, new Callable<List<SubmissionEntity>>() {
      @Override
      @NonNull
      public List<SubmissionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfMoodleAssignmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleAssignmentId");
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfFilteredOut = CursorUtil.getColumnIndexOrThrow(_cursor, "filteredOut");
          final int _cursorIndexOfNeedsReview = CursorUtil.getColumnIndexOrThrow(_cursor, "needsReview");
          final int _cursorIndexOfMoodleVanished = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleVanished");
          final List<SubmissionEntity> _result = new ArrayList<SubmissionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SubmissionEntity _item;
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
            _tmpSessionType = __converters.toSessionType(_tmp);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDeadline;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDeadline);
            }
            final LocalDateTime _tmp_2 = __converters.toLocalDateTime(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDeadline = _tmp_2;
            }
            final SubmissionStatus _tmpStatus;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            }
            final SubmissionStatus _tmp_4 = __converters.toSubmissionStatus(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_4;
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            final SubmissionSource _tmpSource;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfSource);
            }
            final SubmissionSource _tmp_6 = __converters.toSubmissionSource(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionSource', but it was NULL.");
            } else {
              _tmpSource = _tmp_6;
            }
            final Long _tmpMoodleAssignmentId;
            if (_cursor.isNull(_cursorIndexOfMoodleAssignmentId)) {
              _tmpMoodleAssignmentId = null;
            } else {
              _tmpMoodleAssignmentId = _cursor.getLong(_cursorIndexOfMoodleAssignmentId);
            }
            final Long _tmpMoodleCourseId;
            if (_cursor.isNull(_cursorIndexOfMoodleCourseId)) {
              _tmpMoodleCourseId = null;
            } else {
              _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            }
            final boolean _tmpFilteredOut;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfFilteredOut);
            _tmpFilteredOut = _tmp_7 != 0;
            final boolean _tmpNeedsReview;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfNeedsReview);
            _tmpNeedsReview = _tmp_8 != 0;
            final boolean _tmpMoodleVanished;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMoodleVanished);
            _tmpMoodleVanished = _tmp_9 != 0;
            _item = new SubmissionEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpTitle,_tmpDeadline,_tmpStatus,_tmpRoomOverride,_tmpSource,_tmpMoodleAssignmentId,_tmpMoodleCourseId,_tmpFilteredOut,_tmpNeedsReview,_tmpMoodleVanished);
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
  public Object getSubmissionById(final String id,
      final Continuation<? super SubmissionEntity> $completion) {
    final String _sql = "SELECT * FROM submissions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SubmissionEntity>() {
      @Override
      @Nullable
      public SubmissionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfMoodleAssignmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleAssignmentId");
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfFilteredOut = CursorUtil.getColumnIndexOrThrow(_cursor, "filteredOut");
          final int _cursorIndexOfNeedsReview = CursorUtil.getColumnIndexOrThrow(_cursor, "needsReview");
          final int _cursorIndexOfMoodleVanished = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleVanished");
          final SubmissionEntity _result;
          if (_cursor.moveToFirst()) {
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
            _tmpSessionType = __converters.toSessionType(_tmp);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDeadline;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDeadline);
            }
            final LocalDateTime _tmp_2 = __converters.toLocalDateTime(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDeadline = _tmp_2;
            }
            final SubmissionStatus _tmpStatus;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            }
            final SubmissionStatus _tmp_4 = __converters.toSubmissionStatus(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_4;
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            final SubmissionSource _tmpSource;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfSource);
            }
            final SubmissionSource _tmp_6 = __converters.toSubmissionSource(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionSource', but it was NULL.");
            } else {
              _tmpSource = _tmp_6;
            }
            final Long _tmpMoodleAssignmentId;
            if (_cursor.isNull(_cursorIndexOfMoodleAssignmentId)) {
              _tmpMoodleAssignmentId = null;
            } else {
              _tmpMoodleAssignmentId = _cursor.getLong(_cursorIndexOfMoodleAssignmentId);
            }
            final Long _tmpMoodleCourseId;
            if (_cursor.isNull(_cursorIndexOfMoodleCourseId)) {
              _tmpMoodleCourseId = null;
            } else {
              _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            }
            final boolean _tmpFilteredOut;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfFilteredOut);
            _tmpFilteredOut = _tmp_7 != 0;
            final boolean _tmpNeedsReview;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfNeedsReview);
            _tmpNeedsReview = _tmp_8 != 0;
            final boolean _tmpMoodleVanished;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMoodleVanished);
            _tmpMoodleVanished = _tmp_9 != 0;
            _result = new SubmissionEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpTitle,_tmpDeadline,_tmpStatus,_tmpRoomOverride,_tmpSource,_tmpMoodleAssignmentId,_tmpMoodleCourseId,_tmpFilteredOut,_tmpNeedsReview,_tmpMoodleVanished);
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
  public Object getSubmissionByMoodleId(final long cmid,
      final Continuation<? super SubmissionEntity> $completion) {
    final String _sql = "SELECT * FROM submissions WHERE moodleAssignmentId = ? AND source = 'MOODLE' LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cmid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SubmissionEntity>() {
      @Override
      @Nullable
      public SubmissionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfMoodleAssignmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleAssignmentId");
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfFilteredOut = CursorUtil.getColumnIndexOrThrow(_cursor, "filteredOut");
          final int _cursorIndexOfNeedsReview = CursorUtil.getColumnIndexOrThrow(_cursor, "needsReview");
          final int _cursorIndexOfMoodleVanished = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleVanished");
          final SubmissionEntity _result;
          if (_cursor.moveToFirst()) {
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
            _tmpSessionType = __converters.toSessionType(_tmp);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDeadline;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDeadline);
            }
            final LocalDateTime _tmp_2 = __converters.toLocalDateTime(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDeadline = _tmp_2;
            }
            final SubmissionStatus _tmpStatus;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            }
            final SubmissionStatus _tmp_4 = __converters.toSubmissionStatus(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_4;
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            final SubmissionSource _tmpSource;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfSource);
            }
            final SubmissionSource _tmp_6 = __converters.toSubmissionSource(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionSource', but it was NULL.");
            } else {
              _tmpSource = _tmp_6;
            }
            final Long _tmpMoodleAssignmentId;
            if (_cursor.isNull(_cursorIndexOfMoodleAssignmentId)) {
              _tmpMoodleAssignmentId = null;
            } else {
              _tmpMoodleAssignmentId = _cursor.getLong(_cursorIndexOfMoodleAssignmentId);
            }
            final Long _tmpMoodleCourseId;
            if (_cursor.isNull(_cursorIndexOfMoodleCourseId)) {
              _tmpMoodleCourseId = null;
            } else {
              _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            }
            final boolean _tmpFilteredOut;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfFilteredOut);
            _tmpFilteredOut = _tmp_7 != 0;
            final boolean _tmpNeedsReview;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfNeedsReview);
            _tmpNeedsReview = _tmp_8 != 0;
            final boolean _tmpMoodleVanished;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMoodleVanished);
            _tmpMoodleVanished = _tmp_9 != 0;
            _result = new SubmissionEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpTitle,_tmpDeadline,_tmpStatus,_tmpRoomOverride,_tmpSource,_tmpMoodleAssignmentId,_tmpMoodleCourseId,_tmpFilteredOut,_tmpNeedsReview,_tmpMoodleVanished);
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
  public Object getAllMoodleSubmissions(
      final Continuation<? super List<SubmissionEntity>> $completion) {
    final String _sql = "SELECT * FROM submissions WHERE source = 'MOODLE'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SubmissionEntity>>() {
      @Override
      @NonNull
      public List<SubmissionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSessionType = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRoomOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roomOverride");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfMoodleAssignmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleAssignmentId");
          final int _cursorIndexOfMoodleCourseId = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleCourseId");
          final int _cursorIndexOfFilteredOut = CursorUtil.getColumnIndexOrThrow(_cursor, "filteredOut");
          final int _cursorIndexOfNeedsReview = CursorUtil.getColumnIndexOrThrow(_cursor, "needsReview");
          final int _cursorIndexOfMoodleVanished = CursorUtil.getColumnIndexOrThrow(_cursor, "moodleVanished");
          final List<SubmissionEntity> _result = new ArrayList<SubmissionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SubmissionEntity _item;
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
            _tmpSessionType = __converters.toSessionType(_tmp);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final LocalDateTime _tmpDeadline;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDeadline);
            }
            final LocalDateTime _tmp_2 = __converters.toLocalDateTime(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpDeadline = _tmp_2;
            }
            final SubmissionStatus _tmpStatus;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            }
            final SubmissionStatus _tmp_4 = __converters.toSubmissionStatus(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_4;
            }
            final String _tmpRoomOverride;
            if (_cursor.isNull(_cursorIndexOfRoomOverride)) {
              _tmpRoomOverride = null;
            } else {
              _tmpRoomOverride = _cursor.getString(_cursorIndexOfRoomOverride);
            }
            final SubmissionSource _tmpSource;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfSource);
            }
            final SubmissionSource _tmp_6 = __converters.toSubmissionSource(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.tanmay.buddie.data.local.entity.SubmissionSource', but it was NULL.");
            } else {
              _tmpSource = _tmp_6;
            }
            final Long _tmpMoodleAssignmentId;
            if (_cursor.isNull(_cursorIndexOfMoodleAssignmentId)) {
              _tmpMoodleAssignmentId = null;
            } else {
              _tmpMoodleAssignmentId = _cursor.getLong(_cursorIndexOfMoodleAssignmentId);
            }
            final Long _tmpMoodleCourseId;
            if (_cursor.isNull(_cursorIndexOfMoodleCourseId)) {
              _tmpMoodleCourseId = null;
            } else {
              _tmpMoodleCourseId = _cursor.getLong(_cursorIndexOfMoodleCourseId);
            }
            final boolean _tmpFilteredOut;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfFilteredOut);
            _tmpFilteredOut = _tmp_7 != 0;
            final boolean _tmpNeedsReview;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfNeedsReview);
            _tmpNeedsReview = _tmp_8 != 0;
            final boolean _tmpMoodleVanished;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfMoodleVanished);
            _tmpMoodleVanished = _tmp_9 != 0;
            _item = new SubmissionEntity(_tmpId,_tmpSubjectId,_tmpSessionType,_tmpTitle,_tmpDeadline,_tmpStatus,_tmpRoomOverride,_tmpSource,_tmpMoodleAssignmentId,_tmpMoodleCourseId,_tmpFilteredOut,_tmpNeedsReview,_tmpMoodleVanished);
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
