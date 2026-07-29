# LittleTiles 1.16.5 port status

This branch ports LittleTiles to Minecraft 1.16.5 / Forge 36.2.39.

The complete 1.12.2 source tree is intentionally preserved as the functional
reference. The unfinished 1.16 source tree is enabled package-by-package so a
broken subsystem cannot hide the state of the working runtime.

## Current milestone: runtime bootstrap

Verified on Java 8 (Temurin 8u492):

- `gradlew clean build` succeeds, including Forge reobfuscation.
- `gradlew runData` starts Forge 36.2.39 and Minecraft 1.16.5.
- CreativeCore `archive/1.16` loads from the pinned submodule.
- Forge constructs the LittleTiles mod entrypoint.
- The runtime log contains `LittleTiles 1.16.5 port bootstrap loaded`.
- `LittleGrid`, `IGridBased`, `LittleUtils`, and `LittleVec` compile as
  active 1.16 code and pass focused coordinate/NBT tests.

This milestone deliberately registers no LittleTiles blocks, items, tile
entities, packets, screens, or renderers yet. It proves the build, mappings,
dependency, metadata, and mod-loading path before gameplay code is restored.

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
| 1 | Grid and math primitives (`LittleGrid`, vectors, boxes) | In progress: grid/vector working; boxes next |
| 2 | Little block/material registry and tile serialization | Not started |
| 3 | LittleTiles block entity, NBT save/load, block registration | Not started |
| 4 | Placement, removal, collision, selection, networking | Not started |
| 5 | Client rendering and basic tools | Not started |
| 6 | Blueprints, GUI, undo/redo | Not started |
| 7 | Structures, doors, animations, lifts | Not started |
| 8 | Signals, multiplayer hardening, integrations, optimization | Not started |

## Known draft findings

The historical 1.16 draft originally stopped at the first 100 compiler errors.
They are mostly references to systems that were never moved out of the 1.12.2
package, rather than isolated name changes. Six syntax blockers in the draft
have already been repaired, but those files remain outside the bootstrap source
set until their dependency layer is ported.

The grid/vector tests cover default grid initialization, coordinate conversion,
negative block offsets, minimum-grid selection, equality, and NBT round-trips.

No feature should be marked working merely because it compiles. Each stage must
add focused serialization/math tests plus a Forge runtime smoke test before the
next stage is enabled.
