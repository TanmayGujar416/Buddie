package com.tanmay.buddie.di

import android.app.Application
import androidx.room.Room
import com.tanmay.buddie.data.local.AppDatabase
import com.tanmay.buddie.data.repository.SubjectRepositoryImpl
import com.tanmay.buddie.data.repository.TimetableRepositoryImpl
import com.tanmay.buddie.data.repository.SubmissionRepositoryImpl
import com.tanmay.buddie.data.repository.ExamRepositoryImpl
import com.tanmay.buddie.domain.repository.SubjectRepository
import com.tanmay.buddie.domain.repository.TimetableRepository
import com.tanmay.buddie.domain.repository.SubmissionRepository
import com.tanmay.buddie.domain.repository.ExamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS moodle_course_mappings (
                    moodleCourseId INTEGER NOT NULL PRIMARY KEY,
                    moodleCourseName TEXT NOT NULL,
                    subjectId TEXT,
                    FOREIGN KEY (subjectId) REFERENCES subjects(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_moodle_course_mappings_subjectId ON moodle_course_mappings(subjectId)")
        }
    }

    private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS moodle_course_content_cache (
                    courseId INTEGER NOT NULL PRIMARY KEY,
                    contentJson TEXT NOT NULL,
                    fetchedAtMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS filter_codes (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    subjectId TEXT NOT NULL,
                    code TEXT NOT NULL,
                    isMine INTEGER NOT NULL,
                    FOREIGN KEY (subjectId) REFERENCES subjects(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_filter_codes_subjectId ON filter_codes(subjectId)")

            db.execSQL("ALTER TABLE submissions ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'")
            db.execSQL("ALTER TABLE submissions ADD COLUMN moodleAssignmentId INTEGER")
            db.execSQL("ALTER TABLE submissions ADD COLUMN filteredOut INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE submissions ADD COLUMN moodleVanished INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_submissions_moodleAssignmentId ON submissions(moodleAssignmentId)")
        }
    }

    private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE submissions ADD COLUMN moodleCourseId INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_submissions_moodleCourseId ON submissions(moodleCourseId)")
        }
    }

    private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS token_signature_decisions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    subjectId TEXT NOT NULL,
                    signature TEXT NOT NULL,
                    decision TEXT NOT NULL,
                    FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_token_signature_decisions_subjectId_signature ON token_signature_decisions(subjectId, signature)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_token_signature_decisions_subjectId ON token_signature_decisions(subjectId)")
            db.execSQL("ALTER TABLE submissions ADD COLUMN needsReview INTEGER NOT NULL DEFAULT 0")
            db.execSQL("DROP TABLE IF EXISTS filter_codes")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "buddie_db"
        )
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        })
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideMoodleCourseContentCacheDao(db: AppDatabase): com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao {
        return db.moodleCourseContentCacheDao
    }

    @Provides
    @Singleton
    fun provideSubjectRepository(db: AppDatabase): SubjectRepository {
        return SubjectRepositoryImpl(db.subjectDao, db.timetableSlotDao)
    }

    @Provides
    @Singleton
    fun provideTimetableRepository(db: AppDatabase): TimetableRepository {
        return TimetableRepositoryImpl(db.timetableSlotDao)
    }

    @Provides
    @Singleton
    fun provideSubmissionRepository(db: AppDatabase): SubmissionRepository {
        return SubmissionRepositoryImpl(db.submissionDao)
    }

    @Provides
    @Singleton
    fun provideExamRepository(db: AppDatabase): ExamRepository {
        return ExamRepositoryImpl(db.examDao)
    }

    @Provides
    @Singleton
    fun provideMoodleCourseMappingRepository(db: AppDatabase): com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository {
        return com.tanmay.buddie.data.repository.MoodleCourseMappingRepositoryImpl(db.moodleCourseMappingDao)
    }

    @Provides
    @Singleton
    fun provideTokenSignatureDecisionDao(db: AppDatabase): com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao {
        return db.tokenSignatureDecisionDao
    }

    @Provides
    @Singleton
    fun provideTokenSignatureRepository(dao: com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao): com.tanmay.buddie.domain.repository.TokenSignatureRepository {
        return com.tanmay.buddie.data.repository.TokenSignatureRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideFilterCodeRepository(dao: com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao): com.tanmay.buddie.domain.repository.FilterCodeRepository {
        return com.tanmay.buddie.data.repository.FilterCodeRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}

