package com.junco.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    // Tarea simulada de unos 4 s: va emitiendo el avance de 0f a 1f
    fun tareaConProgreso(): Flow<Float>

    // Conteo de primos emitiendo par de (porcentaje de 0f a 1f, cantidad final cuando termina)
    fun contarPrimos(hasta: Int): Flow<Pair<Float, Int?>>

    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
}
