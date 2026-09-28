# Create: Connected — 26.2 adaptation in progress

This checkout is **not a working Minecraft 26.2 release**. No Connected JAR
has been built or installed in Adventures Beyond Eternity, and no game launch
or world test has passed. The upstream Gradle build still targets 1.21.1;
use the diagnostic below for the current migration work.

## Source and target

- Original: https://github.com/hlysine/create_connected
- Original distribution: https://www.curseforge.com/minecraft/mc-mods/create-connected
- Baseline: `be43db74eec507325aa29c19cdef11e25f7d02c1`, version `1.3.3-mc1.21.1`.
- Target instance: Prism Launcher / Adventures Beyond Eternity.
- Minecraft: 26.2; NeoForge: 26.2.0.82; Java: 25.
- Installed Create dependency: Create: Adapted 0.999, private all-in-one edition.
- License: upstream AGPL-3.0 and additional terms remain in `LICENSE`.

## Changes made

- Migrated ResourceLocation usages to Minecraft's Identifier API.
- Updated Catnip and Ponder package locations against the actual embedded
  libraries. Mapping manifests are in `tools/`.
- Targeted Create's isolated `dev.engine_room.create_flywheel` API, matching
  the installed Create binary rather than the separate public Flywheel mod.
- Updated several Minecraft/NeoForge model, advancement and rendering imports.
- Migrated ItemInteractionResult usages and Level.isClientSide access.
- Migrated scalar/compound/list NBT reads to the default-returning 26.2 APIs
  in 21 files. This does not complete the block-entity persistence migration.
- Added a reproducible full-source compiler diagnostic. It selects the exact
  launcher libraries from Prism metadata, includes nested Create/NeoForge
  dependencies and records the Create SHA-256. It writes only under `build/`.

## Reproduce diagnostics

From this checkout, with Python and Java 25 installed:

```powershell
python tools/check_262.py
```

Override the instance or JDK with `--instance <path>` and `--jdk <path>`.
Use `--prepare-only` to inspect dependencies without compiling.

Outputs under `build/diagnostics-26.2/`:

- `environment.json`: exact instance versions, dependencies and Create hash.
- `compile.log`: full javac output.
- `summary.json`: error counts and categories.
- `javac.args`: compiler arguments for reproducing the run.

Latest check: 321 source files; javac failed with **364 errors** and 100
warnings. These are diagnostics, not independently identified bugs:
missing optional dependencies and unresolved types cause cascading failures.
The diagnostic does not apply access transformers or validate mixin targets.
Successful javac compilation alone would not establish runtime compatibility.

## Remaining work before testing

1. Port the actual renderer/model contracts (render state extraction/submission,
   copycat geometry, screens); changing imports alone does not implement them.
2. Port block callbacks, redstone orientation, placement, inventory/fluid APIs,
   components and remaining persistence/recipe interfaces.
3. Resolve optional integrations (Copycats+, Additional Placements, Simulated,
   Sable, Dragons Plus) for the selected first-release scope. Old integration
   JARs must not be added to the 26.2 runtime to suppress compiler errors.
4. Migrate build configuration/datagen, item definitions and resource metadata;
   validate access transformers and every enabled mixin against 26.2/Create.
5. Give the adaptation a distinct display name/icon before distribution, retain
   attribution and provide corresponding source according to the license.
6. Build a real JAR, verify client and dedicated-server startup, then install
   and test in ABE. Exercise rotation/stress, redstone, battery charge retention,
   storage, rendering and save/reload in a separate test world.

The user confirmed continuing the adaptation. All source files and optional
integrations remain in the diagnostic source set; no features have been excluded
to obtain the current diagnostic count.

## Second migration pass (2026-09-14)

- Reimplemented the parallel, six-way and brass gearbox renderers and the
  kinetic battery renderer using 26.2 model submission. Preserved the per-face
  rotation multipliers and half-shaft orientation. Models resolve through the
  current model manager to tolerate resource reloads.
