# Material para una sesión de 20 minutos

Título sugerido: **Coroutines predecibles: de Jobs manuales a intención de negocio**

Este guion cumple el hito de preparar una aportación para la comunidad Mobile
Android. Antes de publicarlo, elimina cualquier referencia interna y adapta los
ejemplos al canal y formato acordados por el equipo.

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

## Definition of done de la aportación

- Material revisado para no incluir información propietaria.
- Código reducido, ejecutable y con pruebas.
- Grabación, enlace, publicación o acta conservada como evidencia del objetivo.
- Una conclusión o mejora surgida de las preguntas incorporada al documento.

