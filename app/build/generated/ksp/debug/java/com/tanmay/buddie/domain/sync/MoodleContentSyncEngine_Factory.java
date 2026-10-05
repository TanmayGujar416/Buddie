package com.tanmay.buddie.domain.sync;

import com.tanmay.buddie.data.local.dao.MoodleCourseContentCacheDao;
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient;
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.TokenSignatureRepository;
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
public final class MoodleContentSyncEngine_Factory implements Factory<MoodleContentSyncEngine> {
  private final Provider<MoodleApiClient> moodleApiClientProvider;

  private final Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<MoodleCourseContentCacheDao> contentCacheDaoProvider;

  private final Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider;

  private final Provider<MoodleMergeEngine> mergeEngineProvider;

  public MoodleContentSyncEngine_Factory(Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseContentCacheDao> contentCacheDaoProvider,
      Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider,
      Provider<MoodleMergeEngine> mergeEngineProvider) {
    this.moodleApiClientProvider = moodleApiClientProvider;
    this.courseMappingRepositoryProvider = courseMappingRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.contentCacheDaoProvider = contentCacheDaoProvider;
    this.tokenSignatureRepositoryProvider = tokenSignatureRepositoryProvider;
    this.mergeEngineProvider = mergeEngineProvider;
  }

  @Override
  public MoodleContentSyncEngine get() {
    return newInstance(moodleApiClientProvider.get(), courseMappingRepositoryProvider.get(), subjectRepositoryProvider.get(), contentCacheDaoProvider.get(), tokenSignatureRepositoryProvider.get(), mergeEngineProvider.get());
  }

  public static MoodleContentSyncEngine_Factory create(
      Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseContentCacheDao> contentCacheDaoProvider,
      Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider,
      Provider<MoodleMergeEngine> mergeEngineProvider) {
    return new MoodleContentSyncEngine_Factory(moodleApiClientProvider, courseMappingRepositoryProvider, subjectRepositoryProvider, contentCacheDaoProvider, tokenSignatureRepositoryProvider, mergeEngineProvider);
  }

  public static MoodleContentSyncEngine newInstance(MoodleApiClient moodleApiClient,
      MoodleCourseMappingRepository courseMappingRepository, SubjectRepository subjectRepository,
      MoodleCourseContentCacheDao contentCacheDao,
      TokenSignatureRepository tokenSignatureRepository, MoodleMergeEngine mergeEngine) {
    return new MoodleContentSyncEngine(moodleApiClient, courseMappingRepository, subjectRepository, contentCacheDao, tokenSignatureRepository, mergeEngine);
  }
}
