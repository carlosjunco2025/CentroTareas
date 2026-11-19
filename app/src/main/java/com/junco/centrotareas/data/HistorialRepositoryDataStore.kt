package com.junco.centrotareas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Un solo DataStore para la app, guardado en el archivo "historial"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "historial")

class HistorialRepositoryDataStore(private val context: Context) : HistorialRepository {

    // Guardo los resultados en un solo texto, separados por saltos de línea
    private val claveHistorial = stringPreferencesKey("ultimos_resultados")
    private val maximo = 5

    override val historial: Flow<List<String>> = context.dataStore.data.map { preferencias ->
        convertirALista(preferencias[claveHistorial])
    }

    override suspend fun agregar(resultado: String) {
        context.dataStore.edit { preferencias ->
            val actual = convertirALista(preferencias[claveHistorial])
            // El nuevo va primero y solo me quedo con los 5 más recientes
            val nueva = (listOf(resultado) + actual).take(maximo)
            preferencias[claveHistorial] = nueva.joinToString("\n")
        }
    }

    override suspend fun borrar() {
        context.dataStore.edit { preferencias ->
            preferencias.remove(claveHistorial)
        }
    }

    private fun convertirALista(texto: String?): List<String> {
        if (texto.isNullOrBlank()) return emptyList()
        return texto.split("\n").filter { it.isNotBlank() }
    }
}
