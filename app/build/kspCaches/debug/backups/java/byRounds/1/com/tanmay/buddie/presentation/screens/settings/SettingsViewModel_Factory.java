package com.tanmay.buddie.presentation.screens.settings;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.data.remote.moodle.MoodleApiClient;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<MoodleApiClient> moodleApiClientProvider;

  private final Provider<MoodleContentSyncEngine> moodleSyncEngineProvider;

  public SettingsViewModel_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<MoodleContentSyncEngine> moodleSyncEngineProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.moodleApiClientProvider = moodleApiClientProvider;
    this.moodleSyncEngineProvider = moodleSyncEngineProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userPreferencesProvider.get(), moodleApiClientProvider.get(), moodleSyncEngineProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferences> userPreferencesProvider,
      Provider<MoodleApiClient> moodleApiClientProvider,
      Provider<MoodleContentSyncEngine> moodleSyncEngineProvider) {
    return new SettingsViewModel_Factory(userPreferencesProvider, moodleApiClientProvider, moodleSyncEngineProvider);
  }

  public static SettingsViewModel newInstance(UserPreferences userPreferences,
      MoodleApiClient moodleApiClient, MoodleContentSyncEngine moodleSyncEngine) {
    return new SettingsViewModel(userPreferences, moodleApiClient, moodleSyncEngine);
  }
}
