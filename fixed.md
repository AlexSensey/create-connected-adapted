# Create Connected: Adapted — Minecraft 26.2

Version: 1.3.3-adapted-26.2-0.6

## Changes in 0.3

- Replaced the mod icon with the supplied Logo_ico.png for Minecraft and Prism mod lists.
- The supplied custom icon is retained.

## Changes in 0.2

- REI now respects Connected's feature toggles and optional-mod conditions,
  matching the creative tab and JEI. Glooming, Enriched and other unsupported
  catalysts no longer appear as usable items without their corresponding mods.
- Added a conditional client resource pack for 21 catalyst models and 23
  third-party texture references, including Dye Depot variants. When the texture
  owner's mod is absent, saved blocks and command-given items use vanilla
  fallback textures instead of the missing-texture checkerboard.
- Original models and textures remain in use when the corresponding optional
  mods are installed. Fallbacks change appearance only; they do not enable fan
  processing types supplied by absent mods.
- REI and Architectury are compile-only APIs, not bundled or required dependencies.
  Config refresh safely handles startup before the Minecraft instance exists.

## Changes retained from 0.1

- Preserved the minimum Create dependency: 6.0.11-adapted-1.20 or newer.
- Migrated the remaining shaft-cutting recipe in the Create namespace to the
  native 26.2 ingredient format, preserving its output and processing time.
- Replaced obsolete NeoForge always/never condition identifiers in advancements.
- Replaced invalid air-item loot entries with empty tables for blocks without
  independent items. Linked controls retain their existing base-control drops.
- Load Dye Depot loot tables only when Dye Depot is installed.
- Added the linked pale oak button blockstate resource and a valid empty loot
  table, preserving all 48 states and the existing transmitter geometry.
- Retained the previous kinetic, bridge settings, hover-label, copycat rendering,
  crank-wheel overlap, fluid handling and recipe migration work.

## Validation

- Version 0.2 passed 161 native REI checks, including enabled/disabled item
  visibility, all conditional model texture resources, and six recipe pages.
  No Connected missing-texture warnings remain with the installed mod set.
- A separate native world launch without REI passed all 42 resource/drop checks.
- The following core checks were also passed by the preceding 0.1 release:
- Gradle JAR build succeeded against the pinned Create 1.20 compile API.
- All 275 Connected recipe files pass the idempotent format check.
- The entire installed mod set loaded a copy of the Prism world with Create
  1.26 and NeoForge 26.2.0.88. Connected data errors decreased from 39 to zero;
  the separate shaft-cutting parse error is also gone.
- 42 native game checks passed: 70 active Connected recipes, six core recipe
  IDs, shaft cutting, base-item drops for all 14 linked buttons and two linked
  levers, and the pale oak button resource/state coverage.
- 57 native REI checks passed for R/U search, category selection, recipe bounds
  and opening six Connected recipe pages, including animated control-chip assembly.
- 299 kinetic logic and 590 bridge settings regressions passed using production
  method bodies with modeled collaborators and native NBT/direction/vector types.

These focused checks do not certify every machine network or visual interaction.
The optional Simulated companion is not included: its real 26.2 API is unavailable
in this checkout.

## Changes in 0.4

- Moved the standard thick Kinetic Bridge setting corners to the solid panel beside the multiplier label, on both bridge halves. Removed the duplicate thin frame. Hit testing and setting synchronization are preserved.

Validation of 0.4: 96 native panel-position and side hit-test assertions passed for all six bridge orientations. Both bridge halves were rendered and captured in-game.

## Changes in 0.5

- Fixed the invisible conveyor belt in the Kinetic Battery Ponder tutorial. Belts created during the animation now receive their controller, length and segment indices before rendering.

Validation of 0.5: all three Minecraft ports compiled. The complete 26.3 modpack passed native assertions for the dynamically created two-segment battery belt; the tutorial was rendered and captured. Only KineticBatteryScene.class and version metadata changed from 0.4.

## Changes in 0.6

- The Kinetic Battery discharge-direction hover hint now appears only over its settings area. Looking at the rest of the battery no longer triggers that hint.

Validation of 0.6: all three Minecraft ports compiled. Native hover checks compare the battery body and settings area for all six facings and all six hit faces (72 assertions per tested port). A client-only mixin filters the battery hint; charging and discharge logic are unchanged.
