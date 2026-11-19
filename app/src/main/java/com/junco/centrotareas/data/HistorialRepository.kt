package com.junco.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface HistorialRepository {
    // Últimos resultados guardados, del más nuevo al más antiguo (máximo 5)
    val historial: Flow<List<String>>
    suspend fun agregar(resultado: String)
    suspend fun borrar()
}
