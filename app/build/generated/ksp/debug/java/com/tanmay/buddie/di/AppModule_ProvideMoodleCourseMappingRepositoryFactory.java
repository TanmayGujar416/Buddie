package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository;
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
public final class AppModule_ProvideMoodleCourseMappingRepositoryFactory implements Factory<MoodleCourseMappingRepository> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideMoodleCourseMappingRepositoryFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MoodleCourseMappingRepository get() {
    return provideMoodleCourseMappingRepository(dbProvider.get());
  }

  public static AppModule_ProvideMoodleCourseMappingRepositoryFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideMoodleCourseMappingRepositoryFactory(dbProvider);
  }

  public static MoodleCourseMappingRepository provideMoodleCourseMappingRepository(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMoodleCourseMappingRepository(db));
  }
}
