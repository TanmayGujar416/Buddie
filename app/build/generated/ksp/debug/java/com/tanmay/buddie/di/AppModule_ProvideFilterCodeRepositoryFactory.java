package com.tanmay.buddie.di;

import com.tanmay.buddie.data.local.dao.TokenSignatureDecisionDao;
import com.tanmay.buddie.domain.repository.FilterCodeRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideFilterCodeRepositoryFactory implements Factory<FilterCodeRepository> {
  private final Provider<TokenSignatureDecisionDao> daoProvider;

  public AppModule_ProvideFilterCodeRepositoryFactory(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public FilterCodeRepository get() {
    return provideFilterCodeRepository(daoProvider.get());
  }

  public static AppModule_ProvideFilterCodeRepositoryFactory create(
      Provider<TokenSignatureDecisionDao> daoProvider) {
    return new AppModule_ProvideFilterCodeRepositoryFactory(daoProvider);
  }

  public static FilterCodeRepository provideFilterCodeRepository(TokenSignatureDecisionDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFilterCodeRepository(dao));
  }
}