- Moved all 24 main block-entity renderer registrations from Registrate's old
  one-type-parameter provider API to the NeoForge renderer registration event.
- Removed obsolete Registrate cutout-layer callbacks. The installed 26.2
  `BakedQuad.MaterialInfo.of` derives chunk layers from material transparency;
  the old callbacks still link the removed vanilla RenderType class.
- Migrated neighbor notification, comparator direction, shape update and fluid
  tick scheduling signatures, preserving their existing logic.
- Migrated item descriptions to Item.Properties.overrideDescription, placement
  helper references, packet protocol version/handler and registry lookups.
- Migrated battery exchange results and tooltip generation. Added the numeric
  item-model property and six-level range-dispatch item definition for the
  charge indicator, replacing the removed ItemProperties registration.
- Updated copycat connectivity signatures, wall properties and skylight queries.
  Added ConnectedDirections.fromDelta: unlike a nearest-axis lookup, it rejects
  diagonals and non-unit offsets, preserving the original adjacency behavior.

Validation:

- Full javac diagnostic: 510 errors, down from 768 at the start of this pass.
  Counts include cascades and missing optional compile dependencies; this is
  not a percentage estimate of remaining work.
- DirectionCompatibilityCheck compiled and executed against the actual 26.2
  dependencies: all 125 offsets in [-2, 2]^3 passed.
- Battery item JSON: all six ordered thresholds resolve to existing, parseable
  model resources.
- Whitespace diff check passed after cleanup.
- No game launch, distributable JAR or ABE installation yet.

Important next issue: inventory/fluid capability migration must preserve
NeoForge transaction rollback, not merely wrap the old simulate/execute API.
The base Create tank adapter also snapshots boiler state, while Connected's
BoilerData shadows gatheredSupply; using that adapter without checking rollback
would be unsafe. This was inspected but no capability bridge was added yet.


### Fluid capability and item data migration

- Added SingleTankTransfer and registered vessel capabilities through
  Capabilities.Fluid.BLOCK. Its journal captures controller tank contents and
  Connected BoilerData.gatheredSupply, including nested transaction rollback.
  This avoids relying on the base Create adapter's different shadowed field.
- Vessel interaction and tooltips now obtain their legacy internal handler
  directly; external transfers use the modern capability.
- Vessel and silo items now use TypedEntityData<BlockEntityType<?>>, retaining
  removal of multiblock coordinates/dimensions before placement. Vessel fluid
  decoding/encoding uses registry-aware FluidStack.OPTIONAL_CODEC and retains
  the per-block capacity clamp. Codec failures are logged.

Validation for this pass:

- Full diagnostic: 319 sources, 498 errors, 100 warnings (previous pass: 510).
- SingleTankTransfer and the transaction harness compile against ABE jars.
- DirectionCompatibilityCheck still passes all 125 offsets.
- FluidTransactionCheck has NOT executed its assertions: standalone startup
  fails in SharedConstants because no FMLLoader is active. The harness remains
  in tools/ for completion with a proper NeoForge test runtime; do not interpret
  compilation as confirmation of transaction behavior.
- tools/run_262_checks.py currently returns failure at that fluid bootstrap step.
- No distributable JAR, game launch or changes to the ABE instance.

Remaining storage work includes block-entity and mounted-storage serialization,
item capability transaction support, and vessel rendering. The older next-issue
note above describes the investigation before this pass; the fluid bridge now
exists but still requires runtime validation.


### Storage serialization and vessel removal follow-up

- Added StorageSerialization to bridge compound-based Create hooks to the
  registry-aware ValueInput/ValueOutput API with scoped error reporting.
- Migrated silo inventory save/load and legacy mounted silo loading. The actual
  NeoForge handler still writes Size and Items with Slot entries; native
  deserialization restores the saved size without per-slot world callbacks.
- Migrated vessel and legacy mounted vessel fluid serialization using the direct
  FluidStack optional codec. Deliberately avoided FluidTank.serialize's new Fluid
  wrapper: the adapted Create superclass expects a direct TankContent stack.
