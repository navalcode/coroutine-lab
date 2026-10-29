@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.coroutinelab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coroutinelab.CoroutineLabViewModel
import com.example.coroutinelab.R
import com.example.coroutinelab.model.*

/** Lifecycle-aware state collection; the ViewModel owns the scenario's work. */
@Composable
fun CoroutineLabApp(viewModel: CoroutineLabViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }, actions = {
            if (state.isRunning) TextButton(onClick = viewModel::cancelScenario,
                shape = MaterialTheme.shapes.medium, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.cancel))
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = state.section.ordinal, edgePadding = 12.dp) {
                LabSection.entries.forEach { section ->
                    val label = when (section) {
                        LabSection.GUIDE -> R.string.tab_guide
                        LabSection.STRUCTURED -> R.string.tab_structured
                        LabSection.FLOW -> R.string.tab_flow
                        LabSection.ERRORS -> R.string.tab_errors
                        LabSection.REAL_CASES -> R.string.tab_cases
                    }
                    Tab(selected = section == state.section, onClick = { viewModel.selectSection(section) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = { Text(stringResource(label)) })
                }
            }
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
private fun GuideScreen() {
    val milestones = listOf(R.string.guide_one, R.string.guide_two, R.string.guide_three, R.string.guide_four)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { ConceptSection(stringResource(R.string.guide_title), stringResource(R.string.guide_body)) }
        items(milestones.size) { index ->
            Column {
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
                    Text("${index + 1}", Modifier.width(32.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(milestones[index]), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider()
            }
        }
        item { ConceptSection(stringResource(R.string.guide_use_title), stringResource(R.string.guide_use_body)) }
    }
}

@Composable
private fun StructuredScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::clearTimeline) {
        item { ConceptSection(stringResource(R.string.structured_title), stringResource(R.string.structured_body)) }
        item { CodeBlock("coroutineScope {\n  val stations = async { loadStations() }\n  val alerts = async { loadDisruptions() }\n  Dashboard(stations.await(), alerts.await())\n}") }
        item {
            ActionButton(stringResource(R.string.run_parallel), !state.isRunning, viewModel::runStructuredDashboard)
            Spacer(Modifier.height(8.dp))
            ActionButton(stringResource(R.string.run_cancellable), !state.isRunning, viewModel::runCancellation)
        }
    }
}

@Composable
private fun FlowScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::clearTimeline) {
        item { ConceptSection(stringResource(R.string.flow_title), stringResource(R.string.flow_body)) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FlowStrategy.entries.forEach { strategy ->
                    FilterChip(selected = strategy == state.flowStrategy, onClick = { viewModel.selectFlowStrategy(strategy) },
                        enabled = !state.isRunning, shape = MaterialTheme.shapes.medium, label = { Text(strategy.name.lowercase()) })
                }
            }
        }
        item {
            CodeBlock(when (state.flowStrategy) {
                FlowStrategy.CONCAT -> "queries.flatMapConcat(\n  ::request\n)"
                FlowStrategy.MERGE -> "queries.flatMapMerge(\n  concurrency = 3,\n  transform = ::request\n)"
                FlowStrategy.LATEST -> "queries.flatMapLatest(\n  ::request\n)"
            })
        }
        item { ActionButton(stringResource(R.string.run_operator, state.flowStrategy.label), !state.isRunning, viewModel::runFlow) }
    }
}

@Composable
private fun ErrorScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::clearTimeline) {
        item { ConceptSection(stringResource(R.string.errors_title), stringResource(R.string.errors_body)) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ErrorStrategy.entries.forEach { strategy ->
                    FilterChip(selected = strategy == state.errorStrategy, onClick = { viewModel.selectErrorStrategy(strategy) },
                        enabled = !state.isRunning, shape = MaterialTheme.shapes.medium, label = { Text(errorLabel(strategy)) })
                }
            }
        }
        item { CodeBlock(errorCode(state.errorStrategy)) }
        item { ActionButton(stringResource(R.string.run_operator, errorLabel(state.errorStrategy)), !state.isRunning, viewModel::runErrorExample) }
    }
}

private fun errorCode(strategy: ErrorStrategy): String = when (strategy) {
    ErrorStrategy.LAUNCH_LOCAL_CATCH -> "launch {\n  try { error(\"demo\") }\n  catch (cancelled: CancellationException) {\n    throw cancelled\n  } catch (failure: IllegalStateException) {\n    println(failure.message)\n  }\n}"
    ErrorStrategy.ASYNC_AWAIT -> "supervisorScope {\n  val task = async { error(\"demo\") }\n  try { task.await() }\n  catch (cancelled: CancellationException) {\n    throw cancelled\n  } catch (failure: IllegalStateException) {\n    println(failure.message)\n  }\n}"
    ErrorStrategy.EXCEPTION_HANDLER -> "val handler = CoroutineExceptionHandler {\n  _, failure -> println(failure.message)\n}\nviewModelScope.launch(handler) {\n  error(\"demo\")\n}"
    ErrorStrategy.SUPERVISOR -> "supervisorScope {\n  val fragile = async { error(\"demo\") }\n  val independent = async { \"ok\" }\n  try { fragile.await() }\n  catch (cancelled: CancellationException) {\n    throw cancelled\n  } catch (failure: IllegalStateException) {\n    println(failure.message)\n  }\n  println(independent.await())\n}"
}

@Composable
private fun errorLabel(strategy: ErrorStrategy): String = when (strategy) {
    ErrorStrategy.LAUNCH_LOCAL_CATCH -> stringResource(R.string.local_catch)
    else -> strategy.label
}

@Composable
private fun RealCasesScreen(state: LabUiState, viewModel: CoroutineLabViewModel) {
    LabPage(state, viewModel::clearTimeline) {
        item {
            ConceptSection(stringResource(R.string.search_title), stringResource(R.string.search_body))
            Spacer(Modifier.height(12.dp))
            ActionButton(stringResource(R.string.run_search), !state.isRunning, viewModel::runReactiveSearch)
        }
        item {
            ConceptSection(stringResource(R.string.dashboard_title), stringResource(R.string.dashboard_body))
            Spacer(Modifier.height(12.dp))
            ActionButton(stringResource(R.string.run_dashboard), !state.isRunning, viewModel::runRealDashboard)
        }
    }
}

@Composable
private fun LabPage(state: LabUiState, onClear: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        content()
        item {
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.timeline), Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                if (!state.isRunning && state.events.isNotEmpty()) OutlinedButton(onClick = onClear, shape = MaterialTheme.shapes.medium) { Text(stringResource(R.string.clear)) }
            }
            Text(stringResource(if (state.isRunning) R.string.running else R.string.ready), Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(state.scenarioTitle, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Text(stringResource(R.string.trace_language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.events.isEmpty()) item { Text(stringResource(R.string.empty_trace), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else items(state.events, key = { it.id }) { TimelineRow(it) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent) {
    val kind = when (event.kind) {
        EventKind.INFO -> R.string.event_info
        EventKind.SUCCESS -> R.string.event_success
        EventKind.WARNING -> R.string.event_warning
        EventKind.ERROR -> R.string.event_error
    }
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
            Text("${event.id}", Modifier.width(32.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Column(Modifier.weight(1f)) {
                Text(event.source, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(kind), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(event.message, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ConceptSection(title: String, body: String) {
    Column {
        Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium).padding(16.dp)) {
        Text(code, color = MaterialTheme.colorScheme.onPrimaryContainer, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.primaryContainer, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = MaterialTheme.shapes.medium) { Text(label) }
}
