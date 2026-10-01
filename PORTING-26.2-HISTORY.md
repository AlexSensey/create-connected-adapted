# Create: Connected — 26.2 adaptation in progress

Current core release: **1.3.3-adapted-26.2-0.2**. On 2026-10-02, the complete
installed mod set loaded a copy of the Prism world with Create 1.26. Connected
data errors are now zero; 42 native resource/drop checks without REI and 161 native REI checks
passed. See fixed.md for exact coverage and remaining limitations. The optional
Simulated companion remains unavailable. Optional catalysts now follow feature
visibility in REI and use conditional fallback textures when their asset-owner
mods are missing. Historical development notes follow.

Previously, this checkout was **not a validated Minecraft 26.2 release**. The core development
JAR has been built successfully against Create 1.20 and installed in the target
Prism instance. Client startup and world entry have now been observed, but
rendering and world behavior are not yet accepted. The Gradle environment targets 26.2 and stages the diagnosed core libraries
locally. Simulated is an optional companion source set; its real compile API
is still missing. Core compilation now succeeds with actual access-transformer
application, using NeoForm Runtime 2.0.31 / JST 2.0.11.

## Source and target

- Original: https://github.com/hlysine/create_connected
- Original distribution: https://www.curseforge.com/minecraft/mc-mods/create-connected
- Baseline: `be43db74eec507325aa29c19cdef11e25f7d02c1`, version `1.3.3-mc1.21.1`.
- Target instance: Prism Launcher / 26.2 (formerly Adventures Beyond Eternity).
- Minecraft: 26.2; NeoForge: 26.2.0.88; Java: 25.
- Installed Create dependency: Create: Adapted 1.20, public external-assets edition.
- License: upstream AGPL-3.0 and additional terms remain in `LICENSE`.

## Changes made

### Kinetic Bridge side labels (2026-10-01)

Both Bridge halves retain normal renderer submission when Flywheel is active.
KineticBridgeRenderer keeps a valid block entity independently of the shaft
visibility flag and submits the active scroll setting before that geometry guard.
The label appears on the targeted lateral face, centered at the existing slot
and moved beyond the casing surface. Native font submission preserves the whole
formatted multiplier, including x and decimal separators. The output half reads
the existing source-owned setting proxy; no interaction or kinetic changes.
Gradle jar passes. Installed in Prism 26.2 with matching artifact hash; actual
in-world label appearance remains unverified.
Backup: build/api-check-26.2/create_connected-before-bridge-labels-fix.jar.
User confirmed labels visible but oversized and overlapping the open casing.
Follow-up halves the maximum font scale (1/96), caps width at 0.24 blocks,
and shifts the label by four voxels to the center of each half's solid panel
(source opposite FACING, destination along FACING). The source casing texture
has transparent center pixels above row 8; blockstate rotations were reviewed.
Interaction hit areas and stored values remain unchanged. Built and installed;
visual acceptance of the new layout is pending.
Backup: build/api-check-26.2/create_connected-before-bridge-label-layout-fix.jar.
Added a five-voxel square represented only by four white L-shaped corners around
the compact value, submitted in the same face plane before font scaling. Both
the frame and text fit within the solid panel. Build passes; in-world appearance
of the corner frame remains pending.
Backup: build/api-check-26.2/create_connected-before-bridge-label-corners-fix.jar.
Visual follow-up: inset the corner frame to 4.5 voxels (from 5), increase corner
stroke thickness by 50%, and reduce the maximum text width to 0.21 blocks to
keep its margin inside the frame. Built and installed; revised appearance pending.
Backup: build/api-check-26.2/create_connected-before-bridge-label-inset-fix.jar.
User clarified that the label should move rather than shrink. Restored the
five-voxel frame and 0.24-block text width cap, retaining thicker corners.
Moved both anchors one voxel away from the outer casing edge toward the open
edge: source axis coordinate 5/16, destination 11/16 in the UP orientation.
The frame stays on the solid panel (source 2.5..7.5, destination 8.5..13.5 voxels).
Build passes; installed appearance pending.
Backup: build/api-check-26.2/create_connected-before-bridge-label-position-fix.jar.

### Adjustable clutch hover labels (2026-10-01)

Overstress, Freewheel and Centrifugal clutches now use Connected's ClutchRenderer.
The native SplitShaftRenderer submits shaft geometry without scroll behaviour
labels. The subclass preserves that geometry and submits native 26.2 numeric
or option overlays afterwards, including when Flywheel handles the shafts.
Follow-up: the first build did not display labels in the user's world. All three
visualizer registrations used renderNormally=false, suppressing the entire BER
when Flywheel was active. They now use true so ClutchRenderer is called; the
native SplitShaftRenderer still skips shaft submission with visualization active.
Existing behaviour transforms still exclude shaft end faces and determine the
hover target; values, callbacks and interaction logic are unchanged.
Gradle jar passes against the installed Create API. The built renderer is
packaged and installed in Prism 26.2; in-world appearance is not yet verified.
Backup: build/api-check-26.2/create_connected-before-clutch-labels-fix.jar.
Follow-up backup: build/api-check-26.2/create_connected-before-clutch-visualizer-fix.jar.

### Missing JEI recipes / remaining recipe data migration (2026-10-01)

- JEI 30.39.0.232 starts and Create's recipe registration completes in the
  current Prism log. Connected still has 67 recipe parse errors, including
  legacy shaped ingredients, shapeless recipes decoding to empty ingredient
  lists, the control-chip sequence and the removed neoforge:false condition.
- Migrated the remaining crafting/stonecutting/sequenced-assembly inputs to
  holder strings/arrays, including nested assembly steps. Replaced obsolete
  neoforge:false/true IDs with neoforge:never/always; the names are present in
  the installed NeoForge condition registration. No recipe outputs, counts,
  patterns, loops or compatibility/feature availability semantics change.
- 122 JSON files changed; the full 275-recipe set now passes the idempotent
  migration check. Native holder syntax passed for 405 item and 70 fluid inputs
  using symbolic lookup holders. JAR build passed, all packaged JSON matched
  sources, and crafting counts/keys passed structural checks. Full FML recipe
  decoding and visible JEI recipes remain unverified until the game restarts.
- Only Connected is replaced in Prism 26.2, with a backup of the previous JAR.

### Linked Transmitter hover frame occlusion (2026-10-01)

- User reported no visible dots after the native frame integration. The slot
  transform placed unlocked icons/frames at 1.1 voxels from the attachment plane,
  below the model's slot panel surface at 1.51 voxels. Native frame offset
  further moves into the surface under this transform.
- Shifted the slot anchor outward by 0.5 voxels for floor, wall and ceiling
  attachment. Unlocked anchor is now 1.6 voxels, leaving the frame outside the
  panel after its native offset. Frequency hit positions move with the icons.
- JAR build and 24 panel-clearance checks passed for both slots, four horizontal
  facings and three attachment faces, using production position/rotation bodies,
  native Catnip/PoseStack math and modeled block states/Flywheel rotation facade.
  This does not execute the Flywheel mixin or verify visible frames in game.

### Linked Transmitter frequency hover dots (2026-10-01)

- User confirmed attachment works, but the custom frequency icon renderer lacked
  Redstone Link's hover dots. It now checks the aimed block and LinkBehaviour's
  hit test for each slot, then invokes native LinkRenderer.submitValueBoxFrame.
- Hover frames render for empty and populated slots. Locked slots stay hidden.
  The custom slot translation/rotation is retained; its scale is undone only
  while submitting the native frame, preserving the original frame size.
- JAR build passed. Native Mixin audit matched the static frame Invoker's name,
  descriptor and staticness, and the renderer injection target. No game launch;
  the visible highlight still needs user confirmation after restart.

### Linked Transmitter attachment and frequency rendering (2026-10-01)

- Create 1.20 AnalogLeverBlock now consumes `useItemOn` via its lever action.
  ItemUseOverrides invokes that action before ItemStack.onItemUseFirst. The
  existing Connected mixin only exempted `useWithoutItem`; now the held module
  also returns PASS from the new item-use path on the unlinked analog lever,
  allowing LinkedTransmitterItem to install the module. Other held items and
  already-linked analog levers retain their existing interactions.
- Prism log showed LinkRenderer.submitOnBlockEntity attempting to read the
  six-direction RedstoneLinkBlock.FACING from linked_lever, which instead has
  horizontal facing plus attach face. A client-only LinkRenderer mixin now
  renders Connected frequency items through LinkedTransmitterFrequencySlot,
  covering buttons, ordinary and analog levers including Ponder. Ordinary
  Create redstone links keep their native renderer. Locked/empty slots skip
  icon rendering; existing hover outlines remain in LinkRenderer.tick.
- JAR build passed; native Mixin target audit matched both added injection
  methods. Existing 15 placement/data-preservation checks were rerun after
  confirming the fixture item method matches production, using current 1.20
  dependencies. Checks model world/player/entity collaborators; actual mounting,
  wireless transmission and rendering still require in-game confirmation.

### Catalyst solid-input placement prediction (2026-10-01)

- User confirmed bucket filling/retrieval, then reported a briefly placed block
  when applying netherrack or soul sand. Consume those client clicks on the empty
  catalyst, extending the bucket fix to the two unconditional solid inputs.
- The server click remains available to Create's manual-application recipe;
  Connected does not predict the catalyst transformation or consume the item.
  Placement on other blocks remains unchanged.
- 51 production-handler checks passed with modeled world/player collaborators.
  Visual confirmation in game is still required after installing the new JAR.

### Catalyst bucket interaction (2026-10-01)

- User confirmed filling after the recipe migration, but reported a second
  bucket placement above the catalyst. Native Create's manual-application
  listener returns early on the client. Native MultiPlayerGameMode consumes
  a canceled SUCCESS block click while still sending the block-use packet;
  Connected now consumes the client bucket click on the empty catalyst so the
  separate ordinary bucket-use action does not follow the server recipe.
- Full water/lava catalysts also consume filled-bucket clicks on both sides,
  preventing placement above them during repeated use.
- Empty buckets now retrieve water or lava from the corresponding catalyst,
  replacing it with the empty catalyst on the server. The native
  ItemUtils.createFilledResult handles hand stack, inventory and creative mode.
  No bucket is awarded when the block replacement fails. Spectator, build,
  world interaction and already-canceled event guards are retained.
- Native event API and packet path inspected; JAR build and 43 production-handler
  checks passed with modeled world/player collaborators. Real inventory behavior
  and the client/server exchange remain unverified in game. Other catalyst
  contents do not gain bucket retrieval in this change.

### Empty Fan Catalyst recipe inputs (2026-10-01)

- The Prism log explicitly rejected `item_application/splashing_catalyst_from_empty`
  and `filling/fan_splashing_catalyst`: legacy `{item: ...}` inputs no longer
  decode with the installed 26.2 codecs. This prevented recipe-driven filling.
- Migrated all 84 catalyst item-application and 70 filling recipes to holder
  strings/arrays and sized fluids with `{ingredient: ..., amount: 1000}`.
  Outputs, conditions, consumption flags and quantities are retained, including
  optional compatibility recipes; their required mods still govern availability.
- Added an idempotent migration/check tool. Native Minecraft holder syntax passed
  for 238 item and 70 fluid inputs. Full Ingredient/Create recipe decoding requires
  FML initialization and was not run; symbolic holder lookups do not validate
  registered items, feature conditions or world interaction.
- Gradle JAR build passed. All 154 packaged recipe JSON files are checked against
  source resources before installing Connected alone in Prism 26.2, with backup.

### Kinetic Bridge settings interaction (2026-10-01)

- Both halves now expose stress settings. The destination forwards reads and
  writes to the valid source's behaviour, retaining one authoritative saved and
  synchronized value and the existing source kinetic-update callback.
- Holding use works across the whole lateral face on either half, instead of
  only within the small central value-box radius. Shaft faces remain excluded.
  Label placement and battery interaction remain unchanged.
- Built against installed Create Adapted 1.20. Focused production-body checks
  passed 590 cases (modeled entities, native Direction/Vec3); the previous 299
  kinetic checks also passed. This does not verify the screen in a running game.
- Installed JAR is backed up before replacement in `build/api-check-26.2`.

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

Outputs under `build/diagnostics-core-26.2/` (historical all-in-one snapshots retained):

- `environment.json`: exact instance versions, dependencies and Create hash.
- `compile.log`: full javac output.
- `summary.json`: error counts and categories.
- `javac.args`: compiler arguments for reproducing the run.

Latest core check: 324 source files; bare javac reports **2 errors** and 100
warnings. Both errors are access to ButtonBlock fields covered by the packaged
access transformer; this diagnostic does not apply that transformer.
The optional Simulated bridge has 7 separate source files and is not part of
the core diagnostic or core JAR. Its actual 26.2 compile dependency is missing.

Latest core log: `build/diagnostics-core-26.2/compile.log` (2026-10-01).
Historical all-in-one diagnostics remain under `build/diagnostics-create-1.15/`
and `build/diagnostics-26.2/`; the last combined-source snapshot reported 91 errors.
The earlier offline Gradle compile attempt failed before Java compilation because
NeoForge 26.2.0.88 development artifacts were not cached. Its historical log is
`build/api-check-26.2/gradle-core-compile.log`. The subsequent online Gradle
`jar` build succeeded, compiling all 324 core source files with actual ATs;
it reports 100 warnings and no errors. Current build log:
`build/api-check-26.2/gradle-core-jar.log`.

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

### Small API migration pass (2026-09-30)

- Crank wheel placement now returns PlacementOffset.placeInWorld's
  InteractionResult directly. Removed the obsolete numeric helper-ID sentinel:
  both placement helpers are registered as IPlacementHelper objects in the constructor.
- Brake smoke uses the current particle overload, keeping distance limiting and
  normal particle settings (both override flags false).
- Shear pin effects obtain the block center through Vec3.atCenterOf.
- Pulse instruction translation fallbacks use Language.getInstance().has,
  replacing the removed client I18n.exists method.

Validation: a small API probe compiled successfully with Java 25 against the
cached 26.2 dependency jars (build/api-check-26.2/ConnectedApiCheck.java).
This checks the changed API calls, not compilation of the four complete classes.
No full compiler diagnostic, build or game launch was run in this pass;
the previously recorded overall error count has not been refreshed.

### Config, registry and feedback API pass (2026-09-30)

- Feature and category config synchronization iterates CompoundTag.keySet;
  boolean defaults and visibility refresh behavior are unchanged.
- Mods item lookups use the defaulted registry's getValue API rather than the
  holder-returning get API, retaining the registry's missing-item fallback.
- Kinetic bridge placement feedback uses a colored component and
  sendOverlayMessage, preserving its existing color and overlay destination.
- Both disabled withering-catalyst recipe conditions now use NeverCondition.

Validation: Mods.java and a focused API probe compiled successfully against the
cached 26.2 jars with Java 25. The probe covers NBT iteration, registry lookup,
colored overlay messages and NeverCondition (build/api-check-26.2/ConnectedConfigApiCheck.java).
The other five complete classes were not compiled. No full diagnostic, datagen,
build or game launch; the overall compiler error count remains unrefreshed.

### Block shapes and interactions pass (2026-09-30)

- Copycat Board stores and applies the Function returned by getShapeForEachState;
  its multiface shape calculation is unchanged.
- Brass chute retrieval uses the block-entity optional directly with current
  InteractionResult values, preserving empty-hand fallback and inventory return.
- Cross connector encasing converts Create's legacy result through its adapter.
  Connection updates on removal now use affectNeighborsAfterRemoval and its
  ServerLevel callback instead of the removed vanilla onRemove signature.

Validation: build/api-check-26.2/ConnectedBlockApiCheck.java compiled successfully
against the cached 26.2 jars with Java 25, including the removal override, shape
function, encasing adapter and chute retrieval API calls. The three full source
classes were not compiled. No full build or game launch; kinetic-network updates
after removal and item interactions still require runtime verification.

### Transmitter, loader and pick-block pass (2026-09-30)

- Linked transmitter waxing/unwaxing preserves the old sided-success semantics
  explicitly (client SUCCESS, server CONSUME). Axe durability now uses the
  InteractionHand overload, retaining the actual hand used for the interaction.
- MixinPlugin reads the loading mod list from FMLLoader.getCurrent(), retaining
  the existing any-mod-present and applyIfPresent selection logic.
- Kinetic bridge destination pick-block overrides the current NeoForge method
  signature and still returns the source bridge item without copying data.

Validation: the complete LinkedTransmitterBlock and MixinPlugin sources compiled
with CCShapes and ModMixin against cached 26.2 jars using Java 25. A small
ConnectedPickBlockApiCheck also compiled, checking the exact pick-block override.
KineticBridgeDestinationBlock was not compiled as a whole. No full diagnostic,
build or game launch; mixin selection and in-game interactions remain untested.

