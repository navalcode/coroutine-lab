# Coroutine Lab

[Español](README.es.md) · [![CI](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml/badge.svg)](https://github.com/navalcode/coroutine-lab/actions/workflows/checks.yml)

An interactive Android learning lab for Kotlin Coroutines and Flow, presented with Jetpack Compose. Fictional transport examples make execution order, cancellation, parallel work and errors visible on a timeline.

The learning loop is **predict, run, observe and explain**. The examples are educational and self-contained; they do not connect to production transport services.

## Explore the behavior

| Area | What to inspect |
|---|---|
| Structured concurrency | Parent/child ownership, cancellation and waiting for related work. |
| Flow operators | Compare `flatMapConcat`, `flatMapMerge` and `flatMapLatest` against their recorded timelines. |
| Errors | Follow `launch`/`async`, exception propagation and supervision choices. |
| Practical examples | Compare fictional autocomplete and parallel-dashboard implementations with the refactored versions. |

Start with the domain examples and their tests under `app/src/test/`, then follow state into the ViewModel and Compose screens. The domain does not depend on Android; fake repositories and injectable dispatchers keep time deterministic in tests. Compose collects state with `collectAsStateWithLifecycle`, and `viewModelScope` owns the jobs started by the screen.

## Run

Requires JDK 17, Android SDK 36 and an API 26+ emulator/device. Open the repository in Android Studio or configure your SDK in untracked `local.properties`.

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
```

The Gradle 8.13 wrapper is executable on Unix and verifies its distribution SHA-256. Library/plugin versions are pinned in `gradle/libs.versions.toml`.

## Guided reading

The detailed learning material is in Spanish:

- [Guide and exercises](docs/01_GUIA_DIDACTICA.md)
- [Launch, async and exceptions](docs/02_LAUNCH_ASYNC_EXCEPCIONES.md)
- [Fictional refactoring examples](docs/03_REFACTORIZACIONES.md)
- [Material for sharing the topic](docs/04_MATERIAL_PARA_COMPARTIR.md)

Official references: [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-guide.html), [Flow](https://kotlinlang.org/docs/coroutines-flow.html) and [Android coroutine practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices).

## Validation scope

On 29 October 2026, nine JVM tests, lint and the debug APK build passed locally with JDK 17. Lint reported one warning and no errors; the existing build configuration suppresses dependency-version update notices. This result does not claim emulator journey coverage, an optimized release, physical-device behavior or store distribution.

An advisory check of the debug APK found aligned native libraries; R8 metadata is absent in this debug artifact, so it is not release-optimization evidence.
