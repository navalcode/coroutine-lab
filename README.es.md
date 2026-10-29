[English](README.md) · **Español** · [![CI](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml/badge.svg)](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml)

# Coroutine Lab

Proyecto Android didáctico para adquirir y aplicar conocimientos avanzados de
Kotlin Coroutines y Flow con Jetpack Compose. Los ejemplos usan un dominio de
transporte ficticio, sin código ni datos propietarios.

La idea central es sencilla: **predecir, ejecutar y explicar**. Cada laboratorio
muestra una línea temporal para que el orden, el paralelismo, la cancelación y
los errores dejen de ser comportamientos invisibles.

## Qué cubre

| Hito del objetivo | Evidencia en el proyecto |
|---|---|
| Structured Concurrency y cancelación | Pestaña `Estructura`, `StructuredConcurrencyExamples` y sus tests |
| `flatMapConcat`, `flatMapMerge`, `flatMapLatest` | Pestaña `Flow` y tres tests comparativos |
| Compartir conocimiento con Mobile Android | [Material para compartir](docs/04_MATERIAL_PARA_COMPARTIR.md) listo para adaptar |
| `launch`, `async` y manejo de errores | Pestaña `Errores` y [documento de conclusiones](docs/02_LAUNCH_ASYNC_EXCEPCIONES.md) |
| Al menos tres ejemplos prácticos | Laboratorios de estructura, Flow y errores; todos ejecutables |
| Refactorizar 1–2 casos reales | Autocompletado reactivo y dashboard paralelo en [refactorizaciones](docs/03_REFACTORIZACIONES.md) |

## Ejecutar

1. Abrir esta carpeta con Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo con Android 8 (API 26) o superior.
4. Ejecutar la configuración `app`.

Desde PowerShell también se puede validar sin dispositivo:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

## Ruta de aprendizaje recomendada

1. Lee [la guía y el mapa del objetivo](docs/01_GUIA_DIDACTICA.md).
2. Ejecuta `Estructura` y cancela el segundo escenario a mitad.
3. Predice y compara los tres operadores de la pestaña `Flow`.
4. Ejecuta los cuatro escenarios de errores y lee sus conclusiones.
5. Revisa los dos casos antes/después y resuelve los ejercicios.
6. Ejecuta `./gradlew test` y modifica delays o fallos para comprobar tus hipótesis.
7. Adapta el material para una sesión breve con la comunidad Android.

## Arquitectura

```text
Compose UI ──eventos──> ViewModel ──suspend/Flow──> ejemplos de dominio
    ^                       |                            |
    └── StateFlow UI ───────┘                      repositorio fake
                                                      |
                                              dispatcher inyectable
```

- Compose colecciona estado con `collectAsStateWithLifecycle`.
- `viewModelScope` es propietario de los Jobs iniciados desde la pantalla.
- El dominio no conoce Android y se prueba con tiempo virtual.
- El repositorio inyecta su dispatcher; no fija un dispatcher en los tests.

## Referencias oficiales

- [Guía de Coroutines de Kotlin](https://kotlinlang.org/docs/coroutines-guide.html)
- [Flow en Kotlin](https://kotlinlang.org/docs/coroutines-flow.html)
- [Buenas prácticas de Coroutines en Android](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Coroutines conscientes del ciclo de vida](https://developer.android.com/topic/libraries/architecture/coroutines)
- [Estado y Flow en Jetpack Compose](https://developer.android.com/develop/ui/compose/state)

## Verificación del escaparate

Comprobado el 29 de octubre de 2026: nueve tests JVM, lint sin errores (un aviso) y APK debug. No se afirma una suite UI en emulador o release optimizada.

El wrapper comprueba SHA-256 de Gradle 8.13. La CI se comprueba por separado; las evidencias anteriores son locales. Se conservan las licencias existentes.
