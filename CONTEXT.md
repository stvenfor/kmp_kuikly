# kmp_kuikly

A Kuikly / KMP app repo that **migrates** the Flutter product at `my_ai_project` into Compose Track Feature Modules, while keeping the former Demo Skeleton layout as engineering scaffolding—not as the default user experience.

## Language

**Migration Source**:
The Flutter multi-module app at `/Users/mac/Desktop/github/my_ai_project` (`module_sample`), whose UI and business behaviour we restore in Kuikly in phased hard-1:1 slices.
_Avoid_: Treating this repo’s old Hello/demo pages as the product truth

**Product Vertical Slice**:
A closed end-to-end path (pages + Mock scenarios + three-platform golden gate) that must be hard 1:1 before the next slice starts.
_Avoid_: “Structure first, polish later”; finishing all features on one platform first

**Demo Skeleton**:
The prior teaching-oriented layout and leftover sample pages in this repo; no longer the default launch experience after migration work lands.
_Avoid_: Calling the migrated product path a “skeleton demo”

**Shell App**:
A platform host project (`androidApp` / `iosApp` / `ohosApp` / `h5App`) that can run the shared surface alone and is not the long-term home of business logic.
_Avoid_: Host conflated with shared business code

**Embeddable Shared**:
The KMP business surface designed so an external native host can depend on it without taking the Shell Apps.
_Avoid_: Monolithic app module

**Feature Module**:
A KMP module for one product subdomain (e.g. auth, home), owning that subdomain’s pages and use-cases.
_Avoid_: Putting all product UI in one undifferentiated `shared` package only

**Platform Extension Module**:
A capability that needs native implementations (e.g. permission, share): a common API module plus per-platform impl modules.
_Avoid_: Feature Module (those are business domains, not native capability packs)

**Compose Track**:
The only UI DSL for product pages—Kuikly Compose DSL.
_Avoid_: Jetpack Compose on Android only; Legacy DSL as a second product stack

**Legacy DSL Track**:
Former teaching contrast pages using Kuikly self DSL (`Pager` / `ViewBuilder`); not used on the product path or product golden gate.
_Avoid_: Primary UI stack; required dual-track for product work

**AOT Delivery**:
v1 ships platform binaries / bundles with the app; no OTA page download yet.
_Avoid_: Hot update, dynamic交付 as a v1 requirement

**Dynamicization Seam**:
Reserved Module / bundle boundaries for a later dynamic-delivery phase without implementing the pipeline in v1.
_Avoid_: Claiming dynamicization is “done” when only seams exist

**Mock Backend**:
In-process fake APIs with controllable success / failure / empty / unauthorized states; the only backend mode that can pass slice completion gates.
_Avoid_: Requiring a live `my_go_study` server to call a slice “done”

**Backend Seam**:
A Repository (or equivalent) boundary reserved for a future HTTP implementation against `my_go_study`, without implementing that client in the active slice unless the slice explicitly says so.
_Avoid_: “Optional real backend” meaning the HTTP client is already required for acceptance

**Per-Platform Golden**:
Locked screenshot baselines per platform (Android / iOS / Harmony) used for **regression** against that platform’s own prior lock. In Phase-2 Visual Parity, iOS/Ohos stay on this gate; Android **visual** completion additionally requires Cross-Source Parity.
_Avoid_: Treating self-lock alone as “matches Flutter”

**Flutter Reference Golden**:
Screenshots captured from Migration Source (`my_ai_project`) on a locked Android device profile; stored under `goldens/flutter-ref/`. The visual source of truth for Phase-2.
_Avoid_: Hand-drawn mockups or Kuikly self-baselines as the parity target

**Cross-Source Parity**:
Kuikly Android screenshot matches Flutter Reference Golden for the same route+state (masked status bar / home indicator; AE threshold). Required for Phase-2 slice PASS.
_Avoid_: Claiming 1:1 recreation after Phase-1 self-lock only

**Shell-level Home**:
**Deprecated for Phase-2 Destination.** Phase-1 allowed Home reduced to used-car entry; Phase-2 Wave 1 requires full Flutter Home dashboard parity for the「首页」top tab.
_Avoid_: Shipping Shell-level Home as “done” under Visual Parity

**App Shared**:
The thin KMP assembly module that is the single Kuikly page-entry product artifact dependents compile into.
_Avoid_: Fat shared that owns all features directly

**Target Platforms (slice gate)**:
Android, iOS, and HarmonyOS must all run and pass agreed goldens for a Product Vertical Slice to complete; H5 is out of the first migration slice unless a later decision adds it. Phase-2: Android also vs Flutter Reference Golden.
_Avoid_: “Three platforms” meaning only Android screenshots with iOS/Harmony compile-only

**Migration Program**:
The full restoration of Migration Source via ordered Product Vertical Slices, tracked by `.scratch/my-ai-migration/PROGRAM.md` (Wayfinder map). Phase-1 = structural/Mock; Phase-2 = Cross-Source Visual Parity ([ADR-0018](docs/adr/0018-cross-source-visual-parity.md), [plan](docs/architecture/VISUAL-PARITY-PHASE2.md)).
_Avoid_: Treating Phase-1 Program PASS as visual Destination done

**Orchestrator Agent**:
The Cursor agent that grills, writes ADR/spec/tickets/handoffs, and accepts evidence; it does not implement product UI tickets while WorkBuddy is the executor. Owns Flutter-ref + parity diff gates.
_Avoid_: Orchestrator silently implementing WorkBuddy-owned tickets

**WorkBuddy Executor**:
The WorkBuddy app agent that implements one ready ticket per session from a handoff file and returns an Executor Report for Orchestrator acceptance.
_Avoid_: Self-declaring Slice/Program PASS; inventing simplified shells for in-scope Phase-2 screens
