package com.tanmay.buddie.presentation.screens.exams;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.domain.repository.ExamRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.SubmissionRepository;
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase;
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
public final class ExamsViewModel_Factory implements Factory<ExamsViewModel> {
  private final Provider<ExamRepository> examRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public ExamsViewModel_Factory(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.examRepositoryProvider = examRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.getMascotMoodUseCaseProvider = getMascotMoodUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public ExamsViewModel get() {
    return newInstance(examRepositoryProvider.get(), subjectRepositoryProvider.get(), submissionRepositoryProvider.get(), getMascotMoodUseCaseProvider.get(), userPreferencesProvider.get());
  }

  public static ExamsViewModel_Factory create(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new ExamsViewModel_Factory(examRepositoryProvider, subjectRepositoryProvider, submissionRepositoryProvider, getMascotMoodUseCaseProvider, userPreferencesProvider);
  }

  public static ExamsViewModel newInstance(ExamRepository examRepository,
      SubjectRepository subjectRepository, SubmissionRepository submissionRepository,
      GetMascotMoodUseCase getMascotMoodUseCase, UserPreferences userPreferences) {
    return new ExamsViewModel(examRepository, subjectRepository, submissionRepository, getMascotMoodUseCase, userPreferences);
  }
}
