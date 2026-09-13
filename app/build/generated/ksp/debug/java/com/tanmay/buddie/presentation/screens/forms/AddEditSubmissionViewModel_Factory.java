package com.tanmay.buddie.presentation.screens.forms;

import androidx.lifecycle.SavedStateHandle;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.SubmissionRepository;
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
public final class AddEditSubmissionViewModel_Factory implements Factory<AddEditSubmissionViewModel> {
  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditSubmissionViewModel_Factory(
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditSubmissionViewModel get() {
    return newInstance(submissionRepositoryProvider.get(), subjectRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditSubmissionViewModel_Factory create(
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditSubmissionViewModel_Factory(submissionRepositoryProvider, subjectRepositoryProvider, savedStateHandleProvider);
  }

  public static AddEditSubmissionViewModel newInstance(SubmissionRepository submissionRepository,
      SubjectRepository subjectRepository, SavedStateHandle savedStateHandle) {
    return new AddEditSubmissionViewModel(submissionRepository, subjectRepository, savedStateHandle);
  }
}
