# Migrate my_ai_project via hard 1:1 vertical slices

We migrate the Flutter app at `/Users/mac/Desktop/github/my_ai_project` into this Kuikly repo in **phased Product Vertical Slices**, each hard 1:1 for UI and business behaviour—not a structure-only skeleton pass. Default launch becomes the product path; leftover Demo Skeleton samples leave the default experience (hide first, delete when unreferenced).

**First slice (closed):** Splash → privacy consent → Main (four-tab chrome; non-Home tabs placeholder) → Shell-level Home (used-car entry only; no Music mini-player) → used-car gate (modal OTP Mock login + redirect) → list → detail. Product UI is **Compose Track only**. Reuse `feature-home`, `feature-auth`, `app-shared`, `core-data`; keep `feature-feed` off the default path.

**Data:** Mock Backend with scenario switches is the acceptance backend; a Backend Seam may exist for later `my_go_study` HTTP, but live Go/Supabase is not a completion condition for the first slice.

**Platforms & screenshots:** Slice done only when Android + iOS + HarmonyOS all run and pass **Per-Platform Golden** automatic diffs (one locked device profile per platform, recorded in a runbook). H5 is out of the first slice. Goldens are produced/checked by **in-repo scripts** as a local gate; three-platform screenshot CI is not required to finish the first slice. Privacy UX matches the source (block / persist / retry) but copy describes only capabilities this slice actually ships.

**Supersedes for product work:** ADR-0001’s requirement to keep Legacy DSL Track visible on the default teaching path—Legacy DSL is no longer part of the product path or product golden gate (see glossary).

**Program ops (2026-09-16):** Full restoration continues via `.scratch/my-ai-migration/PROGRAM.md`. Orchestrator (Cursor) assigns/accepts; WorkBuddy executes ticket handoffs. Slice-02 decisions: ADR-0006.