### Copycat and packager mixin pass (2026-09-30)

- Copycat vertical step uses EnumProperty<Direction> for HORIZONTAL_FACING.
- Fence-gate survival mixin targets FarmlandBlock, the current vanilla class;
  its existing DirtPathBlock target and wrapped-fence-gate check are unchanged.
- Packager placement rejection uses an instance injection handler, matching
  the non-static getStateForPlacement target. All three rejection messages now
  use sendOverlayMessage with the existing translated component.

Validation: javap confirmed both survival target signatures and the instance
packager placement method. BlockCanSurviveMixin and ICopycatWithWrappedBlock
compiled against cached 26.2 jars with Java 25, as did a focused enum-property
and overlay API probe (build/api-check-26.2/ConnectedCopycatApiCheck.java).
PackagerBlockMixin and CopycatVerticalStepBlock were not compiled as complete
classes. Annotation processing was disabled; mixin application is not validated.
No full build or game launch in this pass.

### Resource and recipe datagen batch (2026-09-30)

- Replaced 29 removed provider modLoc calls with CreateConnected.asResource in
  CCBlocks, CCBlockStateGen, CCBuilderTransformers, FluidVesselGenerator and
  InventoryAccessPortGenerator. Existing paths and the create_connected namespace
  are preserved; Create-owned textures still use Create.asResource.
- Battery loot uses copyComponentsFromBlockEntity with BLOCK_ENTITY context,
  retaining the kinetic charge component. Linked button discovery unwraps the
  optional block lookup and still skips absent/non-button registry entries.
- CCStandardRecipes supplies the item registry lookup to shaped/shapeless
  builders and item predicates, resolves tag ingredients through holder sets,
  and passes ItemStackTemplate results to preserve count and components.
- Crafting, stonecutting and smithing saves use recipe resource keys with the
  original IDs. Special recipe factories use the supplier API with MISC category.

Validation: ConnectedDatagenApiCheck compiled against cached 26.2 jars with Java
25, covering the changed recipe builders, saves, predicates, tag ingredients,
loot component builder and optional block lookup. The six full source classes
were not compiled. No full datagen/build/game run was performed. Model provider
contracts and the cooking-output serializer shim remain incomplete, so these
changes do not establish working datagen or reduce a freshly measured error count.

### Cooking recipe serialization pass (2026-09-30)

- Cooking builders use the serializers on the concrete recipe classes, the
  current generic factory signature and recipe resource keys. Existing output
  IDs, time multipliers, experience, unlock criteria and conditions are retained.
- Cooking book categories are selected explicitly: smoker/campfire are FOOD;
  furnace food results are FOOD; other block outputs are BLOCKS; others are MISC.
- Updated the datagen-only output shim using the local adapted Create pattern:
  construct the final RecipeSerializer record with map/stream codecs, implement
  current Recipe methods and delegate RecipeOutput.includeRootAdvancement.
  The existing foreign-item result override and serializer registry alias remain.

Validation: the actual GeneratedCookingRecipeBuilder and both output-shim nested
classes were extracted unchanged into a small compile fixture, with minimal
outer provider plumbing, and compiled successfully against cached 26.2 jars.
Fixture: build/api-check-26.2/ConnectedCookingMigrationCheck.java. The complete
CCStandardRecipes class was not compiled. JSON encoding, registry alias injection,
conditional recipe loading and full datagen still need runtime validation;
no build/game launch or fresh overall diagnostic was performed.

### Pulse generator GUI migration (2026-09-30)

- Ported CCGuiTextures to GuiGraphicsExtractor and GUI_TEXTURED blits with
  explicit 256x256 atlas dimensions and ARGB tint; removed global texture binding.
- Pulse generator screen now submits its background before inherited widgets
  through extractRenderState. Window centering/offset is calculated during init;
  dynamic instruction rows use the current widget add/remove APIs.
- Text uses the current extractor calls with opaque ARGB colors. The large item
  preview uses GuiGameElement.submit. Saving uses ClientNetworkHelper and screen
  removal delegates to the superclass after sending changed settings.

Validation: one cached-dependency javac pass exposed two additional GUI API
errors; after correcting them, a second pass completed in about three seconds.
Overall diagnostics: 250 errors (previous recorded full run: 364), 100 warnings.
No diagnostics remain for CCGuiTextures, SequencedPulseGeneratorScreen or
CCStandardRecipes. These counts include missing optional dependencies/cascades;
the project still does not compile. No Gradle build, dependency downloads or game
launch occurred. Screen appearance, input handling and packet behavior still
require in-game verification.

### Inventory capability discovery and brass chute (2026-09-30)

- Inventory access port and inventory bridge placement query Capabilities.Item.BLOCK
  with ResourceHandler<ItemResource>, retaining their existing neighbor search and
  fallback orientation logic.
- Brass chute registers the modern item capability using the inherited Create
  getItemResourceHandler, preserving the base transaction implementation instead
  of wrapping legacy simulate/execute calls. Its legacy itemHandler accessor stays
  available for internal callers.

Validation: a cached-classpath javac pass took about three seconds and reported
247 errors / 100 warnings. No diagnostics remain in the three changed classes.
No game launch or Gradle build. Port/bridge block-entity capability registration
and their filtered legacy wrappers are still unported; discovery alone does not
establish working transfers. Next storage work must forward native transactions
while preserving filtering, power-state behavior, invalidation and recursion guards.

### Native inventory port/bridge transactions (2026-09-30)

- Both block entities expose Capabilities.Item.BLOCK through RoutedResourceHandler.
  Inserts forward the caller's transaction to the actual neighboring storage;
  extracts use a nested transaction so a filter rejection rolls back the attempted
  extraction, including underlying side effects. No legacy simulate/execute bridge
  is used on the exposed transfer path.
- Bridge routes retain negative-before-positive slot ordering and asymmetric
  filter priority: an explicit opposite-side match wins over an empty local filter.
  Filtering sees the actual extracted amount, as in the old simulated check.
- Neighbor capabilities are looked up live on the connected face without loading
  chunks. Powered/removed/uninitialized blocks expose no connected storage. Direct
  port/bridge targets remain excluded, and per-handler guards stop indirect loops.
  Attachment checks now use the same native lookup. Existing observed-inventory
  behaviours remain for inventory identity and neighbor updates; legacy wrapper
  implementations remain internal and are no longer registered as capabilities.

Validation: tools/ItemRoutingTransactionCheck.java compiled and executed using
real NeoForge Transaction/SnapshotJournal classes with an in-memory test resource,
without Minecraft bootstrap. Passed insertion/extraction commit and parent abort,
rejected extraction rollback, filtered visibility, actual-quantity filtering,
positive-route identity with absent negative route, invalid slots, live detachment,
recursion and recovery. Full cached-classpath javac: 323 sources, 245 errors,
100 warnings; no diagnostics in either block entity or the two new helper classes.
No Gradle build or game launch. In-game filter UI, redstone transitions, third-party
inventory invalidation and logistics identity still require runtime verification.

### Fluid vessel renderer (2026-09-30)

- Replaced the legacy buffer renderer with extractRenderState/submit. Extraction
  copies controller/window flags, dimensions, axis, interpolated fill, fluid,
  buoyancy, gauge progress and occlusion; submission does not read the block entity.
- FluidRenderHelper submits the fluid volume with the same cap/hull dimensions,
  puddle minimum and upper alignment for lighter-than-air fluids. Removed the old
  global render-buffer dependency and simplified the equivalent vertical transform.
- Boiler gauge/dial use Create standalone models and submitted block geometry,
  preserving the existing axis, occlusion, pivot and angle calculations. Models
  resolve from the current manager to respect resource reloads. Off-screen drawing
  remains enabled for multiblocks, with controller-only submission.

Validation: cached javac pass took about three seconds: 239 errors / 100 warnings,
no FluidVesselRenderer diagnostics. Compared old transformed bounds with the new
formulas for both axes, widths 1-3, lengths 1-32, five fill levels and both buoyancy
states (1920 cases); all matched within tolerance. No game launch or visual QA.
Connected-texture vessel models and window-type persistence still need migration.

### Vessel connected textures, culling and window NBT (2026-09-30)

- Standard/creative vessel registrations now use Create's connectedTextures
  registry with FluidVesselCTBehaviour and their respective existing sprite sets.
  The old blockModel callback is no longer used for these blocks.
- FluidVesselModel now wraps modern BlockStateModel parts in the client model-bake
  event at LOWEST priority. It retains the old cross-section connectivity culling,
  keeps axial faces and unculled quads, and delegates material/AO properties.
  The geometry key includes the connected-face mask, allowing cached geometry to
  distinguish topology changes. Registration runs again on resource reloads.
- WindowType writes enum names as strings. StorageSerialization.readEnum accepts
  enum names case-insensitively and falls back to SIDE_WIDE for absent, malformed
  or unknown values rather than failing the block-entity load.

Validation: full cached javac reports 231 errors / 100 warnings; no diagnostics
remain in FluidVesselModel, FluidVesselBlockEntity or StorageSerialization. The
changed model registration expressions introduce no reported diagnostics (CCBlocks
still has unrelated datagen errors). Checked actual cached Create bytecode for
connected-texture registration. The extracted, unchanged readEnum method passed
a short CompoundTag test for all four names, lowercase variants and fallback cases
(build/api-check-26.2/VesselWindowNbtCheck.java). No full build or game/visual test;
texture orientation, event ordering and formed-vessel seams still need in-game QA.


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


### Linked analog lever render-state migration (2026-09-30)

- Replaced the obsolete renderSafe overlay hook with extractRenderState/submit.
  A nested parent render state preserves Create's handle and indicator animation.
- Frequency item stacks and slot matrices are captured during extraction;
  submission does not read the live block entity. Reused states clear both items
  before early returns. Locked slots and out-of-range non-virtual blocks hide
  frequency items using the existing slot rules and client distance setting.
- Uses LinkedTransmitterFrequencySlot for floor/wall/ceiling placement, rather
  than the current LinkRenderer helper, which assumes RedstoneLinkBlock.FACING.
  This entity registers only LinkBehaviour, so the old filtering overlay call
  has no corresponding behaviour to render.

Validation: one cached javac pass (about 3 seconds), 323 sources, 230 errors;
no LinkedAnalogLeverRenderer diagnostics. No Gradle build or game launch.
Item scale, orientation, hover overlays and animated handle still need in-game QA.

Datagen investigation: cached legacy BlockStateProvider/ModelFile APIs are
compatibility shells, and Create's BlockStateGen contains no-op callbacks.
Do not replace missing generator methods with these no-ops to silence errors;
a real modern provider migration remains necessary. No datagen files changed
in this pass.


### Native tag providers and recipe remainder ownership (2026-09-30)

- CCTagGen now registers two native TagsProvider instances with the server
  GatherDataEvent. Removed the dependency on RegistrateTagsProvider's missing
  IntrinsicHolderTagsProvider superclass and the legacy CreateTagsProvider wrapper.
  All existing compatibility tag memberships are retained; entries use typed
  registry keys, including optional dye catalyst entries. No block-to-item tag
  copying is needed for the separate contraption-controlled item tag.
- Removed ManualApplicationRecipeMixin and its mixin-list registration. The
  cached Create Adapted dependency already obtains Item.getCraftingRemainder(),
  creates the remainder stack, consumes one input, and either replaces the held
  stack, inserts into inventory, or drops the remainder. Keeping Connected's old
  injection would repeat remainder handling for stacked inputs. Verified this
  in actual dependency bytecode, not just the local Create source.
- Kept applicationRemainingItemFix as a legacy configuration key and updated its
  description to explain that it has no effect on this target.

Validation: cached javac after both changes reports 322 sources, 224 errors,
100 warnings (previous: 230 errors). No diagnostics in the changed tag/event/
config classes. Mixin JSON parses and no removed-mixin registration remains.
No full datagen run, Gradle build or in-game recipe/inventory test performed.


### Crank wheel renderer and stairs removal callback (2026-09-30)

- Registered CrankWheelRenderer through the existing RegisterRenderers listener,
  replacing HandCrankRenderer, whose modern implementation chooses Create's
  standard handle and never calls Connected's old getRenderedHandle override.
- Removed that obsolete buffer override. Registered all four small/large base
  and handle assets as standalone models. Render extraction captures size,
  facing, kinetic base angle and independent handle angle; submission reads only
  these snapshots. Orientation follows the existing CrankWheelVisual transforms.
  Baked parts are resolved each submission to support resource reloads. Rendering
  defers to visualization when the dependency reports that it is active.
- Copycat stairs now delegates wrapped stairs neighbour-removal behaviour through
  affectNeighborsAfterRemoval. Calling the superclass first retains the inherited
  copycat removal dispatch and migration hook before wrapped neighbour updates.

Validation: cached javac reports 323 sources, 222 errors / 100 warnings (previous:
224). No diagnostics in the crank renderer/entity, renderer registration or
CopycatStairsBlock. No Gradle build or game launch. Wheel orientation, lighting,
kinetic/independent animation and visualization switching still need in-game QA;
copycat material drops depend on the inherited Create removal implementation.


### Water catalyst block and item tint sources (2026-09-30)

- Replaced removed BlockColor/ItemColor callbacks with client event registration
  for BlockTintSource and an ItemTintSource codec. Removed the old Registrate
  color callbacks from FAN_SPLASHING_CATALYST registration.
- Block tint retains biome water coloring at the world position and the uncolored
  fallback outside the world. The item tint queries the current baked water
  fluid model, replacing the removed IClientFluidTypeExtensions.getTintColor
  API and reflecting fluid model updates after resource reloads.
- Added assets/create_connected/items/fan_splashing_catalyst.json using the
  existing item geometry and create_connected:water tint at index zero.

Validation: one cached javac pass, 323 sources, 217 errors / 100 warnings
(previous: 222). No CCColorHandlers diagnostics. Checked item JSON parsing,
model/parent asset existence, tint identifier and geometry tint index zero.
No full build or game launch; biome tint, composite model loading, inventory
appearance and resource-reload behaviour still need in-game QA.
Copycat geometry migration remains pending and was not changed in this pass.


### Full cube copycat material model (2026-09-30)

- CopycatBlockModel now uses DelegateBlockStateModel rather than legacy
  CopycatModel/BakedModel. The full cube forwards the selected material's modern
  model parts with their original cull faces, ambient occlusion and materials;
  no quad cloning or cropping is needed for the existing full cube behaviour.
- Registered the wrapper in the client model bake event and removed its obsolete
  CreateRegistrate.blockModel callback. Unpainted/missing/air materials fall back
  to the original cube model. Malformed full cube copycat materials are guarded
  against recursive model calls.
- Geometry keys include the material state and the material model's own world
  geometry key. World particle material and material flags also follow that model.
  Registered Create's existing CopycatBlockClientExtensions for dynamic material
  tint values and hit/destroy particles on Connected's full cube.

Validation: one cached javac pass, 323 sources, 210 errors / 100 warnings
(previous: 217). No CopycatBlockModel diagnostics. No Gradle build or game launch.
In-game QA remains for painted/unpainted cubes, material swaps, translucent or
emissive materials, connected textures, particle/tint appearance and reloads.
Other copycat shapes still use the legacy geometry API and require migration.


### Eight copycat shape models and modern quad cropping (2026-09-30)

- Migrated slab, beam, vertical step, board, stairs, fence, fence gate and wall
  models to ConnectedCopycatModel. Kept their original piece assembly, selection
  boxes, rotations, offsets and internal-face masks, replacing legacy BakedModel
  access with the material's modern model parts.
- Added CopycatQuadGeometry for position clipping and packed-UV interpolation.
  The UV basis handles both orthogonal and skewed edges, with a finite fallback
  for degenerate input. New quads retain material info, baked normals and colors;
  source quads are not mutated. ISimpleCopycatModel now uses this helper.
- Each source model part retains its AO, particle material and material flags.
  Cropped quads are submitted without boundary cull faces because assembled faces
  may lie inside the block. Selected material model keys and copycat states are
  included in geometry keys, covering all shape/connection variants.
- Replaced all eight obsolete blockModel callbacks with client model-bake
  wrappers and registered material tint/particle client extensions for the shapes.

Validation: full cached javac, 325 sources, 153 errors / 100 warnings (previous:
210), no diagnostics in the migrated shape models or geometry adapter. A small
standalone Java check of the actual CopycatQuadGeometry passed position clipping,
movement, UV interpolation, reversed axes, skewed edges, degenerate edges, source
immutability and metadata forwarding checks. Fixture:
build/api-check-26.2/CopycatQuadGeometryCheck.java. No full build or game launch;
all shape variants, culling, translucent/emissive materials, tint, connected
textures, particles and resource reloads still need in-game QA.


