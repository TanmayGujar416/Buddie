package com.tanmay.buddie.presentation.screens.moodle;

import androidx.lifecycle.SavedStateHandle;
import com.tanmay.buddie.domain.repository.FilterCodeRepository;
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine;
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
public final class FilterCodeViewModel_Factory implements Factory<FilterCodeViewModel> {
  private final Provider<FilterCodeRepository> filterCodeRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider;

  private final Provider<MoodleContentSyncEngine> syncEngineProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public FilterCodeViewModel_Factory(Provider<FilterCodeRepository> filterCodeRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<MoodleContentSyncEngine> syncEngineProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.filterCodeRepositoryProvider = filterCodeRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.courseMappingRepositoryProvider = courseMappingRepositoryProvider;
    this.syncEngineProvider = syncEngineProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public FilterCodeViewModel get() {
    return newInstance(filterCodeRepositoryProvider.get(), subjectRepositoryProvider.get(), courseMappingRepositoryProvider.get(), syncEngineProvider.get(), savedStateHandleProvider.get());
  }

  public static FilterCodeViewModel_Factory create(
      Provider<FilterCodeRepository> filterCodeRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<MoodleContentSyncEngine> syncEngineProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new FilterCodeViewModel_Factory(filterCodeRepositoryProvider, subjectRepositoryProvider, courseMappingRepositoryProvider, syncEngineProvider, savedStateHandleProvider);
  }

  public static FilterCodeViewModel newInstance(FilterCodeRepository filterCodeRepository,
      SubjectRepository subjectRepository, MoodleCourseMappingRepository courseMappingRepository,
      MoodleContentSyncEngine syncEngine, SavedStateHandle savedStateHandle) {
    return new FilterCodeViewModel(filterCodeRepository, subjectRepository, courseMappingRepository, syncEngine, savedStateHandle);
  }
}
