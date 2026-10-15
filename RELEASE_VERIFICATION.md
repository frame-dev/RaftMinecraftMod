# Release verification — 0.13.0 preview

Addon API v3 verified locally on 2026-10-15 with Minecraft26.3,
NeoForge26.3.0.48-beta, Java25, ModDevGradle2.0.147 and Gradle9.2.1.

## Current checks

- Final isolated build and server boot passed with example addon 1.2.0 loaded;
  both mods saved/closed cleanly. Loader metadata accepted the current main mod.
- 69 native machine API assertions passed: old lifecycle/menu/save behavior,
  multi-resource preflight/commit, exact component batches, blocked outputs,
  same-slot processing, insertion/extraction locks, generator readiness, paused
  sources, global disablement, stale handles, cable removal and all four rotated
  non-anchor footprint connections. A network touching two faces is counted once.
- Loaded example passed 13 native assertions, including the actual utility washer
  generating power and consuming water/sand, idle/blocked conservation and pause.
- Example addon compiled. Standalone SDK starter compiled independently with
  local bundled API dependency and wrapper; no parent project is required.
  The final SDK ZIP was also freshly extracted and built with its own wrapper.
- Final runtime suites: 2,904 movement, 729 carry, 425 collision, 3,641 construction,
  263,322 crafting, 978 survival and 161 navigation assertions plus remaining
  station/research/crew/trading/salvage/creature/config suites passed.
- Native model/motion checks passed unchanged: 952 models/57,779 elements,
  165 fitted GUI silhouettes, 243 rotor/detail, 18,797 fill/mount checks,
  75 meshes/13,934 quads. Packaged coverage/surface bounds and UV audits passed.
- Packaged crafting: 153 main recipes (63 fit 2x2; 90 require 3x3), 1,422 loaded
  rows including three example recipes. Research: 30 projects/60 unlocks.
  Localization: 708 English/German keys, placeholder parity, sounds/config passed.

Evidence: build/release-0.13.0-verified.log,
build/release-0.13.0-addon-build.log, build/release-0.13.0-sdk-build.log
and build/release-0.13.0-sdk-extracted.log.
Earlier API development runs are superseded by the verified run above.
The example jar was installed only into the isolated test server's mods folder
and is removed there after testing. It is not installed in the user's client.

Packages verify jar copies, ZIP integrity and SHA-256 checksums. SDK archives
exclude build caches, worlds, run directories and runtime logs.
Fresh Ocean World/story template audits are carried forward from 0.12.4;
world generation, story content, collision envelopes and model resources were
not altered by the API expansion. The open-ended story and requested propulsion
speeds are retained.

## Pending

Install matching main/addon jars on a local client and server. Inspect washer
models, pipe/cable joins at four rotations, upper-floor/wide connections, resource
shortages, output locks, saves and shared pickup under actual network latency.
Test your own addons/mod combinations; API contracts do not guarantee arbitrary
world-machine transplantation or automatic research/JEI integration.
