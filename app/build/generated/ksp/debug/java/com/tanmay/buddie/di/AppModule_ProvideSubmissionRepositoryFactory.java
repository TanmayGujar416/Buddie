package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.domain.repository.SubmissionRepository;
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
public final class AppModule_ProvideSubmissionRepositoryFactory implements Factory<SubmissionRepository> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideSubmissionRepositoryFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SubmissionRepository get() {
    return provideSubmissionRepository(dbProvider.get());
  }

  public static AppModule_ProvideSubmissionRepositoryFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSubmissionRepositoryFactory(dbProvider);
  }

  public static SubmissionRepository provideSubmissionRepository(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSubmissionRepository(db));
  }
}
