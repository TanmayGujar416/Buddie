package com.tanmay.buddie.service;

import com.tanmay.buddie.data.datastore.UserPreferences;
import com.tanmay.buddie.domain.repository.ExamRepository;
import com.tanmay.buddie.domain.repository.SubjectRepository;
import com.tanmay.buddie.domain.repository.SubmissionRepository;
import com.tanmay.buddie.domain.repository.TimetableRepository;
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

  private final Provider<SubmissionRepository> submissionRepositoryProvider;

  private final Provider<SubjectRepository> subjectRepositoryProvider;

  private final Provider<TimetableRepository> timetableRepositoryProvider;

  private final Provider<ExamRepository> examRepositoryProvider;

  public BubbleService_MembersInjector(Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    this.getMascotMoodUseCaseProvider = getMascotMoodUseCaseProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.submissionRepositoryProvider = submissionRepositoryProvider;
    this.subjectRepositoryProvider = subjectRepositoryProvider;
    this.timetableRepositoryProvider = timetableRepositoryProvider;
    this.examRepositoryProvider = examRepositoryProvider;
  }

  public static MembersInjector<BubbleService> create(
      Provider<GetMascotMoodUseCase> getMascotMoodUseCaseProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SubmissionRepository> submissionRepositoryProvider,
      Provider<SubjectRepository> subjectRepositoryProvider,
      Provider<TimetableRepository> timetableRepositoryProvider,
      Provider<ExamRepository> examRepositoryProvider) {
    return new BubbleService_MembersInjector(getMascotMoodUseCaseProvider, userPreferencesProvider, submissionRepositoryProvider, subjectRepositoryProvider, timetableRepositoryProvider, examRepositoryProvider);
  }

  @Override
  public void injectMembers(BubbleService instance) {
    injectGetMascotMoodUseCase(instance, getMascotMoodUseCaseProvider.get());
    injectUserPreferences(instance, userPreferencesProvider.get());
    injectSubmissionRepository(instance, submissionRepositoryProvider.get());
    injectSubjectRepository(instance, subjectRepositoryProvider.get());
    injectTimetableRepository(instance, timetableRepositoryProvider.get());
    injectExamRepository(instance, examRepositoryProvider.get());
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

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.submissionRepository")
  public static void injectSubmissionRepository(BubbleService instance,
      SubmissionRepository submissionRepository) {
    instance.submissionRepository = submissionRepository;
  }

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.subjectRepository")
  public static void injectSubjectRepository(BubbleService instance,
      SubjectRepository subjectRepository) {
    instance.subjectRepository = subjectRepository;
  }

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.timetableRepository")
  public static void injectTimetableRepository(BubbleService instance,
      TimetableRepository timetableRepository) {
    instance.timetableRepository = timetableRepository;
  }

  @InjectedFieldSignature("com.tanmay.buddie.service.BubbleService.examRepository")
  public static void injectExamRepository(BubbleService instance, ExamRepository examRepository) {
    instance.examRepository = examRepository;
  }
}
