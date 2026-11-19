package com.junco.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.junco.centrotareas.model.CargaUi
import com.junco.centrotareas.model.PrimosUi
import com.junco.centrotareas.ui.theme.VerdeExito
import com.junco.centrotareas.ui.theme.VerdeExitoTexto
import com.junco.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val primos by vm.primos.collectAsState()
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val limiteTiempo by vm.limiteTiempo.collectAsState()

    // Texto del campo numérico de los primos
    var textoHasta by rememberSaveable { mutableStateOf("500000") }

    // Número con el que se hizo el último conteo, para mostrarlo en el resultado
    var hastaContado by rememberSaveable { mutableIntStateOf(0) }

    // Contador del botón Tócame (es solo de la pantalla)
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaTarea(titulo = "Tarea con progreso") {
                // Prueba de vida: si el contador deja de sumar, la pantalla está congelada
                Button(onClick = { toques++ }) {
                    Text("Tócame: $toques")
                }

                // Iniciar solo mientras no corre; Cancelar solo mientras corre
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.tareaLarga() },
                        enabled = !corriendo
                    ) {
                        Text("Iniciar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelar() },
                        enabled = corriendo
                    ) {
                        Text("Cancelar")
                    }
                }

                // Barra de progreso y el porcentaje debajo
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje)
            }

            TarjetaTarea(titulo = "Contar números primos") {
                // Solo se acepta un número entero mayor o igual a 2
                val hasta = textoHasta.toIntOrNull()
                val numeroValido = hasta != null && hasta >= 2
                val calculando = primos is PrimosUi.Calculando

                OutlinedTextField(
                    value = textoHasta,
                    onValueChange = { textoHasta = it },
                    label = { Text("Hasta el número") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (hasta != null) {
                                hastaContado = hasta
                                vm.contar(hasta)
                            }
                        },
                        enabled = numeroValido && !calculando
                    ) {
                        Text("Contar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelarPrimos() },
                        enabled = calculando
                    ) {
                        Text("Cancelar")
                    }
                }

                when (val p = primos) {
                    is PrimosUi.Inicial -> {}
                    is PrimosUi.Calculando -> {
                        CircularProgressIndicator()
                        Text("Calculando...")
                    }
                    is PrimosUi.Listo -> RecuadroExito(
                        titulo = "Primos hasta $hastaContado: ${p.cantidad}",
                        detalle = "Tiempo: ${p.milisegundos} ms"
                    )
                    is PrimosUi.Cancelado -> Text("Cancelado")
                }
            }

            TarjetaTarea(titulo = "Dos cargas") {
                val cargando = carga is CargaUi.Cargando

                // Interruptor para simular que los pedidos fallan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = simularError,
                        onCheckedChange = { vm.cambiarSimularError(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simular error")
                }

                // Interruptor del tiempo máximo de 2 segundos (withTimeout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = limiteTiempo,
                        onCheckedChange = { vm.cambiarLimiteTiempo(it) },
                        enabled = !cargando
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tiempo máximo de 2 s")
                }

                // Mientras carga, los dos botones se deshabilitan
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { vm.cargarUnaPorUna() },
                        enabled = !cargando
                    ) {
                        Text("Una por una")
                    }
                    Button(
                        onClick = { vm.cargarALaVez() },
                        enabled = !cargando
                    ) {
                        Text("A la vez")
                    }
                }

                when (val c = carga) {
                    is CargaUi.Inicial -> {}
                    is CargaUi.Cargando -> {
                        CircularProgressIndicator()
                        Text("Cargando...")
                    }
                    is CargaUi.Exito -> RecuadroExito(
                        titulo = "Modo: ${c.modo}",
                        detalle = "Perfil: ${c.perfil}\nPedidos: ${c.pedidos}\nTiempo: ${c.milisegundos} ms"
                    )
                    is CargaUi.Error -> RecuadroError(
                        mensaje = c.mensaje,
                        onReintentar = { vm.cargarALaVez() }
                    )
                }
            }
        }
    }
}

// Card con el título de la tarea en azul
@Composable
fun TarjetaTarea(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            contenido()
        }
    }
}

// Recuadro verde para mostrar un resultado: título en negrita y el detalle debajo, centrados
@Composable
fun RecuadroExito(titulo: String, detalle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdeExito, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, color = VerdeExitoTexto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(detalle, color = VerdeExitoTexto, textAlign = TextAlign.Center)
    }
}

// Recuadro con el color de error y el mensaje; debajo, el botón Reintentar
@Composable
fun RecuadroError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
        OutlinedButton(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}
