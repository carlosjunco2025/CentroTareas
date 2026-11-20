package com.junco.centrotareas.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junco.centrotareas.model.CargaUi
import com.junco.centrotareas.model.ComparacionTiempos
import com.junco.centrotareas.model.PrimosUi
import com.junco.centrotareas.ui.theme.RojoErrorContainer
import com.junco.centrotareas.ui.theme.RojoErrorTexto
import com.junco.centrotareas.ui.theme.VerdeExitoContainer
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
    val historial by vm.historial.collectAsState()
    val comparacion by vm.comparacion.collectAsState()

    var textoHasta by rememberSaveable { mutableStateOf("500000") }
    var hastaContado by rememberSaveable { mutableIntStateOf(0) }
    var toques by rememberSaveable { mutableIntStateOf(0) }

    // Animación de rebote (spring scale) al hacer tap en Tócame
    var presionadoTocame by remember { mutableStateOf(false) }
    val escalaTocame by animateFloatAsState(
        targetValue = if (presionadoTocame) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { presionadoTocame = false },
        label = "escalaTocame"
    )

    // Animación suavizada de la barra de progreso principal
    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(durationMillis = 200),
        label = "progresoAnimado"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CentroTareas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
            // ---------------- TAREA 1: Tarea con progreso ----------------
            TarjetaTarea(
                titulo = "Tarea con progreso",
                icono = Icons.Default.PlayArrow
            ) {
                // Prueba de respuesta táctil (no bloquea UI)
                Button(
                    onClick = {
                        toques++
                        presionadoTocame = true
                    },
                    modifier = Modifier
                        .scale(escalaTocame)
                        .defaultMinSize(minHeight = 48.dp)
                        .semantics { contentDescription = "Botón Tócame para probar fluidez de la UI" }
                ) {
                    Text("Tócame: $toques", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { vm.tareaLarga() },
                        enabled = !corriendo,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Iniciar tarea con progreso" }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelar() },
                        enabled = corriendo,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Cancelar tarea con progreso" }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                }

                LinearProgressIndicator(
                    progress = { progresoAnimado },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(progresoAnimado * 100).toInt()} %",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------------- TAREA 2: Contar primos ----------------
            TarjetaTarea(
                titulo = "Contar números primos",
                icono = Icons.Default.Info
            ) {
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (hasta != null) {
                                hastaContado = hasta
                                vm.contar(hasta)
                            }
                        },
                        enabled = numeroValido && !calculando,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Iniciar conteo de números primos" }
                    ) {
                        Text("Contar")
                    }
                    OutlinedButton(
                        onClick = { vm.cancelarPrimos() },
                        enabled = calculando,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Cancelar conteo de primos" }
                    ) {
                        Text("Cancelar")
                    }
                }

                AnimatedContent(
                    targetState = primos,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "transicionPrimos"
                ) { estadoPrimos ->
                    when (estadoPrimos) {
                        is PrimosUi.Inicial -> {}
                        is PrimosUi.Calculando -> {
                            val pctAnimado by animateFloatAsState(
                                targetValue = estadoPrimos.progreso,
                                animationSpec = tween(150),
                                label = "pctPrimos"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = { pctAnimado },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = "${(pctAnimado * 100).toInt()} % — Calculando...",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        is PrimosUi.Listo -> {
                            RecuadroExito(
                                titulo = "Primos hasta $hastaContado: ${estadoPrimos.cantidad}",
                                milisegundos = estadoPrimos.milisegundos
                            )
                        }
                        is PrimosUi.Cancelado -> {
                            RecuadroCancelado()
                        }
                    }
                }
            }

            // ---------------- TAREA 3: Dos cargas ----------------
            TarjetaTarea(
                titulo = "Dos cargas",
                icono = Icons.Default.Refresh
            ) {
                val cargando = carga is CargaUi.Cargando

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Simular error", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = simularError,
                        onCheckedChange = { vm.cambiarSimularError(it) },
                        modifier = Modifier.semantics { contentDescription = "Interruptor para simular error en pedidos" }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tiempo máximo de 2 s", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = limiteTiempo,
                        onCheckedChange = { vm.cambiarLimiteTiempo(it) },
                        enabled = !cargando,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.semantics { contentDescription = "Interruptor de tiempo máximo 2 segundos" }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { vm.cargarUnaPorUna() },
                        enabled = !cargando,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Cargar datos una por una" }
                    ) {
                        Text("Una por una")
                    }
                    Button(
                        onClick = { vm.cargarALaVez() },
                        enabled = !cargando,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Cargar datos a la vez en paralelo" }
                    ) {
                        Text("A la vez")
                    }
                }

                AnimatedContent(
                    targetState = carga,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "transicionCarga"
                ) { estadoCarga ->
                    when (estadoCarga) {
                        is CargaUi.Inicial -> {}
                        is CargaUi.Cargando -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = estadoCarga.mensaje,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        is CargaUi.Exito -> {
                            RecuadroExito(
                                titulo = "Modo: ${estadoCarga.modo}",
                                detalle = "Perfil: ${estadoCarga.perfil}\nPedidos: ${estadoCarga.pedidos}",
                                milisegundos = estadoCarga.milisegundos
                            )
                        }
                        is CargaUi.Error -> {
                            RecuadroError(
                                mensaje = estadoCarga.mensaje,
                                onReintentar = { vm.cargarALaVez() }
                            )
                        }
                    }
                }
            }

            // ---------------- TAREA 4: Comparación de tiempos ----------------
            TarjetaTarea(
                titulo = "Comparación de tiempos",
                icono = Icons.Default.Info
            ) {
                TarjetaComparacion(comparacion = comparacion)
            }

            // ---------------- TAREA 5: Últimos resultados (Historial) ----------------
            TarjetaTarea(
                titulo = "Últimos resultados",
                icono = Icons.Default.CheckCircle
            ) {
                if (historial.isEmpty()) {
                    Text(
                        text = "Todavía no hay resultados guardados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        historial.forEach { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "• $item",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { vm.borrarHistorial() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics { contentDescription = "Borrar historial guardado" }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar historial")
                    }
                }
            }
        }
    }
}

