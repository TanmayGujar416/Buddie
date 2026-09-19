package com.tanmay.buddie;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.TimetableRepository;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<TimetableRepository> timetableRepositoryProvider;

  public MainActivity_MembersInjector(Provider<UserPreferences> userPreferencesProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider) {
    return new MainActivity_MembersInjector(userPreferencesProvider, subjectRepositoryProvider, timetableRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectUserPreferences(instance, userPreferencesProvider.get());
    injectSubjectRepository(instance, subjectRepositoryProvider.get());
    injectTimetableRepository(instance, timetableRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.tanmay.buddie.MainActivity.userPreferences")
  public static void injectUserPreferences(MainActivity instance, UserPreferences userPreferences) {
    instance.userPreferences = userPreferences;
  }

  @InjectedFieldSignature("com.tanmay.buddie.MainActivity.subjectRepository")
  public static void injectSubjectRepository(MainActivity instance,
      SubjectRepository subjectRepository) {
    instance.subjectRepository = subjectRepository;
  }

  @InjectedFieldSignature("com.tanmay.buddie.MainActivity.timetableRepository")
  public static void injectTimetableRepository(MainActivity instance,
      TimetableRepository timetableRepository) {
    instance.timetableRepository = timetableRepository;
  }
}
