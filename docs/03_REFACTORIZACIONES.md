# Dos casos de refactorización

Los ejemplos son deliberadamente ficticios y trasladables a cualquier app móvil.

## Caso 1: búsqueda con Jobs manuales

### Antes

```kotlin
var searchJob: Job? = null

fun onQueryChanged(query: String) {
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        delay(300)
        val result = repository.search(query)
        _state.value = result
    }
}
```

Problemas: estado mutable auxiliar, política temporal mezclada con coordinación,
riesgo de olvidar la cancelación y difícil composición con otras fuentes.

### Después

```kotlin
queries
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest(repository::searchJourneys)
```

Cuando `debounce` emite una consulta nueva, `flatMapLatest` cancela la colección
interna anterior. La sustitución ocurre después de la espera de `debounce`, no
inmediatamente con cada pulsación.

## Caso 2: dashboard secuencial

### Antes

```kotlin
val stations = repository.loadStations()       // 600 ms
val disruptions = repository.loadDisruptions() // 850 ms
return Dashboard(stations, disruptions)         // ~1450 ms
```

### Después

```kotlin
return coroutineScope {
    val stations = async { repository.loadStations() }
    val disruptions = async { repository.loadDisruptions() }
    Dashboard(stations.await(), disruptions.await()) // ~850 ms
}
```

La mejora solo es válida porque ambas lecturas son independientes. `coroutineScope`
mantiene el contrato “todo o nada”: si una fuente falla, se cancela la otra y el
llamador recibe el error.

## Antes de adaptar los ejemplos

- ¿Las operaciones son realmente independientes?
- ¿Un resultado parcial tiene valor? Si lo tiene, considera supervisión explícita.
- ¿Quién posee el Job y cuándo se cancela?
- ¿La cancelación llega hasta el cliente HTTP/SDK usado?
- ¿Se relanza `CancellationException` en catches amplios?
- ¿El Flow se colecciona respetando el lifecycle?
- ¿Existe una prueba que demuestre orden, cancelación o aislamiento?
- ¿Se ha medido la mejora en lugar de asumirla?

