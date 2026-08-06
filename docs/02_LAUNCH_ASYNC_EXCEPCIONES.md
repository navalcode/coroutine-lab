# Conclusiones: launch, async y excepciones

## `launch`

- Expresa trabajo cuyo resultado útil es su finalización; devuelve un `Job`.
- Una excepción no capturada se propaga inmediatamente al padre.
- Un `try/catch` alrededor de la llamada a `launch` no captura un fallo que ocurre
  después dentro de la coroutine. El bloque debe capturarlo o dejarlo propagar.

```kotlin
viewModelScope.launch {
    try {
        repository.save()
    } catch (error: IOException) {
        showRecoverableError(error)
    }
}
```

## `async`

- Expresa un valor futuro; devuelve `Deferred<T>`.
- `await()` obtiene el valor y vuelve a lanzar el fallo del `Deferred`.
- En concurrencia estructurada el hijo sigue perteneciendo al scope: no llamar a
  `await` no convierte mágicamente el fallo en inocuo.
- No uses `async` si nunca necesitas un resultado.

```kotlin
supervisorScope {
    val fare = async { repository.loadFare() }
    try {
        render(fare.await())
    } catch (error: IOException) {
        renderUnavailableFare()
    }
}
```

## `CoroutineExceptionHandler`

- Observa excepciones **no capturadas** en coroutines raíz.
- No recupera una coroutine: cuando recibe el error, esa coroutine ya terminó.
- No sustituye el manejo de errores de negocio.
- Un handler instalado en un hijo normal no intercepta la excepción que debe
  propagarse al padre. En la app se instala en un `launch` raíz de `viewModelScope`.

## `coroutineScope` y `supervisorScope`

Usa `coroutineScope` cuando todas las piezas forman una única operación: si una
falla, el resultado global no es válido. Usa `supervisorScope` si una pieza puede
fallar y aun así existe un resultado parcial útil. Supervisar exige observar de
forma explícita el resultado o error de cada hijo.

## Errores frecuentes

1. Crear `CoroutineScope(...)` ad hoc y olvidarse de cancelarlo.
2. Capturar `Throwable` sin relanzar `CancellationException`.
3. Usar `CoroutineExceptionHandler` esperando reintentar desde él.
4. Envolver `launch { ... }` en un `try/catch` externo.
5. Usar `async` como una variante estética de `launch`.
6. Lanzar trabajo de datos desde un composable sin ligarlo a `LaunchedEffect` o
   a un propietario como el ViewModel.

