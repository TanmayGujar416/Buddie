package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class FilterCodeRepositoryImpl_Factory implements Factory<FilterCodeRepositoryImpl> {
  private final Provider<TokenSignatureDecisionDao> daoProvider;

  public FilterCodeRepositoryImpl_Factory(Provider<TokenSignatureDecisionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public FilterCodeRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static FilterCodeRepositoryImpl_Factory create(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    return new FilterCodeRepositoryImpl_Factory(daoProvider);
  }

  public static FilterCodeRepositoryImpl newInstance(TokenSignatureDecisionDao dao) {
    return new FilterCodeRepositoryImpl(dao);
  }
}
