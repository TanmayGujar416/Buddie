package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.TimetableSlotDao;
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
public final class TimetableRepositoryImpl_Factory implements Factory<TimetableRepositoryImpl> {
  private final Provider<TimetableSlotDao> daoProvider;

  public TimetableRepositoryImpl_Factory(Provider<TimetableSlotDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public TimetableRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static TimetableRepositoryImpl_Factory create(Provider<TimetableSlotDao> daoProvider) {
    return new TimetableRepositoryImpl_Factory(daoProvider);
  }

  public static TimetableRepositoryImpl newInstance(TimetableSlotDao dao) {
    return new TimetableRepositoryImpl(dao);
  }
}
