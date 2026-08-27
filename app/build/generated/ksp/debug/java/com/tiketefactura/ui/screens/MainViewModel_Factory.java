package com.tiketefactura.ui.screens;

import com.tiketefactura.data.local.CategoriaDao;
import com.tiketefactura.data.local.ClienteDao;
import com.tiketefactura.data.local.DocumentoDao;
import com.tiketefactura.data.local.EmpresaDao;
import com.tiketefactura.data.local.GastoDao;
import com.tiketefactura.data.local.ProductoDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<ProductoDao> productoDaoProvider;

  private final Provider<EmpresaDao> empresaDaoProvider;

  private final Provider<DocumentoDao> documentoDaoProvider;

  private final Provider<CategoriaDao> categoriaDaoProvider;

  private final Provider<ClienteDao> clienteDaoProvider;

  private final Provider<GastoDao> gastoDaoProvider;

  public MainViewModel_Factory(Provider<ProductoDao> productoDaoProvider,
      Provider<EmpresaDao> empresaDaoProvider, Provider<DocumentoDao> documentoDaoProvider,
      Provider<CategoriaDao> categoriaDaoProvider, Provider<ClienteDao> clienteDaoProvider,
      Provider<GastoDao> gastoDaoProvider) {
    this.productoDaoProvider = productoDaoProvider;
    this.empresaDaoProvider = empresaDaoProvider;
    this.documentoDaoProvider = documentoDaoProvider;
    this.categoriaDaoProvider = categoriaDaoProvider;
    this.clienteDaoProvider = clienteDaoProvider;
    this.gastoDaoProvider = gastoDaoProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(productoDaoProvider.get(), empresaDaoProvider.get(), documentoDaoProvider.get(), categoriaDaoProvider.get(), clienteDaoProvider.get(), gastoDaoProvider.get());
  }

  public static MainViewModel_Factory create(Provider<ProductoDao> productoDaoProvider,
      Provider<EmpresaDao> empresaDaoProvider, Provider<DocumentoDao> documentoDaoProvider,
      Provider<CategoriaDao> categoriaDaoProvider, Provider<ClienteDao> clienteDaoProvider,
      Provider<GastoDao> gastoDaoProvider) {
    return new MainViewModel_Factory(productoDaoProvider, empresaDaoProvider, documentoDaoProvider, categoriaDaoProvider, clienteDaoProvider, gastoDaoProvider);
  }

  public static MainViewModel newInstance(ProductoDao productoDao, EmpresaDao empresaDao,
      DocumentoDao documentoDao, CategoriaDao categoriaDao, ClienteDao clienteDao,
      GastoDao gastoDao) {
    return new MainViewModel(productoDao, empresaDao, documentoDao, categoriaDao, clienteDao, gastoDao);
  }
}
