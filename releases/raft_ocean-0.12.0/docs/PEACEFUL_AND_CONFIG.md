# Peaceful and expanded configuration — 0.11.4

Minecraft Peaceful always prevents shark attacks, even with `gameMode = "HARD"`. Selecting the mod's `PEACEFUL` mode also prevents attacks on other Minecraft difficulties. Switching during a foundation bite cancels the burst and clears its animation. Birds cannot steal crops, and dehydration cannot hurt players in either form of Peaceful. Passive sharks remain available.

Stop the world/server before editing gameplay TOML and restart afterwards. Use `config/raft_ocean-synced.toml` relative to your instance/server, or the world's `syncedconfig/raft_ocean-synced.toml` when it has an override. Settings synchronize from the host to every client. Existing values remain intact; missing new settings receive defaults.

| Section and setting | Default | Allowed range / behavior |
| --- | --- | --- |
| survival.thirstInPeaceful | true | false restores hydration in Peaceful; true retains harmless thirst |
| creatures.foundationBiteWindupTicks | 40 | 10–200; each foundation bite's anticipation, animation scales with duration |
| creatures.foundationBitePauseTicks | 12 | 12–200; recovery between bites |
| creatures.sharkRetreatTicks | 55 | 20–400; retreat after attacks or repelling |
| creatures.sharkRepelCooldownTicks | 120 | 20–12000; minimum delay after successful crew defense |
| creatures.peacefulSharksPerRaft | 1 | 0–8 passive sharks |
| creatures.easySharksPerRaft | 1 | 0–8 sharks |
| creatures.normalSharksPerRaft | 1 | 0–8 sharks |
| creatures.hardSharksPerRaft | 2 | 0–8 sharks |
| multiplayer.giveStarterSupplies | true | Grant once on first Ocean World entry |
| multiplayer.starterHammer | true | Include building hammer when supplies enabled |
| multiplayer.starterFoundations | 8 | 0–64; zero omits foundations |
| multiplayer.starterPlanks | 16 | 0–128; larger amounts split into inventory stacks |
| multiplayer.starterBread | 8 | 0–64; zero omits bread |

Tick durations assume 20 ticks per second. Shark counts remain bounded by `creatures.levelCap`; only occupied, loaded rafts spawn creatures. Lowering a count or setting it to zero prevents new spawns without deleting existing sharks. `creatures.sharks = false` additionally disables existing sharks' attacks. Windup settings affect foundation attacks; swimmer attacks keep their existing timing.

Starter items are granted only once, including co-op. Reconnect, death and `/raftcrew join` do not supply more items. Changing settings does not retroactively grant supplies to existing players. Shared entry remains opt-in with `multiplayer.sharedStarterRaft = true`.

Use the [Peaceful co-op preset](config-examples/presets/peaceful-coop-synced.toml) for shared entry and no thirst drain. Replace a gameplay file with that preset only when you intend to adopt all its settings. The [complete default example](config-examples/raft_ocean-synced.toml) and other presets include the new options.

## Local test checklist

- Select Minecraft Peaceful with mod mode Hard: sharks circle without biting foundations or swimmers; crops remain intact.
- Switch to Peaceful during a bite: no further foundation damage or lingering attack pose.
- In mod Peaceful, set thirstInPeaceful=false: hydration stays full. With true it can drain, but cannot cause damage.
- In Normal, change windup/pause: anticipation and gaps change; three successful crew hits still repel the shark by default.
- Enable shared entry and change starter amounts, then connect a new player: one customized kit; reconnect and respawn never duplicate it.

Automated native server checks cover configuration consumers, Peaceful transitions, custom starter supplies and existing movement/construction/survival checks. Connected multiplayer latency and the rendered animation still require an in-game test.
