package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class TokenSignatureRepositoryImpl_Factory implements Factory<TokenSignatureRepositoryImpl> {
  private final Provider<TokenSignatureDecisionDao> daoProvider;

  public TokenSignatureRepositoryImpl_Factory(Provider<TokenSignatureDecisionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TokenSignatureRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static TokenSignatureRepositoryImpl_Factory create(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    return new TokenSignatureRepositoryImpl_Factory(daoProvider);
  }

  public static TokenSignatureRepositoryImpl newInstance(TokenSignatureDecisionDao dao) {
    return new TokenSignatureRepositoryImpl(dao);
  }
}
