# Ejercicios propuestos

No mires la solución antes de escribir una predicción.

## Nivel 1 — Observar

1. Ejecuta `flatMapMerge`. ¿Por qué aparecen tres inicios antes del primer fin?
2. Ejecuta `flatMapLatest`. ¿Por qué hay tres inicios y una sola respuesta?
3. Cancela el escenario de trabajo. ¿Por qué aparece el evento de `finally`?

## Nivel 2 — Modificar

1. Cambia la concurrencia de `flatMapMerge` de 3 a 1. Compara con concat y
   explica qué propiedades comparten y cuáles no garantiza la API en general.
2. Sustituye `ensureActive()` por un cálculo CPU-bound que no suspenda. Comprueba
   que la cancelación tarda; restáurala después.
3. Haz fallar `loadDisruptions`. Observa qué le ocurre a `loadStations`.
4. Cambia el dashboard a `supervisorScope` y define un modelo de resultado parcial.

## Nivel 3 — Transferir

1. Localiza en un proyecto autorizado un Job manual asociado a una búsqueda.
2. Dibuja su propietario actual y su política de cancelación.
3. Escribe primero un test de comportamiento.
4. Propón una versión con Flow y justifica cada operador.
5. Mide llamadas evitadas, tiempo o estados obsoletos antes y después.

## Reto de revisión

Analiza por qué este fragmento es peligroso:

```kotlin
try {
    viewModelScope.launch { repository.refresh() }
} catch (error: Throwable) {
    showError(error)
}
```

Pistas: ¿cuándo termina el `try`?, ¿cuándo falla el `launch`?, ¿qué pasa con una
`CancellationException`?, ¿qué componente conoce el error de negocio?
