# LittleTiles 1.16.5 port status

This branch ports LittleTiles to Minecraft 1.16.5 / Forge 36.2.39.

The complete 1.12.2 source tree is intentionally preserved as the functional
reference. The unfinished 1.16 source tree is enabled package-by-package so a
broken subsystem cannot hide the state of the working runtime.

## Current milestone: original-style box editing foundation

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
- Twenty-nine focused tests cover grid/vector coordinates, box splitting and
  combining, clipping, ray tracing, volume accounting, face coverage, current/legacy
  NBT, tool-grid alignment, inclusive two-point selection, and positive/negative
  multi-block area splitting.
- The Forge runtime smoke-test verifies full `BlockState` properties, element/tile/
  collection NBT, box combination, registry creation, grid conversion, and block
  entity persistence, half-block voxel shape generation, and client update NBT.

The `littletiles:tiles` block, item, and block entity are registered and have
minimal item models, language entries, and a loot table. A client block-entity
renderer draws each base `LittleBox` as direct cuboid faces with all baked
`BlockState` texture layers, absolute per-face UV coordinates, biome/ARGB tint,
state-aware vanilla flat lighting, and full-face culling.
Per-vertex ambient occlusion, partial face clipping, complex non-cube block
models, full placement modes,
packets, screens, and most tools are not restored yet. The first chisel
interaction converts a safe
vanilla block into a full-size little tile while preserving its `BlockState`.
The chisel now follows the original two-point right-click contract: the first click
fixes a corner, the live world overlay follows the cursor, and the second click applies
the inclusive area. Its original-style `fill` behavior subtracts occupied tile volume
and places every free fragment across block boundaries. The hammer uses the original
left-click two-point contract and removes every material intersecting the split area.
Survival bag changes are staged before either tool mutates the world. Tool selection
is cancelled on deselect or secondary-mode use. Tile-dependent collision is dynamic,
so sparse microblocks do not retain an invisible full-block collision shape.

The multi-block selection math and Forge event wiring compile and pass tests. The
right-click chisel build has reached an integrated world without LittleTiles exceptions;
the latest left-click hammer event adaptation still requires the next client restart and
manual interaction smoke-test before it is marked runtime-verified.

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
| 4 | Placement, removal, collision, selection, networking | In progress: original-style two-point multi-block fill/removal, dynamic shapes, staged bag updates, and vanilla sync working; custom action packets and undo next |
| 5 | Client rendering and basic tools | In progress: layered UV-correct BlockState cuboids, biome/ARGB tint, flat lighting, face culling, world selection overlay, RMB chisel and LMB hammer |
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
