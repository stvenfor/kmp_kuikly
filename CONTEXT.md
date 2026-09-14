# kmp_kuikly

A Kuikly-based cross-platform **teaching and forkable skeleton**: runnable demo apps plus a quasi-production module layout, not a real product backend.

## Language

**Demo Skeleton**:
The deliverable of this repo — a multi-platform Kuikly sample that teaches structure and is close enough to fork as a golden template.
_Avoid_: Product app, business app, production app (unless meaning “production-like engineering”)

**Shell App**:
A platform host project (`androidApp` / `iosApp` / `ohosApp` / `h5App`) that can run demos alone and is not the long-term home of business logic.
_Avoid_: Host conflated with shared business code

**Embeddable Shared**:
The KMP business surface designed so an external native host can depend on it without taking the Shell Apps.
_Avoid_: Monolithic app module

**Feature Module**:
A KMP module for one demo subdomain (e.g. auth, feed), owning that subdomain’s pages and use-cases.
_Avoid_: Putting all demos in one undifferentiated `shared` package only

**Platform Extension Module**:
A capability that needs native implementations (e.g. permission, share): a common API module plus per-platform impl modules.
_Avoid_: Feature Module (those are business demos, not native capability packs)

**Compose Track**:
Primary UI style for formal sample pages using Kuikly Compose DSL.
_Avoid_: Calling this “Jetpack Compose on Android only”

**Legacy DSL Track**:
A small set of pages written in Kuikly’s self DSL (`Pager` / `ViewBuilder`) kept for teaching contrast.
_Avoid_: Primary UI stack

**AOT Delivery**:
v1 ships platform binaries / bundles with the app; no OTA page download yet.
_Avoid_: Hot update, dynamic交付 as a v1 requirement

**Dynamicization Seam**:
Reserved Module / bundle boundaries for a later dynamic-delivery phase without implementing the pipeline in v1.
_Avoid_: Claiming dynamicization is “done” when only seams exist

**Mock Backend**:
In-process fake APIs with controllable success / failure / empty states and no external server.
_Avoid_: Real API, staging environment (out of v1 scope)

**Vertical Slice**:
A thin end-to-end demo path (login + one list) proven on all v1 platforms before expanding other samples.
_Avoid_: Finishing all features on Android before touching other platforms

**App Shared**:
The thin KMP assembly module that is the single Kuikly page-entry product artifact dependents compile into.
_Avoid_: Fat shared that owns all features directly
