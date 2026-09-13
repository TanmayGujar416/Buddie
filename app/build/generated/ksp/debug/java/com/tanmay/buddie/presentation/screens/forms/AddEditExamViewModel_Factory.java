package com.tanmay.buddie.presentation.screens.forms;

import androidx.lifecycle.SavedStateHandle;
import com.tanmay.buddie.domain.repository.ExamRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
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
public final class AddEditExamViewModel_Factory implements Factory<AddEditExamViewModel> {
  private final Provider<ExamRepository> examRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditExamViewModel_Factory(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.examRepositoryProvider = examRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditExamViewModel get() {
    return newInstance(examRepositoryProvider.get(), subjectRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditExamViewModel_Factory create(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditExamViewModel_Factory(examRepositoryProvider, subjectRepositoryProvider, savedStateHandleProvider);
  }

  public static AddEditExamViewModel newInstance(ExamRepository examRepository,
      SubjectRepository subjectRepository, SavedStateHandle savedStateHandle) {
    return new AddEditExamViewModel(examRepository, subjectRepository, savedStateHandle);
  }
}
