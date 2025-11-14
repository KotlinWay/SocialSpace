# Repository Guidelines

## Project Structure & Module Organization
`androidApp/` hosts the platform shell for Compose screens and is the entry point for Android Studio builds. Shared presentation, networking, and DI code lives in `sharedUI/` (KMP targets Android + iOS), while cross-module DTOs are in `api-models/`. The Ktor backend is under `backend/` with routes, services, and Exposed table mappings in `src/main/kotlin`, configuration under `src/main/resources`, and local file uploads stored in `backend/uploads/` (mirrored to root `uploads/` for the mobile client). Use `docker-compose.yml` when you need PostgreSQL locally; Gradle metadata sits in `gradle/` and repo-wide settings in `settings.gradle.kts`.

## Build, Test, and Development Commands
- `./gradlew :backend:run` — starts the Ktor server (`ApplicationKt`) and exposes `/health` on `localhost:8080`.
- `cd backend && ./add_test_data.sh` — seeds two demo users and sample listings so the Android list screens do not show an endless loader.
- `./gradlew :androidApp:installDebug` — deploys the Compose client to a connected emulator/device; use `:androidApp:assembleDebug` for CI artifacts in `androidApp/build/outputs/apk`.
- `./gradlew :backend:test` and `./gradlew :sharedUI:allTests` — run JVM tests (JUnit/Ktor) and common Kotlin tests respectively before opening a PR.

## Coding Style & Naming Conventions
Kotlin sources use 4-space indentation, trailing commas in multiline constructs, and JetBrains naming: PascalCase for composables/screens (`ProductsScreen`), camelCase for functions/properties, and `UpperSnakeCase` for constants. Prefer `data` classes plus kotlinx-serialization in `api-models` so payloads stay aligned across modules. Compose UI stays declarative—hoist state into view models or `Decompose` components in `sharedUI`, keep side effects inside `LaunchedEffect`. Backend packages mirror feature areas (`info.javaway.sc.backend.products`); name files after the feature, not the framework type.

## Testing Guidelines
Backend tests live in `backend/src/test/kotlin` and should wrap new routes with Ktor `testApplication {}` plus realistic repositories. Shared UI logic belongs in `sharedUI/src/commonTest` with coroutine test dispatchers; name tests `FunctionName_WhenCondition_ReturnsExpected`. Target green runs of `:backend:test` + `:sharedUI:allTests` before pushing, and add regression coverage when touching auth, payments, or paging.

## Commit & Pull Request Guidelines
Follow the existing Git history: start messages with an emoji + short Russian summary (e.g., `✨ Этап 13.4: ...`, `🐛 Исправлен ...`), optionally referencing the relevant stage. Squash noisy WIP commits locally. Pull requests should describe scope, affected modules (`backend`, `sharedUI`, etc.), include reproduction steps or screenshots for UI tweaks, and link Jira/GitHub issues. Confirm lints/tests, note any schema changes, and mention when seed data, uploads, or `application.conf` need manual updates.

## Environment & Configuration Tips
Ktor picks up settings from `backend/src/main/resources/application.conf`; keep secrets in local `.env` files or environment variables instead of committing them. Android clients expect the backend at `10.0.2.2:8080` inside the emulator—override the base URL in `ApiClient` only for device testing. Clean up any large assets before pushing; `uploads/` is ignored by default, so share sample media via cloud storage when collaborating.
