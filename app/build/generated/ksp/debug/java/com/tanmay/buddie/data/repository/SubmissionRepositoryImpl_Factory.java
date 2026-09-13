package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.SubmissionDao;
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
public final class SubmissionRepositoryImpl_Factory implements Factory<SubmissionRepositoryImpl> {
  private final Provider<SubmissionDao> daoProvider;

  public SubmissionRepositoryImpl_Factory(Provider<SubmissionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public SubmissionRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static SubmissionRepositoryImpl_Factory create(Provider<SubmissionDao> daoProvider) {
    return new SubmissionRepositoryImpl_Factory(daoProvider);
  }

  public static SubmissionRepositoryImpl newInstance(SubmissionDao dao) {
    return new SubmissionRepositoryImpl(dao);
  }
}
