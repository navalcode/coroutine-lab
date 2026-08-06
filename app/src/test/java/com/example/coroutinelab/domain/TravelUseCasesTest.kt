package com.example.coroutinelab.domain

import com.example.coroutinelab.data.FakeTravelRepository
import com.example.coroutinelab.model.SearchUpdate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TravelUseCasesTest {
    @Test
    fun `la busqueda reactiva descarta el resultado que queda obsoleto`() = runTest {
        val repository = FakeTravelRepository(StandardTestDispatcher(testScheduler))
        val useCases = TravelUseCases(repository)

        val updates = useCases.reactiveSearch(useCases.simulatedTyping()).toList()
        val completedQueries = updates
            .filterIsInstance<SearchUpdate.Results>()
            .map { it.query }

        assertEquals(listOf("MAD-BCN"), completedQueries)
    }
}

