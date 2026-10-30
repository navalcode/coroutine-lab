# Verification record

30 October 2026. Editorial revision of bilingual resources, README, design notes and teaching text. No domain, ViewModel, effect ownership, dependencies or workflow code changed. Core scenario messages and probe identifiers remain stable. Resource-backed instructions and observer labels are revised.

## Current local results

- JDK 17.0.14, SDK 36; all Gradle tasks rerun on the final application sources.
- **10 JVM tests**, zero failures/errors/skips.
- Lint: **0 errors, 1 warning**. Existing target/dependency warnings remain; no warning suppression was added.
- Debug and Android-test APK builds passed. This revision does not build or validate an optimized release.

Dedicated API 37 emulator, 360 × 800 dp:

| Run | Result |
|---|---|
| English, 100% text | 4/4 native tests passed |
| 150% text, qaLanguage=es | 4/4 native tests passed |
| Spanish MainActivity capture, 100% text | 1/1 selected journey passed |

The locale rule applies to every test in this project’s native suite. The complete enlarged-text run uses Spanish.


The app declares a lower minimum SDK, while native locale rules use API 33+ and the local device is API 37. A minimum-SDK declaration does not prove testing on that version. No physical-device, TalkBack or formal accessibility certification is claimed.

## Commands

From the repository root:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
ANDROID_SERIAL=DEVICE_ID ./gradlew :app:connectedDebugAndroidTest
```

Replace `DEVICE_ID` with the serial of a dedicated test emulator. For opt-in language/captures, install the app/test APK and use the Android runner:

```sh
ANDROID_SERIAL=DEVICE_ID ./gradlew :app:installDebug :app:installDebugAndroidTest
adb -s DEVICE_ID shell am instrument -w -r -e qaLanguage es -e captureScreenshots true com.example.coroutinelab.test/androidx.test.runner.AndroidJUnitRunner
```

The capture argument is optional. It writes test PNGs inside this demo’s private files directory, retrievable with `adb exec-out run-as`. Locale rules restore the previous per-app locale after a test. Use a disposable emulator for viewport/text-scale changes and restore its settings afterward. Screenshots are executed frames, without pixel editing.

The four MainActivity journeys check latest Flow/event ordering and clearing, cancellation from Guide, activity recreation and root-handler failure. JVM regression tests also check that two error strategies propagate cancellation. Raw scenario/event text is Spanish even with English controls, as the screen states.

## Artifact audit

Debug APK SHA-256: `e12b5d1e7cf534716d62efd195607f3fb9c6e8c645e46ec9ef88047e48f97c38`.

The advisory audit measured **25.63 MB uncompressed DEX**. Native ELF and APK ZIP 16 KB alignment checks passed. R8 percentage metadata is absent. An overall tool pass does not establish an optimization rate, official Play policy, signing or store approval. The audit did not remove dependencies or alter any artifact.

## Hosted CI and earlier evidence

The linked workflow runs JVM tests, lint and the debug build. Native journeys here are local; a green badge does not imply hosted device coverage. Check the workflow against the published commit. Earlier 29 October runs, including dark/landscape checks and a repaired scroll-ownership regression, remain historical evidence and are not silently attributed to this revision.
