package com.tanmay.buddie.domain.usecase;

import com.tanmay.buddie.domain.repository.ExamRepository;
import com.tanmay.buddie.domain.repository.SubmissionRepository;
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
public final class GetMascotMoodUseCase_Factory implements Factory<GetMascotMoodUseCase> {
  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<ExamRepository> examRepositoryProvider;

  public GetMascotMoodUseCase_Factory(Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.examRepositoryProvider = examRepositoryProvider;
  }

  @Override
  public GetMascotMoodUseCase get() {
    return newInstance(submissionRepositoryProvider.get(), examRepositoryProvider.get());
  }

  public static GetMascotMoodUseCase_Factory create(
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    return new GetMascotMoodUseCase_Factory(submissionRepositoryProvider, examRepositoryProvider);
  }

  public static GetMascotMoodUseCase newInstance(SubmissionRepository submissionRepository,
      ExamRepository examRepository) {
    return new GetMascotMoodUseCase(submissionRepository, examRepository);
  }
}
