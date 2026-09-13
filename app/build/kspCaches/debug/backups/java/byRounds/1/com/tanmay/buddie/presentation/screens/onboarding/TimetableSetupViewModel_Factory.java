package com.tanmay.buddie.presentation.screens.onboarding;

import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.TimetableRepository;
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
public final class TimetableSetupViewModel_Factory implements Factory<TimetableSetupViewModel> {
  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<TimetableRepository> timetableRepositoryProvider;

  public TimetableSetupViewModel_Factory(Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider) {
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
  }

  @Override
  public TimetableSetupViewModel get() {
    return newInstance(subjectRepositoryProvider.get(), timetableRepositoryProvider.get());
  }

  public static TimetableSetupViewModel_Factory create(
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider) {
    return new TimetableSetupViewModel_Factory(subjectRepositoryProvider, timetableRepositoryProvider);
  }

  public static TimetableSetupViewModel newInstance(SubjectRepository subjectRepository,
      TimetableRepository timetableRepository) {
    return new TimetableSetupViewModel(subjectRepository, timetableRepository);
  }
}
