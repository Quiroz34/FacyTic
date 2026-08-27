package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.ProductoDao;
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
public final class DatabaseModule_ProvideProductoDaoFactory implements Factory<ProductoDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideProductoDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ProductoDao get() {
    return provideProductoDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideProductoDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideProductoDaoFactory(dbProvider);
  }

  public static ProductoDao provideProductoDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideProductoDao(db));
  }
}
