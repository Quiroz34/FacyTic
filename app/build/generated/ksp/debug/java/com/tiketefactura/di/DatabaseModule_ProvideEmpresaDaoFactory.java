package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.EmpresaDao;
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
public final class DatabaseModule_ProvideEmpresaDaoFactory implements Factory<EmpresaDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideEmpresaDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public EmpresaDao get() {
    return provideEmpresaDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideEmpresaDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideEmpresaDaoFactory(dbProvider);
  }

  public static EmpresaDao provideEmpresaDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideEmpresaDao(db));
  }
}
