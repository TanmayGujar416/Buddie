package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao;
import com.tanmay.buddie.domain.repository.TokenSignatureRepository;
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
public final class AppModule_ProvideTokenSignatureRepositoryFactory implements Factory<TokenSignatureRepository> {
  private final Provider<TokenSignatureDecisionDao> daoProvider;

  public AppModule_ProvideTokenSignatureRepositoryFactory(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TokenSignatureRepository get() {
    return provideTokenSignatureRepository(daoProvider.get());
  }

  public static AppModule_ProvideTokenSignatureRepositoryFactory create(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    return new AppModule_ProvideTokenSignatureRepositoryFactory(daoProvider);
  }

  public static TokenSignatureRepository provideTokenSignatureRepository(
      TokenSignatureDecisionDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTokenSignatureRepository(dao));
  }
}
