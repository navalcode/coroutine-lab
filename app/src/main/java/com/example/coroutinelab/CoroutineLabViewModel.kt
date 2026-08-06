package com.example.coroutinelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinelab.data.FakeTravelRepository
import com.example.coroutinelab.domain.ErrorHandlingExamples
import com.example.coroutinelab.domain.FlowOperatorExamples
import com.example.coroutinelab.domain.StructuredConcurrencyExamples
import com.example.coroutinelab.domain.TravelUseCases
import com.example.coroutinelab.model.ErrorStrategy
import com.example.coroutinelab.model.EventKind
import com.example.coroutinelab.model.FlowStrategy
import com.example.coroutinelab.model.LabSection
import com.example.coroutinelab.model.LabUiState
import com.example.coroutinelab.model.SearchUpdate
import com.example.coroutinelab.model.TimelineEvent
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * El ViewModel es propietario del trabajo de pantalla. viewModelScope aporta un
 * SupervisorJob y cancela todos sus hijos automáticamente al destruirse.
 */
class CoroutineLabViewModel : ViewModel() {
    private val repository = FakeTravelRepository()
    private val structuredExamples = StructuredConcurrencyExamples(repository)
    private val flowExamples = FlowOperatorExamples()
    private val errorExamples = ErrorHandlingExamples()
    private val travelUseCases = TravelUseCases(repository)

    private val _uiState = MutableStateFlow(LabUiState())
    val uiState: StateFlow<LabUiState> = _uiState.asStateFlow()

    private val eventIds = AtomicLong(0)
    private var activeJob: Job? = null

    fun selectSection(section: LabSection) {
        _uiState.update { it.copy(section = section) }
    }

    fun selectFlowStrategy(strategy: FlowStrategy) {
        _uiState.update { it.copy(flowStrategy = strategy) }
    }

    fun selectErrorStrategy(strategy: ErrorStrategy) {
        _uiState.update { it.copy(errorStrategy = strategy) }
    }

    fun runStructuredDashboard() = startScenario("Dos hijos en coroutineScope") {
        val dashboard = structuredExamples.loadDashboard(::append)
        append(
            "resultado",
            "Dashboard listo: ${dashboard.stations.size} estaciones y " +
                "${dashboard.disruptions.size} incidencia",
            EventKind.SUCCESS,
        )
    }

    fun runCancellation() = startScenario("Cancelación cooperativa") {
        append("consejo", "Pulsa Cancelar antes de que lleguen los 20 bloques")
        structuredExamples.cooperativeCancellation(::append)
    }

    fun runFlow() {
        val strategy = _uiState.value.flowStrategy
        startScenario(strategy.label) {
            append("Flow", "El Flow es frío: empieza al llamar a collect")
            flowExamples.demonstrate(strategy).collect { message ->
                append(strategy.label, message)
            }
        }
    }

    fun runErrorExample() {
        val strategy = _uiState.value.errorStrategy
        if (strategy == ErrorStrategy.EXCEPTION_HANDLER) {
            runExceptionHandler()
            return
        }
        startScenario(strategy.label) {
            errorExamples.demonstrate(strategy, ::append)
        }
    }

    /**
     * El handler solo recibe excepciones no capturadas de una coroutine raíz.
     * Un launch directo en viewModelScope es raíz respecto a su SupervisorJob.
     */
    private fun runExceptionHandler() {
        val handler = CoroutineExceptionHandler { _, error ->
            append("handler", "Excepción final observada: ${error.message}", EventKind.ERROR)
        }
        startScenario("CoroutineExceptionHandler", handler) {
            append("launch raíz", "El error quedará sin capturar dentro del bloque")
            delay(250)
            error("Fallo no recuperable simulado")
        }
    }

    fun runReactiveSearch() = startScenario("Búsqueda reactiva refactorizada") {
        travelUseCases.reactiveSearch(travelUseCases.simulatedTyping()).collect { update ->
            when (update) {
                is SearchUpdate.Loading -> append(
                    "búsqueda",
                    "Cargando «${update.query}»",
                    EventKind.WARNING,
                )
                is SearchUpdate.Results -> append(
                    "búsqueda",
                    "${update.query}: ${update.journeys.size} resultado; " +
                        "destino ${update.journeys.first().destination}",
                    EventKind.SUCCESS,
                )
            }
        }
    }

    fun runRealDashboard() = startScenario("Dashboard de viaje refactorizado") {
        append("antes", "Secuencial serían ~1450 ms (600 + 850)", EventKind.WARNING)
        val startNanos = System.nanoTime()
        structuredExamples.loadDashboard(::append)
        val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000
        append(
            "después",
            "En paralelo tarda ~$elapsedMs ms: domina la operación más lenta",
            EventKind.SUCCESS,
        )
    }

    fun cancelScenario() {
        activeJob?.cancel(CancellationException("Cancelación solicitada por el usuario"))
    }

    fun clearTimeline() {
        _uiState.update { it.copy(events = emptyList()) }
    }

    private fun startScenario(
        title: String,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        // La UI deshabilita los botones, pero esta guarda evita dobles lanzamientos.
        if (_uiState.value.isRunning) return

        _uiState.update {
            it.copy(isRunning = true, scenarioTitle = title, events = emptyList())
        }

        val job = viewModelScope.launch(context = context, block = block)
        activeJob = job
        job.invokeOnCompletion { cause ->
            when (cause) {
                null -> append("Job", "Escenario terminado", EventKind.SUCCESS)
                is CancellationException -> append("Job", "Escenario cancelado", EventKind.WARNING)
                else -> append("Job", "Escenario falló: ${cause.message}", EventKind.ERROR)
            }
            // Solo el Job vigente puede cambiar el indicador de ejecución.
            if (activeJob === job) {
                activeJob = null
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    private fun append(
        source: String,
        message: String,
        kind: EventKind = EventKind.INFO,
    ) {
        val event = TimelineEvent(eventIds.incrementAndGet(), source, message, kind)
        _uiState.update { state -> state.copy(events = state.events + event) }
    }
}