### Shear pin world model ownership (2026-09-30)

- Replaced the obsolete BracketedKineticBlockModel registration with the modern
  ShearPinModel wrapper at the client model-bake event.
- The wrapper suppresses static world geometry while retaining context-free
  model parts for the rotating kinetic renderer. It keeps the delegate's particle
  material and uses a stable empty-world geometry key.
- The existing BracketedKineticBlockEntityRenderer registration remains responsible
  for rendering the rotating pin and stationary attached bracket. Confirmed
  submitBracket/getBracket calls in the actual cached dependency bytecode.

Validation: one cached javac pass, 326 sources, 151 errors / 100 warnings
(previous: 153). No ShearPinModel diagnostics or legacy BakedModel access errors
at the former registration. No full build or game launch; in-game checks remain
for rotation, attached brackets, visualization, Ponder and model reloads.


### Native simple block and item model datagen (2026-09-30)

- Added CCSimpleModelGen as a client DataProvider writing stable blockstate,
  generated model and modern item-definition JSON through PackOutput paths.
  Registration collects definitions via onRegister rather than legacy model
  callbacks. Generation checks for a real block item before emitting an item
  definition, so the hidden helper blocks do not gain inventory definitions.
- Migrated 19 existing partial-base registrations, two shared empty-catalyst
  models, the dynamic dye-catalyst registration and four wrapped-copycat helpers.
  Dye models retain the content texture namespace/path and with_content parent;
  wrapped helpers retain their barrier parent and generated model identifiers.
- Ordinary item definitions reference existing item models. Generated dye models
  can be referenced directly. The splashing catalyst definition retains its
  custom water tint and matches the existing main-resource definition exactly.

Validation: final cached javac, 327 sources, 125 errors / 100 warnings (previous:
151). No CCSimpleModelGen or modified datagen-event diagnostics. Checked all 19
existing block/item asset pairs and their JSON. Executed the exact production
blockstate/item JSON helper methods in an isolated Java fixture, checking the
empty variant, normal item model and water tint; water output matches the main
item definition. No full registry-backed datagen run, Gradle build or game launch.
Conditional dye registrations, generated file coverage and other datagen
providers still require end-to-end validation after the remaining migration.


### Directional clutch/bridge and item silo model datagen (2026-09-30)

- Extended CCSimpleModelGen with complete per-state directional variant output,
  deterministic property keys and the existing six-direction rotation convention.
- Migrated centrifugal/freewheel clutch state-dependent coupled/uncoupled models,
  kinetic bridge source/destination models, and the item silo's shared model.
  Modern item definitions reuse existing item models when a block item exists.
- Removed the five corresponding legacy directional/variant builder callbacks.

Validation: one cached javac pass, 327 sources, 120 errors / 100 warnings
(previous: 125), no CCSimpleModelGen diagnostics. The exact production directional
JSON helper ran in isolation; all 36 saved upstream variants match its rotations,
and all referenced block model assets exist. Fixture:
build/api-check-26.2/DirectionalModelJsonCheck.java. No full registry-backed
datagen run, build or game launch; state coverage and runtime visuals still need
end-to-end validation.


### Linked control multipart model datagen (2026-09-30)

- Added CCLinkedModelGen writing native multipart JSON through CCSimpleModelGen.
  Migrated linked button, lever and analog-lever registration callbacks; the
  optional Simulated throttle lever uses the same analog-mode registration.
- Preserved base control selection, module powered/locked variants, mounting face
  rotations, horizontal facing and wall-button UV locking. Kept the legacy
  lever's powered/off model choice as-is. Analog bases remain independent of the
  powered condition. Full turns are normalized to zero for equivalent rotation.
- Removed the obsolete multipart generators from CCBlockStateGen. Remaining
  sequenced pulse, gearbox and axis generators still require migration.

Validation: one cached javac pass, 328 sources, 114 errors / 100 warnings
(previous: 120). No CCLinkedModelGen diagnostics. Exact production JSON helpers
ran in isolation: 204 multipart entries match saved upstream oak-button, lever
and analog-lever resources after normalizing rotations modulo 360 and defaults.
Fixture: build/api-check-26.2/LinkedModelJsonCheck.java. No registry-backed datagen
run or game launch; optional Simulated compatibility remains blocked by missing
local dependency APIs in the diagnostic classpath.


### Pulse generator, brass gearbox and parallel gearbox model datagen (2026-09-30)

- Replaced the remaining CCBlockStateGen legacy builders with native JSON output:
  32 pulse state variants/eight texture models, 48 brass gearbox face variants
  and models, and three parallel gearbox axis variants. The gearbox/axis state
  keys omit waterlogged as before. Registered via onRegister and the client
  CCSimpleModelGen provider, which now also writes extra generated block models.
- Fixed a saved generated pulse model parent cycle: its base model formerly used
  its own identifier as parent. Preserved the authored geometry in a separate
  sequenced_pulse_generator_template resource and point all eight pulse models
  at it. Updated their saved generated JSON as well as the generator output.

Validation: one cached javac pass, 328 sources, 111 errors / 100 warnings
(previous: 114), no CCBlockStateGen/CCSimpleModelGen diagnostics. Exact production
JSON helpers executed in an isolated fixture and matched all 83 saved state
variants and 56 generated models (pulse parents include the deliberate cycle
fix). All parent model resources exist and none of those models references itself.
Fixture: build/api-check-26.2/PulseGearboxJsonCheck.java. No full registry-backed
datagen run or game launch; generated file coverage, conditional registration
and visual appearance still require end-to-end validation.


### Fluid vessel native block and item model datagen (2026-09-30)

- Replaced FluidVesselGenerator's legacy SpecialBlockStateGen/ModelFile builders
  with native JSON registered through CCSimpleModelGen. Preserved axis, positive/
  negative ends, window shapes and the conversion from single-window to regular
  window variants when both ends are present.
- Creative variants emit their shared-geometry texture overrides. Both inventory
  models now emit directly, replacing the old customBlockItemModel and
  withExistingParent callbacks. Modern item definitions reference these models.

Validation: one cached javac pass, 328 sources, 110 errors / 100 warnings
(previous: 111), no FluidVesselGenerator diagnostics. Exact production JSON
helpers ran in isolation and matched all 144 saved state variants, 64 creative
texture models and both item models; every creative parent asset exists. Fixture:
build/api-check-26.2/VesselModelJsonCheck.java. No registry-backed datagen run or
game launch; connected texture appearance and creative item presentation still
need runtime validation.


### Kinetic bridge render-state migration (2026-09-30)

- Replaced old partialFacing/buffer rendering with extractRenderState/submit for
  both bridge endpoints. Snapshots carry model orientation, kinetic angle and
  separate shaft/coupling light coordinates; submission reads no live block entity.
- Registered source/destination coupler standalone models and reuse Create's
  SHAFT_HALF standalone model. Resolve baked models each submission for reloads.
- Retained endpoint-facing inversion, rotation around the positive kinetic axis,
  and the original behind/front light sample positions. Modern orientation uses
  the SOUTH-authored models, consistent with KineticBridgeVisual.
- Lighting now uses native LightCoordsUtil.getLightCoords. Visualization ownership
  guards avoid submitting the same moving parts through both rendering paths.

Validation: one cached javac pass, 328 sources, 105 errors / 100 warnings
(previous: 110), no KineticBridgeRenderer diagnostics. Coupler JSON resources
exist and parse. No full build or game launch; endpoint orientation, motion,
lighting, visualization ownership and reloads still need in-game validation.


### Dashboard native block and item model datagen (2026-09-30)

- Replaced the last horizontalBlock callback in CCBlocks with native dashboard
  variant JSON through CCBlockStateGen/CCSimpleModelGen. Preserved four facing
  directions, open/closed geometry and both waterlogged values.
- Generates the existing open-dashboard item model parent directly and its
  modern item definition. Removed the legacy customItemModel callback and
  explicitly completes the item builder before registering the block.

Validation: final cached javac, 328 sources, 104 errors / 100 warnings
(previous: 105), no CCBlocks or CCBlockStateGen diagnostics. The exact production
JSON helper ran in isolation and matched all 16 saved dashboard state variants.
Fixture: build/api-check-26.2/DashboardModelJsonCheck.java. No full datagen run or
game launch; remaining diagnostics are concentrated in optional compatibility
code and access-transformer-dependent button fields.


## Optional coloring data map migration

Replaced the Registrate data-map callback and direct Dragons Plus registry
reference with a native server DataProvider. It writes the original
`create_dragons_plus:data_maps/block/fan_processing_catalysts/coloring` map
from the registered catalyst colors. Vanilla colors retain the Dragons Plus
mod condition; Dye Depot colors retain the combined Dragons Plus / Dye Depot
condition. NeoForge's current ConditionalOps keys are used for each entry.
The generator no longer needs Dragons Plus classes to compile or load.

Validation: isolated execution of the production JSON helper matched all 32
saved coloring entries, including both condition branches. Cached javac of
328 sources reports 102 errors / 100 warnings (previously 104); no diagnostics
remain for CCDataMapGen or CCDatagen. Log:
`build/api-check-26.2/compile-after-coloring-map.log`.
Full datagen and in-game behavior remain untested.


## Optional mixin target isolation

Copycats+ slab/board and Simulated throttle-lever mixins now use named targets,
@Pseudo and the existing ModMixin plugin's mod-presence gate. This keeps the
same target classes and injections while avoiding mandatory target resolution
when those optional mods are absent. The Copycats+ mixin no longer imports
external block classes. The throttle mixin still needs Simulated's block class
for its original instanceof checks; the linked throttle content also remains
dependent on Simulated. No integration behavior was removed.

Validation: cached javac of 328 sources reports 97 errors / 100 warnings
(previously 102). CopycatBlockMixin diagnostics are gone; three dependency
diagnostics remain for ThrottleLeverBlockMixin. Log:
`build/api-check-26.2/compile-after-optional-mixin-targets.log`.
Mixin application with and without these mods has not been tested in-game.


## Throttle mixin dependency isolation

Removed the remaining Simulated class import from ThrottleLeverBlockMixin.
The replacement guard walks each block's superclass chain and matches the
original target's fully qualified name. It accepts both the base throttle
lever and derived linked levers, preserving the previous instanceof guard
without loading optional classes. The helper is @Unique to avoid target
method collisions. The prior mod-presence gate and named target remain.
Linked throttle blocks, entities and renderers still need Simulated's API.

Validation: isolated execution of the production helper matched assignability
for five classes and all 25 replacement pairs (base, two subclass depths and
two unrelated classes). Cached javac of 328 sources reports 94 errors /
100 warnings (previously 97), with no ThrottleLeverBlockMixin diagnostics.
Log: `build/api-check-26.2/compile-after-throttle-mixin-isolation.log`.
Runtime mixin application remains untested.


## Copycats block registry lookup

CopycatsManager's nine block destinations now store identifiers and resolve
blocks through BuiltInRegistries.BLOCK at conversion time, rather than
referencing Copycats+'s CCBlocks fields. Block-item conversion still uses the
resolved block's asItem(), preserving the existing behavior. Missing block
identifiers return the original source instead of the default registry entry.
Block-state property copying and feature-toggle checks are unchanged.

Validation: all nine destination identifiers matched the saved compatibility
recipe results. Cached javac of 328 sources reports 85 errors / 100 warnings
(previously 94). Log: `build/api-check-26.2/compile-after-copycats-registry.log`.
The two standalone item mappings and FeatureToggle still directly depend on
Copycats+; this is not complete isolation of CopycatsManager. Their APIs and
item identifiers need confirmation before further migration. Runtime registry
conversion and world migration have not been tested.


## Copycats standalone item registry lookup

Replaced the two Copycats+ CCItems field references with immutable identifier
mappings and BuiltInRegistries.ITEM lookup. Destinations copycats:copycat_box
and copycats:copycat_catwalk were confirmed in the author's CCItems source:
https://github.com/copycats-plus/copycats/blob/multiloader/common/src/main/java/com/copycatsplus/copycats/CCItems.java
Missing registry items leave the original item unchanged; block-item fallback
still uses the destination block's asItem(). CopycatsManager initialization
no longer resolves the optional CCItems class.

FeatureToggle.isEnabled remains a direct dependency. The author's source
checks categories, individual toggles and dependencies (both standalone items
are dependent on the board); replacing it with a registry-presence check
would change behavior. The inspected source uses the older ResourceLocation
API, so it does not establish the 26.2 configuration API.

Validation: cached javac of 328 sources reports 83 errors / 100 warnings
(previously 85). Log: `build/api-check-26.2/compile-after-copycats-items.log`.
Runtime conversion and feature configuration remain untested.


## Inventory access model generation

Migrated InventoryAccessPortGenerator from SpecialBlockStateGen and legacy
NeoForge model builders to native JSON output through CCSimpleModelGen.
Both inventory access port and inventory bridge register their model data via
onRegister. The port retains 24 facing/target/attachment variants and six
on/off texture models; the bridge retains 12 axis/attachment variants.
Both item models are generated, including the bridge's separate item geometry.

Validation: isolated execution of the production JSON methods exactly matched
36 saved state variants, six indicator models and two item models. Cached
javac still reports 83 errors / 100 warnings across 328 sources. The port's
ResourceLocation error is gone; the same legacy API diagnostic now surfaces
in KineticBatteryGenerator, so the total did not decrease. Log:
`build/api-check-26.2/compile-after-inventory-models.log`.
Full datagen and in-game rendering remain untested.


## Kinetic battery native model generation

Replaced KineticBatteryGenerator's legacy SpecialBlockStateGen and model
builders with native blockstate/model JSON registered through onRegister.
All six directions, six charge levels and 16 redstone powers are retained.
Power zero selects charging textures; all positive powers select discharge.
KineticBatteryOverrides now registers its six item level models directly.
CCSimpleModelGen supports explicit per-block item definitions so battery
output preserves the existing numeric range dispatch and fallback instead
of replacing it with a single static model. The numeric property ID and
client registration are unchanged; the base item model inherits level zero.

Validation: isolated execution of the production JSON methods matched all
576 saved blockstate variants, 12 block models, six item level models and
the existing modern range-dispatch definition. Cached javac of 328 sources
reports 82 errors / 100 warnings (previously 83); no diagnostics remain for
the battery generators, CCSimpleModelGen or CCBlocks. Log:
`build/api-check-26.2/compile-after-battery-models.log`.
Full datagen and in-game rendering remain untested.


## Eight kinetic model registrations

Replaced legacy Registrate blockstate callbacks for both crank wheels,
six-way gearbox, cross connector, shear pin, inverted clutch, inverted
gearshift and brake with native callbacks. Crank wheels use the existing
all-state directional generator, retaining axis and waterlogged variants.
The other blocks use a native rotated-axis generator; powered variants keep
the original powered model suffix. All eight item parents are generated
through CCSimpleModelGen, including the shear pin's standalone block model.
The old customItemModel callbacks for these registrations were removed.

Validation: isolated production JSON helpers matched all 99 saved variants
and eight item models. Cached javac remains at 82 errors / 100 warnings
across 328 sources, with no CCBlocks or CCBlockStateGen diagnostics. Log:
`build/api-check-26.2/compile-after-kinetic-model-batch.log`.
Full datagen and in-game rendering remain untested.


## Chain, chute and overstress model generation

Replaced the last direct legacy blockstate callbacks in CCBlocks with native
JSON registration for encased chain cogwheel, brass chute and overstress
clutch. Chain sections retain axis/connection-dependent rotations and the
single, horizontal and vertical geometries. Chutes retain downward and
horizontal diagonal shapes, encased/intersection/window distinctions and
waterlogged variants. Overstress clutch preserves all three mechanical
states; uncoupling shares the coupled model as before. Item parents are
registered natively for all three blocks.

Validation: formulas were checked against local Create generator bytecode;
isolated production JSON methods exactly matched all 82 saved state variants
and three item models. Cached javac remains at 82 errors / 100 warnings over
328 sources, with no CCBlocks or CCBlockStateGen diagnostics. CCBlocks now
contains no direct .blockstate callbacks; shared builder transformers and
optional integrations still require separate auditing. Log:
`build/api-check-26.2/compile-after-chain-chute-clutch-models.log`.
Full datagen and in-game rendering remain untested.


## Encased connectors and standalone item model generation

Migrated CCBuilderTransformers' andesite/brass encased cross connectors to
native blockstate generation, retaining all axis rotations and uvlock=true.
Their item parents still use the corresponding cross_connector/item casing
assets. CCItems' seven custom models now register natively: three vertical
gearboxes, deprecated charged battery item, linked transmitter, copycat box
and copycat catwalk. CCSimpleModelGen writes their parent models and modern
items/<id>.json definitions using minecraft:model, including standalone items
that do not participate in a block registration. No legacy .model callbacks
remain in CCItems or CCBuilderTransformers.

