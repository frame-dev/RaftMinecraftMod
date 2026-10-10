# Raft Ocean API v2 example addon

Requires Raft Ocean 0.11.17, Minecraft 26.3, NeoForge 26.3.0.48-beta and Java 25.
This is a separate mod with original resources, a two-cell work crate and a
one-cell kelp dryer. The dryer saves inventory, fuel, progress and pause state.

From the main project directory:

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-25'
.\gradlew.bat -p examples/raft-addon build --no-parallel
```

The file dependency expects `../../build/libs/raft_ocean-0.11.17.jar`. If you
unpack this example elsewhere, update that dependency and use a Gradle 9.2.1
wrapper. Output: `build/libs/raft-example-addon-1.1.1.jar`. Install both mod jars
on client and dedicated server and restart. Sources jars are development files.

Use `/give @s raft_example:work_crate` or `/give @s raft_example:kelp_dryer`,
or use the included normal crafting recipes. The crate needs two foundations.
For the dryer, use an empty hand to open its inventory: slot 1 kelp, slot 2
output, slot 3 sticks. One stick processes two kelp, five loaded seconds each.
Slots 4–9 are storage. Sneak-click with an empty hand to pause/resume. The orange
trim indicates activity. Output rejects insertion; incorrect input/fuel is
rejected by the server. No JEI dependency is required.

Check all four placement rotations, save/reload midway, blocked output, pause,
shared multiplayer use, and removal with inventory. Contents should drop once;
loaded inventories should retain exact counts. Test movement and models locally.

Full documentation: `docs/MACHINE_ADDON_GUIDE.md` in the main project.
API reference: `docs/RAFT_ADDON_API.md`.

License: MIT, including the example source/model definitions. Model references
use original Raft Ocean textures supplied by the required main mod.
