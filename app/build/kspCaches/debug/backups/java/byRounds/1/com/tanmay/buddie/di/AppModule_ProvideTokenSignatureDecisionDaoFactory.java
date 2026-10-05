package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.AppDatabase;
import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao;
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
public final class AppModule_ProvideTokenSignatureDecisionDaoFactory implements Factory<TokenSignatureDecisionDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideTokenSignatureDecisionDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TokenSignatureDecisionDao get() {
    return provideTokenSignatureDecisionDao(dbProvider.get());
  }

  public static AppModule_ProvideTokenSignatureDecisionDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideTokenSignatureDecisionDaoFactory(dbProvider);
  }

  public static TokenSignatureDecisionDao provideTokenSignatureDecisionDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTokenSignatureDecisionDao(db));
  }
}
