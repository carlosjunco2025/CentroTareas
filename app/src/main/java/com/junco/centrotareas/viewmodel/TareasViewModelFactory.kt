package com.junco.centrotareas.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.junco.centrotareas.data.HistorialRepositoryDataStore
import com.junco.centrotareas.data.TareasRepositoryLocal

// Recibe el Context solo para crear el DataStore; el ViewModel no lo recibe
class TareasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TareasViewModel(
            TareasRepositoryLocal(),
            HistorialRepositoryDataStore(context.applicationContext)
        ) as T
    }
}
