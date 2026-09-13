package com.tanmay.buddie.presentation.screens.exams;

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
public final class ExamsViewModel_Factory implements Factory<ExamsViewModel> {
  private final Provider<ExamRepository> examRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  public ExamsViewModel_Factory(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider) {
    this.examRepositoryProvider = examRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.submissionRepositoryProvider = submissionRepositoryProvider;
  }

  @Override
  public ExamsViewModel get() {
    return newInstance(examRepositoryProvider.get(), subjectRepositoryProvider.get(), submissionRepositoryProvider.get());
  }

  public static ExamsViewModel_Factory create(Provider<ExamRepository> examRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider) {
    return new ExamsViewModel_Factory(examRepositoryProvider, subjectRepositoryProvider, submissionRepositoryProvider);
  }

  public static ExamsViewModel newInstance(ExamRepository examRepository,
      SubjectRepository subjectRepository, SubmissionRepository submissionRepository) {
    return new ExamsViewModel(examRepository, subjectRepository, submissionRepository);
  }
}
