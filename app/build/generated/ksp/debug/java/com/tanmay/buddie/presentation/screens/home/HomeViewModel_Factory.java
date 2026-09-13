package com.tanmay.buddie.presentation.screens.home;

import com.tanmay.buddie.domain.repository.ExamRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<ExamRepository> examRepositoryProvider;

  public HomeViewModel_Factory(Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.examRepositoryProvider = examRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(submissionRepositoryProvider.get(), subjectRepositoryProvider.get(), examRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    return new HomeViewModel_Factory(submissionRepositoryProvider, subjectRepositoryProvider, examRepositoryProvider);
  }

  public static HomeViewModel newInstance(SubmissionRepository submissionRepository,
      SubjectRepository subjectRepository, ExamRepository examRepository) {
    return new HomeViewModel(submissionRepository, subjectRepository, examRepository);
  }
}
