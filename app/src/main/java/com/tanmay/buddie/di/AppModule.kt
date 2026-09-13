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
        .fallbackToDestructiveMigration()
        .build()
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
}
