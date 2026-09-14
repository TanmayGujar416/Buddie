package com.tanmay.buddie.presentation.screens.home;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<ExamRepository> examRepositoryProvider;

  private final Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider;

  public HomeViewModel_Factory(Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider,
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider) {
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.examRepositoryProvider = examRepositoryProvider;
    this.getMascotMoodUseCaseProvider = getMascotMoodUseCaseProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(submissionRepositoryProvider.get(), subjectRepositoryProvider.get(), examRepositoryProvider.get(), getMascotMoodUseCaseProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider,
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider) {
    return new HomeViewModel_Factory(submissionRepositoryProvider, subjectRepositoryProvider, examRepositoryProvider, getMascotMoodUseCaseProvider);
  }

  public static HomeViewModel newInstance(SubmissionRepository submissionRepository,
      SubjectRepository subjectRepository, ExamRepository examRepository,
      GetMascotMoodUseCase getMascotMoodUseCase) {
    return new HomeViewModel(submissionRepository, subjectRepository, examRepository, getMascotMoodUseCase);
  }
}
