package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.domain.repository.TimetableRepository;
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
public final class AppModule_ProvideTimetableRepositoryFactory implements Factory<TimetableRepository> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideTimetableRepositoryFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TimetableRepository get() {
    return provideTimetableRepository(dbProvider.get());
  }

  public static AppModule_ProvideTimetableRepositoryFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideTimetableRepositoryFactory(dbProvider);
  }

  public static TimetableRepository provideTimetableRepository(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTimetableRepository(db));
  }
}
