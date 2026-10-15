# Shark and crop-pest delays — 0.11.21

Sharks now wait at least ten minutes before their first raft attack and between
raft attacks by default. This delay is separate from the aggression-scaled bite
interval, so Hard difficulty and increased aggression cannot shorten it.

Stop the world/server, edit `config/raft_ocean-synced.toml` in your Minecraft
instance (or the world's `syncedconfig/raft_ocean-synced.toml` override), and restart:

```toml
[creatures]
sharkRaftAttackDelaySeconds = 600
birdCropTheftDelaySeconds = 300
```

Add the entry to the existing `[creatures]` section; do not create a second section.
For this development project's client the default file is
`run/client/config/raft_ocean-synced.toml`. Gameplay settings are host-controlled
and synchronize to joining clients. Existing configs retain their values. This update sets the development client/server shark setting to 600 seconds and adds the bird setting at 300 seconds; previous files are backed up beside them.

- `300` = five minutes; `600` = ten minutes.
- Range: `0`–`3600` seconds. `0` disables this additional delay; the existing
  `sharkBiteIntervalTicks` and repel cooldown still apply.
- The timer restarts after a completed bite sequence, cooperative repelling,
  cancelled attack or failed approach. Individual bites within a sequence still
  use `foundationBiteWindupTicks` and `foundationBitePauseTicks`.
- Each shark has its own timer. Multiple sharks can attack during the same period.
- Time advances only while the shark is loaded and the server is ticking.
  Pausing, unloading and closing the game do not advance the timer.
- Remaining time saves with the shark. Legacy saves receive the configured grace
  period. Reloading during an attack cancels that sequence and gives a full delay.
- Reducing the setting caps the remaining timer on the next AI tick. Increasing
  it takes effect on the next reset or newly spawned shark.
- Sharks still circle the raft and can attack swimming players during this wait.
  Peaceful mode continues to prevent all hostile shark behavior.

## Crop-stealing birds

`birdCropTheftDelaySeconds` defaults to 300 (five minutes), range 0–3600.
Each bird waits before its first theft and after a successful theft or interrupted
approach. It circles the raft during the wait. Its countdown persists through
save/reload, runs only during loaded gameplay, and legacy birds receive the
configured grace period. Saving during a dive cancels that dive on reload and
starts a full wait. Reducing the setting caps the existing timer; increases apply
on the next reset. Zero removes this extra wait; `birdTheftIntervalTicks` still
applies. Peaceful, covered planters and the bird enable/disable setting remain
effective.

## Local test

1. With `600`, let a newly spawned shark circle the raft: no foundation approach
   should begin for five loaded gameplay minutes. Approach travel adds some time.
2. Repel it during a foundation attack with the configured number of crew hits:
   it should retreat and wait another ten minutes before starting a new attack.
3. Save/reload during a wait: the remaining wait should be preserved.
4. Set `10` for a quick timing test, then restore `600`. Test Peaceful as well:
   no foundation damage or player attacks should occur.

5. For birds, use a ripe uncovered planter: no theft should occur during the
   five-minute wait. After one stolen crop, the bird should wait again.

Native server checks cover the full 12,000-tick default, high aggression/Hard mode,
timer persistence, legacy saves, the one-hour maximum, completion and repelling.
Actual in-game timing and co-op observation remain for local confirmation.

Validation log: `build/creature-delays-final.log`.
Additional checks cover the full bird delay, post-theft wait, saving and
interrupted approaches. Local in-game timing still needs confirmation.
Build, bootstrap and ocean suites passed with 756 ocean assertions;
packaged audits verified 521 English/German keys and existing model coverage.
