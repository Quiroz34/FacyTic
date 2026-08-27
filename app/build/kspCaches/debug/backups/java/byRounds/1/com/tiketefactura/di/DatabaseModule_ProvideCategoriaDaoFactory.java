package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.CategoriaDao;
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
public final class DatabaseModule_ProvideCategoriaDaoFactory implements Factory<CategoriaDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideCategoriaDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CategoriaDao get() {
    return provideCategoriaDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCategoriaDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideCategoriaDaoFactory(dbProvider);
  }

  public static CategoriaDao provideCategoriaDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCategoriaDao(db));
  }
}
