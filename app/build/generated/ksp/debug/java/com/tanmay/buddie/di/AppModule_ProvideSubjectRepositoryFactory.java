package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.domain.repository.SubjectRepository;
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
public final class AppModule_ProvideSubjectRepositoryFactory implements Factory<SubjectRepository> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideSubjectRepositoryFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SubjectRepository get() {
    return provideSubjectRepository(dbProvider.get());
  }

  public static AppModule_ProvideSubjectRepositoryFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSubjectRepositoryFactory(dbProvider);
  }

  public static SubjectRepository provideSubjectRepository(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSubjectRepository(db));
  }
}
