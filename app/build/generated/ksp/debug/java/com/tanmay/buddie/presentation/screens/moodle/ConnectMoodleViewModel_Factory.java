package com.tanmay.buddie.presentation.screens.moodle;

import com.tanmay.buddie.data.remote.moodle.MoodleApiClient;
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
public final class ConnectMoodleViewModel_Factory implements Factory<ConnectMoodleViewModel> {
  private final Provider<MoodleApiClient> moodleApiClientProvider;

  public ConnectMoodleViewModel_Factory(Provider<MoodleApiClient> moodleApiClientProvider) {
    this.moodleApiClientProvider = moodleApiClientProvider;
  }

  @Override
  public ConnectMoodleViewModel get() {
    return newInstance(moodleApiClientProvider.get());
  }

  public static ConnectMoodleViewModel_Factory create(
      Provider<MoodleApiClient> moodleApiClientProvider) {
    return new ConnectMoodleViewModel_Factory(moodleApiClientProvider);
  }

  public static ConnectMoodleViewModel newInstance(MoodleApiClient moodleApiClient) {
    return new ConnectMoodleViewModel(moodleApiClient);
  }
}
