## Local migration regression tests

With JDK 21 and the Android SDK configured (`ANDROID_HOME` or `local.properties`), run:

```sh
./gradlew testAospOmegaDebugUnitTest --tests '*GridSizeMigrationRegressionTest'
```

These tests live in `tests/unit/src`, outside the production `src` source root. They use
Robolectric API 35 and native, file-backed SQLite with the production migration/controller
implementation. Coverage includes SQLite rollback, retry/reopen, widget and folder preservation,
fresh database initialization, invalid/no-op migrations, and loader cancellation before defaults,
cleanup, binding, or loader-transaction commit. Application DI, logging, and the smartspace setting
are mocked; no device or launcher application startup is required. This suite does not replace
the existing device/Soong tests under `tests/src`.

## Development Progress

- [ ] Rebase on A13

- [ ] Port the features back from 0.9.x

- [ ] Theming

- [ ] Sorting

  - [ ] Alphabetical

  - [ ] By Usage

- [ ] Dock

  - [ ] Theming

  - [ ] Search bar

- [ ] Desktop

  - [ ] Size and naming

  - [ ] Allow rotation

  - [ ] Apply pref directly

- [ ] Search engines

- [ ] Edit app's icons


- [ ] Folder cover mode

- [ ] Categories

  - [ ] Tabs

  - [ ] Folders

- [ ] Backup & restore

- [ ] Categories

  - [ ] Tabs & Folders

- [ ] Backup & restore

## Progress on 1.0.0

- [x] Use icon packs
- [X] Get Widgets working
- [X] Gestures
  - [x] Double Tap
  - [X] Long Press

## Low Priority/ Future nice-to-haves

- [ ] Omega Widgets

- [ ] Different drawer layouts

  - [ ] Linear Horizontal

  - [ ] Paged Vertical

- [ ] Add custom search provider
- [ ] Simpler coloring themes