Validation: isolated production JSON helpers matched all six saved encased
blockstate variants and nine item parent models. There were no existing
modern definitions for these seven standalone items to compare; the new
output uses the previously established static item-definition helper.
Cached javac remains at 82 errors / 100 warnings over 328 sources, with no
diagnostics in the changed registration/generator classes. Log:
`build/api-check-26.2/compile-after-encased-and-item-models.log`.
Full datagen and in-game rendering remain untested.


## Remaining 35 block item model callbacks

Migrated all remaining CCBlocks customItemModel callbacks to native item
registration: 26 block-specific item parents and nine shared copycat parents.
CCSimpleModelGen now emits explicit model parents and modern definitions for
these block items, including copycats without a native blockstate definition.
When an explicit item registration exists, automatic block-item output skips
that identifier so each items JSON is written once. Splashing catalyst keeps
its direct model reference and custom water tint; battery range dispatch is
unchanged. CCBlocks no longer imports or calls legacy customItemModel.

Validation: all 35 destinations were checked against saved parent models;
isolated production JSON helpers confirmed the definitions and matched the
existing splashing catalyst definition including its tint. Cached javac
remains at 82 errors / 100 warnings over 328 sources, with no diagnostics in
CCBlocks or CCSimpleModelGen. Log:
`build/api-check-26.2/compile-after-block-item-models.log`.
Full datagen and in-game rendering remain untested.


## Generated standalone item models

Added native generated-item model registration for control chip, incomplete
control chip, redstone link wildcard and both music discs. The provider emits
minecraft:item/generated with the original layer0 texture and a modern
minecraft:model item definition. All 12 standalone CCItems registrations now
have explicit native model output; jukebox components, tags and feature
conditions are unchanged.

Validation: isolated production JSON helper exactly matched five saved item
models and all five referenced texture files exist. Registration inspection
confirmed native callbacks for all 12 standalone items. Cached javac remains
at 82 errors / 100 warnings over 328 sources, with no diagnostics in CCItems
or CCSimpleModelGen. Log:
`build/api-check-26.2/compile-after-generated-items.log`.
Full datagen and in-game rendering remain untested.


## Remaining item parent coverage

Added explicit native item registrations for item silo and sequenced pulse
generator, retaining their standalone block-model parents. The content
catalyst registration also writes the original item parent model for each
color, in addition to its generated block model and existing modern direct
block-model item definition. This covers 16 vanilla and 16 Dye Depot colors
when those colors are registered. Optional-mod feature conditions are
unchanged. The silo and pulse generator use the same item provider path as
other explicit registrations, avoiding duplicate item-definition output.

Validation: all 34 saved parent models match the new destinations, and both
standalone block assets exist. The shared parent JSON helper was verified in
preceding batches. Cached javac remains at 82 errors / 100 warnings across
328 sources, with no diagnostics in CCBlocks or CCSimpleModelGen. Log:
`build/api-check-26.2/compile-after-item-model-coverage.log`.
Full datagen and in-game rendering remain untested.


## Wildcard link center projection

Corrected wildcard link range checks to transform block centers instead of
block corners before applying sublevel poses, matching the author's Sable
Create integration:
https://github.com/ryanhcode/sable/blob/main/neoforge/src/main/java/dev/ryanhcode/sable/neoforge/mixin/compatibility/create/redstone_links/RedstoneLinkNetworkHandlerMixin.java
The 0.5 offset cancels between stationary links, but must be applied before
rotation when links belong to different moving coordinate systems.

The installed Create exposes withinRange(from, to), but Sable redirects its
call sites inside updateNetworkOf, not the method itself. Direct delegation
from the wildcard handler would bypass Sable projection; it was not used.
The existing Companion calls remain and still require the optional API.

Validation: isolated production position helper passed stationary-distance,
rotated range and negative-coordinate checks. Cached javac remains at
82 errors / 100 warnings across 328 sources. Log:
`build/api-check-26.2/compile-after-link-centers.log`.
Sable/Companion 26.2 API compatibility and in-game behavior remain unverified.


## Copycats feature-toggle API bridge

Removed CopycatsManager's last compile-time Copycats+ reference. Recognized
features first require the mod to be loaded, then lazily resolve the same
public static FeatureToggle.isEnabled(Identifier) contract. The bridge invokes
Copycats+'s implementation, retaining its category/dependency behavior rather
than approximating configuration. Only the Method is cached; results remain
live. API incompatibility fails explicitly instead of silently enabling or
disabling migration. Runtime exceptions and errors from the API propagate.

Validation: isolated production bridge against an API fixture passed enabled,
disabled, live configuration, key forwarding and exception propagation checks.
This fixture does not establish a real Copycats+ 26.2 API or runtime support;
that version's binary is still unavailable locally. Cached javac now reports
81 errors / 100 warnings across 328 sources (previously 82), with no
CopycatsManager diagnostics. Log:
`build/api-check-26.2/compile-after-copycats-feature-bridge.log`.
Actual optional-mod loading and in-game migration remain untested.


## Additional Placements optional registration bridge

Removed all three direct Additional Placements imports. Registration remains
in mod construction, behind the existing mod-presence guard. Lazy interface
proxies register the same global blacklist for CopycatBlock,
ICopycatWithWrappedBlock and IWrappedBlock. Untouched initializer callbacks
invoke their actual interface defaults; Object identity methods are handled
explicitly. API signature failures and registration exceptions remain visible.

Contracts were confirmed in the author's 26.1 branch (not a 26.2 binary):
https://github.com/FirEmerald/Additional-Placements/blob/26.1/common/src/main/java/com/firemerald/additionalplacements/generation/Registration.java
https://github.com/FirEmerald/Additional-Placements/blob/26.1/common/src/main/java/com/firemerald/additionalplacements/generation/RegistrationInitializer.java
https://github.com/FirEmerald/Additional-Placements/blob/26.1/common/src/main/java/com/firemerald/additionalplacements/generation/IBlockBlacklister.java
Reference sources are saved only under build/api-check-26.2.

Validation: isolated production proxy helpers against an API fixture passed
single-filter registration, three blacklist categories, default-method and
identity checks. The fixture substitutes block categories and does not
establish actual mod loading. Cached javac now reports 74 errors / 100
warnings across 328 sources (previously 81), with no AdditionalPlacementsCompat
diagnostics. Log:
`build/api-check-26.2/compile-after-additional-placements-bridge.log`.
Actual Additional Placements 26.2 loading and gameplay remain untested.


## Sable range API bridge

Removed direct SableCompanion/SubLevelAccess imports from the wildcard network.
Without the Sable mod, centered link positions use normal squared distance
and no Companion classes are resolved. With Sable, a lazy bridge invokes
SableCompanion.INSTANCE.distanceSquaredWithSubLevels(Level, Vector3dc, Vector3dc).
The method and instance are cached, while each range result is recomputed;
API incompatibilities fail explicitly and underlying runtime failures propagate.
Self-links and the existing strict configured-range comparison are unchanged.

The public method was confirmed in the author's Companion source:
https://github.com/ryanhcode/sable-companion/blob/main/common/src/main/java/dev/ryanhcode/sable/companion/SableCompanion.java
The active implementation projects both points and accounts for the level's
pose provider when available, otherwise using logical sublevel poses:
https://github.com/ryanhcode/sable/blob/main/common/src/main/java/dev/ryanhcode/sable/ActiveSableCompanion.java
This replaces the locally copied projection implementation with the official
range API; no real 26.2 Companion binary was validated.

Validation: isolated production bridge with an API fixture passed stationary
and moving distances, live projection, input preservation and exception
propagation. Cached javac now reports 68 errors / 100 warnings over 328 sources
(previously 74), with no wildcard network diagnostics. Remaining compiler
failures concern Simulated content and two access-transformer field accesses.
Log: `build/api-check-26.2/compile-after-sable-range-bridge.log`.
Actual optional-mod loading and in-game signal behavior remain untested.


## Simulated throttle lever removal callbacks

Replaced the linked throttle lever's legacy onRemove callback with the same
26.2 two-phase module ownership handoff used by the other linked transmitters:
preRemoveSideEffects prepares ownership and affectNeighborsAfterRemoval consumes
it. Wrench removal clears ownership before replacement; moving blocks do not
drop the module, and finishing consumes the pending entry before any drop.
The parent removal callbacks are retained.

The entity now calls the parent's addBehaviours before adding its wireless
link, preserving the author's HoldTipBehaviour. Author source reference:
https://github.com/Creators-of-Aeronautics/Simulated-Project/blob/main/simulated/common/src/main/java/dev/simulated_team/simulated/content/blocks/throttle_lever/ThrottleLeverBlockEntity.java
The native linked model registration also uses Mods.SIMULATED for its resource
identifier, avoiding a direct dependency on Simulated's entry class.

Validation: six short source checks passed callback ordering, parent behaviour
ordering, wrench ownership clearing and the existing helper's moving/consume
guards. These checks do not establish runtime removal behavior. Cached javac
reports 69 errors / 100 warnings over 328 sources (previously 68): unresolved
Simulated inheritance produces additional diagnostics for the new parent
callbacks. Log: `build/api-check-26.2/compile-after-throttle-removal.log`.
The installed diagnostic classpath has no Simulated dependency. Its actual
26.2 renderer, visual and parent callback contracts remain unverified; the
public main branch still uses the older renderer API. No game test was run.


## Native Simulated throttle renderer

Replaced inheritance from Simulated's legacy ThrottleLeverRenderer with an
owned SafeBlockEntityRenderer using 26.2 extractRenderState/submit. Snapshots
contain the handle and button angles, orientation, diode color, optional
handle outline, copied frequency items and slot matrices. Reused states clear
visibility, items and outline before early returns. Submission does not read
the live block entity. Standalone models resolve through the current model
manager, preserving resource reload behavior.

The handle pivot, wall reversal, ceiling rotation, button press pivot and
redstone palette follow the author's renderer and model resources:
https://github.com/Creators-of-Aeronautics/Simulated-Project/blob/main/simulated/common/src/main/java/dev/simulated_team/simulated/content/blocks/throttle_lever/ThrottleLeverRenderer.java
https://github.com/Creators-of-Aeronautics/Simulated-Project/blob/main/simulated/common/src/main/java/dev/simulated_team/simulated/index/SimPartialModels.java
https://github.com/Creators-of-Aeronautics/Simulated-Project/blob/main/simulated/common/src/main/java/dev/simulated_team/simulated/util/SimColors.java
The diode wrapper marks every quad for tinting, including otherwise untinted
faces, while retaining material, normal and vertex-color metadata. Frequency
items use Connected's actual horizontal-lever slots rather than Create's
six-direction redstone-link assumptions. Hover outlines use submitShapeOutline.

Removed the unverified external ThrottleLeverVisual registration: the owned
renderer now draws its models without requiring Simulated's Flywheel visual.
Renderer providers and standalone models register through explicit client
NeoForge events guarded by Mods.SIMULATED, avoiding automatic scanning of
an event subscriber whose entity superclass belongs to an optional mod.
A compatible optimized visual can be restored after validating Simulated 26.2.

Validation: the production renderer and frequency slot compile against the
installed 26.2 libraries with fixture substitutions for the absent Simulated
entity and registration constants. 21 short checks passed nine angle cases
and twelve attachment/direction matrices. Runtime state construction cannot
be tested in this bare JVM without Minecraft bootstrap; Flywheel's transform
extension also requires game mixins, so the matrix reference uses JOML directly.
Source checks passed state reset, snapshot-only submission and removal of
legacy visual/renderer registration. No fixture classes enter the production
source or the full diagnostic classpath.

Cached full javac: 80 errors / 100 warnings across 328 sources (previously 69).
The added diagnostics are unresolved Simulated inheritance/accesses, including
the native renderer provider's generic bound; the actual renderer API passed
the isolated compilation. Log:
`build/api-check-26.2/compile-after-throttle-renderer.log`.
Actual Simulated 26.2 parent members/model availability, optional loading,
rendered tint, hover outline appearance and gameplay remain untested.


## Transmitter wrench authority and throttle pick-block API

Linked buttons, analog levers and Simulated throttle levers now return PASS
for wrench contexts with no player and return SUCCESS on the client before
any inventory, ownership, block replacement or parent sneak-removal operation.
The server retains the existing module return and creative-player behavior;
ownership is cleared before replacing the block so the removal helper does
not return a second module. Both regular and sneak-wrench entry points have
the guard, preventing the latter from continuing into parent removal locally.

Updated the throttle lever's getCloneItemStack to the 26.2 LevelReader/pos/state/
includeData/player signature already used by the other transmitter blocks.
The current player ray hit selects either the underlying base item or the
transmitter module, and the base query receives includeData unchanged.

Read-only review confirmed that CCBlocks registers SimCompatRegistry through
Mods.SIMULATED.executeIfInstalled and the explicit client model/renderer event
paths check the same mod. The installed Create AnalogLeverBlock still declares
its legacy onRemove method as well as the new removal callback; its existing
onRemove mixin was not changed on the assumption that the method disappeared.
Bytecode reference: build/api-check-26.2/analog-lever-removal-api.txt.
The public Simulated main branch also retains onRemove; its actual 26.2 binary
has not been supplied or validated.

Validation: 10 short source checks passed early-return and ownership ordering
and the new base-item signature. Cached javac reports 79 errors / 100 warnings
over 328 sources (previously 80), with no new linked-button/analog-lever errors.
Log: `build/api-check-26.2/compile-after-transmitter-wrench.log`.
No full build or game test was run. Actual client prediction, inventory sync,
sneak-wrench drops and optional Simulated loading remain to be tested in-game.


## Transmitter wax interaction authority

The shared LinkedTransmitterBlock.useWax now returns PASS without a player.
For valid honeycomb/axe interactions the client returns SUCCESS before changing
the block, consuming honeycomb, damaging the axe, triggering advancements,
playing sounds or publishing level/game events. Those effects execute only
on the server. Creative behavior, the server CONSUME result, LOCKED transitions
and TRY_WITH_EMPTY_HAND for unmatched items are retained for all linked buttons
and levers.

LinkedThrottleLeverBlockEntity.setSignal still calls its parent's implementation,
then skips Connected's network/block-state updates without a level or on the
client. Its POWERED block update and wireless notification execute on the server.
The Simulated parent implementation itself has not been verified for 26.2.

Persistence review: installed Create's SyncedBlockEntity implements native
ValueInput/ValueOutput callbacks and delegates to the old CompoundTag hooks.
No additional parallel persistence implementation was added to transmitters.
Read-only bytecode reference: build/api-check-26.2/synced-persistence-api.txt.
This confirms the hook bridge, not a world save/reload test.

Validation: seven short source checks passed client guards before mutations,
parent signal callback ordering and unmatched-item fallback. Cached javac reports
81 errors / 100 warnings over 328 sources (previously 79). The two extra
errors concern the new inherited hasLevel/level references whose Simulated
superclass is absent; the shared wax handler has no compiler errors.
Log: `build/api-check-26.2/compile-after-transmitter-wax.log`.
No full build or game test was run; wax consumption, prediction/sync and
Simulated signal behavior remain to be checked in-game.


## Mixin target correction: value boxes, nested schematics and sequencer UI

Installed Create's ValueBoxRenderer no longer declares customZOffset(Item).
Replaced the required copycat mixin's stale target with a WrapOperation around
the sole PoseStack.translate(FFF) invocation in the six-argument native
submitItemIntoValueBox overload. Wrapped copycat fence/shaft/button/end-rod
items receive the original -0.1 model-space nudge scaled by the new block-item
fixed scale; translation precedes scaling in this API. Other items retain the
installed renderer's default offset and every path calls the original operation.
Only method arguments are captured. The original nudge categories are from:
https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/foundation/blockEntity/behaviour/ValueBoxRenderer.java
Moved this renderer mixin from the common list to the client-only list.

Corrected ServerSchematicLoaderMixin's maxSchematics field slice descriptor
from catnip/config to catnip/api/config. String descriptors are not checked
by javac and this stale slice could prevent required mixin application.
SequencedGearshiftScreenMixin now installs the TURN_TIME step function at
updateParamsOfRow RETURN, after native row setup, instead of shifting two
bytecode instructions from standardStep. Client nested schematic scans now
close each Files.list stream through try-with-resources, including recursion.

