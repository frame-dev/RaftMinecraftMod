# Expanded gameplay settings — 0.11.5

All 17 new options retain previous behavior at their defaults. Peaceful protections from 0.11.4 still apply.

Stop the world/server, edit `config/raft_ocean-synced.toml` (or the world's `syncedconfig` override), and restart. Host settings synchronize to clients. Existing settings are retained and missing entries receive their defaults.

| Setting | Default | Range / purpose |
| --- | --- | --- |
| building.pieceLimit | 512 | 1–512. Limit new structural pieces per raft; existing saves remain intact. Technical ceiling is 512. |
| survival.drinkHydration | 6 | 1–20. Hydration restored by one fresh-water cup. |
| survival.sprintThirstMultiplier | 2 | 1–4. Hydration drain multiplier while sprinting. |
| survival.purifierSpeed | 1 | .1–4. Purifier speed multiplier, combined with processingSpeed. |
| survival.rainCollectorSpeed | 1 | .1–4. Rain collector speed multiplier; still requires exposed rain. |
| survival.grillSpeed | 1 | .1–4. Grill speed multiplier, combined with processingSpeed. |
| survival.smelterSpeed | 1 | .1–4. Smelter speed multiplier, combined with processingSpeed. |
| survival.cookingPotSpeed | 1 | .1–4. Cooking pot speed multiplier, combined with processingSpeed. |
| survival.cropGrowthSpeed | 1 | .1–4. Small planter growth multiplier, combined with processingSpeed. |
| survival.treeGrowthSpeed | 1 | .1–4. Tree planter growth multiplier, combined with processingSpeed. |
| survival.machineFuelMultiplier | 1 | .1–4. Fuel duration multiplier for newly consumed survival-machine fuel; existing burning fuel is preserved. |
| survival.planterMinimumLight | 9 | 0–15. Minimum light for both planters; zero removes brightness requirement. |
| survival.plantersRequireOpenSky | true | Boolean. Require both planters to be uncovered by raft pieces; brightness is checked separately. |
| navigation.engineFuelMultiplier | 1 | .1–4. Engine fuel duration multiplier for newly consumed fuel. |
| navigation.engineThrustMultiplier | 1 | 0–3. Engine thrust multiplier; zero stops engines without consuming fuel. Speed cap still applies. |
| navigation.sailThrustMultiplier | 1 | 0–3. Sail thrust multiplier; speed cap and wind alignment still apply. |
| navigation.paddleStrengthMultiplier | 1 | 0–3. Paddle impulse multiplier; zero disables strokes. Speed cap still applies. |

Machine-specific speed multiplies `survival.processingSpeed`, so setting both to 2 gives 4× recipe progress. Inputs/water commit on completion, outputs must have room, and rain collectors still require exposed rainfall. Fuel burns in real ticks; fuel efficiency affects each newly consumed item and preserves fuel already burning. Vanilla mounted furnaces retain native Minecraft behavior; these machine settings affect the custom raft survival machines. Recipe and JEI durations show base recipe ticks, while machine progress reflects configured processing.

Planter brightness and raft-cover requirements are independent. `planterMinimumLight = 0` removes brightness checks; `plantersRequireOpenSky = false` permits growth below raft roofs. This cover setting does not bypass natural-world light calculations. Neither setting changes rain collection.

Building limits reject new additions without deleting saved pieces; loading still supports the technical 512-piece ceiling. Foundations have their separate 1024 limit. No save/network format changed.

Propulsion multipliers preserve direction, anchor behavior, engine foundation load, wind alignment, and the configured maximum raft speed. Zero engine thrust stops fuel burning; zero sail thrust gives no sail force; zero paddle strength rejects new strokes. Existing paddle impulse decays normally. Engine fuel efficiency does not change thrust.

See the [complete default settings](config-examples/raft_ocean-synced.toml), [faster workshop preset](config-examples/presets/workshop-synced.toml), and [Peaceful/co-op guide](PEACEFUL_AND_CONFIG.md). Adopting a preset replaces its full balance settings.

## Test checklist

- Set purifierSpeed=2: purification progresses twice as quickly with ordinary fuel drain.
- Set machineFuelMultiplier=2: the next fuel item lasts twice as long; already burning fuel retains its reserve.
- Set engineThrustMultiplier=0: engines produce no thrust and preserve fuel. Restore 1 to resume.
- Lower pieceLimit: existing structures remain; additional placement is rejected at the limit.
- Change drinkHydration and sprintThirstMultiplier: cup recovery and sprint drain follow the new values.
- Turn off planter sky requirements and set minimum light to 0: roofed planters can grow. Rain collectors still need rain access.

Automated server checks cover actual processing, fuel transactions, propulsion, hydration, building caps, and retained Peaceful/co-op behavior. In-game visuals and network latency still need local testing.
