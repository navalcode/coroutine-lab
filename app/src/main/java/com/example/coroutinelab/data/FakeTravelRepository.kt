package com.example.coroutinelab.data

import com.example.coroutinelab.model.Disruption
import com.example.coroutinelab.model.Journey
import com.example.coroutinelab.model.SearchUpdate
import com.example.coroutinelab.model.Station
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Sustituto determinista de una API real.
 *
 * Inyectamos el dispatcher para que producción use IO y los tests puedan usar
 * un dispatcher virtual. Esta pequeña decisión elimina esperas reales en tests.
 */
class FakeTravelRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun loadStations(): List<Station> = withContext(ioDispatcher) {
        delay(600) // Simula latencia de red sin bloquear el hilo.
        listOf(
            Station("MAD", "Madrid Puerta de Atocha"),
            Station("BCN", "Barcelona Sants"),
        )
    }

    suspend fun loadDisruptions(): List<Disruption> = withContext(ioDispatcher) {
        delay(850)
        listOf(Disruption("AVE 0312", "Circulación normal"))
    }

    /**
     * Es un Flow frío: no hace trabajo hasta que alguien lo colecciona.
     * flowOn mueve solamente el productor al dispatcher de IO.
     */
    fun searchJourneys(query: String): Flow<SearchUpdate> = flow {
        emit(SearchUpdate.Loading(query))
        delay(450)
        emit(
            SearchUpdate.Results(
                query = query,
                journeys = listOf(Journey(query, "BCN", durationMinutes = 165)),
            ),
        )
    }.flowOn(ioDispatcher)
}