Validation: seven bounded bytecode/resource checks passed the unique native
translate target, absence of customZOffset, exact migrated config descriptor,
row method return, client-only mixin placement, stream closure and removal of
the instruction-count shift. All configured mixin Java files exist. Additional
read-only checks confirmed ItemUseOverrides.onBlockActivated's invokeUse call
and DeployerHandler.shouldActivate signatures; no edits were needed there.
Reference dumps are under build/api-check-26.2: value-box-renderer-api.txt,
server-schematic-mixin-api.txt, client-mixin-api.txt and item-use-mixin-api.txt.
These checks do not execute the mixin engine or establish runtime local capture.

Cached javac remains 81 errors / 100 warnings across 328 sources; the changed
mixin classes introduce no compile errors. Log:
`build/api-check-26.2/compile-after-mixin-targets.log`.
Game startup, rendered copycat offsets, nested uploads and sequencer UI behavior
remain untested. No full build or game launch was performed.


## Sequencer enum constructors, initialization and native GUI background

Updated both SequencerInstructions constructor invokers from AllGuiTextures
to String backgrounds, matching the installed Create descriptors including
the enum's synthetic name/ordinal arguments. TURN_AWAIT and TURN_TIME use
sequencer_instruction; LOOP uses sequencer_end. Parameter flags, defaults,
limits and the append order remain unchanged.

Moved the three additions from mixin static field initializers to an explicit
clinit injection immediately after Create assigns its base $VALUES array.
This preserves the five base ordinals and appends the custom values before
later initialization consumers. The installed Catnip codec constructs with a
Class reference and calls Class.getEnumConstants on decode; it does not snapshot
the constants in its constructor. Early extension ensures later enum lookup
and caching see the completed values array, without resetting enum caches.

Installed SequencedGearshiftScreen.backgroundFor uses an exhaustive switch
whose default throws MatchException. Added a cancellable HEAD hook returning
SEQUENCER_INSTRUCTION for TURN_AWAIT/TURN_TIME and SEQUENCER_END for LOOP, while
letting every standard instruction use the original switch. This prevents
custom instructions reaching that unsupported default branch.

Validation: 12 bounded source/bytecode checks passed both constructor descriptors,
three additions, unique original values assignment, append-before-codec order,
GUI helper signature and all custom background mappings. Read-only checks
also confirmed the nested-export createDirectories call and instruction runtime
method signatures. References: build/api-check-26.2/schematic-sequencer-mixin-api.txt,
client-mixin-api.txt, enum-codec-api.txt and enum-codec-implementation-api.txt.
These checks do not run Mixin or test actual enum mutation/packet round trips.

Cached javac remains 81 errors / 100 warnings across 328 sources, without new
sequencer diagnostics. Log:
`build/api-check-26.2/compile-after-sequencer-enum.log`.
Actual mixin application, custom instruction GUI display, packet encoding,
save/reload and sequencer execution remain untested; no full build or game launch.


## Inventory and vessel mixin contract verification

Installed PackagerBlockEntity.supportsBlockEntity(BlockEntity) is a private
instance method. Removed static from Connected's HEAD injection handler and
selected its exact descriptor, with remap=false for the Create method. The
existing checks rejecting inventory ports/bridges attached to portable storage
interfaces are unchanged; the previous handler's staticness did not match the
target and could prevent mixin application.

MountedStorageManager.readLegacy still calls iterateCompoundList twice, first
for item storage. Updated the ordinal-0 injection target to Catnip's api/nbt
package and selected the full readLegacy descriptor. Its item addStorage method
is private in the installed binary; changed the shadow from protected abstract
to a private stub matching that visibility. Legacy silo restoration and its
NoFuel marker are unchanged. Removed two unused imports.

Validation: 11 bounded source/bytecode checks passed target instance/staticness,
packager and legacy method descriptors, private addStorage shadow and the two
native legacy list calls. FluidTankBlockEntity read/write and bounding-box hooks,
SteamEngineBlockEntity.isValid and PackagerBlock placement signature were also
confirmed against the installed binary; they required no changes.
References: build/api-check-26.2/inventory-vessel-mixin-api.txt and
mounted-storage-mixin-api.txt. Checks do not execute Mixin or establish runtime
access transformation and handler application.

Cached javac remains 81 errors / 100 warnings across 328 sources; changed mixin
classes have no new compiler errors. Log:
`build/api-check-26.2/compile-after-storage-mixins.log`.
Packager restrictions, legacy silo restoration, tank persistence and steam-engine
operation remain untested in-game. No full build or game launch was performed.


## Cross-connector propagation and chunk loading

The native RotationPropagator filters unloaded neighboring positions, then
returns KineticBlockEntity.addPropagationLocations' list. Connected's forwarding
pass now preserves that loaded-position rule along the entire connector chain:
it checks isLoaded before querying a forwarded block and removes an unloaded
endpoint from the returned candidates. It returns a separate ArrayList through
a cancellable RETURN hook instead of clearing the provider's returned list,
which may be immutable or shared. Special BlockPos subclasses remain excluded
from forwarding as before. Selected exact native method descriptors.

CrossConnectorBlock.updateConnections now compares BlockPos coordinates through
equals instead of reference identity, checks chunk loading before walking to
a neighbor and skips unloaded endpoints before getBlockEntity. Loaded endpoints
still call the original kinetic refresh, and forwarding axis/shaft rules are
unchanged. No level queries were added to client-side updates.

Validation: nine short source checks passed loading guards, value comparison,
separate return list and preservation of special positions. Bounded bytecode
review confirmed axis/neighbour method signatures, chain-drive invoke target,
redstone network add/update/remove targets, track paving BlockItem.getBlock and
HashSet anchors, and TrackPaver.isWallLike. Those mixins required no target edits.
Reference: build/api-check-26.2/kinetic-track-link-mixin-api.txt.
No runtime forwarding fixture or mixin engine was run.

Cached javac remains 81 errors / 100 warnings across 328 sources, without new
cross-connector/rotation diagnostics. Log:
`build/api-check-26.2/compile-after-cross-connector-loading.log`.
Actual chunk-boundary reconnection, kinetic refresh, wildcard-network and
copycat paving behavior remain untested in-game. No full build or game launch.


## Copycat vanilla pathfinding mixin target

Updated WalkNodeEvaluatorMixin from the removed getBlockPathTypeRaw method
returning BlockPathTypes to the installed getPathTypeFromState(BlockGetter,
BlockPos) returning PathType. Its single BlockState.getBlock invocation still
allows the same wrapped-block substitution, with a static handler matching
the target. Kept require=0 for compatibility with optimization mods that
replace this implementation; previously the stale selector could silently
skip the copycat gate pathfinding correction even without an optimization mod.
Native NeoForge block-path-type overrides still execute before this invocation.

Validation: 12 bounded source/bytecode checks confirmed the exact native
pathfinding descriptor, unique getBlock invocation, handler delegation and
optional-injection policy, plus vanilla fence connection, stair classification,
wall-post and farmland/path survival targets. Seat movement and structure
transform method signatures were also confirmed; no edits were needed there.
References: build/api-check-26.2/copycat-vanilla-mixin-api.txt and
copycat-path-transform-api.txt. These checks do not execute the mixin engine.

Cached javac remains 81 errors / 100 warnings across 328 sources; no new
copycat mixin compiler diagnostics. Log:
`build/api-check-26.2/compile-after-copycat-path-mixin.log`.
Mob movement through copycat gates, seat movement and structure rotations
remain untested in-game. The separate legacy Radium/Lithium PathNodeDefaults
compatibility target was not migrated without authoritative target evidence;
its actual 26.2 optional-mod API remains pending. No full build or game launch.


## Batch audit of installed mixin contracts and screen widget removal

Added tools/audit_mixin_targets_262.py and tools/MixinTargetAudit.java for a
short, reproducible ASM audit against the cached installed dependency classpath.
It reads configured common/client mixin sources, resolves target classes without
initializing them and checks literal method selectors, handler staticness,
literal method/field instruction anchors, and Shadow/Accessor/Invoker member
names/staticness (including inherited members). Reports stay under
build/api-check-26.2/mixin-audit. Run:

```powershell
python tools/audit_mixin_targets_262.py
```

This is a subset source parser and a diagnostic report, not a Mixin test or
pass/fail gate. Member descriptors, local captures, NEW/RETURN/FIELD ordinals,
slices, overwrites, access transformations and runtime remapping are not validated.
Unavailable optional targets are reported, not supplied as fake dependencies.

The initial audit identified AbstractSimiScreenAccessor's removed removeWidgets
method. Updated its mixin target to vanilla Screen and invoke removeWidget for
each widget through a default callRemoveWidgets adapter, retaining the existing
sequencer call site and collection API. Moved the accessor from the common
mixin list to the client list to avoid a common target referencing client GUI.
The native Screen.removeWidget(GuiEventListener) signature was verified separately
in build/api-check-26.2/screen-widget-api.txt.

Final audit: 66 method selector/target pairs matched, with no detected handler
staticness or literal instruction-anchor discrepancies; all 24 member
name/staticness pairs matched. One optional require=0 target, FenceBlock's
hasProperties, is absent from the baseline because Diagonal Fences supplies it.
Four selector/target pairs are unavailable: two Copycats blocks, legacy Radium
PathNodeDefaults and Simulated's throttle block. Their runtime contracts remain
unverified. Per-row results: build/api-check-26.2/mixin-audit/results.tsv.

Cached full javac remains 81 errors / 100 warnings across 328 production sources;
no new sequencer/accessor diagnostics. Log:
`build/api-check-26.2/compile-after-batch-mixin-audit.log`.
Actual mixin transformation, screen widget removal, optional integrations and
dedicated-server startup remain untested. No full build or game launch.


## Exact member descriptors and access-transformer cleanup

Extended the batch audit to erase member source generics and build JVM field/
method descriptors, including nested classes, arrays and enum constructor
arguments. A member match now requires its name, exact descriptor and staticness
to agree together, preventing a similarly named field or overload from passing.
All 24 configured Shadow/Accessor/Invoker entries match the installed targets.
The 66 available injection selectors and literal anchors retain their previous
matches; four optional targets and Diagonal Fences' baseline-missing method
remain outside successful checks.

Removed two obsolete access-transformer entries: RecipeProvider.getName() and
Ingredient(Stream) do not exist in installed Minecraft 26.2. The migrated
recipe/ingredient code no longer uses those members. Retained the two ButtonBlock
field rules; type has BlockSetType descriptor and ticksToStayPressed is int,
and both exist as private final fields in the installed binary. No replacement
Ingredient constructor was exposed without a caller requiring it.
Reference: build/api-check-26.2/access-transformer-api.txt.

Validation: the expanded short ASM audit passed all 24 exact member contracts.
Two negative fixtures intentionally supplied the wrong CACHED_PARAMETERS field
type and removeWidget method signature; both were reported as descriptor
mismatches. Fixture records stay under build/api-check-26.2/mixin-audit and are
not part of normal audit inputs or production sources. Verified both retained
AT targets separately. Actual access-transformer application is still untested.

No production Java changed, so no redundant full-source javac run was performed.
The latest full cached diagnostic remains 81 errors / 100 warnings across 328
sources in compile-after-batch-mixin-audit.log. The two private ButtonBlock
javac errors remain expected because that diagnostic does not apply AT rules.
Mixin local captures, ordinals, slices, overwrites, remapping and actual startup
remain unverified. No full build or game launch was performed.


## Moving menu range and packet API batch

Updated eight production files. TrackingContainerLevelAccess now shares the
installed vanilla menu range check, Player.isWithinBlockInteractionRange with
padding 4, instead of a fixed squared distance of 64. Both moving-menu mixins
delegate to that check. Removed entities and players in a different level fail
validation; removed entities also stop ContainerLevelAccess evaluation. Both
tracking world/access wrappers retain immutable local positions. Reference:
build/api-check-26.2/menu-range-api.txt.

Menu interactions on the server now stop when the entity was removed, its
contraption is absent, or the local block no longer exists, before accessing
the block state. Pulse configuration packets validate their NBT list type and
copy the list so later caller mutation does not alter the pending packet.
Instruction loading skips non-compound list elements while retaining existing
unknown-ID and empty-list behavior.

Replaced the jukebox packet's custom seven-field codec with the installed
StreamCodec.composite overload. Field codecs and wire order remain unchanged;
the redundant unchecked-cast helper was removed.

Validation: one cached full-source javac run reports 81 errors / 100 warnings
across 328 sources, with no diagnostics in the eight changed files. Log:
`build/api-check-26.2/compile-after-menu-packet-api.log`. Short source checks
confirmed the range/removed-entity branches, defensive copies, NBT type guards
and unchanged jukebox field order. These are source and compiler checks; actual
packet round trips, moving-menu interaction and mixin transformation in the game
remain untested. No full build or game launch was performed.


## Moving workbench menus: code adaptation complete

Scope: crafting table, stonecutter, grindstone, smithing table, loom and
cartography table interaction on contraptions. The installed 26.2 block bytecode
confirms that all six open menus using ContainerLevelAccess.create. Their menu
implementations use the shared stillValid checks (smithing via ItemCombinerMenu)
and return input items through ContainerLevelAccess.execute on removal.
References: build/api-check-26.2/moving-menu-contracts.txt and
build/api-check-26.2/moving-menu-lifecycle.txt.

Corrected a lifecycle regression in the previous batch: suppressing evaluate
after entity removal also suppressed native menu cleanup. Evaluation now follows
the live entity and freezes its last world position after removal or level
change, while continuing to execute callbacks in the original world. This
preserves the callback which returns input items when a menu closes after
disassembly. Validity is independently rejected for removed/absent entities,
entity or player level mismatch, absent contraptions, missing local blocks,
incorrect workstation type, and out-of-range players.

AbstractContainerMenu's mixin now checks its supplied target block against the
contraption block state. ItemCombinerMenu uses its native isValidBlock predicate
through an exact-signature shadow rather than accepting any nearby block.
ContainerLevelAccess.create tracks its actual pPos argument. Opening an
interaction also rejects entities in another level. Steam n Rails retains
priority: the six registrations and all three moving-menu mixins are disabled
when railways is loaded; ordinary worlds and client NULL access retain their
existing behavior.

Validation: 15 executable lifecycle checks passed against the actual production
TrackingContainerLevelAccess and installed ContainerLevelAccess default methods.
World/entity/player/block collaborators are deliberately minimal test doubles;
the harness is under build/api-check-26.2/moving-menu-test, not production.
Coverage includes movement, native range-call arguments, range rejection, level
changes, missing/wrong blocks, absent contraption/entity, and continued cleanup
with a frozen position after entity removal. These checks verify callback
execution, not a real player inventory in a running world.

The short ASM audit matches all 66 available selectors and 25 exact member
contracts, including ItemCombinerMenu.isValidBlock(BlockState). The overwritten
ContainerLevelAccess.create signature was verified separately in native bytecode.
The same four unavailable optional targets and Diagonal Fences' optional missing
method remain unrelated to this section. One cached full-source javac run stays
at 81 errors / 100 warnings across 328 sources, with no errors in the five changed
files. Log: build/api-check-26.2/compile-after-moving-menu-completion.log.

This section has no known remaining source/API migration tasks. Runtime release
acceptance still requires the complete mod to compile and launch: actual mixin
transformation, six menu screens on moving/disassembled contraptions and player
inventory return have not been tested in-game. No working release is claimed;
no full build or game launch was performed.


## Moving music: client playback lifecycle complete in code

Seven production files updated. ContraptionMusicManager now removes finished
or stopped sound entries on client post-ticks, stops all retained sounds on
logout, and clears its map when the client world changes. Playback cannot
start for removed entities or entities in a different client world. Record
positions used as keys and packet/sound positions are immutable snapshots.
ContraptionRecordSoundInstance retains a weak original-level reference and
stops when its entity changes levels, in addition to its existing removed/
collected-entity checks. Natural sound completion also clears the nearby-entity
record-playing notification at the sound's final position.

Packet field codecs and order remain unchanged. Start packets require a loaded
world position and an existing jukebox block in the contraption. Stop packets
are allowed through the loaded-position check so an already playing record can
be stopped after its original packet position unloads. Client world/entity
checks still apply; client tick cleanup handles removed entities.

Server jukebox interaction and temporary block entities now reject stale/missing
contraptions or blocks, removed entities and inappropriate levels before NBT
loading or item changes. Both levelEvent overloads explicitly route start/stop
notifications through the same packet path. Jukebox stopMoving no longer
dereferences a missing local block. Shared automatic music/note updates run on
the server only and reject absent/removed entities; this prevents client-side
repetition of automatic note playback.

