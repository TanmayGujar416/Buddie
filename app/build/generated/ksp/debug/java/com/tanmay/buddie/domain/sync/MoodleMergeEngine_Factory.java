package com.tanmay.buddie.domain.sync;

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
public final class MoodleMergeEngine_Factory implements Factory<MoodleMergeEngine> {
  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  public MoodleMergeEngine_Factory(Provider<SubmissionRepository> submissionRepositoryProvider) {
    this.submissionRepositoryProvider = submissionRepositoryProvider;
  }

  @Override
  public MoodleMergeEngine get() {
    return newInstance(submissionRepositoryProvider.get());
  }

  public static MoodleMergeEngine_Factory create(
      Provider<SubmissionRepository> submissionRepositoryProvider) {
    return new MoodleMergeEngine_Factory(submissionRepositoryProvider);
  }

  public static MoodleMergeEngine newInstance(SubmissionRepository submissionRepository) {
    return new MoodleMergeEngine(submissionRepository);
  }
}
