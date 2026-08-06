package com.example.coroutinelab.model

/** Las cinco áreas visibles de la aplicación. */
enum class LabSection(val title: String, val shortTitle: String) {
    GUIDE("Mapa del objetivo", "Objetivo"),
    STRUCTURED("Concurrencia estructurada", "Estructura"),
    FLOW("Operadores de Flow", "Flow"),
    ERRORS("launch, async y errores", "Errores"),
    REAL_CASES("Casos de viaje", "Casos"),
}

/** Operador que se aplicará al Flow exterior de consultas. */
enum class FlowStrategy(val label: String) {
    CONCAT("flatMapConcat"),
    MERGE("flatMapMerge"),
    LATEST("flatMapLatest"),
}

/** Escenarios que permiten comparar las reglas de propagación de errores. */
enum class ErrorStrategy(val label: String) {
    LAUNCH_LOCAL_CATCH("launch + try/catch interno"),
    ASYNC_AWAIT("async + await"),
    EXCEPTION_HANDLER("CoroutineExceptionHandler"),
    SUPERVISOR("supervisorScope"),
}

enum class EventKind {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
}

/**
 * Una entrada de la línea temporal. El contador [id] hace visible el orden real
 * de los eventos incluso cuando dos coroutines trabajan en paralelo.
 */
data class TimelineEvent(
    val id: Long,
    val source: String,
    val message: String,
    val kind: EventKind = EventKind.INFO,
)

/** Estado inmutable que Compose observa y pinta en pantalla. */
data class LabUiState(
    val section: LabSection = LabSection.GUIDE,
    val flowStrategy: FlowStrategy = FlowStrategy.CONCAT,
    val errorStrategy: ErrorStrategy = ErrorStrategy.LAUNCH_LOCAL_CATCH,
    val isRunning: Boolean = false,
    val scenarioTitle: String = "Ningún escenario en ejecución",
    val events: List<TimelineEvent> = emptyList(),
)

data class Station(val code: String, val name: String)

data class Disruption(val line: String, val message: String)

data class Journey(val origin: String, val destination: String, val durationMinutes: Int)

data class TravelDashboard(
    val stations: List<Station>,
    val disruptions: List<Disruption>,
)

/** Estados intermedios de una búsqueda reactiva. */
sealed interface SearchUpdate {
    data class Loading(val query: String) : SearchUpdate
    data class Results(val query: String, val journeys: List<Journey>) : SearchUpdate
}