Native APIs were inspected in build/api-check-26.2/moving-music-api.txt and
wrapped-music-api.txt. The initial lookup of the old common ClientTickEvent
package failed; registration uses the installed client.event package instead.
Validation: 13 executable checks passed for actual production music-manager
and sound-instance classes, using test doubles for Minecraft/audio/world
collaborators. Coverage includes replacement, motion, explicit stop, natural
completion/map removal, removal and level change of the entity, world changes,
logout and repeated cleanup. Harness: build/api-check-26.2/moving-music-test.
Short source checks cover packet field order, stale-block guards, server-only
automatic updates and both client event registrations. No actual audio was
played and no real packet round trip was run.

One cached full-source javac run remains at 81 errors / 100 warnings across
328 sources, with no errors in these seven files. Log:
build/api-check-26.2/compile-after-moving-music.log.

The client sound lifecycle has no known remaining source/API work. This does
not claim the entire musical-block feature is runtime validated: record
insertion/ejection and NBT persistence, server-side song-duration progression,
custom skull instruments, nearby-entity behavior during movement, optional
integrations and actual client event execution still need in-game acceptance.
No full build, game launch or working release was produced.


## Moving jukebox: server duration and persistence

Completed the server song-timer path in JukeboxMovementBehaviour and
JukeboxInteractionBehaviour. Each enabled server movement tick first retains
the existing automatic start/stop behavior, then advances an already playing
record via native JukeboxSongPlayer.tick. Idle jukeboxes without the native
ticks_since_song_started NBT marker do not reconstruct a temporary block entity.
Client ticks, missing/removed entities and disabled controls do not advance
the timer. Existing control-disable and disassembly stop hooks remain active.

Elapsed time is copied into the contraption's current block NBT while the song
continues, preserving the saved item/component data and avoiding full item
serialization on that path. Native song loading refuses to restore records
whose elapsed time has reached their duration. That case explicitly emits the
stop event and re-saves the block entity without the playback marker, so it
does not silently retain a finished song or emit repeated stop notifications.
The record remains inserted after playback finishes.

Ordinary timer changes update the server contraption block map directly. Native
AbstractContraptionEntity.setBlock sends a state packet, so it is used only for
explicit interactions or an actual block-state change, not every elapsed tick.
The temporary block-entity wrapper still routes music start/stop events through
the jukebox packet independently of that state synchronization. Existing public
interaction calls retain their previous synchronization behavior. Native
contracts and persistence inspected in jukebox-server-api.txt and
jukebox-entity-api.txt under build/api-check-26.2.

Validation: 13 executable checks passed using extracted production tickPlaying
and NBT-commit code with native CompoundTag. Song/world collaborators model the
inspected native load-duration boundary; this is not a full production-class or
real-world integration test. Checks cover timer progression, unchanged input
snapshots, component retention, elapsed boundary cleanup, one-time stop,
retaining the disc, resuming a persisted timer, an unrestorable song, absent
blocks/contraptions, no per-tick state broadcast, and explicit interaction
synchronization. Harness: build/api-check-26.2/jukebox-server-test. Short source
checks additionally confirm server/disabled guards and the native tick call.

One cached full-source javac run reports the unchanged 81 errors / 100 warnings
across 328 sources; neither changed file has compiler errors. Log:
build/api-check-26.2/compile-after-jukebox-server-timer.log. The server duration
and NBT path has no known remaining API migration work. In-game verification of
save/reload, disassembly, record insertion/ejection, particles/game events and
actual stop delivery is still outstanding. No full build or game launch.


## Moving note blocks: tuning and playback adapter

Completed the source/API path for manual tuning and automatic moving-note
playback in the two existing note-block classes. The installed Create
SimpleBlockMovingInteraction calls handle on both logical sides without stale
block guards. The owned note interaction now returns early on the client and
rejects removed entities, different levels, absent contraptions or missing/wrong
local blocks before delegating. Its server tuning still uses CommonHooks
onNoteChange, preserves cancellation and the native note cycling/stat behavior.

Both manual and automatic notes now use one shared server playback adapter.
It applies the original local-above obstruction rule and transforms the local
note position to the current world position. Native NoteBlock.triggerEvent
executes against a WrappedLevel view that exposes the tuned state and, for a
custom instrument, loads the local-above skull's NBT/components from the
contraption. This avoids looking up an unrelated stationary-world skull. A
missing/invalid head provides no custom sound, as in native playback.

The wrapper forwards native seeded sound to ServerLevel and sends native note
particle fields through sendParticles with count zero and speed one. It retains
the installed NoteBlockEvent.Play cancellation, pitch, instrument modification
and custom sound logic by invoking the native handler rather than copying it.
The hook sees the wrapped playback view. The game event is emitted in the real
server world, retaining the previous behavior after the native trigger call.
No packet type or new production source file was introduced.

Native contracts inspected in moving-note-api.txt and moving-music-api.txt
under build/api-check-26.2. Validation: 15 executable checks passed for the
extracted production playback adapter with test doubles for world/block/event
collaborators and native CompoundTag for head data. Coverage includes transformed
coordinates, sound/particle forwarding, current tuned state, obstruction,
contraption head lookup, missing custom head data, event cancellation, wrong
level/block, non-server worlds and removed/absent entities. This verifies adapter
behavior, not the actual native event bus, sound engine or profile resolution.
Harness: build/api-check-26.2/moving-note-test. Short source checks also confirmed
that client and canceled tuning paths precede playback and stat changes.

One cached full-source javac run remains at 81 errors / 100 warnings across
328 sources, with no compiler errors in the two changed files. Log:
build/api-check-26.2/compile-after-moving-notes.log. No known remaining source/API
tasks for this path. Real-world custom head/profile resolution, third-party hook
compatibility with the wrapped view, particles, sound and tuning synchronization
remain untested in-game. No full build, game launch or working release.


## Linked transmitters: signal and module-removal lifecycle

Three production files updated. LinkedTransmitterBlockEntity now clamps live
and restored redstone strength to 0?15, and notifies LinkBehaviour only with an
attached server level. Its NBT read tolerates an uncreated link while retaining
the existing rule that a newly positioned server transmitter does not restore
stale saved power. Client NBT updates still restore the displayed signal.
LinkedAnalogLeverBlockEntity uses the same server/attached-level guard for
network notification and its delayed block update. The installed LinkBehaviour
notifySignalChange directly updates the shared redstone-link handler without a
client guard, as confirmed in build/api-check-26.2/linked-signal-api.txt. Native
analog addBehaviours is empty, so there is no missing parent behavior to add.

LinkedTransmitterRemoval pending ownership is now keyed by the actual ServerLevel
in a weak map, rather than only its dimension ID. Positions are immutable
snapshots; empty per-world maps are discarded after finish. This prevents
separate server worlds with the same dimension and coordinates from consuming
one another's pending ownership, and avoids retaining an unloaded world through
the pending map. Existing no-drop-on-movement and already-returned-by-wrench
semantics remain unchanged, with finish consuming a pending entry exactly once.

Validation: 19 executable checks passed for the production removal class and
extracted production transmit/read methods. Level/link/item-drop collaborators
are test doubles; CompoundTag persistence uses the native implementation.
Coverage includes one-time drops, wrench ownership, moving removal, isolated
worlds, mutable input positions, non-server preparation, pending-map cleanup,
signal bounds, offline/client notification suppression, server notification,
missing-link reads and new-position/client NBT behavior. Harness:
build/api-check-26.2/linked-lifecycle-test. Weak-map type and empty-entry cleanup
were checked; no nondeterministic garbage-collection test was performed. Short
source checks also cover the analog lever's guarded notification/update paths.

One cached full-source javac run remains at 81 errors / 100 warnings over 328
sources, with no errors in these three changed files. Log:
build/api-check-26.2/compile-after-linked-signal-lifecycle.log. Remaining compiler
failures still concern unavailable Simulated classes/inheritance and two private
ButtonBlock fields covered by existing AT rules, which this javac run does not
apply. No production compatibility sources were excluded or replaced by stubs.
Actual network delivery, chunk/world lifecycle and wrench/removal drops in-game
remain untested. No full build or game launch.


## Transmitter installation and analog-lever data preservation

Three production files updated. LinkedTransmitterItem now checks a nonempty
stack and the player's item-use permission in addition to the existing build
permission. On the server it replaces the base first, confirms that the module
block is actually installed, and then consumes one item in survival. Failed
replacement returns FAIL without consuming the item. Creative and client
prediction paths retain their no-consumption behavior.

Added a shared replacePreservingData helper and use it for both installation
and removal of the analog-lever module. It captures native block-entity NBT
before replacement and loads it, with components/registry context, into the
new entity. Restored entities are marked dirty, SmartBlockEntity data is sent
to clients, and neighbor updates are requested. Analog installation then
reconciles its POWERED state with the restored analog value and notifies its
server radio link. Sound is emitted only after successful replacement. This
keeps the analog value and native persisted fields instead of resetting the
new block entity to its defaults. The helper is not applied to the unavailable
Simulated throttle integration in this batch.

Analog wrench removal now returns the module item only after replacement is
confirmed. It clears ownership before replacement to suppress a duplicate
removal drop, restores ownership on failure, and returns FAIL. Sneak-wrench
removal respects that failure instead of continuing into base-block removal.
Other button/lever wrench implementations retain their existing behavior.

Validation: 15 executable checks passed for extracted production item-use and
replacement-helper code with native CompoundTag and test doubles for level,
player and block entities. Coverage includes saved analog/component fields,
source snapshot preservation, dirty/client/neighbor notifications, no client
mutation, failed replacement, missing entities, survival/creative consumption,
client prediction, permissions and empty/no-player inputs. Harness:
build/api-check-26.2/transmitter-placement-test. Source checks additionally
confirm analog ownership restoration precedes any inventory return on failure
and both analog conversion directions use the helper. The real native analog
NBT round trip and wrench callbacks remain untested in-game.

Cached full-source javac remains 81 errors / 100 warnings across 328 sources,
without errors in the three changed files. It was repeated once after the
additional wrench-failure fix. Latest log:
build/api-check-26.2/compile-after-transmitter-placement.log. This batch completes
the owned analog conversion and shared installation paths at source/API level,
not Simulated compatibility or a running release. No full build or game launch.


## Button and ordinary lever module conversion

Completed installation/removal handling in LinkedButtonBlock and LinkedLeverBlock.
Both now stop client mutations and missing-player wrench calls, return a module
to survival inventory only after confirmed base replacement, and restore module
ownership and the original transmitted power when replacement fails. Sneak
wrenching stops on conversion failure before removing the underlying base.
Installation and removal sounds are emitted only after successful server
replacement. Orientation and POWERED values remain preserved.

Pressed button conversion now explicitly schedules the resulting block type.
Installing on a pressed button schedules the linked button and transmits its
current power; removing its module schedules the vanilla base button. The former
super.checkPressed call after restoring the base could schedule the linked
block instead. A conversion restarts the base button's full native press
interval; it does not preserve the exact remaining ticks of the original
scheduled entry. An unpressed installed button retains its native arrow check.
The base press duration is captured through a private constructor overload
without adding any new private-field access or changing public constructor calls.

Validation: 22 executable checks passed for extracted production button/lever
conversion and wrench methods. World/inventory/tick collaborators are test
doubles. Coverage includes one-time inventory return without removal drop,
state/orientation preservation, correct scheduled block and duration, failed
replacement ownership/signal restoration, no failure sounds/timers, sneak
wrench failure, client/no-player guards, creative behavior and installation
failure. Harness: build/api-check-26.2/button-lever-test. The harness initially
needed a missing Supplier import; production compilation was unaffected.
Native tick execution and arrow collisions were not simulated.

One cached full-source javac run remains at 81 errors / 100 warnings across
328 sources. LinkedLeverBlock has no errors; LinkedButtonBlock has only its two
existing private ButtonBlock field diagnostics, covered by the AT rules which
bare javac does not apply. No new diagnostics were introduced. Latest log:
build/api-check-26.2/compile-after-button-lever-conversion.log. This completes
the owned button/ordinary-lever conversion paths at source level. Actual
wrench drops, redstone delivery and scheduled native button release/arrow
behavior still require in-game verification. No full build or game launch.


## Wildcard link routes across world and node lifecycle

Verified that the installed native LinkBehaviour owns FrequencyFirst,
FrequencyLast and LastKnownPosition persistence and restores both frequency
item stacks through its native read path. No replacement frequency NBT schema
was introduced. Inspection: build/api-check-26.2/linked-signal-api.txt.

Updated LinkWildcardNetworkHandler's owned route lifecycle. Early route lookups
now prepare and return retained maps; repeated LevelEvent.Load calls keep those
existing maps instead of replacing them. Client worlds are not prepared by load
events, and add/remove/update entry points are guarded to server Level instances.
World unload still removes both maps. Existing world-identity separation is
retained. This prevents routes populated before a load callback from being
discarded or written into a temporary map that is never retained.

Removal now checks for a remaining live node of the same role rather than any
remaining node at the frequency. Removing the last transmitter therefore drops
its routes even when receivers remain at that key, and removing the last
receiver does the converse. Opposite routes retain their existing recalculation
behavior. Incoming strengths are clamped to 0?15 before aggregation, and
squared configured range uses double multiplication to avoid integer overflow.
The Sable bridge, wildcard matching rules and native frequency serialization
were not changed in this batch.

Validation: 14 executable checks passed for extracted production world-load,
map lookup and add/remove routing code with test doubles for native networks,
level events, actors and wildcard matching. Coverage includes early routes,
repeated load, isolated worlds, client/non-Level guards, unload, reciprocal
routes, last-role removal with opposite-role nodes still present, dead nodes
and retained same-role nodes. Harness: build/api-check-26.2/wildcard-lifecycle-test.
These checks do not execute the real wildcard predicate, event bus or
frequency-item serialization; those still need runtime acceptance. Short source
checks verified server guards, signal bounds and the non-overflowing range
expression against the installed APIs.

One cached full-source javac run remains 81 errors / 100 warnings across 328
sources, with no diagnostics for this changed class. Log:
build/api-check-26.2/compile-after-wildcard-world-lifecycle.log. Owned route
initialization and role-aware cleanup are complete in code. Frequency restore
after chunk/world reload, actual receiver delivery and Sable behavior remain
unverified in-game. No full build or game launch.


## Wildcard power: receiver-specific range and reset

Updated the owned wildcard delivery path to calculate each registered live
receiver's power from transmitters within that receiver's range. The previous
path calculated one value relative to the actor initiating the update and
broadcast it only to receivers near that actor. This could miss another
receiver's reachable transmitter, copy an inappropriate value, or leave an
old value after an initiating transmitter moved out of range. The native
Create algorithm uses an actor-relative aggregate too; this is an intentional
behavior correction in the owned wildcard handler, not merely an API rename.
Native reference: build/api-check-26.2/wildcard-power-native-api.txt.

Exact-frequency and matching wildcard transmitters are collected once into
an identity set. Dead nodes are pruned, listeners do not contribute power,
and incoming strengths retain their 0?15 clamp. Each receiver receives the
maximum reachable strength, including zero when no transmitter reaches it.
The registered receiver set is snapshotted before delivery so callbacks can
remove nodes safely; liveness is checked before subsequent delivery. The
initiating native LinkBehaviour receiver retains its newPosition handling.
The Sable-aware range method and strict configured range boundary remain in
use unchanged. World/client/feature guards and the role-aware route lifecycle
from the preceding batch remain intact.

Validation: 18 executable checks passed for extracted production update and
collection code with controlled network/node/range test doubles. Coverage
includes differently positioned receivers, recomputation far from the update
actor, native and generic receiver actors, moving out of range, wildcard/exact
source combination, dead nodes, power bounds, range boundary, callback removal,
last-transmitter reset, disabled/client paths and missing networks. Harness:
build/api-check-26.2/wildcard-power-test. This does not exercise the native
network runtime or Sable transforms. No new mixin targets were introduced.

One cached full-source javac run remains 81 errors / 100 warnings across
328 sources, with no diagnostics in the changed class. Log:
build/api-check-26.2/compile-after-wildcard-receiver-power.log. Receiver-specific
delivery is complete at source/logic level. Computing power now scales with
receiver count times candidate-transmitter count for each affected key; no
large-network performance benchmark was run. Actual network delivery, world
reload and Sable integration remain untested in-game. No full build or launch.


## Wildcard slot matching and dual-wildcard policy

Corrected the owned matching rule to evaluate the two ordered frequency slots
independently: each slot may match through the transmitter wildcard or receiver
wildcard, while both slots must match overall. The previous expression required
both matches to originate from the same endpoint. For example, [wildcard, A]
and [B, wildcard] now connect, consistent with the bundled item tooltip's
per-slot description. Slot order, native ordinary-frequency equality and
custom ILinkWildcard predicates are retained.

