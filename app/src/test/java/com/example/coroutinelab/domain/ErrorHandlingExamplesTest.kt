package com.example.coroutinelab.domain

import com.example.coroutinelab.model.ErrorStrategy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlingExamplesTest {
    private val examples = ErrorHandlingExamples()

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

