# Create: Connected (Adapted) - Minecraft 26.3

Version: 1.3.3-adapted-26.3-0.6
Branch: adapted/26.3
NeoForge: 26.3.0.40-beta or newer within Minecraft 26.3.
Create dependency: 6.0.11-adapted-1.26 or newer.

- Replaced the mod icon with the supplied Logo_ico.png for Minecraft and Prism mod lists. The supplied custom icon is retained.
- Adapted inventory/fluid bridges, block item NBT hooks, immutable sign text, rendering, redstone, config registration and data generation to 26.3. Migrated shipped advancements and loot tables to the new data format.
- Preserved the 26.2 adaptation's kinetic, bridge, copycat, fluid, recipe and linked-control fixes.
- REI respects feature toggles and optional-mod requirements. Unsupported catalysts are hidden.
- Conditional fallback textures cover 21 models and 23 optional texture references when their owning mods are absent. Original assets are used when their mods are installed.
- Preserved linked-control base item drops and all 48 pale oak linked button states.
- REI and Architectury remain optional compile-only integrations.

Validation: a disposable Minecraft world passed 44 native recipe, resource and drop assertions. REI validation is recorded in the accompanying REI-PASS.txt. Tests use Create Adapted 1.26, not the complete installed modpack.

The optional Simulated companion is excluded because a matching 26.x API is unavailable. Optional fan processing requires its corresponding addon; fallback textures do not enable it.

License: AGPL-3.0 with the additional terms in LICENSE. Corresponding source is included alongside the release. Retained upstream assets keep their original licenses and attribution.

## Changes in 0.4

- Moved the standard thick Kinetic Bridge setting corners to the solid panel beside the multiplier label, on both bridge halves. Removed the duplicate thin frame. Hit testing and setting synchronization are preserved.

Validation of 0.4: 96 native panel-position and side hit-test assertions passed for all six bridge orientations. Both bridge halves were rendered and captured in-game.

## Changes in 0.5

- Fixed the invisible conveyor belt in the Kinetic Battery Ponder tutorial. Belts created during the animation now receive their controller, length and segment indices before rendering.

Validation of 0.5: all three Minecraft ports compiled. The complete 26.3 modpack passed native assertions for the dynamically created two-segment battery belt; the tutorial was rendered and captured. Only KineticBatteryScene.class and version metadata changed from 0.4.

## Changes in 0.6

- The Kinetic Battery discharge-direction hover hint now appears only over its settings area. Looking at the rest of the battery no longer triggers that hint.

Validation of 0.6: all three Minecraft ports compiled. Native hover checks compare the battery body and settings area for all six facings and all six hit faces (72 assertions per tested port). A client-only mixin filters the battery hint; charging and discharge logic are unchanged.