A dedicated hasAllowedWildcards check enforces allowDualWildcardLink separately
from actual frequency matching. When disabled, a pair with wildcards in both
slots cannot contribute or receive power, including an identical pair in the
exact-frequency network. Such receivers are updated to zero. Separating this
policy from matching avoids rejecting an allowed selective wildcard by testing
its predicate against itself. Retained cross-frequency routes are rechecked
before supplying power. The setting remains documented as requiring restart;
this batch does not add full route rebuilding for live config changes.

Validation: 41 executable checks passed using extracted production matching,
policy and power-delivery code with controlled frequency/network/predicate
collaborators. The prior delivery regression scenarios are included alongside
ordered exact matches, mismatches, each wildcard endpoint, mixed endpoints,
empty-frequency acceptance, selective predicates, enabled/disabled dual pairs,
identical dual-channel delivery and retained compatible/incompatible routes.
Harness: build/api-check-26.2/wildcard-slot-test. Its initial regression fixture
used the previous always-match stub's ordinary key for a wildcard route; changing
that fixture to an actual wildcard test item made the scenario obey the new
production predicate. These checks do not serialize actual frequency items or
run the native wildcard item/network in-game.

Cached full-source javac remains 81 errors / 100 warnings across 328 sources,
with no diagnostics in the changed class. It was repeated after separating the
dual-policy helper from matching. Log:
build/api-check-26.2/compile-after-wildcard-slot-rules.log. No new source files
or mixin targets were introduced. Owned slot matching and dual policy are
complete in code; world/network integration, item component equality and
third-party wildcard items still require in-game acceptance. No full build
or game launch.


## Transmitter wax interaction and shared frequency-slot properties

The shared wax interaction now rejects absent/spectator players and checks
build/item-use permissions for applicable wax actions. Both honeycomb locking
and axe unlocking require successful server block replacement before criteria,
item costs, sounds or effects are applied. Failed replacement returns FAIL
without consuming honeycomb, damaging the axe or reporting successful waxing.
Client prediction and creative item-cost behavior remain intact. Unrelated
frequency items and inapplicable honeycomb/axe actions keep the previous fallback.

Frequency slots now reference the shared vanilla face, facing and locked
properties instead of the linked button class. Locked render/hit guards and
slot geometry remain unchanged. This does not introduce a new hit-test shape.

Validation: 26 executable checks of the extracted production wax interaction
passed with world/player/item/criterion test doubles. They cover successful and
failed mutations, ordering, client prediction, creative/survival costs, null and
spectator players, denied permissions and item fallback. Harness:
build/api-check-26.2/transmitter-wax-test. Source checks confirm the locked slot
guards remain and the linked-button dependency is removed. Cached full-source
javac remains 81 errors / 100 warnings with no diagnostics in either changed
file. Log: build/api-check-26.2/compile-after-transmitter-wax-access.log.
Actual world events, network synchronization and frequency-slot interaction
still require in-game acceptance. No full build or game launch.


## Optional throttle module wrench safety

LinkedThrottleLeverBlock now refunds the transmitter only after the base block
is confirmed installed. Failed replacement restores containsBase ownership and
returns FAIL. Sneak wrenching stops at that failure rather than continuing to
remove the base. Direct installation/removal are server-only, and controller
sounds occur only after successful replacement. Existing face, facing and
inversion transfer is unchanged. LinkedThrottleLeverBlockEntity.transmit now
requires an attached server level before notifying the shared link network.

Validation: 17 checks passed using current extracted production wrench,
conversion and transmit bodies with modeled block/world/inventory/link and
parent-wrench collaborators. They cover refund/drop ordering, failed mutation,
sneak failure propagation, creative/client/null-player behavior, orientation
and inversion, sound ordering and server-only network notification. Harness:
build/api-check-26.2/throttle-conversion-test. No production stubs or excluded
sources were introduced.

One cached full-source javac run reports 84 errors / 100 warnings across 328
sources (previously 81). The three added diagnostics are unresolved inherited
withBlockEntityDo, hasLevel and level references in the same optional Simulated
classes whose parents are absent from the diagnostic classpath. This is not a
successful compilation of the bridge. Log:
build/api-check-26.2/compile-after-throttle-wrench-safety.log.
The actual Simulated parent wrench implementation, native throttle value/NBT
preservation, removal callbacks and in-game synchronization remain unverified;
this batch does not claim that the optional bridge is complete. No full build
or game launch.


## Gradle environment and resource metadata for 26.2

The Gradle environment now selects Minecraft 26.2, NeoForge 26.2.0.82 and
Java 25. Both wrapper distribution and wrapper task select Gradle 9.5.0.
Parchment is disabled for the current target; the configuration skips its block
when the version is none. The obsolete createSrgToMcp refmap run properties
were removed, and optional enhanced class redefinition is paired with JVM
IgnoreUnrecognizedVMOptions. The data run selects serverData and writes under
build/generated/runData, with both checked-in resource directories as inputs.
No data generation was executed.

Runtime metadata requires Minecraft [26.2], NeoForge [26.2.0.82,26.3) and the
exact diagnosed Create binary [6.0.11-adapted-0.999]. Dependency entries use
NeoForge's required type. The display name is Create: Connected (Adapted), and
the development version is 1.3.3-adapted-26.2-dev. Original authorship and license
remain. The upstream release update endpoint and upstream publishing plugin,
credentials loading and publishMods configuration were removed from this local
adaptation. A distinct adapted icon remains future distribution work.

pack.mcmeta uses min_format [88,0] and max_format [107,1], covering both resource
and data formats read from the installed Minecraft 26.2 version.json. The native
PackFormat.packCodec for CLIENT_RESOURCES and SERVER_DATA accepted that range.
JSON and expanded TOML were parsed, and the actual Gradle processResources output
was checked for resolved placeholders, identity and unchanged mixin/AT resources.
Build-only native codec harness: build/api-check-26.2/resource-metadata-test.

Validation: offline Gradle help and processResources both succeeded on Java
25.0.2 using the actual wrapper. The initial offline help failed because the
upstream publication plugin 2.2.0 was unavailable; after removing the upstream
publication configuration, the retry succeeded. Logs:
build/api-check-26.2/gradle-26.2-environment.log and
build/api-check-26.2/gradle-26.2-resources.log.
The original Maven coordinates for Create, Ponder/Flywheel and optional
integrations are still legacy values, not a resolved 26.2 dependency graph.
No compileJava, full build, JAR, datagen or game launch was attempted. Production
Java was unchanged; the latest cached source diagnostic remains 84 errors /
100 warnings. Metadata/resource preparation is complete for this pass, not the
full build migration.

References: https://neoforged.net/news/26.1release/ (Java 25 / Gradle requirements),
https://docs.gradle.org/current/userguide/compatibility.html and
https://docs.neoforged.net/docs/gettingstarted/modfiles/ (dependency types).
Target versions and pack formats were checked against local 26.2 binaries.


## Pinned local Gradle dependency preparation

Gradle no longer requests the upstream 1.21.1 Create, Flywheel, JEI or optional
integration coordinates. tools/prepare_build_dependencies_262.py stages the
exact cached Create 0.999 JAR after verifying its fixed SHA-256 and native mod
identity. It extracts that same binary's Catnip, isolated create_flywheel,
Ponder and Registrate jars, then stages the diagnosed JEI 26.2 and annotations
jars. The seven dependencies and their hashes are recorded in
build/dependencies-26.2/manifest.json. The cached diagnostic classpath and Prism
files are not rewritten. This is local preparation from an existing private
snapshot, not an online resolver or a portable clean-checkout build.

Create itself is an implementation file dependency. Its nested libraries, JEI
and annotations are compile-only; no duplicate Flywheel/Ponder runtime files
or jarJar copies are declared. Optional third-party dev runtime Maven entries
were removed rather than loading incompatible legacy binaries. All 328
production sources and all compatibility features remain in the source set.
Unused legacy dependency version properties were removed.

Gradle verifyAdaptedDependencies checks the target identity, required unique
roles, project-local paths and file SHA-256 values, including the fixed Create
hash. compileJava depends on requireSimulatedDependency, which first verifies
those files and then requires the actual Simulated compile API. The preparation
tool accepts --simulated-jar only for a real simulated mod declaring Minecraft
[26.2] or [26.2,26.3), with the three required classes and Java <=25 class files.
This conservative metadata/class check does not establish parent API or game
compatibility. No simulated substitute or source exclusion was introduced.

Validation: offline verifyAdaptedDependencies succeeded and verified all seven
staged files; configuration cache was stored without a cache error. Running
requireSimulatedDependency separately failed as expected with the explicit
missing Simulated 26.2 message. Neither test ran compileJava or NeoForm artifact
preparation. Logs: build/api-check-26.2/gradle-adapted-dependencies.log and
build/api-check-26.2/gradle-simulated-dependency-gate.log. Production Java was
unchanged; the latest javac diagnostic remains 84 errors / 100 warnings. No
full resolved Minecraft compile classpath, full build, JAR or game launch.

Environment drift found during this pass: the historical Adventures Beyond
Eternity instance path no longer exists. The current Prism instance named 26.2
lists NeoForge 26.2.0.88 and Create 1.15 (public external assets). This pass uses
the existing, unchanged cached 26.2.0.82 / Create 0.999 baseline; it does not
claim compatibility with or modify the newer installation. Retargeting that
binary and supplying the real optional Simulated API are separate remaining
steps before a release build.

Preparation and focused verification from this project directory:

```powershell
python tools/prepare_build_dependencies_262.py
./gradlew.bat --offline verifyAdaptedDependencies
# When a real supported Simulated binary is available:
python tools/prepare_build_dependencies_262.py --simulated-jar <path>
```


## Current installed Create 1.15 baseline

Following the dependency preparation pass, the current Prism 26.2 installation
was checked separately: Minecraft 26.2, NeoForge 26.2.0.88 and Create version
6.0.11-adapted-1.15, SHA-256
36ace6ebe45dd85ec33bfd08a034a0f5d44e2112f3152c09de625c4a852f13de.
Its four embedded dependency filenames match the diagnosed family, but the
staging tool extracts the actual current bytes rather than reusing older nested
jars. Gradle's target, runtime Create pin, NeoForge requirement, binary hash and
staged manifest now select this newer diagnosed baseline.

The diagnostic and mixin-audit tools accept separate output/classpath options.
Default instance/output now select 26.2 and diagnostics-create-1.15, while the
old 0.999 snapshot remains available. The focused regression runner follows
the current snapshot. If JEI is absent from the runtime instance, diagnostics
can reuse the hash-checked staged JEI compile-only API or an explicitly supplied
--compile-only-jar. This adds no runtime mod or stub.

Validation: one full-source javac run against current installed binaries covers
328 sources and reports 84 errors / 100 warnings. All error locations remain in
the optional Simulated bridge/its registration and the two private ButtonBlock
fields handled by access transformers; no new owned API error locations were
introduced. The old PowerShell log wraps filenames/messages differently, so a
raw text-category comparison is unsuitable for establishing equivalence.
The current ASM audit matches 66 selectors and 25 exact member descriptors.
One optional require=0 FenceBlock.hasProperties selector remains absent in
vanilla; four unavailable optional target selectors are unchanged. Runtime
mixins, locals, ordinals and actual access-transformer application were not tested.

Offline Gradle verifyAdaptedDependencies and processResources succeeded together
with seven newly staged files and configuration cache enabled. Actual processed
TOML contains Create [6.0.11-adapted-1.15] and NeoForge [26.2.0.88,26.3).
Logs: build/diagnostics-create-1.15/compile.log,
build/api-check-26.2/mixin-audit-create-1.15/results.tsv, and
build/api-check-26.2/gradle-create-1.15-dependencies-resources.log.
No compileJava, resolved full NeoForm classpath, full JAR, game or external-assets
acceptance test was performed. The real Simulated 26.2 compile API remains missing.
Prism and the historical diagnostic snapshot were not modified.


## Throttle state retention and delayed radio output

The optional bridge cannot safely shed its Simulated parent types while retaining
native grip behavior: Simulated's client interaction paths use its concrete
throttle block-entity type. No replacement implementation, fake API jar or
source exclusion was introduced. The upstream main sources and the cached
1.21.1 / Simulated 1.2.1 bytecode were consulted only as reference. The legacy
JAR was not added to any 26.2 compile or runtime classpath.

LinkedThrottleLeverBlock now uses the existing native-data replacement helper
when installing and removing the transmitter. It transfers saved data before
reporting successful conversion, preserves face/facing/inversion, and derives
POWERED and the initial radio output from the restored state. This completes
the owned conversion wiring; actual native Simulated 26.2 serialization still
needs a real parent implementation to validate.

LinkedThrottleLeverBlockEntity now observes the parent's change timer around
super.tick and refreshes radio output/POWERED when a pending change completes.
This covers changeState updates that did not pass through the setSignal override.
setSignal clamps its input to 0..15 before the parent's inversion processing;
the existing immediate output refresh is retained. POWERED is rewritten only
when its boolean value changes. Server/attached-level guards protect the owned
refresh. Native client grip/animations and hold-tip behavior remain inherited.

Validation: 24 checks passed using extracted current production conversion,
replacement, transmit, setSignal, tick and refresh bodies with native
CompoundTag and modeled world/link/parent collaborators. Scenarios include
state/timer/custom-data roundtrips, both inversions, orientation, restored
initial power, failed/client conversion, delayed change completion, idle ticks,
clamping, client guards and avoiding redundant POWERED writes. This is not a
native Simulated integration test. Harness: build/api-check-26.2/throttle-state-test.
Reference bytecode: build/api-check-26.2/throttle-parent-api.txt.

One cached full-source javac run reports 91 errors / 100 warnings across 328
sources (previously 84). Error files remain the same optional Simulated bridge,
registration/renderer and the two ButtonBlock private fields covered by AT.
The added diagnostics are inherited field/method/override failures caused by
the absent Simulated parents, including lastChange and super.tick. Log:
build/api-check-26.2/compile-after-throttle-state-retention.log.
No real Simulated 26.2 API, native grip packet handling, converted-data loading,
full Gradle compile, JAR or game acceptance test is available yet.

Reference sources:
https://github.com/Creators-of-Aeronautics/Simulated-Project/blob/main/simulated/common/src/main/java/dev/simulated_team/simulated/content/blocks/throttle_lever/ThrottleLeverBlockEntity.java
and the neighboring ThrottleLeverBlock.java. These are original parent behavior
references, not evidence of a released 26.2 dependency.


## Complete ordinary item-definition resources

The checked-in assets still contained 106 legacy models/item JSON files but
only two hand-authored modern items definitions. Added 98 ordinary item
definitions under src/generated/resources/assets/create_connected/items, each
using the native minecraft:model type and its existing item model. Together
with the retained kinetic battery range-dispatch and water-tinted catalyst
files, all 100 item-model identities have current-format definitions. Six
kinetic_battery_level variants remain model assets, not invented item IDs.
Conditional Dye Depot catalyst definitions retain their existing model IDs;
unused optional resources do not register additional items.

The reproducible offline tool tools/prepare_item_definitions_262.py prepares
missing definitions without a game or full datagen run. --check performs a
read-only validation. The tool preserves hand-authored item definitions and
refuses to flatten legacy overrides without an explicit modern definition.
This supplements the existing native data provider until a full datagen run
is available; it does not recreate the complete registry or generated assets.

Validation: the native Minecraft 26.2 CuboidItemModelWrapper.Unbaked MAP_CODEC
accepted all 98 new model payloads. Full ItemModels.bootstrap initially failed
at NeoForge's mod event because ModList is not initialized outside a game; the
final focused check uses the direct native model codec instead. No loader stub
was introduced. The check establishes simple model-payload decoding, not full
ClientItem dispatch, custom tint/property registration or baking.
Harness: build/api-check-26.2/item-definition-test.

All 219 create_connected model/parent nodes referenced by the new definitions
resolved in the existing main/generated assets, with no missing local parent
or parent cycle. Foreign namespace assets/textures and in-game appearance were
not validated. Offline Gradle processResources succeeded; its output contains
100 items definitions, with the 98 new files and two authored special files
preserved byte-for-byte, and no item definitions for battery level variants.
Log: build/api-check-26.2/gradle-item-definitions.log. Production Java was
unchanged; the last compiler diagnostic remains 91 errors / 100 warnings.
No full build, JAR or game launch.

The user was asked whether the missing Simulated bridge should remain in the
shared build or move into an additional module. No source separation or
feature exclusion was performed in this resource pass.


## Separate optional Simulated companion module

The original five Simulated integration sources were moved intact into
src/simulated/java: registry, linked throttle block/entity/renderer and its
mixin. Two companion entry-point/client-registration classes were added. The
main source set now contains 324 Java files, and the companion contains 7.
This is a deliberate module boundary, not evidence that the full Simulated
integration now compiles. The core artifact does not include throttle bridge
classes or its mixin. The bridge's registry namespace/block IDs remain unchanged.

