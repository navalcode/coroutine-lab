@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.coroutinelab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coroutinelab.CoroutineLabViewModel
import com.example.coroutinelab.model.ErrorStrategy
import com.example.coroutinelab.model.EventKind
import com.example.coroutinelab.model.FlowStrategy
import com.example.coroutinelab.model.LabSection
import com.example.coroutinelab.model.LabUiState
import com.example.coroutinelab.model.TimelineEvent

/**
 * collectAsStateWithLifecycle empieza y detiene la colección según el Lifecycle.
 * La pantalla no mantiene scopes propios ni conserva referencias a la Activity.
 */
@Composable
fun CoroutineLabApp(viewModel: CoroutineLabViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Coroutine Lab", fontWeight = FontWeight.Bold)
                        Text(
                            "Aprender viendo el orden real de ejecución",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            LabNavigation(
                selected = state.section,
                onSelected = viewModel::selectSection,
            )

            when (state.section) {
                LabSection.GUIDE -> GuideScreen()
                LabSection.STRUCTURED -> StructuredScreen(state, viewModel)
                LabSection.FLOW -> FlowScreen(state, viewModel)
                LabSection.ERRORS -> ErrorScreen(state, viewModel)
                LabSection.REAL_CASES -> RealCasesScreen(state, viewModel)
            }
        }
    }
}

@Composable
private fun LabNavigation(
    selected: LabSection,
    onSelected: (LabSection) -> Unit,
) {
    ScrollableTabRow(selectedTabIndex = selected.ordinal, edgePadding = 12.dp) {
        LabSection.entries.forEach { section ->
            Tab(
                selected = selected == section,
                onClick = { onSelected(section) },
                text = { Text(section.shortTitle) },
            )
        }
    }
}

@Composable
private fun GuideScreen() {
    val milestones = listOf(
        "1" to "Concurrencia estructurada y cancelación cooperativa",
        "2" to "flatMapConcat, flatMapMerge y flatMapLatest",
        "3" to "Material compartible para la comunidad Android",
        "4" to "launch, async, await y CoroutineExceptionHandler",
        "5" to "Tres sandboxes ejecutables con pruebas automáticas",
        "6" to "Dos refactorizaciones cercanas a un proyecto de transporte",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Objetivo traducido a competencias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "No se trata de memorizar APIs: al terminar deberías poder predecir " +
                    "quién es padre de quién, qué se cancela, dónde aparece un error " +
                    "y qué operador expresa mejor la intención del producto.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        items(milestones) { (number, text) ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(number, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            ConceptCard(
                title = "Cómo usar el laboratorio",
                body = "Antes de pulsar Ejecutar, predice el orden de la línea temporal. " +
                    "Después compáralo, repite cancelando a mitad y modifica los delays. " +
                    "La guía completa y los ejercicios están en la carpeta docs/.",
            )
        }
    }
}

@Composable
private fun StructuredScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::cancelScenario, viewModel::clearTimeline) {
        item {
            ConceptCard(
                "Regla mental",
                "Un scope posee a sus hijos: espera a que terminen y les propaga la " +
                    "cancelación. coroutineScope falla como una unidad; supervisorScope " +
                    "permite aislar fallos cuando el negocio lo justifica.",
            )
        }
        item {
            CodeBlock(
                "coroutineScope {\n" +
                    "  val stations = async { loadStations() }\n" +
                    "  val alerts = async { loadDisruptions() }\n" +
                    "  Dashboard(stations.await(), alerts.await())\n" +
                    "}",
            )
        }
        item {
            ActionButton(
                "Ejecutar hijos en paralelo",
                enabled = !state.isRunning,
                onClick = viewModel::runStructuredDashboard,
            )
            Spacer(Modifier.height(8.dp))
            ActionButton(
                "Iniciar trabajo cancelable",
                enabled = !state.isRunning,
                onClick = viewModel::runCancellation,
            )
        }
    }
}

