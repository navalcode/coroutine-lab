package com.example.coroutinelab.domain

import com.example.coroutinelab.model.ErrorStrategy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlingExamplesTest {
    private val examples = ErrorHandlingExamples()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun cancellationIsNotReportedAsABusinessFailure() = runTest {
        val misclassified = mutableListOf<String>()
        for (strategy in listOf(ErrorStrategy.LAUNCH_LOCAL_CATCH, ErrorStrategy.ASYNC_AWAIT)) {
            val events = mutableListOf<String>()
            val task = launch { examples.demonstrate(strategy) { source, message -> events += "$source:$message" } }
            runCurrent()
            task.cancel()
            task.join()
            assertTrue(task.isCancelled)
            if (events.any { "Error capturado dentro" in it || "Error observado al pedir" in it }) {
                misclassified += strategy.name
            }
        }
        assertTrue("Cancellation was classified as a business failure: $misclassified", misclassified.isEmpty())
    }

    @Test
    fun `launch captura el error dentro de su propio bloque`() = runTest {
        val events = mutableListOf<String>()

        examples.demonstrate(ErrorStrategy.LAUNCH_LOCAL_CATCH) { source, message ->
            events += "$source:$message"
        }

        assertTrue(events.any { "Error capturado dentro" in it })
    }

    @Test
    fun `async expone el error cuando se espera el resultado`() = runTest {
        val events = mutableListOf<String>()

        examples.demonstrate(ErrorStrategy.ASYNC_AWAIT) { source, message ->
            events += "$source:$message"
        }

        assertTrue(events.any { it.startsWith("await:Error observado") })
    }

    @Test
    fun `supervisor permite terminar al hermano independiente`() = runTest {
        val events = mutableListOf<String>()

        examples.demonstrate(ErrorStrategy.SUPERVISOR) { source, message ->
            events += "$source:$message"
        }

        assertTrue(events.any { "Fallo aislado" in it })
        assertTrue(events.any { "Horario cargado" in it })
    }
}