Core registration uses SimulatedCompat, a common-side loader with no client
imports or Simulated type references. It invokes the bridge only when both
simulated and create_connected_simulated are loaded. Client renderer/model
callbacks load the companion's client registration class only from client
listeners. Both required mods absent, either absent separately, positive
common/client calls and bridge error propagation were covered by 8 checks
using production loader code with modeled mod-presence collaborators.
Harness: build/api-check-26.2/simulated-boundary-test. No real classloader/mod
construction ordering or dedicated-server launch has passed yet.

Companion metadata requires the exact matching core version, Simulated,
Minecraft 26.2 and the current NeoForge range. It owns its mixin configuration.
Gradle defines compileSimulatedJava and simulatedJar, with a required real
Simulated API check; default compileJava requires only the staged core files.
The companion is not injected into default development runs. Core and bridge
jar tasks include LICENSE, and sourcesJar/simulatedSourcesJar retain their
respective source sets. The bridge still needs native Simulated API/callback
porting and runtime acceptance before it can be distributed.

Preparation/diagnostic tools now default to diagnostics-core-26.2.
--include-simulated explicitly adds companion sources to a diagnostic.
Staging records missing_bridge_dependencies rather than marking the core
compile API missing; stable JEI/annotations filenames avoid repeated diagnostic
prefix accumulation. The historical snapshots remain unchanged.

Validation: one cached core javac diagnostic reports only the two known
ButtonBlock private-field errors across 324 files, with no Simulated errors.
Core ASM audit matches 66 selectors / 25 members, with the same one optional
require=0 FenceBlock selector absent and three unavailable optional target
selectors (the fourth moved to the companion). Offline Gradle core dependency
verification and processResources succeeded; companion TOML was expanded and
parsed read-only, and mixin ownership was checked in actual processed core
resources. Logs: build/api-check-26.2/gradle-core-module.log and
build/api-check-26.2/mixin-audit-core-26.2/results.tsv.

A single offline Gradle compileJava attempt failed before compilation while
resolving net.neoforged:neoforge:26.2.0.88 development artifacts. Its apparent
configuration-cache serialization failure wraps the missing offline artifact;
it is not a demonstrated Java or access-transformer failure. No long online
NeoForm preparation was attempted. Core compilation with actual AT application,
full JAR, optional companion compilation, world save compatibility, renderer
appearance and game/dedicated-server startup remain unverified.


## Core development JAR completed (2026-10-01)

The online Gradle build obtained the real NeoForge development artifacts.
Its original JST 2.0.6 widened HolderSet.Named.contents but failed to widen
HolderSet$1.contents, so Minecraft source compilation failed before Connected
compilation. No Minecraft source, global cache or NeoForge binary was patched.
The project now pins the supported neoFormRuntime extension to 2.0.31, whose
external tools include JST 2.0.11. The official transformer fix is
https://github.com/neoforged/JavaSourceTransformer/commit/fbf92a7a23231df019a6b54e80106a056f5ad9ef
(Fix application of ATs to anonymous class members).

With this toolchain, `./gradlew.bat --no-daemon --console=plain jar` succeeds
under JDK 25.0.2. Minecraft preparation and all 324 core Java files compiled;
ButtonBlock access transformers were actually applied. There were no compiler
errors and 100 reported warnings, including deprecated NeoForge item handlers.
The build took 1m 28s including fresh Minecraft preparation. No game or long
regression suite was launched.

Artifact: `build/libs/create_connected-1.3.3-adapted-26.2-dev.jar` (6,842,841 bytes).
SHA-256: `8108a949e849bb9bde945b0458c1542542565f529aaf1f3f56a4c6157d251a97`.
Archive inspection confirmed a top-level class for every core Java source,
436 class entries, all 100 item definitions with exact resource bytes,
expanded metadata pinned to Minecraft 26.2 / NeoForge 26.2.0.88 / Create 1.15,
LICENSE and the access-transformer file. The optional throttle bridge classes,
Simulated registry and companion mixin configuration are absent.
Logs: `build/api-check-26.2/gradle-core-jar.log` and
`build/api-check-26.2/core-jar-inspection.json`.

This completes the core compilation/package milestone. Runtime mixin application,
client/dedicated-server startup, rendering and world behavior remain unverified.
The optional Simulated companion still requires its actual 26.2 API and porting.
The core JAR has not been installed into Prism.


## Create 1.20 and Prism installation (2026-10-01)

The user updated Create in Prism 26.2 and requested installation there.
Read the installed create-adapted-26.2-1.20-public-external-assets.jar metadata:
Create version 6.0.11-adapted-1.20; Minecraft 26.2 / NeoForge 26.2.0.88 are unchanged.
Pinned Create SHA-256:
`c49f26a6fc3cfa29746ba41d9506b4c38888f6081ee240de086ecd082f79c77a`.
Updated staging validation, Gradle manifest validation and the packaged exact
Create dependency range to 1.20. Prepared the current dependency snapshot and
staged its four embedded libraries plus cached JEI and annotations.

A single Gradle jar build succeeded in 8 seconds. The quick ASM audit still
matches 66 selectors and 25 members; its one optional missing FenceBlock method
and three absent optional target selectors are unchanged. Archive inspection
confirmed the exact Create 1.20 dependency and 100 item definitions.
Log: build/api-check-26.2/gradle-create-1.20-jar.log.
Audit: build/api-check-26.2/mixin-audit-create-1.20/results.tsv.

Installed create_connected-1.3.3-adapted-26.2-dev.jar in
C:/Users/alexn/AppData/Roaming/PrismLauncher/instances/26.2/minecraft/mods/.
There was no previous Connected JAR at the destination. Installed and source
SHA-256 match: `dca29e3297c0b2470b84e1798bfd29f43f8a0a4fcb0f9d5903aaa3384045ad81`.
No other installed mods were changed. No game launch or world test was performed;
Simulated remains a separate, unfinished companion.


## First Prism startup failure: screen accessor (2026-10-01)

The user's first launch reached Mixin preparation and stopped with
InvalidMixinException: sequencedgearshift.AbstractSimiScreenAccessor targeted
Screen as a normal interface mixin, but Screen is a class. The interface had a
default callRemoveWidgets helper in addition to its @Invoker method. This made
it cease to be a pure accessor mixin. Moved the widget iteration into the
existing SequencedGearshiftScreenMixin caller; the accessor now declares only
the abstract @Invoker removeWidget bridge. Widget removal behavior is retained.

One Gradle jar rebuild succeeded in 5 seconds. javap of the actual packaged
interface confirmed it contains only public abstract callRemoveWidget.
Failure log: build/api-check-26.2/prism-screen-accessor-failure.log.
Build log: build/api-check-26.2/gradle-screen-accessor-fix.log.
Replaced the Prism 26.2 Connected JAR after verifying the old hash and saving
it to build/api-check-26.2/create_connected-before-screen-accessor-fix.jar.
Source and installed replacement hashes match. A new game launch has not yet
been observed; this fixes the demonstrated failure, not all runtime acceptance.


## Second Prism startup failure: advancement icons (2026-10-01)

The next user launch passed the earlier screen accessor preparation and reached
mod registry initialization. Connected's CCAdvancements static initialization
called ItemProviderEntry.asStack for an advancement icon during TRIGGER_TYPES
registration. Minecraft 26.2 rejected ItemStack creation with the demonstrated
NullPointerException: Components not bound yet. The subsequent Ad Astra missing
entity attribute messages occurred after registry rollback; they are not the
first failure in this log.

Kept advancement and trigger registration at the existing event. Icon overloads
for ItemProviderEntry and ItemLike now defer stack construction until save,
when the datagen registry provider is ready. Icon resolution occurs before
deferred criteria; whenIconCollected reserves its criterion key and external
trigger flag immediately but reads the resolved icon only during save.
whenItemCollected(ItemProviderEntry) uses the entry's ItemLike directly, without
constructing a temporary ItemStack. Explicit icon(ItemStack) clears an earlier
icon function so overload precedence remains correct.

A single Gradle jar rebuild succeeded in 5 seconds. Inspection of actual
packaged Builder bytecode confirmed no eager ItemStack construction/asStack
calls in those four registration paths. No game or full datagen test ran.
Failure log: build/api-check-26.2/prism-advancement-components-failure.log.
Build log: build/api-check-26.2/gradle-advancement-components-fix.log.
Bytecode: build/api-check-26.2/advancement-builder-bytecode.txt.

Installed the replacement into Prism 26.2, preserving the previous JAR at
build/api-check-26.2/create_connected-before-advancement-components-fix.jar.
Source/installed SHA-256: 24f7786983fb58a6186d516c7adcc69ffd050404f5623d54bfcad11dcf6c031c.
A subsequent launch is still needed to establish further runtime acceptance.


## Third Prism startup failure: jukebox client handler (2026-10-01)

The user's next launch passed registry initialization and started the client,
but failed at Client network registry lock. The crash report identifies
IllegalStateException: Some clientbound payloads are missing client-side
handlers: [create_connected:play_contraption_jukebox]. The installed Catnip
registry supplies the packet codec/common registration, but Connected must
register its own client handler through RegisterClientPayloadHandlersEvent.

Added a listener only in the CLIENT mod entrypoint. It registers the existing
CCPackets jukebox type and delegates to the existing packet.handle(player),
retaining its dimension/entity/block/song validation. Native event bytecode
confirms the overload defaults to HandlerThread.MAIN. Packaged client bytecode
confirms the matching packet type, registration and handler invocation.
No packet wire format or server registration changed.

A single Gradle jar build succeeded in 4 seconds. Replaced Connected in Prism
26.2 after verifying the previous hash and preserving the old JAR under
build/api-check-26.2/create_connected-before-jukebox-handler-fix.jar.
Installed/source SHA-256: dfe4f52c9eb7aa36200647401b12f6620ed79aa57f1269b8afaf3b2f50b7b716.
Failure log: build/api-check-26.2/prism-jukebox-handler-failure.log.
Build log: build/api-check-26.2/gradle-jukebox-handler-fix.log.
Inspection: build/api-check-26.2/jukebox-client-registration-bytecode.txt.
A subsequent game launch and in-world playback remain unverified.


## In-world copycat visibility and names (2026-10-01)

After the client-handler fix, the user entered a world and reported invisible
copycat shapes (including gates/cubes), translation keys for names and flickering
gears. The new log reaches resource loading and world data; it does not contain
a matching gear-render exception. The log also reports legacy recipe/loot and
optional-addon resource errors; those are separate outstanding data migration.

CopycatBlockModel previously rejected CopycatBlockEntity.hasCustomMaterial=false
and delegated to the generated air blockstate model, so unpainted copycats had
no visible geometry. It also depended on live block entities, although chunk
rendering supplies model-data snapshots. It now reads the native shared
CopycatModelData.MATERIAL_PROPERTY through level.getModelData(pos), falls back
to a live CopycatBlockEntity if necessary, then to AllBlocks.COPYCAT_BASE.
Existing custom-material/cropping, air and recursion guards remain.
The fix is shared by the full cube and all eight shaped copycat wrappers.

Minecraft's BlockItem constructor no longer supplies a block description prefix;
its default Item.Properties naming path can use item.create_connected keys.
The English and upside-down language files previously supplied block keys only
for these blocks. Added 172 missing block-item aliases per file, including
existing tooltip text, while preserving pre-existing item translations.
Checked the packaged names for all nine copycat types in both languages.
One Gradle jar rebuild succeeded in 5 seconds.

Installed replacement/source SHA-256:
63ace1b0db85d36201f7ae0ec7c6a51c2b4780a8f0da22051b140880876e26be.
Previous JAR preserved at
build/api-check-26.2/create_connected-before-copycat-render-fix.jar.
Log: build/api-check-26.2/prism-copycat-render-report.log.
Build: build/api-check-26.2/gradle-copycat-render-fix.log.
Visual confirmation after restart is outstanding. Gear flickering remains
unresolved; asked the user which gear family flickers and whether painted
copycats also disappear. No gear renderer was changed without that evidence.


## Stationary/rotating wheel overlap (2026-10-01)

The user clarified the visual symptom: one stationary gear overlaps another
rotating gear. Asked which gear family is affected; no family answer yet.
Source and native bytecode demonstrate an overlapping path for both Connected
CrankWheelBlock.Small/Large: their inherited HandCrankBlock.getRenderShape returns
MODEL, generated world blockstates point at the complete wheel geometry, and
CrankWheelRenderer independently submits that same wheel plus its handle.
Native Create's ModelSwapper hides only its own registered hand crank/cogwheel
blocks, not these Connected blocks. The chain cogwheel casing world models do
not contain the item model's gear elements, so they were not stripped.

Override CrankWheelBlock.getRenderShape to INVISIBLE: suppress only the static
chunk mesh, retaining block entity renderer/visual geometry, collision shape,
selection and item models. The method is public, matching HandCrankBlock.
The final Gradle jar build succeeded in 6 seconds; actual packaged bytecode
returns INVISIBLE. This is a confirmed duplicate path in the two hand-operated
Connected wheels, not a demonstrated fix to every Create gear or visual bug.
A subsequent visual check is required, and the gear-family clarification remains
pending. No original Create code/JAR was modified.

Installed the corrected JAR in Prism 26.2 with a verified source/destination
hash and preserved the previous version at
build/api-check-26.2/create_connected-before-crank-wheel-overlap-fix.jar.
Build log: build/api-check-26.2/gradle-crank-wheel-overlap-fix.log.
Inspection: build/api-check-26.2/crank-wheel-overlap-bytecode.txt.


## Kinetic logic review: five mechanisms (2026-10-01)

Reviewed Kinetic Bridge, Centrifugal Clutch, Overstress Clutch, Brake and Kinetic
Battery against this checkout's original upstream HEAD and the actual installed
Create Adapted 1.20 API/bytecode. The core formulas and state machines were
largely unchanged by migration; API compilation alone had not demonstrated
server authority, reconnection, placement timing or save precision.

Confirmed that Create 1.20 KineticBlock.affectNeighborsAfterRemoval dispatches
the legacy virtual onRemove method, so the bridge's existing cleanup callbacks
are reachable; they were not blindly replaced. KineticBlockEntity server tick
uses updateSpeed to reattach, and GeneratingKineticBlockEntity uses reActivateSource
to refresh generation. Stress providers are registered by CCConfigs, preserving
the upstream defaults (battery impact 64, capacity 32, discharge 64 RPM;
bridge default multiplier setting 40 converts to 16 SU per RPM).

Changes:
- KineticHelper now ignores null/client/removed entities and explicitly requests
  updateSpeed while retaining neighbor notifications and generator reactivation.
- Bridge destination initializes generated rotation on the server, validates
  the paired source's orientation and the cached source's level, clears invalid
  caches and clears the pending-update flag before a potentially reentrant update.
- Centrifugal clutch state changes and reattachment are server-only.
- Overstress clutch state changes/manual reset are server-only.
- Battery charge loads with getDoubleOr, matching putDouble instead of losing
  precision through float. A transient network with zero battery sources no
  longer divides by zero when calculating per-battery consumption.
- Brake's configured impact/redstone formula was retained; it increases network
  stress rather than directly setting shaft speed to zero.

tools/check_kinetic_logic_262.py extracts the current production method bodies
for focused checks with modeled world/network collaborators and native 26.2
Direction/NBT. 299 checks pass: absent/client/removed network guards; server
reconnection/generator refresh; bridge pairing/cache/missing source across six
orientations and initialization; positive/negative/zero centrifugal thresholds
in both rotation directions; directional clutch transfer; brake impacts;
battery mode/charge/face combinations; native double precision; zero/one/two
battery sources, other generators and unloaded stress. This is not a live
Create network simulation. Overstress countdown/redstone state machine and
battery tick rates were source-reviewed, not exercised in a real world here.
Also scanned all main Java files for mismatched literal NBT write/read types;
no remaining matches were reported by that limited scan.

Gradle jar succeeded in 28 seconds (no compiler errors, 100 warnings).
Logs: build/api-check-26.2/gradle-kinetic-logic-fix.log and
build/api-check-26.2/kinetic-logic-test/java.log.
Installed replacement in Prism 26.2 with matching source/destination hashes;
previous JAR preserved at build/api-check-26.2/create_connected-before-kinetic-logic-fix.jar.

This pass covers the five named mechanisms and their shared kinetic update path.
It does not certify every Connected element. Full machine networks, chunk
reloads, real overload/reset behavior, battery saving/placement, recipe migration
and the unfinished Simulated companion still need runtime acceptance. Earlier
focused checks for other subsystems are recorded above. Asked the user for the
specific observed Bridge failure; no answer was available during this pass.
