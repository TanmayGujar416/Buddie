package com.tanmay.buddie.presentation.screens.moodle;

import com.tanmay.buddie.data.remote.moodle.MoodleApiClient;
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository;
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
public final class CourseMappingViewModel_Factory implements Factory<CourseMappingViewModel> {
  private final Provider<MoodleApiClient> moodleApiClientProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<MoodleCourseMappingRepository> mappingRepositoryProvider;

  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  public CourseMappingViewModel_Factory(Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> mappingRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider) {
    this.moodleApiClientProvider = moodleApiClientProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.mappingRepositoryProvider = mappingRepositoryProvider;
    this.submissionRepositoryProvider = submissionRepositoryProvider;
  }

  @Override
  public CourseMappingViewModel get() {
    return newInstance(moodleApiClientProvider.get(), subjectRepositoryProvider.get(), mappingRepositoryProvider.get(), submissionRepositoryProvider.get());
  }

  public static CourseMappingViewModel_Factory create(
      Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> mappingRepositoryProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider) {
    return new CourseMappingViewModel_Factory(moodleApiClientProvider, subjectRepositoryProvider, mappingRepositoryProvider, submissionRepositoryProvider);
  }

  public static CourseMappingViewModel newInstance(MoodleApiClient moodleApiClient,
      SubjectRepository subjectRepository, MoodleCourseMappingRepository mappingRepository,
      SubmissionRepository submissionRepository) {
    return new CourseMappingViewModel(moodleApiClient, subjectRepository, mappingRepository, submissionRepository);
  }
}
