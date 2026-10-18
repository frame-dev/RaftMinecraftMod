# Configuration â€” 0.9.0







Open **Mods â†’ Raft Ocean â†’ Config** to use NeoForge's configuration screen.



Gameplay settings belong to the world host/server; client settings control your HUD.



For predictable file edits, stop the world/server, edit the TOML file and restart.



Keep the same mod version on every client and server.







## Files







| Purpose | Development path |



| --- | --- |



| Singleplayer gameplay defaults | `run/client/config/raft_ocean-synced.toml` |



| Dedicated-server gameplay defaults | `run/server/config/raft_ocean-synced.toml` |



| Your HUD | `run/client/config/raft_ocean-client.toml` |



| Optional singleplayer world override | `run/client/saves/<world>/syncedconfig/raft_ocean-synced.toml` |



| Optional dedicated world override | `run/server/world/syncedconfig/raft_ocean-synced.toml` |







Installed modpacks use the same `config` and world `syncedconfig` paths relative



to their instance/server folder. World overrides take precedence over global defaults.



In this project's installed FML 12.0.8, gameplay uses the **SYNCED** config type,



which sends the host's settings to connecting clients. Older documentation's



SERVER type and `serverconfig` directory do not describe this pinned loader.



Changing a client's gameplay file does not change a remote server's rules.







Complete default examples are in [config-examples](config-examples/raft_ocean-synced.toml).



The generated files include valid ranges. Tick durations assume 20 server ticks/second.







## Settings







`[building] foundationLimit = 1024` accepts 9–1024 and limits new foundations per



raft. Lower values preserve existing large saves. Set 64, 256 or 512 for your



preferred building preset; 1024 remains the technical save/network ceiling.











| Section | Defaults and effects |



| --- | --- |



| survival | Thirst enabled; one point per 600 ticks (30 seconds), twice as fast sprinting. Processing speed 1.0; fuel burns in actual ticks. Disabling thirst restores hydration. |



| navigation | Current and wind multipliers 1.0; maximum horizontal speed 0.16 blocks/tick (3.2 m/s); turning 0.75 degrees/tick. Zero current disables natural drift. |



| salvage | Enabled; batch every 100 ticks, four attempts; 24 debris per raft, 128 naturally spawned loaded debris per dimension; lifetime 6000 ticks. Existing course-settling behavior remains. |



| creatures | Natural spawning, sharks, birds and friendly fauna enabled; check every 600 ticks; 32 loaded mod creatures per dimension. |



| creatures combat | Shark aggression and damage multiplier 1.0; raft damage enabled; 25 damage per foundation bite, 600-tick base attack delay; bird theft delay 1200 ticks. |



| creatures bait | Float lasts 1200 ticks (60 seconds); maximum 16 loaded item-placed floats per dimension. |



| client | Navigation HUD and thirst HUD enabled. These switches only hide displays. |







`gameMode = "WORLD"` follows Minecraft difficulty; PEACEFUL, EASY, NORMAL and HARD



override mod creature behavior, except that Minecraft Peaceful always takes precedence. Peaceful permits passive sharks but prevents



attacks and crop theft. Hard targets two sharks per occupied raft; Easy halves



bite frequency and Hard increases it by 1.5. See [mode details](RAFT_EXPANSION.md). Easy scales damage



to 0.65, Hard to 1.4; `damageMultiplier` applies on top. Zero damage multiplier



disables hostile attacks and crop theft. Zero shark aggression disables shark attacks.



`raftDamage = false` protects foundations while swimmers remain vulnerable.







Natural spawning also respects Minecraft's mob-spawning game rule. Disabling



spawning stops new creatures; it does not delete existing ones. Disabling sharks



or birds stops their corresponding attacks. Friendly fauna requires suitable



loaded grass or sand nearby. Caps apply to naturally spawned loaded creatures;



operator summons bypass them. No setting forces distant chunks to load.



Existing bait keeps its saved expiry. Attack timers already running may finish



before changed intervals take effect. Raising caps is optional and can cost performance.







For a calmer world, disable raft damage, reduce shark aggression to 0.5 and



increase `birdTheftIntervalTicks`. For construction testing, disable thirst and



natural creature spawning. Retain the existing TOML sections when editing keys.





## Milestone 11 effects and presets



Client `machineEffectDistance = 32` accepts 0–128 blocks; zero disables cosmetic

bubbles, smoke, sparks and rain drops. Water, food, crops and gameplay remain.

Range is measured from the local player to each transformed machine position.

See [milestone 11](MILESTONE_11.md) for balance and low-effect examples.



## Repeated attacks and co-op — 0.11.3



New default foundation damage is 10 per bite; old explicit values are preserved.

`creatures.foundationBitesPerAttack = 10` (1–20) and

`creatures.sharkRepelHits = 3` (1–10) configure attack bursts and crew defense.

`multiplayer.sharedStarterRaft = false` is opt-in shared first entry in Ocean World.

Existing players can use `/raftcrew join` without operator permissions.

See [co-op guide](COOP_AND_SHARK_DEFENSE.md).



See [Peaceful and expanded settings](PEACEFUL_AND_CONFIG.md) for version 0.11.4 attack timing, shark counts, thirst and starter supplies.

Version 0.11.5 adds [expanded building, machine, fuel and navigation settings](EXPANDED_CONFIG.md).

Version 0.11.6 adds [prepared meal bonuses and their duration settings](MEALS_AND_NET_FIXES.md).


Version 0.11.8 adds an `[automation]` section for network enablement, biofuel engine duration, generator yield, purifier energy/time, and sprinkler radius/energy. See [Raft automation](RAFT_AUTOMATION.md) for all seven settings, recipes, routing and capacities.


## Quick collection and interaction hints (0.12.6)

`building.quickMachineHarvest` is a synced server boolean, default `true`.
Sneak-use with an empty hand collects completed machine output slots, leaving
ingredients/fuel/batteries and inventory overflow untouched. Disable to use
ordinary menus for that gesture. It does not drain water reservoirs.

`interactionHints` is a client boolean, default `true`, in the separate client
configuration. It controls short wrapped machine/net/stash prompts under the
crosshair. Hidden HUD, open screens and held items suppress these prompts.
See [quick machine collection](QUICK_MACHINE_HARVEST.md) for behavior/tests.

## Navigation capacity — 0.13.5

`navigation.engineLoadLimits` defaults totrue: shared engine speed, full/half/stall
load bands and a six-powered-engine bypass. False restores older engine scaling.
`navigation.engineFoundationCapacity` retains its saved value (default100, range1–1024).
See [navigation details and testing](SAILING_AND_ENGINE_LOAD.md). Sail drive never
stacks with sail count. Existing speed, wind, fuel and current multipliers remain.

## Cooking batches — 0.13.6

`survival.manualCooking` defaults to true: one recipe per Cook press. Set it to
false for earlier automatic cooking and automation setups. Saved active pots
resume one batch; idle pots wait. See [batch controls and migration](COOKING_BATCHES.md).
