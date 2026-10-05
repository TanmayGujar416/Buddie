package com.tanmay.buddie.presentation.screens.moodle;

import androidx.lifecycle.SavedStateHandle;
import com.tanmay.buddie.domain.repository.MoodleCourseMappingRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.TokenSignatureRepository;
import com.tanmay.buddie.domain.sync.MoodleContentSyncEngine;
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
public final class TokenDecisionViewModel_Factory implements Factory<TokenDecisionViewModel> {
  private final Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider;

  private final Provider<MoodleContentSyncEngine> syncEngineProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public TokenDecisionViewModel_Factory(
      Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<MoodleContentSyncEngine> syncEngineProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.tokenSignatureRepositoryProvider = tokenSignatureRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.courseMappingRepositoryProvider = courseMappingRepositoryProvider;
    this.syncEngineProvider = syncEngineProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public TokenDecisionViewModel get() {
    return newInstance(tokenSignatureRepositoryProvider.get(), subjectRepositoryProvider.get(), courseMappingRepositoryProvider.get(), syncEngineProvider.get(), savedStateHandleProvider.get());
  }

  public static TokenDecisionViewModel_Factory create(
      Provider<TokenSignatureRepository> tokenSignatureRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<MoodleCourseMappingRepository> courseMappingRepositoryProvider,
      Provider<MoodleContentSyncEngine> syncEngineProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new TokenDecisionViewModel_Factory(tokenSignatureRepositoryProvider, subjectRepositoryProvider, courseMappingRepositoryProvider, syncEngineProvider, savedStateHandleProvider);
  }

  public static TokenDecisionViewModel newInstance(
      TokenSignatureRepository tokenSignatureRepository, SubjectRepository subjectRepository,
      MoodleCourseMappingRepository courseMappingRepository, MoodleContentSyncEngine syncEngine,
      SavedStateHandle savedStateHandle) {
    return new TokenDecisionViewModel(tokenSignatureRepository, subjectRepository, courseMappingRepository, syncEngine, savedStateHandle);
  }
}
