package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.ClienteDao;
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
public final class DatabaseModule_ProvideClienteDaoFactory implements Factory<ClienteDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideClienteDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ClienteDao get() {
    return provideClienteDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideClienteDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideClienteDaoFactory(dbProvider);
  }

  public static ClienteDao provideClienteDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideClienteDao(db));
  }
}
