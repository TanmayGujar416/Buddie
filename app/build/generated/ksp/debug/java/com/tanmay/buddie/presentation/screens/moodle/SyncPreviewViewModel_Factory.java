package com.tanmay.buddie.presentation.screens.moodle;

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
public final class SyncPreviewViewModel_Factory implements Factory<SyncPreviewViewModel> {
  private final Provider<MoodleContentSyncEngine> syncEngineProvider;

  public SyncPreviewViewModel_Factory(Provider<MoodleContentSyncEngine> syncEngineProvider) {
    this.syncEngineProvider = syncEngineProvider;
  }

  @Override
  public SyncPreviewViewModel get() {
    return newInstance(syncEngineProvider.get());
  }

  public static SyncPreviewViewModel_Factory create(
      Provider<MoodleContentSyncEngine> syncEngineProvider) {
    return new SyncPreviewViewModel_Factory(syncEngineProvider);
  }

  public static SyncPreviewViewModel newInstance(MoodleContentSyncEngine syncEngine) {
    return new SyncPreviewViewModel(syncEngine);
  }
}
