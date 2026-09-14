package com.tanmay.buddie.service;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.domain.usecase.GetMascotMoodUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class BubbleService_MembersInjector implements MembersInjector<BubbleService> {
  private final Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public BubbleService_MembersInjector(Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.getMascotMoodUseCaseProvider = getMascotMoodUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  public static MembersInjector<BubbleService> create(
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new BubbleService_MembersInjector(getMascotMoodUseCaseProvider, userPreferencesProvider);
  }

  @Override
  public void injectMembers(BubbleService instance) {
    injectGetMascotMoodUseCase(instance, getMascotMoodUseCaseProvider.get());
    injectUserPreferences(instance, userPreferencesProvider.get());
  }

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.getMascotMoodUseCase")
  public static void injectGetMascotMoodUseCase(BubbleService instance,
      GetMascotMoodUseCase getMascotMoodUseCase) {
    instance.getMascotMoodUseCase = getMascotMoodUseCase;
  }

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.userPreferences")
  public static void injectUserPreferences(BubbleService instance,
      UserPreferences userPreferences) {
    instance.userPreferences = userPreferences;
  }
}
