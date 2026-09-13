package com.tanmay.buddie.di;

import android.app.Application;
import com.tanmay.buddie.data.local.AppDatabase;
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
public final class AppModule_ProvideAppDatabaseFactory implements Factory<AppDatabase> {
  private final Provider<Application> appProvider;

  public AppModule_ProvideAppDatabaseFactory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public AppDatabase get() {
    return provideAppDatabase(appProvider.get());
  }

  public static AppModule_ProvideAppDatabaseFactory create(Provider<Application> appProvider) {
    return new AppModule_ProvideAppDatabaseFactory(appProvider);
  }

  public static AppDatabase provideAppDatabase(Application app) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAppDatabase(app));
  }
}
