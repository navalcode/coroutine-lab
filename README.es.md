[English](README.md) · **Español** · [![CI](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml/badge.svg)](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml)

# Coroutine Lab

Proyecto Android didáctico para adquirir y aplicar conocimientos avanzados de
Kotlin Coroutines y Flow con Jetpack Compose. Los ejemplos usan un dominio de
transporte ficticio, sin código ni datos propietarios.

La idea central es sencilla: **predecir, ejecutar y explicar**. Cada laboratorio
muestra una línea temporal para que el orden, el paralelismo, la cancelación y
los errores dejen de ser comportamientos invisibles.

## Usar la pantalla

<img src="docs/images/screen-es.png" alt="Coroutine Lab ejecutando el operador latest en un emulador Android" width="300">

Elige un escenario, predice el orden, ejecútalo y lee sus eventos reales. La estrategia de Flow/errores queda fija mientras trabaja. **Cancelar sigue en la barra al cambiar a Guía**; Limpiar está disponible cuando termina el trabajo. La recreación conserva al ViewModel propietario. El ejemplo del manejador raíz registra el error sin cerrar la actividad.

Controles e instrucciones tienen recursos españoles con fallback inglés. Los títulos originales de escenarios, mensajes de traza y guías didácticas conservan su español original; la pantalla lo indica. Secuencia, origen y tipo describen emisiones reales, sin timestamps inventados.

La revisión corrige además dos ejemplos que capturaban la cancelación como `IllegalStateException`. Ambos relanzan ahora `CancellationException`, con una regresión que cubre las dos estrategias. Ver [decisiones de pantalla](DESIGN.md) y [avisos de terceros](THIRD_PARTY.md).

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

Comprobado localmente el 29 de octubre de 2026: **10 tests JVM y cuatro recorridos de MainActivity real en emulador API 37**, además de lint y builds de APK debug/tests con JDK 17. Los recorridos comprueban Flow latest/orden y limpieza, cancelación desde Guía, recreación y manejador raíz. La suite nativa completa también pasa con texto al 150% en 360×800 dp. El recorrido Flow pasa además en vertical inglés/español, oscuro español y horizontal inglés. Las imágenes son capturas reales del emulador.

Lint no informa errores y mantiene un aviso de target. Las supresiones previas de avisos de versiones no se modifican. La CI enlazada ejecuta JVM, lint y build debug; los recorridos nativos son locales. Para ejecutarlos, usar un único emulador/dispositivo API 33+ (la regla de idioma utiliza la API de locale por aplicación):

```sh
./gradlew :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest
```

La app declara API 26+, pero esta revisión no acredita todos los Android soportados, TalkBack, dispositivos físicos ni tiendas. El APK debug contiene unos 25,63 MB DEX sin comprimir; pasan las comprobaciones consultivas de alineación nativa/ZIP. Falta metadata R8 y no se acredita una tasa de optimización ni una release optimizada. El wrapper verifica SHA-256 de Gradle 8.13. Se conservan licencias y atribuciones existentes.