- Retained XYZ compound positions for controller and last-known position fields,
  matching the adapted Create superclass and Catnip's backward-compatible reader.
- Replaced vessel onRemove with affectNeighborsAfterRemoval. It consumes the
  removal snapshot prepared by inherited FluidTankBlockEntity.preRemoveSideEffects
  and splits the multiblock after the old entity is removed.

Validation: full diagnostic now reports 320 sources, 487 errors, 100 warnings
(previous: 498 errors). No diagnostics remain for StorageSerialization or either
mounted-storage class. Whitespace diff check passed. These changes have not been
round-trip tested or exercised in-game; the standalone fluid harness still needs
an initialized NeoForge runtime. No ABE files were changed and no JAR was built.


### Silo capability and removal migration

- Registered Capabilities.Item.BLOCK through SiloItemTransfer, restricted to the
  silo's modifiable inventory. All faces and constituent blocks resolve the same
  controller adapter and per-slot SnapshotJournal instances. Assembly changes
  invalidate the cached adapter alongside the legacy capability.
- Transfers simulate first, snapshot before mutation, return the actual executed
  amount, and restore copied stacks on rollback. Capacity respects both slot and
  item limits; negative transfer amounts are rejected.
- Moved silo item drops to preRemoveSideEffects with a once-only guard. The block
  consumes the saved removed entity in affectNeighborsAfterRemoval and then
  splits the multiblock, retaining the original drop/remove/split ordering.
- Updated the silo contraption NBT repair's optional Length read and XYZ position
  writing. Mixin target validation remains outstanding.

Validation: full javac diagnostic covers 321 sources and reports 483 errors,
100 warnings (previous: 487 errors). No silo source/mixin diagnostics remain.
Whitespace check passed. Transaction and removal behavior still need an actual
NeoForge runtime; no claim of passing gameplay or rollback tests is made.
No distributable JAR or changes to the ABE instance.


### Moving jukebox migration

- Switched song-player access to the public getSongPlayer API and removed its
  access transformer. Updated JukeboxSong.fromStack to the component-based API.
- Temporary jukebox entities now load through registry-aware TagValueInput with
  scoped error reporting and tolerate absent saved data.
- Updated WrappedLevel.levelEvent to the Entity signature so song start/stop
  events still route to moving-jukebox packets. Vanilla bytecode confirms the
  same 1010/1011 event numbers and registry song ID payload in 26.2.
- Record insertion checks the actual stack's playable component and copies one
  item, matching the one-item decrement from the player's hand.
- Updated the now-playing message to Gui.hud. Nearby-entity notification follows
  the vanilla LevelEventHandler's three-block AABB and public LivingEntity method;
  removed the obsolete LevelRenderer access transformer.
- Migrated jukebox song datagen to the new JsonCodecProvider constructor.

Validation: 321 sources, 469 compiler errors, 100 warnings (previous: 483 errors).
No jukebox source or song-provider diagnostics remain. Whitespace check passed.
No sound playback, packet round-trip or in-game assembly test has run; compilation
alone does not establish runtime compatibility. No JAR or ABE changes.


### Linked button/lever removal and pick-block migration

- Migrated linked buttons, vanilla levers and analog levers to
  affectNeighborsAfterRemoval. A small removal-state map preserves module
  ownership captured during block-entity preRemoveSideEffects. The final callback
  consumes that state and respects isMoving; wrench-returned modules are not
  dropped again. Inherited block callbacks receive the actual old state for
  neighbor notification. Digital transmitters send zero before removal.
- Migrated the three pick-block overrides to the NeoForge 26.2 signature, deriving
  the hit from the player's interaction-range ray and forwarding includeData when
  picking the underlying button/lever rather than the transmitter module.

