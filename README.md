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

## Use the screen

<img src="docs/images/screen-en.png" alt="Coroutine Lab running the latest Flow operator on an Android emulator" width="300">

Choose a scenario, predict its ordering, run it and read its actual events. The selected Flow/error strategy stays fixed while work is running. **Cancel remains in the toolbar even after changing to Guide**; Clear is available after work stops. Recreation keeps the ViewModel owner. The root-handler example reports its failure without closing the activity.

Controls and instructions have Spanish resources with an English fallback. Original scenario titles, raw trace messages and the detailed teaching guides remain in Spanish; the screen labels this boundary explicitly. Sequence IDs, sources and event kinds describe real emissions, with no invented timestamps.

The current review also fixes cancellation being caught as `IllegalStateException` in two error examples. Both now rethrow `CancellationException`; the regression test covers both strategies. See [screen decisions](DESIGN.md) and [third-party notices](THIRD_PARTY.md).

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

Locally checked on 29 October 2026: **10 JVM tests and four real MainActivity journeys on an API 37 emulator**, plus lint and debug/test APK builds with JDK 17. Native journeys cover ordered/latest Flow and clearing, cancellation from Guide, recreation and root-handler resilience. The complete native suite also passes at 150% text on 360×800 dp. A Flow journey additionally passes in English/Spanish portrait, Spanish dark mode and English landscape; screenshots above are actual emulator captures.

Lint reports no errors and one target-version warning. Existing dependency-version notice suppressions remain unchanged. The linked CI runs JVM tests, lint and a debug build; device journeys are local checks, not a CI device suite. Run native tests with a single API 33+ emulator/device (the test locale rule uses the platform per-app locale API):

```sh
./gradlew :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest
```

The app declares API 26+, but this review does not establish behavior on every supported Android version, TalkBack, physical devices or stores. The debug APK contains about 25.63 MB of uncompressed DEX; advisory native/ZIP alignment checks pass. R8 metadata is absent, so no optimization rate or optimized release is established. Existing project/dependency licenses are preserved.
