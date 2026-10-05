package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideMoodleCourseContentCacheDaoFactory implements Factory<MoodleCourseContentCacheDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideMoodleCourseContentCacheDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MoodleCourseContentCacheDao get() {
    return provideMoodleCourseContentCacheDao(dbProvider.get());
  }

  public static AppModule_ProvideMoodleCourseContentCacheDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideMoodleCourseContentCacheDaoFactory(dbProvider);
  }

  public static MoodleCourseContentCacheDao provideMoodleCourseContentCacheDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMoodleCourseContentCacheDao(db));
  }
}
