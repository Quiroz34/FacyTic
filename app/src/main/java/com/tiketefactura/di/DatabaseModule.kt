package com.tiketefactura.di

import android.content.Context
import androidx.room.Room
import com.tiketefactura.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "tikete_v2.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProductoDao(db: AppDatabase): ProductoDao = db.productoDao()

    @Provides
    fun provideEmpresaDao(db: AppDatabase): EmpresaDao = db.empresaDao()

    @Provides
    fun provideDocumentoDao(db: AppDatabase): DocumentoDao = db.documentoDao()

    @Provides
    fun provideCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()

    @Provides
    fun provideClienteDao(db: AppDatabase): ClienteDao = db.clienteDao()

    @Provides
    fun provideGastoDao(db: AppDatabase): GastoDao = db.gastoDao()
}