Validation: 322 sources, 458 errors, 100 warnings (previous: 469 errors).
No diagnostics remain for these removal/pick methods or the new helper. Two
LinkedButtonBlock private-field diagnostics are still covered by existing access
transformer entries, which this javac-only check does not apply. Whitespace check
passed. Wrenching, piston movement, redstone updates and client picking have not
been tested in-game; renderer migration and optional throttle-lever integration
remain outstanding. No JAR built and no ABE instance files changed.


### Gearbox picking, copycat occlusion and Ponder migration

- Updated brass, parallel and six-way gearbox pick-block signatures, preserving
  the axis-dependent choice of regular/vertical item and forwarding includeData.
- Updated encased cross-connector picking, deriving the player's ray hit to retain
  selection of connector versus casing by the targeted face.
- Migrated copycat fence and fence-gate occlusion overrides to state-only queries,
  still delegating to their corresponding wrapped vanilla block states.
- Selected weathering().unaffected() explicitly for copper items in the inventory
  bridge/access-port scenes; quantities and scene instructions are unchanged.
- Replaced seven removed ItemStack.saveOptional calls in battery/transmitter scenes
  with registry-aware ItemStack.OPTIONAL_CODEC writes. Verified that the installed
  Create source reads HeldItem/FrequencyFirst/FrequencyLast with the same codec.

Validation: full javac diagnostic covers 322 sources, reporting 427 errors and
100 warnings (previous: 458 errors). No Ponder source diagnostics remain.
Whitespace check passed. Scene playback, geometry and picking still require
in-game tests; no runtime success is implied. No JAR or ABE instance changes.


### Existing Ponder/Catnip integration audit

- Located ../Ponder (currently configured for Minecraft 26.1.2) and the older
  .tmp-ponder-262-adapted-backup reference. They are useful source references,
  but are not substituted for the actual ABE libraries.
- Confirmed diagnostics already use the Ponder and Catnip 1.0.100046 adapted-fork
  mc26.2 jars extracted from ABE's installed Create all-in-one. No second Ponder
  installation or engine port is needed for Connected's scenes.
- Replaced removed Catnip ClientOnly markers with NeoForge OnlyIn(Dist.CLIENT).
- Removed the SubMenuConfigScreen mixin and its registration: its target is absent
  from the installed dependency jars. Existing CCConfigs.onReload ->
  SyncConfigBase.onReload now remains the config synchronization entry point.
- Config synchronization now queues work on the actual running server thread,
  supporting integrated servers as well as dedicated servers, and avoids sending
  queued work after the server instance changes. No dependency on the removed
  Catnip config screen remains.

Validation: full javac diagnostic reports 321 sources, 417 errors, 100 warnings
(previous: 427 errors). One obsolete mixin source was replaced by the existing
reload-event path; no feature source exclusions were added to diagnostics.
Mixin JSON parses; whitespace check passed. Config reload and multiplayer packet
behavior still need runtime tests. No JAR was built and no ABE files changed.


### Dashboard rendering and interaction migration

- Updated sign applicator calls to pass the held item stack, and HUD messages to
  Player.sendOverlayMessage. Translated open/closed status uses the same overlay.
- Replaced legacy text drawing with extractRenderState/submit. The extracted state
  retains formatted/filtered lines, facing, line height, colors, lighting and the
  outline decision; submit keeps the original panel transforms and text scale.
- Used AbstractSignRenderer.getDarkColor and the actual 26.2 submitText argument
  order (light, color, background, outline color). Glowing text retains full light
  and the distance/scoping/black-text outline conditions. Outline color now follows
  vanilla's dark text color instead of the old loop-index argument.
- DashboardRenderer remains registered through CCBlockEntityRenderers.

Validation: full javac diagnostic reports 321 sources, 404 errors, 100 warnings
(previous: 417 errors). No dashboard source diagnostics remain. Whitespace check
passed. Text rendering, dyes/glow ink and HUD interactions still require gameplay
verification; no screenshot or runtime test is claimed. No JAR or ABE changes.


### Rotating fan catalyst heads