// Card genérica con animación de contenido y color de fondo elegante
@Composable
fun TarjetaTarea(
    titulo: String,
    icono: ImageVector,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "tarjetaColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = titulo,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            contenido()
        }
    }
}

// Recuadro verde para mostrar resultados con tiempo en un chip visual
@Composable
fun RecuadroExito(titulo: String, milisegundos: Long, detalle: String = "") {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdeExitoContainer, RoundedCornerShape(12.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VerdeExitoTexto
                )
                Text(
                    text = titulo,
                    color = VerdeExitoTexto,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (detalle.isNotBlank()) {
                Text(
                    text = detalle,
                    color = VerdeExitoTexto,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Chip con el tiempo transcurrido en ms
            Surface(
                color = VerdeExitoTexto.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Text(
                    text = "$milisegundos ms",
                    color = VerdeExitoTexto,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Recuadro rojo para mostrar error con botón Reintentar
@Composable
fun RecuadroError(mensaje: String, onReintentar: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RojoErrorContainer, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RojoErrorTexto
                )
                Text(
                    text = mensaje,
                    color = RojoErrorTexto,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedButton(
                onClick = onReintentar,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { contentDescription = "Reintentar operación fallida" }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reintentar")
            }
        }
    }
}

// Componente para indicar cancelación limpia
@Composable
fun RecuadroCancelado() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Operación cancelada",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Componente visual para la comparación de tiempos con barras proporcionales animadas
@Composable
fun TarjetaComparacion(comparacion: ComparacionTiempos) {
    val t1 = comparacion.tiempoUnaPorUna
    val t2 = comparacion.tiempoALaVez

    if (t1 == null && t2 == null) {
        Text(
            text = "Ejecuta 'Una por una' y 'A la vez' para comparar los tiempos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val maxTime = maxOf(t1 ?: 1L, t2 ?: 1L).toFloat()

    val frac1 by animateFloatAsState(
        targetValue = if (t1 != null) (t1 / maxTime) else 0f,
        animationSpec = tween(500),
        label = "frac1"
    )

    val frac2 by animateFloatAsState(
        targetValue = if (t2 != null) (t2 / maxTime) else 0f,
        animationSpec = tween(500),
        label = "frac2"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Barra Una por una
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Una por una", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(t1?.let { "$it ms" } ?: "Sin ejecutar", style = MaterialTheme.typography.bodySmall)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(frac1.coerceIn(0.05f, 1f))
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }

        // Barra A la vez
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("A la vez", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(t2?.let { "$it ms" } ?: "Sin ejecutar", style = MaterialTheme.typography.bodySmall)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(frac2.coerceIn(0.05f, 1f))
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = comparacion.obtenerDiferencia(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}