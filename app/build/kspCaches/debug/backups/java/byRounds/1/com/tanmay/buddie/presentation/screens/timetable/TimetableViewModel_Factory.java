package com.tanmay.buddie.presentation.screens.timetable;

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
public final class TimetableViewModel_Factory implements Factory<TimetableViewModel> {
  private final Provider<TimetableRepository> timetableRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  public TimetableViewModel_Factory(Provider<TimetableRepository> timetableRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider) {
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
  }

  @Override
  public TimetableViewModel get() {
    return newInstance(timetableRepositoryProvider.get(), subjectRepositoryProvider.get());
  }

  public static TimetableViewModel_Factory create(
      Provider<TimetableRepository> timetableRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider) {
    return new TimetableViewModel_Factory(timetableRepositoryProvider, subjectRepositoryProvider);
  }

  public static TimetableViewModel newInstance(TimetableRepository timetableRepository,
      SubjectRepository subjectRepository) {
    return new TimetableViewModel(timetableRepository, subjectRepository);
  }
}
