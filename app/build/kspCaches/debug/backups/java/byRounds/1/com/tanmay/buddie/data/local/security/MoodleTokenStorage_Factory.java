package com.tanmay.buddie.data.local.security;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MoodleTokenStorage_Factory implements Factory<MoodleTokenStorage> {
  private final Provider<Context> contextProvider;

  public MoodleTokenStorage_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MoodleTokenStorage get() {
    return newInstance(contextProvider.get());
  }

  public static MoodleTokenStorage_Factory create(Provider<Context> contextProvider) {
    return new MoodleTokenStorage_Factory(contextProvider);
  }

  public static MoodleTokenStorage newInstance(Context context) {
    return new MoodleTokenStorage(context);
  }
}