@Composable
private fun FlowScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::cancelScenario, viewModel::clearTimeline) {
        item {
            ConceptCard(
                "Una pregunta de negocio, tres respuestas",
                "concat conserva orden y espera; merge prioriza paralelismo; latest " +
                    "prioriza únicamente la intención más reciente. Ejecuta cada uno " +
                    "y compara qué peticiones llegan a producir respuesta.",
            )
        }
        item {
            ChoiceRow {
                FlowStrategy.entries.forEach { strategy ->
                    FilterChip(
                        selected = state.flowStrategy == strategy,
                        onClick = { viewModel.selectFlowStrategy(strategy) },
                        label = { Text(strategy.label) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
        }
        item {
            CodeBlock(
                "queries.flatMapConcat(::request) // secuencial\n" +
                    "queries.flatMapMerge(3, ::request) // paralelo\n" +
                    "queries.flatMapLatest(::request) // cancela lo anterior",
            )
        }
        item {
            ActionButton(
                "Ejecutar ${state.flowStrategy.label}",
                enabled = !state.isRunning,
                onClick = viewModel::runFlow,
            )
        }
    }
}

@Composable
private fun ErrorScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::cancelScenario, viewModel::clearTimeline) {
        item {
            ConceptCard(
                "Resultado frente a ciclo de vida",
                "launch devuelve Job y expresa trabajo; async devuelve Deferred y " +
                    "expresa un resultado futuro. El handler es una última barrera para " +
                    "errores no capturados, no un sustituto de try/catch.",
            )
        }
        item {
            ChoiceRow {
                ErrorStrategy.entries.forEach { strategy ->
                    FilterChip(
                        selected = state.errorStrategy == strategy,
                        onClick = { viewModel.selectErrorStrategy(strategy) },
                        label = { Text(strategy.label) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
        }
        item {
            CodeBlock(
                "launch { try { save() } catch (e: IOException) { … } }\n" +
                    "val fare = async { loadFare() }\n" +
                    "val value = try { fare.await() } catch (e: IOException) { … }",
            )
        }
        item {
            ActionButton(
                "Ejecutar ${state.errorStrategy.label}",
                enabled = !state.isRunning,
                onClick = viewModel::runErrorExample,
            )
        }
    }
}

@Composable
private fun RealCasesScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::cancelScenario, viewModel::clearTimeline) {
        item {
            ConceptCard(
                "Caso 1 · Autocompletado",
                "Una cadena debounce + distinctUntilChanged + flatMapLatest reemplaza " +
                    "la gestión manual de Jobs y evita pintar respuestas antiguas.",
            )
            Spacer(Modifier.height(8.dp))
            ActionButton(
                "Simular escritura rápida",
                enabled = !state.isRunning,
                onClick = viewModel::runReactiveSearch,
            )
        }
        item {
            ConceptCard(
                "Caso 2 · Dashboard",
                "Dos fuentes independientes se cargan con async dentro de " +
                    "coroutineScope. Se reduce la latencia sin perder propiedad, " +
                    "cancelación ni propagación de errores.",
            )
            Spacer(Modifier.height(8.dp))
            ActionButton(
                "Comparar carga secuencial/paralela",
                enabled = !state.isRunning,
                onClick = viewModel::runRealDashboard,
            )
        }
    }
}

/**
 * LazyColumn aloja explicación, controles y línea temporal en un solo scroll.
 * El bloque [content] permite que cada laboratorio inyecte sus propias tarjetas.
 */
@Composable
private fun LabPage(
    state: LabUiState,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            TimelineHeader(state, onCancel, onClear)
        }
        if (state.events.isEmpty()) {
            item {
                Text(
                    "La línea temporal aparecerá aquí.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.events, key = { it.id }) { event -> TimelineRow(event) }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun TimelineHeader(state: LabUiState, onCancel: () -> Unit, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Línea temporal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(state.scenarioTitle, style = MaterialTheme.typography.bodySmall)
        }
        if (state.isRunning) {
            CircularProgressIndicator(modifier = Modifier.width(28.dp))
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
        } else if (state.events.isNotEmpty()) {
            OutlinedButton(onClick = onClear) { Text("Limpiar") }
        }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent) {
    val accent = when (event.kind) {
        EventKind.INFO -> MaterialTheme.colorScheme.primary
        EventKind.SUCCESS -> MaterialTheme.colorScheme.secondary
        EventKind.WARNING -> Color(0xFF9A6700)
        EventKind.ERROR -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.09f)),
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) {
                    append("#${event.id}  ${event.source}")
                }
                append("\n${event.message}")
            },
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ConceptCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF17212B), MaterialTheme.shapes.medium)
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(code, color = Color(0xFFD6E4F0), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}

@Composable
private fun ChoiceRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        content = { content() },
    )
}
