# Slice-04: Home deepen — search entry → SearchPage

**Status:** accepted (recommended defaults 2026-09-16)  
**Parent:** ADR-0005  
**Spec:** `.scratch/my-ai-migration/slice-04/spec.md`

## Decision

Deepen Shell-level Home with a **search entry** that opens Compose `SearchPage` (Mock history / discovery / simple rank list). Music mini-player and full Home magazine (video/club tabs) stay out. Modules: `feature-home` + `core-data` search Fake; no new Gradle module.

## Consequences

- Goldens: Main Home with search chrome, SearchPage default state; three-platform gate.
- Parallelizable with Slice-03 (different hot files: auth vs home/search).
