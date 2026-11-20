package com.junco.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junco.centrotareas.data.HistorialRepository
import com.junco.centrotareas.data.TareasRepository
import com.junco.centrotareas.model.CargaUi
import com.junco.centrotareas.model.ComparacionTiempos
import com.junco.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class TareasViewModel(
    private val repo: TareasRepository,
    private val historialRepo: HistorialRepository
) : ViewModel() {

    // ----- Historial (DataStore): los últimos 5 resultados, se ven apenas se abre la app
    val historial: StateFlow<List<String>> = historialRepo.historial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun borrarHistorial() {
        viewModelScope.launch {
            historialRepo.borrar()
        }
    }

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    private var trabajo: Job? = null

    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    fun tareaLarga() {
        if (_corriendo.value) return

        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")

                repo.tareaConProgreso().collect { avance ->
                    _progreso.value = avance
                }

                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } catch (e: CancellationException) {
                _mensaje.value = "Cancelado"
                throw e
            } finally {
                _corriendo.value = false
            }
        }
    }

    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    // ----- Contar primos (paso 5, emitiendo progreso)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    private var trabajoPrimos: Job? = null

    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando) return

        _primos.value = PrimosUi.Calculando(0f)
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                var cantidadFinal = 0
                repo.contarPrimos(hasta).collect { (pct, cantidad) ->
                    _primos.value = PrimosUi.Calculando(pct)
                    if (cantidad != null) {
                        cantidadFinal = cantidad
                    }
                }
                val milisegundos = System.currentTimeMillis() - inicio
                _primos.value = PrimosUi.Listo(cantidadFinal, milisegundos)
                historialRepo.agregar("Primos hasta $hasta: $cantidadFinal ($milisegundos ms)")
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
        _primos.value = PrimosUi.Cancelado
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    private val _limiteTiempo = MutableStateFlow(false)
    val limiteTiempo: StateFlow<Boolean> = _limiteTiempo.asStateFlow()

    fun cambiarLimiteTiempo(valor: Boolean) {
        _limiteTiempo.value = valor
    }

    // Tabla de comparación de tiempos
    private val _comparacion = MutableStateFlow(ComparacionTiempos())
    val comparacion: StateFlow<ComparacionTiempos> = _comparacion.asStateFlow()

    // Ejecuta hasta 3 reintentos con espera creciente (500ms, 1000ms, 2000ms)
    private suspend fun <T> ejecutarConReintentoYTimeout(
        bloque: suspend () -> T
    ): T {
        val esperas = listOf(500L, 1000L, 2000L)
        var ultimoError: Throwable? = null

        for (intento in 1..3) {
            try {
                if (intento > 1) {
                    _carga.value = CargaUi.Cargando("Reintentando $intento/3...")
                    delay(esperas.getOrElse(intento - 2) { 1000L })
                }
                return if (_limiteTiempo.value) {
                    withTimeout(2000) { bloque() }
                } else {
                    bloque()
                }
            } catch (e: TimeoutCancellationException) {
                // Capturado antes que CancellationException por ser subclase
                ultimoError = e
            } catch (e: CancellationException) {
                // SIEMPRE relanzar CancellationException
                throw e
            } catch (e: Exception) {
                ultimoError = e
            }
        }
        throw ultimoError ?: Exception("Error desconocido")
    }

    fun cargarUnaPorUna() {
        if (_carga.value is CargaUi.Cargando) return

        _carga.value = CargaUi.Cargando("Cargando perfil y pedidos...")
        viewModelScope.launch {
            try {
                val inicio = System.currentTimeMillis()
                val (perfil, pedidos) = ejecutarConReintentoYTimeout {
                    val perfil = repo.cargarPerfil()
                    val pedidos = repo.cargarPedidos(_simularError.value)
                    Pair(perfil, pedidos)
                }
                val milisegundos = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito("Una por una", perfil, pedidos.size, milisegundos)
                _comparacion.value = _comparacion.value.copy(tiempoUnaPorUna = milisegundos)
                historialRepo.agregar("Carga una por una: ${pedidos.size} pedidos ($milisegundos ms)")
            } catch (e: TimeoutCancellationException) {
                _carga.value = CargaUi.Error("Tardó demasiado (más de 2 s)")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message}")
            }
        }
    }

    fun cargarALaVez() {
        if (_carga.value is CargaUi.Cargando) return

        _carga.value = CargaUi.Cargando("Cargando en paralelo...")
        viewModelScope.launch {
            try {
                val inicio = System.currentTimeMillis()
                val (perfil, pedidos) = ejecutarConReintentoYTimeout {
                    coroutineScope {
                        val perfilAsync = async { repo.cargarPerfil() }
                        val pedidosAsync = async { repo.cargarPedidos(_simularError.value) }
                        Pair(perfilAsync.await(), pedidosAsync.await())
                    }
                }
                val milisegundos = System.currentTimeMillis() - inicio
                _carga.value = CargaUi.Exito("A la vez", perfil, pedidos.size, milisegundos)
                _comparacion.value = _comparacion.value.copy(tiempoALaVez = milisegundos)
                historialRepo.agregar("Carga a la vez: ${pedidos.size} pedidos ($milisegundos ms)")
            } catch (e: TimeoutCancellationException) {
                _carga.value = CargaUi.Error("Tardó demasiado (más de 2 s)")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message}")
            }
        }
    }
}
