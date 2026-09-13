package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.SubjectDao;
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
public final class SubjectRepositoryImpl_Factory implements Factory<SubjectRepositoryImpl> {
  private final Provider<SubjectDao> daoProvider;

  private final Provider<TimetableSlotDao> slotDaoProvider;

  public SubjectRepositoryImpl_Factory(Provider<SubjectDao> daoProvider,
      Provider<TimetableSlotDao> slotDaoProvider) {
    this.daoProvider = daoProvider;
    this.slotDaoProvider = slotDaoProvider;
  }

  @Override
  public SubjectRepositoryImpl get() {
    return newInstance(daoProvider.get(), slotDaoProvider.get());
  }

  public static SubjectRepositoryImpl_Factory create(Provider<SubjectDao> daoProvider,
      Provider<TimetableSlotDao> slotDaoProvider) {
    return new SubjectRepositoryImpl_Factory(daoProvider, slotDaoProvider);
  }

  public static SubjectRepositoryImpl newInstance(SubjectDao dao, TimetableSlotDao slotDao) {
    return new SubjectRepositoryImpl(dao, slotDao);
  }
}
