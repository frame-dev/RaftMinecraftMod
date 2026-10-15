# Larger rafts, survival modes and double chests — 0.8.1

Build/launch with Java 25 using `gradlew.bat build` and `gradlew.bat runClient`.
Use `build/libs/raft_ocean-0.8.1.jar` on all clients and the server. The movement
protocol is now `raft-movement-8.1`; existing worlds retain their raft contents.

## Larger rafts

The foundation cap increases from 64 to **1024** (16 times as many), and structural
pieces from 128 to **512**. Coordinates can extend to X/Z ±32 relative to the raft
origin; the foundation-count cap still applies. A complete 32×32 deck fits.
Structural height is now **16 grid levels**, allowing more floors supported by
pillars, walls, stairs and ladders. Connectivity and buoyancy rules still apply.
Each new foundation adds mass and buoyancy; large rafts accelerate more slowly.

Solid foundation areas share merged collision rectangles before rotation,
reducing the collision work for wide, filled decks. Holes retain their geometry.
Rendering, structures and water sampling still cost more as you build more.
Sailing toward unloaded chunks remains blocked; this mod does not force them to load.
The limits are finite to keep synchronized raft data within native packet limits.

## Modes

Open **Mods → Raft Ocean → Config**, or edit the root `gameMode` key in
`raft_ocean-synced.toml`, then reload the world/server. Server settings govern all players.

| gameMode | Sharks per occupied raft | Foundation attack timing at default aggression |
| --- | --- | --- |
| PEACEFUL | 1 passive shark | No player bites, foundation bites or bird theft |
| EASY | 1 | About 60 seconds between completed bites; reduced damage |
| NORMAL | 1 | About 30 seconds between completed bites |
| HARD | 2 | About 20 seconds per shark; increased damage |
| WORLD (default) | Follows Minecraft difficulty | Applies the matching mode above |

Actual timing includes approach/windup, bait, combat interruption and spawn checks.
The initial approach delay differs from repeated-bite delays. Existing aggression,
damage and spawn settings still apply. Native Minecraft Peaceful additionally protects
players from mob damage even if you select a different mod mode.

Hard creates up to two sharks **per occupied raft**, subject to the dimension cap
and suitable loaded water. A replacement appears at a future spawn check after a
shark dies. Lowering the mode does not delete a second existing shark; Peaceful
makes both stop attacking. Disabling the shark toggle prevents natural spawning.

## Double raft chests

Place two raft chests side by side, at the same height and facing the same direction.
They automatically pair into a **54-slot** Double Raft Chest. This also works with
existing chests. Chests one behind another do not pair. A row of three forms one
double and one single, with a stable pairing determined by raft coordinates.

Either half opens the same six-row native storage screen. Pairing uses the two
original inventories, so their contents remain saved independently. Removing or
destroying a half drops only its contents, closes the old double menu, and leaves
the surviving half usable. Reopen after changing the chest arrangement.
The original chest models gain a wooden join; lid animation remains future polish.

## Thirst display and test checklist

The HUD now displays hydration percentage and ten original water drops, including
half drops. Low hydration turns amber and shows a flashing “Drink soon!” prompt.
Its visibility still follows the client HUD setting and host thirst setting.

- Extend past 64 foundations and 128 pieces; save/reload, walk on outer edges,
  sail and steer. Expect preserved buildings and normal carry. Test large-build
  frame rate on your own machine; maximum-size multiplayer is not yet live-tested.
- Choose EASY, NORMAL, HARD and PEACEFUL in turn. Expect slower Easy attacks,
  two naturally spawned Hard sharks, and no Peaceful bites or crop theft.
- Drink/consume water at different hydration levels and GUI scales. Check the
  percentage, full/half drops, warning and separation from vanilla HUD elements.
- Put distinct items into each half of a paired chest; shift-click, save/reload,
  open from both halves, then remove one. Expect no duplication or lost contents.
- With two connected players, test simultaneous chest use and larger-raft carry.
  Retest the earlier intermittent snap-back and inventory-loss reports.
