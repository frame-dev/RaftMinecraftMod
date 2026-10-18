# Salvage recycling — 0.13.7 preview

Convert renewable surplus into goods at island outposts. This is an original
materials-to-trade loop inspired by the wiki's recycler; models, names, yields
and prices belong to this mod. Coins, reputation, special trading fish and shop
tiers are future work. The story stays open-ended.

## Build and use

Learn **Radio receiver** under electrical research, using its existing copper
wire, metal plate and glass studies. Previously learned electrical research
also unlocks the compactor; no new story schematic is required.

Craft the Salvage Compactor in a 3×3 crafting table:

```text
Plate  Wire   Plate
Fitting Brace Fitting
Plate  Plate  Plate
```

This uses five metal plates, one copper wire (including compatible wire-tag
alternatives), two scrap fasteners and one timber brace. Place it on a supported
raft foundation/floor; it occupies one cell and has twelve units of mass.

Open its workshop panel:

- Seven left slots accept recyclable feedstock. Materials may be mixed.
- The separate battery socket accepts a Tidal or Deepcycle Battery.
- The right output slot holds up to four Salvage Bales.
- The panel shows feed points, saved processing time, charge and waiting status.
  Pause/Resume preserves the batch. Sneak-use with an empty hand collects output
  when quick machine harvest is enabled.

| Material | Points per item | Single-material batch |
| --- | ---: | ---: |
| Drift leaves, kelp, dried kelp | 1 | 40 |
| Any plank, salvage scrap | 2 | 20 |
| Iron or copper ingots, including compatible tags | 4 | 10 |

Each bale costs **exactly forty points**. The processor finds an exact mixed
combination and leaves excess material intact. Renamed or otherwise modified
stacks are refused to protect valuable items. Bales cannot be recycled by the
shipped material tags.

At defaults a bale takes **180 powered seconds** and **180 battery charge**.
A full Tidal Battery can complete five bales with 100 charge left. Empty power,
insufficient materials, a full/blocked output, Pause, or disabled recycling stops
progress and charge use. The last processing step atomically removes inputs and
adds one bale; inputs are not consumed in advance. Intermediate progress and
battery charge survive actual raft save/reload. Replacing/removing input materials
pauses work; a new valid forty-point combination can finish the saved progress.
Dismantling returns ordinary inventory through the existing machine system;
unfinished time is lost, but no input material has been converted or hidden.

These presses use portable native batteries. Wired power and foreign energy
items are not supported here. Recharge native batteries in the existing charger.
There is no separate recycling JEI category yet; the compactor's crafting recipe
uses the existing research-aware recipe discovery.

## Spend bales

| Payment | Outpost goods | Stock before restock |
| --- | --- | ---: |
| 1 bale | 8 beetroot seeds | 8 purchases |
| 1 bale | 4 carrots | 8 purchases |
| 2 bales | 1 raft table lamp | 8 purchases |
| 2 bales | 1 raft woven rug | 8 purchases |
| 3 bales | 1 pair of reef flippers | 8 purchases |

The previous twenty fish/scrap/valuable offers remain. Existing saved counters
gain these five offers once when reopened, retaining old offer uses and their
restock deadline. Stock is shared per counter; existing native merchant payment,
reach and session checks apply. Default restock is twenty gameplay minutes.

## Configure and extend

In `raft_ocean-synced.toml`, under `[trading]`:

```toml
recyclingEnabled = true
compactorSeconds = 180
```

Time range:30–1800 seconds. Disabling preserves placed machines and inventory;
it pauses processing. The stock enable/restock settings still control outposts.
The general survival processing-speed multiplier does not alter this independent
recycling timer. Change the three item tags in a data pack to extend feedstock:

- `raft_ocean:recycling/light` — one point.
- `raft_ocean:recycling/standard` — two points.
- `raft_ocean:recycling/dense` — four points.

If tiers overlap, dense takes precedence over standard, then light. Merchant
prices and goods remain data-driven in `data/raft_ocean/trading/outpost.json`.
Migration appends bale-paid offers only once to legacy stock; later catalog edits
appear on the normal restock, rather than resetting a used counter. The compactor
itself uses addon API3's owned machine state, filtered inventory and atomic batch
transactions; no new machine protocol or enum ordinal is required.

## Local playtest

1. Research/craft/place a compactor and confirm its ghost and solid collision.
2. Add mixed feed, insert a charged battery, pause/resume and reopen the panel.
3. Save/reload mid-batch; confirm charge, time and inputs remain.
4. Let a bale finish, fill the four-bale output, then collect and resume.
5. Buy a lamp or seeds with bales; repeat with a second co-op player.

Automated evidence and remaining live checks are in `RELEASE_VERIFICATION.md`.
Use matching0.13.7 clients/server (movement handshake13.7). Existing user worlds
were not opened or modified by development checks.
