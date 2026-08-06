# Guía didáctica y mapa del objetivo

## 1. Qué significa realmente el objetivo

“Adquirir y aplicar conocimientos avanzados de Coroutines y Flow” combina tres
niveles distintos:

1. **Comprender:** ser capaz de predecir el árbol de Jobs, la cancelación y la
   propagación de una excepción antes de ejecutar el código.
2. **Aplicar:** elegir una abstracción por intención de negocio, no porque sea la
   API más familiar.
3. **Transferir:** refactorizar código de producción y explicar la decisión a
   otra persona con ejemplos y pruebas.

La calidad y robustez mejoran cuando cada tarea tiene un propietario, los fallos
siguen reglas visibles y las respuestas obsoletas no alcanzan la UI. La eficiencia
mejora al paralelizar únicamente trabajo independiente y al cancelar trabajo que
ya no aporta valor.

## 2. Modelo mental de concurrencia estructurada

Un `CoroutineScope` no es solo un sitio desde el que llamar a `launch`. Define un
árbol de responsabilidad:

```text
viewModelScope (SupervisorJob)
└── launch del escenario
    └── coroutineScope de dashboard
        ├── async de estaciones
        └── async de incidencias
```

- El padre espera a sus hijos.
- Cancelar el padre cancela a todos sus descendientes.
- En `coroutineScope`, el fallo de un hijo cancela a sus hermanos.
- En `supervisorScope`, los fallos de los hijos se aíslan; aun así deben observarse.
- La cancelación es cooperativa. `delay`, `yield` y muchas APIs suspendibles la
  comprueban; un bucle intensivo debe llamar a `ensureActive`.
- `CancellationException` es una señal de control. Si se captura, se relanza.

## 3. Cómo elegir un operador flatMap

Haz primero la pregunta de producto:

| Necesidad | Operador | Ejemplo |
|---|---|---|
| Debe respetarse el orden y no solaparse | `flatMapConcat` | Cola de escrituras |
| Todo debe terminar y puede ir en paralelo | `flatMapMerge` | Precarga de varias fichas |
| Solo importa la intención más reciente | `flatMapLatest` | Búsqueda mientras se escribe |

`flatMapMerge` necesita además un límite de concurrencia razonado. Más concurrencia
no siempre significa más rapidez: puede agotar sockets, memoria o cuotas del API.

## 4. Cancelación frente a fallo

Una cancelación esperada no debe mostrarse como un error. El bloque `finally` es
el lugar para liberar recursos. Solo una limpieza breve e imprescindible debería
usar `withContext(NonCancellable)`.

Un fallo de negocio recuperable se transforma cerca de la capa que lo entiende.
Un fallo inesperado se deja propagar hasta una frontera que pueda registrarlo y
restaurar un estado coherente.

## 5. Secuencia práctica

Para cada pestaña:

1. Lee la regla mental.
2. Predice las entradas y el orden de la línea temporal.
3. Ejecuta una vez sin cancelar.
4. Ejecuta otra vez y cancela.
5. Abre la implementación señalada en el README.
6. Cambia un `delay`, introduce un fallo y vuelve a ejecutar los tests.
7. Explica en voz alta por qué el resultado es correcto.

## 6. Criterio de finalización de los hitos

- Puedes dibujar el árbol de Jobs de los dos casos reales.
- Explicas por qué `GlobalScope` no aparece en el proyecto.
- Predices qué respuestas produce cada `flatMap*`.
- Sabes dónde colocar el `try/catch` para `launch` y para `await`.
- Distingues `coroutineScope` de `supervisorScope` mediante una regla de negocio.
- Los tests pasan y puedes defender qué comportamiento verifica cada uno.
- Has compartido o presentado el material de `04_MATERIAL_PARA_COMPARTIR.md`.

