package com.tanmay.buddie.data.remote.moodle;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.data.local.security.MoodleTokenStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class MoodleApiClient_Factory implements Factory<MoodleApiClient> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<MoodleTokenStorage> tokenStorageProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public MoodleApiClient_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<MoodleTokenStorage> tokenStorageProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.tokenStorageProvider = tokenStorageProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public MoodleApiClient get() {
    return newInstance(okHttpClientProvider.get(), tokenStorageProvider.get(), userPreferencesProvider.get());
  }

  public static MoodleApiClient_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<MoodleTokenStorage> tokenStorageProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new MoodleApiClient_Factory(okHttpClientProvider, tokenStorageProvider, userPreferencesProvider);
  }

  public static MoodleApiClient newInstance(OkHttpClient okHttpClient,
      MoodleTokenStorage tokenStorage, UserPreferences userPreferences) {
    return new MoodleApiClient(okHttpClient, tokenStorage, userPreferences);
  }
}
