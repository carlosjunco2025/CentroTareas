package com.junco.centrotareas.model

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data class Calculando(val progreso: Float = 0f) : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data class Cargando(val mensaje: String = "Cargando...") : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}

data class ComparacionTiempos(
    val tiempoUnaPorUna: Long? = null,
    val tiempoALaVez: Long? = null
) {
    val hayDatos: Boolean get() = tiempoUnaPorUna != null || tiempoALaVez != null

    fun obtenerDiferencia(): String {
        val t1 = tiempoUnaPorUna ?: return "Ejecuta ambos modos para comparar"
        val t2 = tiempoALaVez ?: return "Ejecuta ambos modos para comparar"
        val diffMs = t1 - t2
        return if (diffMs > 0) {
            val s = String.format(java.util.Locale.US, "%.1f", diffMs / 1000f)
            "A la vez fue $s s ($diffMs ms) más rápida"
        } else if (diffMs < 0) {
            val s = String.format(java.util.Locale.US, "%.1f", -diffMs / 1000f)
            "Una por una fue $s s (${-diffMs} ms) más rápida"
        } else {
            "Ambos modos tardaron exactamente lo mismo"
        }
    }
}
