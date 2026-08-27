package com.tiketefactura.di;

import com.tiketefactura.data.local.AppDatabase;
import com.tiketefactura.data.local.DocumentoDao;
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
public final class DatabaseModule_ProvideDocumentoDaoFactory implements Factory<DocumentoDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideDocumentoDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DocumentoDao get() {
    return provideDocumentoDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDocumentoDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideDocumentoDaoFactory(dbProvider);
  }

  public static DocumentoDao provideDocumentoDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDocumentoDao(db));
  }
}
