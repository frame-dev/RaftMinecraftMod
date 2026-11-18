# Bonus thirst — 0.13.12 preview

Prepared juices now fill a separate thirst reserve. It drains before ordinary
hydration and lasts longer per point. This makes prepared drinks useful before
an island expedition.

The reference is Raft's [bonus thirst](https://raft.fandom.com/wiki/Thirst).
The mod uses its existing twenty-point hydration scale and configurable original
balance. Each prepared juice restores twelve normal points and adds six reserve
points by default. Each meter is capped at twenty.

## Use it

1. Prepare Melon Breeze, Orchard Cooler or Shoreberry Tonic in the juicer.
2. Serve it into a Drinking Glass and drink it.
3. The purple Reserve meter appears below the normal hydration drops.
4. The reserve drains first. When it runs out, ordinary hydration depletion
   resumes with its previous partial progress.

At the default thirst period of thirty seconds, one reserve point lasts sixty
seconds while walking. Six reserve points provide six minutes before normal
hydration starts draining again. Sprinting applies the configured thirst
multiplier to both meters.

Drinks can build reserve while ordinary hydration is full. Additional juice
still consumes its prepared portion at the cap. Each consumed drink returns
its reusable glass through the existing item consumption behavior.
Ordinary fresh water restores the normal hydration meter.

Potion effects from prepared juices remain controlled separately by
preparedMealBonuses. Turning those effects off still permits bonus thirst.
The low-water warning appears when normal hydration is low and the reserve
is empty.

## Configure

These synced settings belong under [survival] in raft_ocean-synced.toml:

~~~toml
juiceBonusHydration = 6
bonusThirstPeriodMultiplier = 2
~~~

The first setting accepts 0–20 and controls reserve gained per juice.
Zero disables new gains. Previously stored reserve continues to deplete.
The second accepts 1–8 and multiplies thirstPeriodTicks for each reserve point.
The cap is always twenty reserve points.

Reserve amount and partial depletion progress persist across player saves.
Death clears the reserve. Non-death player replacement retains it.
Disabling thirst, Creative, Spectator or a Peaceful thirst exemption restores
normal hydration and clears reserve and depletion progress.

## Local check

- Drink one prepared juice at full hydration. Expect a 30% reserve with defaults.
- Walk for a minute. Expect one reserve point spent while normal hydration stays full.
- Save/reload with a partial reserve and confirm it continues to drain.
- Drink ordinary fresh water and confirm it restores normal hydration.
- Try the zero-gain setting and the separate potion-effect setting.

Native drink consumption, save/load and timing boundaries are covered by
BonusThirstCheck. Live HUD placement and real co-op synchronization still need
local testing. All players and the server need the 0.13.12 version.
