@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.coroutinelab.domain

import com.example.coroutinelab.data.FakeTravelRepository
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredConcurrencyExamplesTest {
    @Test
    fun `el dashboard espera a sus dos hijos`() = runTest {
        val repository = FakeTravelRepository(StandardTestDispatcher(testScheduler))
        val examples = StructuredConcurrencyExamples(repository)
        val events = mutableListOf<String>()

        val result = examples.loadDashboard { source, message -> events += "$source:$message" }

        assertEquals(2, result.stations.size)
        assertEquals(1, result.disruptions.size)
        assertTrue(events.last().startsWith("padre:Los dos hijos"))
    }

    @Test
    fun `cancelar ejecuta finally y no marca el trabajo como completado`() = runTest {
        val repository = FakeTravelRepository(StandardTestDispatcher(testScheduler))
        val examples = StructuredConcurrencyExamples(repository)
        val events = mutableListOf<String>()

        val job = launch {
            examples.cooperativeCancellation { source, message -> events += "$source:$message" }
        }
        advanceTimeBy(600)
        job.cancelAndJoin()

        assertTrue(events.any { it == "finally:Recursos liberados de forma segura" })
        assertFalse(events.any { it == "trabajo:Proceso completado" })
    }
}
