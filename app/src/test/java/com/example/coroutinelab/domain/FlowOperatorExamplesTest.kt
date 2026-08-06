package com.example.coroutinelab.domain

import com.example.coroutinelab.model.FlowStrategy
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowOperatorExamplesTest {
    private val examples = FlowOperatorExamples()

    @Test
    fun `concat espera cada respuesta y conserva el orden`() = runTest {
        val events = examples.demonstrate(FlowStrategy.CONCAT).toList()

        assertEquals(
            listOf(
                "MAD · petición iniciada",
                "MAD · respuesta recibida",
                "MAD-B · petición iniciada",
                "MAD-B · respuesta recibida",
                "MAD-BCN · petición iniciada",
                "MAD-BCN · respuesta recibida",
            ),
            events,
        )
    }

    @Test
    fun `merge permite que las tres peticiones terminen`() = runTest {
        val events = examples.demonstrate(FlowStrategy.MERGE).toList()

        assertEquals(3, events.count { "iniciada" in it })
        assertEquals(3, events.count { "recibida" in it })
    }

    @Test
    fun `latest solo deja responder a la consulta mas reciente`() = runTest {
        val events = examples.demonstrate(FlowStrategy.LATEST).toList()

        assertEquals(3, events.count { "iniciada" in it })
        assertEquals(listOf("MAD-BCN · respuesta recibida"), events.filter { "recibida" in it })
    }
}

