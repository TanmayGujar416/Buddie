package com.tanmay.buddie.data.local;

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
import com.tanmay.buddie.data.local.dao.ExamDao;
import com.tanmay.buddie.data.local.dao.ExamDao_Impl;
import com.tanmay.buddie.data.local.dao.SubjectDao;
import com.tanmay.buddie.data.local.dao.SubjectDao_Impl;
import com.tanmay.buddie.data.local.dao.SubmissionDao;
import com.tanmay.buddie.data.local.dao.SubmissionDao_Impl;
import com.tanmay.buddie.data.local.dao.TimetableSlotDao;
import com.tanmay.buddie.data.local.dao.TimetableSlotDao_Impl;
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
  private volatile SubjectDao _subjectDao;

  private volatile TimetableSlotDao _timetableSlotDao;

  private volatile SubmissionDao _submissionDao;

  private volatile ExamDao _examDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `color` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `timetable_slots` (`id` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `sessionType` TEXT NOT NULL, `room` TEXT NOT NULL, `dayOfWeek` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_timetable_slots_subjectId` ON `timetable_slots` (`subjectId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `submissions` (`id` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `sessionType` TEXT, `title` TEXT NOT NULL, `deadline` TEXT NOT NULL, `status` TEXT NOT NULL, `roomOverride` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_submissions_subjectId` ON `submissions` (`subjectId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `exams` (`id` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `title` TEXT NOT NULL, `datetime` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `hall` TEXT NOT NULL, `seatNumber` TEXT, `syllabusScope` TEXT, `allowedMaterials` TEXT, `roomOverride` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exams_subjectId` ON `exams` (`subjectId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '759eb20aca543dbafc5f495f7af32fcb')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `subjects`");
        db.execSQL("DROP TABLE IF EXISTS `timetable_slots`");
        db.execSQL("DROP TABLE IF EXISTS `submissions`");
        db.execSQL("DROP TABLE IF EXISTS `exams`");
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
        final HashMap<String, TableInfo.Column> _columnsSubjects = new HashMap<String, TableInfo.Column>(3);
        _columnsSubjects.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("color", new TableInfo.Column("color", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSubjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSubjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSubjects = new TableInfo("subjects", _columnsSubjects, _foreignKeysSubjects, _indicesSubjects);
        final TableInfo _existingSubjects = TableInfo.read(db, "subjects");
        if (!_infoSubjects.equals(_existingSubjects)) {
          return new RoomOpenHelper.ValidationResult(false, "subjects(com.tanmay.buddie.data.local.entity.SubjectEntity).\n"
                  + " Expected:\n" + _infoSubjects + "\n"
                  + " Found:\n" + _existingSubjects);
        }
        final HashMap<String, TableInfo.Column> _columnsTimetableSlots = new HashMap<String, TableInfo.Column>(7);
        _columnsTimetableSlots.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("subjectId", new TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("sessionType", new TableInfo.Column("sessionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("room", new TableInfo.Column("room", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("dayOfWeek", new TableInfo.Column("dayOfWeek", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimetableSlots.put("endTime", new TableInfo.Column("endTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTimetableSlots = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTimetableSlots.add(new TableInfo.ForeignKey("subjects", "CASCADE", "NO ACTION", Arrays.asList("subjectId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTimetableSlots = new HashSet<TableInfo.Index>(1);
        _indicesTimetableSlots.add(new TableInfo.Index("index_timetable_slots_subjectId", false, Arrays.asList("subjectId"), Arrays.asList("ASC")));
        final TableInfo _infoTimetableSlots = new TableInfo("timetable_slots", _columnsTimetableSlots, _foreignKeysTimetableSlots, _indicesTimetableSlots);
        final TableInfo _existingTimetableSlots = TableInfo.read(db, "timetable_slots");
        if (!_infoTimetableSlots.equals(_existingTimetableSlots)) {
          return new RoomOpenHelper.ValidationResult(false, "timetable_slots(com.tanmay.buddie.data.local.entity.TimetableSlotEntity).\n"
                  + " Expected:\n" + _infoTimetableSlots + "\n"
                  + " Found:\n" + _existingTimetableSlots);
        }
        final HashMap<String, TableInfo.Column> _columnsSubmissions = new HashMap<String, TableInfo.Column>(7);
        _columnsSubmissions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("subjectId", new TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("sessionType", new TableInfo.Column("sessionType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("deadline", new TableInfo.Column("deadline", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubmissions.put("roomOverride", new TableInfo.Column("roomOverride", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSubmissions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSubmissions.add(new TableInfo.ForeignKey("subjects", "CASCADE", "NO ACTION", Arrays.asList("subjectId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSubmissions = new HashSet<TableInfo.Index>(1);
        _indicesSubmissions.add(new TableInfo.Index("index_submissions_subjectId", false, Arrays.asList("subjectId"), Arrays.asList("ASC")));
        final TableInfo _infoSubmissions = new TableInfo("submissions", _columnsSubmissions, _foreignKeysSubmissions, _indicesSubmissions);
        final TableInfo _existingSubmissions = TableInfo.read(db, "submissions");
        if (!_infoSubmissions.equals(_existingSubmissions)) {
          return new RoomOpenHelper.ValidationResult(false, "submissions(com.tanmay.buddie.data.local.entity.SubmissionEntity).\n"
                  + " Expected:\n" + _infoSubmissions + "\n"
                  + " Found:\n" + _existingSubmissions);
        }
        final HashMap<String, TableInfo.Column> _columnsExams = new HashMap<String, TableInfo.Column>(10);
        _columnsExams.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("subjectId", new TableInfo.Column("subjectId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("datetime", new TableInfo.Column("datetime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("durationMinutes", new TableInfo.Column("durationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("hall", new TableInfo.Column("hall", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("seatNumber", new TableInfo.Column("seatNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("syllabusScope", new TableInfo.Column("syllabusScope", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("allowedMaterials", new TableInfo.Column("allowedMaterials", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExams.put("roomOverride", new TableInfo.Column("roomOverride", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExams = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExams.add(new TableInfo.ForeignKey("subjects", "CASCADE", "NO ACTION", Arrays.asList("subjectId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExams = new HashSet<TableInfo.Index>(1);
        _indicesExams.add(new TableInfo.Index("index_exams_subjectId", false, Arrays.asList("subjectId"), Arrays.asList("ASC")));
        final TableInfo _infoExams = new TableInfo("exams", _columnsExams, _foreignKeysExams, _indicesExams);
        final TableInfo _existingExams = TableInfo.read(db, "exams");
        if (!_infoExams.equals(_existingExams)) {
          return new RoomOpenHelper.ValidationResult(false, "exams(com.tanmay.buddie.data.local.entity.ExamEntity).\n"
                  + " Expected:\n" + _infoExams + "\n"
                  + " Found:\n" + _existingExams);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "759eb20aca543dbafc5f495f7af32fcb", "19dfb667c2d7a6a7d81ee74e6d555935");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "subjects","timetable_slots","submissions","exams");
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
      _db.execSQL("DELETE FROM `subjects`");
      _db.execSQL("DELETE FROM `timetable_slots`");
      _db.execSQL("DELETE FROM `submissions`");
      _db.execSQL("DELETE FROM `exams`");
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
    _typeConvertersMap.put(SubjectDao.class, SubjectDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TimetableSlotDao.class, TimetableSlotDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SubmissionDao.class, SubmissionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExamDao.class, ExamDao_Impl.getRequiredConverters());
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
  public SubjectDao getSubjectDao() {
    if (_subjectDao != null) {
      return _subjectDao;
    } else {
      synchronized(this) {
        if(_subjectDao == null) {
          _subjectDao = new SubjectDao_Impl(this);
        }
        return _subjectDao;
      }
    }
  }

  @Override
  public TimetableSlotDao getTimetableSlotDao() {
    if (_timetableSlotDao != null) {
      return _timetableSlotDao;
    } else {
      synchronized(this) {
        if(_timetableSlotDao == null) {
          _timetableSlotDao = new TimetableSlotDao_Impl(this);
        }
        return _timetableSlotDao;
      }
    }
  }

  @Override
  public SubmissionDao getSubmissionDao() {
    if (_submissionDao != null) {
      return _submissionDao;
    } else {
      synchronized(this) {
        if(_submissionDao == null) {
          _submissionDao = new SubmissionDao_Impl(this);
        }
        return _submissionDao;
      }
    }
  }

  @Override
  public ExamDao getExamDao() {
    if (_examDao != null) {
      return _examDao;
    } else {
      synchronized(this) {
        if(_examDao == null) {
          _examDao = new ExamDao_Impl(this);
        }
        return _examDao;
      }
    }
  }
}
