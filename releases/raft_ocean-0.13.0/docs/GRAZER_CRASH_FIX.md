# Tide Grazer crash fix — 0.9.11

The report identifies `raft_ocean:tide_grazer` ticking on the server. Its native
`TemptGoal.canUse()` reads `minecraft:tempt_range`, but the mod registered only
generic mob attributes. Minecraft 26.3's `Animal.createAnimalAttributes()` adds
this required attribute with the native default range of ten blocks.

The grazer now uses that animal builder, retaining 16 health and 0.2 movement
speed. The fix applies when new or previously saved grazers are instantiated;
no world migration or entity deletion is required. Wheat temptation and breeding
remain enabled.

Install `build/libs/raft_ocean-0.9.11.jar`, replacing the previous mod jar, and
restart the client and any dedicated server. Existing raft saves remain compatible;
the movement protocol remains `raft-movement-9.10`.

Regression checks read the registered temptation range, invoke the exact native
goal that crashed, run a grazer's normal AI for 120 ticks, and run AI after
save/reload and for bred offspring. Other mod gameplay checks remain enabled.

Reopen the affected world, approach the island grazer, and hold wheat nearby.
The world should keep running and the grazer should follow the wheat. Revisit
after saving/reloading, and test breeding two adults with wheat. If a different
crash occurs, provide its new report; the old report's missing attribute is fixed.
