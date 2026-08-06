@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)

package com.example.coroutinelab.domain

import com.example.coroutinelab.data.FakeTravelRepository
import com.example.coroutinelab.model.ErrorStrategy
import com.example.coroutinelab.model.FlowStrategy
import com.example.coroutinelab.model.SearchUpdate
import com.example.coroutinelab.model.TravelDashboard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

typealias EventSink = (source: String, message: String) -> Unit

/** Ejemplos puros de jerarquía padre-hijo y cancelación cooperativa. */
class StructuredConcurrencyExamples(
    private val repository: FakeTravelRepository,
) {
    /**
     * coroutineScope no finaliza hasta que todos sus hijos han terminado.
     * Si un hijo falla, cancela al hermano y propaga el error al llamador.
     */
    suspend fun loadDashboard(onEvent: EventSink): TravelDashboard = coroutineScope {
        onEvent("padre", "Crea dos tareas hijas dentro de coroutineScope")

        val stations = async {
            onEvent("estaciones", "Inicio en paralelo")
            repository.loadStations().also {
                onEvent("estaciones", "Fin: ${it.size} estaciones")
            }
        }
        val disruptions = async {
            onEvent("incidencias", "Inicio en paralelo")
            repository.loadDisruptions().also {
                onEvent("incidencias", "Fin: ${it.size} incidencia")
            }
        }

        // await obtiene cada resultado. El scope conserva la propiedad de ambos hijos.
        TravelDashboard(stations.await(), disruptions.await()).also {
            onEvent("padre", "Los dos hijos terminaron; el padre ya puede devolver")
        }
    }

    /**
     * Las suspending functions como delay ya comprueban la cancelación. En un
     * cálculo CPU-bound también habría que consultar ensureActive periódicamente.
     */
    suspend fun cooperativeCancellation(onEvent: EventSink) {
        try {
            repeat(20) { step ->
                currentCoroutineContext().ensureActive()
                onEvent("trabajo", "Procesando bloque ${step + 1}/20")
                delay(250)
            }
            onEvent("trabajo", "Proceso completado")
        } finally {
            // Una limpieza corta y necesaria puede terminar aunque el Job esté cancelado.
            withContext(NonCancellable) {
                onEvent("finally", "Recursos liberados de forma segura")
            }
        }
    }
}

/** Laboratorio que hace visible la semántica de los tres operadores flatMap. */
class FlowOperatorExamples {
    fun demonstrate(strategy: FlowStrategy): Flow<String> {
        val queries = flow {
            emit("MAD")
            delay(100)
            emit("MAD-B")
            delay(100)
            emit("MAD-BCN")
        }

        val transform: suspend (String) -> Flow<String> = { query -> simulatedRequest(query) }

        return when (strategy) {
            // Espera a que termine cada Flow interno antes de iniciar el siguiente.
            FlowStrategy.CONCAT -> queries.flatMapConcat(transform)
            // Colecciona varios Flows internos simultáneamente (máximo 3 aquí).
            FlowStrategy.MERGE -> queries.flatMapMerge(concurrency = 3, transform = transform)
            // Cancela el Flow interno anterior cuando llega una consulta más nueva.
            FlowStrategy.LATEST -> queries.flatMapLatest(transform)
        }
    }

    private fun simulatedRequest(query: String): Flow<String> = flow {
        emit("$query · petición iniciada")
        delay(350)
        emit("$query · respuesta recibida")
    }
}

/** Comparación ejecutable entre launch, async/await y supervisión. */
class ErrorHandlingExamples {
    suspend fun demonstrate(strategy: ErrorStrategy, onEvent: EventSink) {
        when (strategy) {
            ErrorStrategy.LAUNCH_LOCAL_CATCH -> launchCaughtLocally(onEvent)
            ErrorStrategy.ASYNC_AWAIT -> asyncObservedAtAwait(onEvent)
            ErrorStrategy.SUPERVISOR -> supervisorKeepsSiblingAlive(onEvent)
            ErrorStrategy.EXCEPTION_HANDLER -> error(
                "CoroutineExceptionHandler se ejecuta como Job raíz desde el ViewModel",
            )
        }
    }

    private suspend fun launchCaughtLocally(onEvent: EventSink) = coroutineScope {
        launch {
            try {
                onEvent("launch", "Inicia una tarea sin valor de retorno")
                delay(200)
                error("HTTP 503 simulado")
            } catch (error: IllegalStateException) {
                // El try/catch debe estar dentro de launch para capturar su excepción.
                onEvent("launch", "Error capturado dentro: ${error.message}")
            }
        }
    }

    private suspend fun asyncObservedAtAwait(onEvent: EventSink) = supervisorScope {
        val result: Deferred<String> = async {
            onEvent("async", "Calcula un valor que llegará mediante Deferred")
            delay(200)
            error("Tarifa no disponible")
        }

        try {
            result.await() // await suspende y vuelve a lanzar el fallo del Deferred.
        } catch (error: IllegalStateException) {
            onEvent("await", "Error observado al pedir el resultado: ${error.message}")
        }
    }

    private suspend fun supervisorKeepsSiblingAlive(onEvent: EventSink) = supervisorScope {
        val fragile = async {
            delay(150)
            error("Proveedor de tarifas caído")
        }
        val independent = async {
            delay(350)
            "Horario cargado"
        }

        observeFailure(fragile, onEvent)
        onEvent("supervisor", independent.await())
        onEvent("supervisor", "El fallo de un hijo no canceló al hermano")
    }

    private suspend fun observeFailure(task: Deferred<*>, onEvent: EventSink) {
        try {
            task.await()
        } catch (cancelled: CancellationException) {
            // Nunca convertir cancelación en un error de negocio: se debe propagar.
            throw cancelled
        } catch (error: Throwable) {
            onEvent("supervisor", "Fallo aislado y observado: ${error.message}")
        }
    }
}

/** Dos refactorizaciones inspiradas en una aplicación de transporte. */
class TravelUseCases(
    private val repository: FakeTravelRepository,
) {
    /**
     * Sustituye Jobs manuales: debounce reduce llamadas, distinctUntilChanged
     * evita duplicados y flatMapLatest cancela búsquedas que ya quedaron obsoletas.
     */
    fun reactiveSearch(typedQueries: Flow<String>): Flow<SearchUpdate> = typedQueries
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest(repository::searchJourneys)

    fun simulatedTyping(): Flow<String> = flow {
        emit("M")
        delay(80)
        emit("MA")
        delay(80)
        emit("MAD")
        delay(400) // MAD supera debounce y empieza su petición.
        emit("MAD-")
        delay(80)
        emit("MAD-B")
        delay(80)
        emit("MAD-BCN") // Esta consulta cancela la petición anterior.
    }
}
