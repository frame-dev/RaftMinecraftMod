# Raft Ocean API v3 example addon

Targets Raft Ocean 0.13.0, Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25.
The declared main-mod range is [0.13.0,0.14); choose a tested range for your addon.
Output: examples/raft-addon/build/libs/raft-example-addon-1.2.0.jar.

From the main project, first build its jar into build/libs, then:

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-25'
.\gradlew.bat -p examples/raft-addon build --offline --no-parallel
```

The file dependency reads the current main-project version automatically.
For a standalone copy, update build.gradle to your local jar or repository
coordinate and use a Gradle 9.2.1 wrapper. Install both playable jars on every
client and server, then restart. Do not install sources jars as mods.

## Complete examples

- `raft_example:work_crate`: two-foundation decoration with a server interaction.
- `raft_example:kelp_dryer`: persistent v2-compatible machine. Inventory slots
  1 kelp, 2 output, 3 sticks; remaining slots are storage. A stick dries two kelp
  in five seconds each. Sneak-empty-hand toggles pause; normal use opens slots.
- `raft_example:sediment_washer`: v3 utility machine. One sand plus 10 FE and
  one cup of piped fresh water yields one clay ball per one-second update.
  Slot 1 is sand; slot 2 is clay. Other slots reject insertion. Empty-handed
  sneak-use collects the output. Its output is read-only in the generic menu.

Obtain with /give or the three included normal crafting recipes. Place the
washer beside a raft biofuel generator and a water tank, or connect them with
power cables and water pipes. Add biofuel to the generator and fresh water to
the tank. The washer advertises demand only when sand/output space/water are
ready; idle, dry or blocked machines preserve generator burn reserve.
The washer's enabled state is an API example property, not a custom pause UI.

All examples reuse original Raft Ocean material textures. The washer shares
original example geometry with a teal material variant. Source/model code is
MIT; texture licensing remains with the required main mod.

## Test

Check four rotations, drifting/turning rafts, saves mid-process, full outputs,
missing water/power, removal and co-op pickup. Repeated actions must not duplicate
items. All peers must have matching addon definitions. Native example smoke
checks run when the main bootstrap server loads this addon with
`raftOcean.carrySmoke=true`; ordinary game startup does not run them.

Guides in the main project: docs/RAFT_ADDON_API.md,
docs/MACHINE_ADDON_GUIDE.md and docs/API_V3_UTILITIES.md.
