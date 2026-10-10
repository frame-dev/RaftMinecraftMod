# Milestone 11 — polish, balance and performance (0.11.0)

All 383 English keys have matching German entries and format placeholders,
including commands, cup messages, station help and config labels. Destination
proper names and the six open-ended story journals remain unchanged.

Three original synthesized mono Vorbis effects accompany successful seawater
cup fills, finished drinks and new hook casts. The server emits them once for
nearby players. Failed actions and hook reeling are silent. Subtitles are present
in both languages. No external or Raft sound samples are used. Regeneration:
python tools/generate_original_sounds.py (requires ffmpeg with Vorbis support).

## Performance

Client machineEffectDistance defaults to 32 blocks (range 0–128). It limits only
cosmetic smoke, sparks, bubbles and rain droplets. Zero disables them. Water
levels, crops, food and moving parts stay visible and processing is unchanged.
Range uses each machine's transformed location and the local player position.
Mesh resource lookups now use short names prepared on reload, avoiding repeated
identifier/path allocations per rendered part. F3+T rebuilds that lookup map.

No measured FPS or server tick-speed increase is claimed. Collision, networking
and large raft geometry costs remain. Natural caps and loaded-area spawning
continue; no new building limit is imposed.

## Balance presets

Normal defaults preserve the existing loop: one thirst point every 30 seconds,
twice as fast sprinting; six points per drink; approximately ten-second water
purification and twenty-second pot recipes. Processing speed changes progress,
while fuel burns in real ticks. Research, recipes and saved equipment are retained.

Complete examples in docs/config-examples/presets:

- calm-synced.toml: Easy mode, 45-second thirst period, aggression 0.75 and slower
  bird theft. Easy's existing attack-frequency and damage multipliers also apply.
- large-raft-synced.toml: reduced spawn attempts and loaded salvage/creature caps;
  foundation ceiling remains 1024. Ocean density is lower.
- low-effects-client.toml: both HUDs enabled, effect distance 12 blocks.

These files are examples, not automatically applied or guaranteed hardware targets.
Back up the appropriate config, stop the world and replace the host synced TOML
with the selected host example. See CONFIGURATION.md for paths. Client example
belongs in raft_ocean-client.toml. Do not append duplicate TOML sections.

## Validation and local checklist

Artifact build/libs/raft_ocean-0.11.0.jar; use matching clients/server. Restart
installed Minecraft after updating. For development, close, rebuild and relaunch.

- Switch to German; inspect station help, cup messages and config labels.
- Fill a cup at sea, drink fresh water, cast/reel a hook. Expect one short sound
  on each successful fill/drink/cast, readable subtitles and no reeling sound.
- Run several machines and compare effect distances 32, 12 and 0. Liquid,
  food and crops should remain; inventory and processing must stay unchanged.
- Inspect a large raft while turning; reload resources with F3+T.
- Compare the same scene/resolution with default and low-effect settings. Record
  frame and server tick times rather than assuming a performance improvement.
- Test two players, difficulty presets, save/reload and the unresolved final signal.

Build/model/geometry checks, packaged localization/audio/preset audits and an
isolated dedicated-server regression run provide automated coverage. Sound quality,
GPU performance, connected multiplayer and earlier movement/crash reports still
need local tests. Milestone 12 waits for these results.
