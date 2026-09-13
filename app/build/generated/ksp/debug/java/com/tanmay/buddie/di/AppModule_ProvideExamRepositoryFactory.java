package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.domain.repository.ExamRepository;
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
public final class AppModule_ProvideExamRepositoryFactory implements Factory<ExamRepository> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideExamRepositoryFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ExamRepository get() {
    return provideExamRepository(dbProvider.get());
  }

  public static AppModule_ProvideExamRepositoryFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideExamRepositoryFactory(dbProvider);
  }

  public static ExamRepository provideExamRepository(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideExamRepository(db));
  }
}
