package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.ExamDao;
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
public final class ExamRepositoryImpl_Factory implements Factory<ExamRepositoryImpl> {
  private final Provider<ExamDao> daoProvider;

  public ExamRepositoryImpl_Factory(Provider<ExamDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ExamRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ExamRepositoryImpl_Factory create(Provider<ExamDao> daoProvider) {
    return new ExamRepositoryImpl_Factory(daoProvider);
  }

  public static ExamRepositoryImpl newInstance(ExamDao dao) {
    return new ExamRepositoryImpl(dao);
  }
}
