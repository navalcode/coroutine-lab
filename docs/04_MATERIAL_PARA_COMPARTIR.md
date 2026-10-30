# Material para una sesión de 20 minutos

Título sugerido: **Coroutines: orden de ejecución, cancelación y errores**

Guion propuesto para explicar los experimentos del repositorio en una sesión de veinte minutos. Selecciona los ejemplos según el público y deja tiempo para comparar sus predicciones con los eventos. No registra una sesión ya impartida.

## Agenda

1. **2 min — El problema:** el código asíncrono funciona, pero no sabemos quién
   lo cancela ni qué ocurre cuando una pieza falla.
2. **5 min — El árbol:** dibujar `viewModelScope → launch → coroutineScope → async`.
3. **5 min — Demo Flow:** ejecutar concat, merge y latest; pedir al público que
   prediga qué respuestas aparecerán.
4. **4 min — Errores:** comparar `launch`, `async/await`, handler y supervisor.
5. **3 min — Refactor:** enseñar búsqueda manual frente a `flatMapLatest`.
6. **1 min — Cierre:** checklist y enlace al repositorio o fragmentos aprobados.

## Tres ideas que deben quedar

- Cada coroutine debe tener un propietario y una vida útil justificable.
- El operador de Flow se elige por semántica de producto: orden, todo en paralelo
  o solo el último.
- La supervisión es una decisión de negocio sobre resultados parciales, no una
  forma de ocultar excepciones.

## Preguntas para activar la conversación

- Si falla la carga de incidencias, ¿el dashboard sin incidencias sigue siendo útil?
- ¿Cuántas búsquedas deberían continuar cuando el usuario ya escribió otra letra?
- ¿Qué sucede si un `catch (Throwable)` consume una cancelación?
- ¿Quién cancela una coroutine iniciada desde esta pantalla?

## Preparar y recoger la sesión

- Elegir dos experimentos y ejecutarlos antes de la sesión.
- Código reducido, ejecutable y con pruebas.
- Si se imparte, anotar fecha, formato y preguntas que merezca la pena incorporar.
- Una conclusión o mejora surgida de las preguntas incorporada al documento.