- Migrated creeper/dragon head rendering to extractRenderState/submit and
  SkullModelBase.State. Model and render type creation now use vanilla
  SkullBlockRenderer factories with the actual 26.2 renderer context.
- Models belong to renderer instances instead of mutable enum singletons;
  removed reflective construction and its silent fallback to the wrong model.
- Preserved the per-type translation and scale, packed lighting and zero pitch/
  mouth animation. Rotation now advances one degree per game tick with partial
  tick interpolation, rather than accumulating once per rendered head/frame.

Validation: 321 sources, 399 compiler errors, 100 warnings (previous: 404 errors).
No fan catalyst source diagnostics remain. Whitespace check passed. Visual
appearance and animation have not been exercised in-game. No JAR or ABE changes.


### Advancement migration

- Replaced the removed CriterionTrigger.Listener bookkeeping with vanilla
  SimpleCriterionTrigger and predicate-based dispatch, following the adapted
  Create implementation. This also delegates player-condition evaluation to
  Minecraft. Explicitly disambiguated the nullable supplier-list overload.
- Migrated owner UUID NBT to UUIDUtil.CODEC and advancement lookup to the player's
  server level. Updated Criterion imports and advancement display icons to
  ItemStackTemplate, preserving count and data components.
- Tag-based item criteria are now created at save time with the datagen registry
  lookup. Criterion keys and external-trigger classification are reserved when
  the builder is configured, preserving their identity and avoiding an unwanted
  built-in trigger.

Validation: full javac diagnostic covers 321 sources with 381 errors, 100 warnings
(previous: 399 errors). Advancement source diagnostics are resolved. Whitespace
check passed. Advancement awarding, saved-owner round trips and datagen execution
still require runtime/build validation. No distributable JAR or ABE changes.


### Datagen compatibility ingredients

- Migrated custom ingredient item/fluid enumeration to Holder streams and current
  registry lookups for matching IDs. These datagen placeholders still enumerate
  no runtime entries, as before.
- Updated their compound child codecs to identifier strings under children,
  matching the 26.2 Ingredient and SimpleFluidIngredient holder-set codecs and
  CompoundIngredient/CompoundFluidIngredient field names. The codec enforces
  exactly one child. The enclosing NeoForge codec supplies its current type key.
- Corrected the fluid registry alias lookup to use the compound FLUID ingredient
  type rather than the item ingredient type. The pre-existing datagen-only
  registry alias mechanism remains and still needs validation in a full run.
- Replaced hex parsing in fluid ingredient hashCode with a hash of the same mod/id
  fields used by equals, avoiding failures on ordinary namespaced identifiers.

Validation: full javac diagnostic covers 321 sources, 375 errors, 100 warnings
(previous: 381 errors). No diagnostics remain for these two ingredient classes.
Verified codec shapes against the actual installed NeoForge bytecode; whitespace
check passed. Datagen execution and generated recipe loading remain untested.
No distributable JAR or ABE instance changes.


### Datagen event and recipe runner migration

- Replaced abstract GatherDataEvent listeners with concrete Client/Server listeners
  and the current mod-container ID check. Sounds use client datagen; advancements,
  jukebox songs and recipes use server datagen. Extra Registrate setup is guarded
  against duplicate registration across both event variants.
- Removed ExistingFileHelper/getMods/includeServer calls, absent in 26.2.
- Registered all five recipe groups through RecipeProvider.Runner: standard,
  sequenced assembly, cutting, filling and item application. Constructors now
  receive the resolved registry lookup and RecipeOutput, forwarding both to the
  adapted Create superclass rather than the legacy PackOutput/future bridge.
- Replaced direct ProcessingRecipeGen.run calls with runner-owned execution.

Validation: full diagnostic covers 321 sources, 364 errors, 100 warnings
(previous: 375 errors). Datagen entry points and runner registration have no
remaining diagnostics; standard recipe builders, cooking output shim and one
item-application condition still need migration. Whitespace check passed. No
complete datagen execution, generated output validation or game launch yet.
No distributable JAR or ABE instance changes.
