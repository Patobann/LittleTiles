# LittleTiles 1.16.5 port status

This branch ports LittleTiles to Minecraft 1.16.5 / Forge 36.2.39.

The complete 1.12.2 source tree is intentionally preserved as the functional
reference. The unfinished 1.16 source tree is enabled package-by-package so a
broken subsystem cannot hide the state of the working runtime.

## Current milestone: persistent tile foundation

Verified on Java 8 (Temurin 8u492):

- `gradlew clean build` succeeds, including Forge reobfuscation.
- `gradlew runData` starts Forge 36.2.39 and Minecraft 1.16.5.
- `gradlew runClient` reaches an integrated world, registers the block-entity
  renderer, and reloads models and seven recipes without LittleTiles exceptions.
- CreativeCore `archive/1.16` loads from the pinned submodule.
- Forge constructs the LittleTiles mod entrypoint.
- The runtime log contains `LittleTiles 1.16.5 port bootstrap loaded`.
- Grid, vector, box, element, tile, and tile-collection cores compile as active
  1.16 code.
- Sixteen focused tests cover grid/vector coordinates plus box splitting,
  combining, clipping, ray tracing, volume accounting, and current/legacy NBT.
- The Forge runtime smoke-test verifies full `BlockState` properties, element/tile/
  collection NBT, box combination, registry creation, grid conversion, and block
  entity persistence, half-block voxel shape generation, and client update NBT.

The `littletiles:tiles` block, item, and block entity are registered and have
minimal item models, language entries, and a loot table. A client block-entity
renderer draws each base `LittleBox` with its stored vanilla `BlockState`.
Per-element tinting, internal-face culling, full placement modes, packets, screens,
and most tools are not restored yet. The first chisel interaction converts a safe
vanilla block into a full-size little tile while preserving its `BlockState`.
The hammer removes one grid cell without damaging the remaining boxes. In survival,
it atomically stores the exact fractional `BlockState` volume in the original-style
24-slot ingredient bag. The chisel remembers a converted block material and places
single cells back into cavities or adjacent air, atomically consuming bag volume.

## Source policy

- `com.creativemd.littletiles`: untouched 1.12.2 reference implementation.
- `team.creative.littletiles`: historical 1.16 draft being completed.
- `dependencies/CreativeCore`: official `archive/1.16` commit
  `ab445d263aaaa292a18d41c36130e6b8e5f0d678`.
- Mojang official 1.16.5 mappings are required by that CreativeCore snapshot.
- The small `SingularMatrixException` compatibility class replaces a JavaFX
  exception absent from modern OpenJDK 8 distributions.

Clone with submodules:

```text
git clone --recurse-submodules <repository-url>
```

Build with a Java 8 `JAVA_HOME`:

```text
gradlew clean build
gradlew runData
```

## Restoration order

| Stage | Subsystem | State |
| --- | --- | --- |
| 0 | Forge workspace, metadata, CreativeCore, mod entrypoint | Working |
| 1 | Grid and math primitives (`LittleGrid`, vectors, boxes) | In progress: base boxes working; transformable boxes and face adapters next |
| 2 | Little block/material registry and tile serialization | Working for base boxes: state, color, multi-box tile, collection NBT |
| 3 | LittleTiles block entity, NBT save/load, block registration | In progress: block/item/type registered and persistent; world behavior next |
| 4 | Placement, removal, collision, selection, networking | In progress: one-cell survival placement/removal, tile-derived shapes, and vanilla sync working |
| 5 | Client rendering and basic tools | In progress: base boxes render; chisel conversion and creative one-cell hammer cutout work |
| 6 | Blueprints, GUI, undo/redo | Not started |
| 7 | Structures, doors, animations, lifts | Not started |
| 8 | Signals, multiplayer hardening, integrations, optimization | Not started |

## Known draft findings

The historical 1.16 draft originally stopped at the first 100 compiler errors.
They are mostly references to systems that were never moved out of the 1.12.2
package, rather than isolated name changes. Six syntax blockers in the draft
have already been repaired, but those files remain outside the bootstrap source
set until their dependency layer is ported.

The grid/vector/box tests cover default grid initialization, coordinate conversion,
negative block offsets, minimum-grid selection, equality, block-boundary splits,
box combining and cutouts, ray hits, volume accounting, and NBT round-trips.

Legacy slice payloads currently load as their bounding boxes, matching the modern
crash-safe migration behavior. Transformable-box payloads, face slicing, and client
render-box generation remain disabled until their dependency layer is ported.

No feature should be marked working merely because it compiles. Each stage must
add focused serialization/math tests plus a Forge runtime smoke test before the
next stage is enabled.
