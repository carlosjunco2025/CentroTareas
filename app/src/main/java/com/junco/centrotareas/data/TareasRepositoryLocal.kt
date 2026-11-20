package com.junco.centrotareas.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    // Tarea simulada: 100 vueltas de 40 ms (unos 4 s) y en cada vuelta emite el avance.
    // flowOn hace que este bloque corra en Dispatchers.Default; quien recolecta sigue en main
    override fun tareaConProgreso(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    // Carga simulada: tarda 2 segundos
    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    // Carga simulada: tarda 3 segundos y puede fallar si se pide
    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Cálculo pesado de primos en Dispatchers.Default.
    // Emite (porcentaje 0f..1f, cantidad final si terminó) y respeta la cancelación con ensureActive()
    override fun contarPrimos(hasta: Int): Flow<Pair<Float, Int?>> = flow {
        var cantidad = 0
        val total = (hasta - 1).coerceAtLeast(1)
        var ultimoPct = -1

        for (n in 2..hasta) {
            currentCoroutineContext().ensureActive()
            if (esPrimo(n)) {
                cantidad++
            }
            val pct = ((n - 1) * 100) / total
            if (pct != ultimoPct || n == hasta) {
                ultimoPct = pct
                emit(Pair(pct / 100f, if (n == hasta) cantidad else null))
            }
        }
    }.flowOn(Dispatchers.Default)

    // Un número es primo si ningún d entre 2 y su raíz lo divide
    private fun esPrimo(n: Int): Boolean {
        var d = 2
        // d * d <= n es lo mismo que d <= raíz de n (con Long para no desbordar)
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
