package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.GastoDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "KotlinInternalInJava",
    "cast"
})
public final class DatabaseModule_ProvideGastoDaoFactory implements Factory<GastoDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideGastoDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GastoDao get() {
    return provideGastoDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideGastoDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideGastoDaoFactory(dbProvider);
  }

  public static GastoDao provideGastoDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideGastoDao(db));
  }
}
