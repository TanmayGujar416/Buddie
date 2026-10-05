package com.tanmay.buddie.data.repository;

import com.tanmay.buddie.data.local.dao.MoodleCourseMappingDao;
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
public final class MoodleCourseMappingRepositoryImpl_Factory implements Factory<MoodleCourseMappingRepositoryImpl> {
  private final Provider<MoodleCourseMappingDao> daoProvider;

  public MoodleCourseMappingRepositoryImpl_Factory(Provider<MoodleCourseMappingDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public MoodleCourseMappingRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static MoodleCourseMappingRepositoryImpl_Factory create(
      Provider<MoodleCourseMappingDao> daoProvider) {
    return new MoodleCourseMappingRepositoryImpl_Factory(daoProvider);
  }

  public static MoodleCourseMappingRepositoryImpl newInstance(MoodleCourseMappingDao dao) {
    return new MoodleCourseMappingRepositoryImpl(dao);
  }
}
