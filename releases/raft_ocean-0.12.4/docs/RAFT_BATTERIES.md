# Raft Ocean 0.11.11 — rechargeable batteries

Target: Minecraft 26.3 / NeoForge 26.3.0.48-beta / Java 25.
Both client and server need 0.11.11 (network protocol raft-movement-10.5).
Milestone 11 continues; the story remains open-ended.

## Equipment and use

- **Tidal Battery:** 1,000 energy capacity.
- **Deep-Cycle Battery:** 4,000 energy capacity.
- **Battery Charging Station:** two battery sockets and a biofuel input.

Crafted batteries start empty. Charge appears in the inventory bar and item tooltip.
Batteries do not stack. Discharging never destroys them; recharge the same battery.
Charge belongs to the item, so chest storage, inventory transfers and world saves retain it.
Original colored models distinguish both battery tiers and the charging station.

Place the charger on a supported raft foundation or floor and open it.
Put either battery type into either socket. Add biofuel to the left input, or connect a
Biofuel Generator using power cables. Fuel tanks/pipes can supply the charger automatically.
A direct side-adjacent generator also connects without a cable between the two cells.

Default charging is **20 energy per second in total**, shared between both sockets.
One biofuel canister supplies **2,000 energy**. A lone empty Tidal Battery takes 50 seconds;
a lone empty Deep-Cycle Battery takes 200 seconds. With two cells, sockets share the rate
fairly; when one fills, all available charging goes to the other.
Connected generator energy is preferred; charger-local biofuel covers any remaining demand.

Remove a battery whenever you want, even before it fills. The screen displays each
socket's charge percentage, combined charge progress and remaining fuel. The station
has a pulsing activity indicator and Pause/Resume controls. Empty/full/paused chargers
consume no charging energy. Remaining energy from an opened canister is retained for later.

Insert a charged battery into the new battery slot on an **Electric Purifier** or
**Sprinkler** to power that machine without cables. Wired power is preferred, and the
battery pays only the missing energy. An underpowered processing step spends nothing.
The machine stops when the battery empties and keeps the empty battery for recharging.
Normal water and planter requirements still apply. Batteries power their own machine;
they do not feed other machines through power cables.

## Crafting and research

All three recipes are shapeless 3x3 crafting recipes, visible in JEI's crafting category.
They join the existing Electrical / Radio receiver research project without resetting
saved research. Recipe-book entries unlock when you obtain copper wire.

| Result | Ingredients |
| --- | --- |
| Tidal Battery | 2 copper wire + copper ingot + metal plate + scrap + binding paste |
| Deep-Cycle Battery | 2 metal plates + 2 copper wire + 2 copper ingots + glass + binding paste |
| Battery Charging Station | 2 metal plates + 2 copper wire + scrap fasteners + glass + timber brace |

Each recipe produces one item. Advanced cells use fresh materials rather than consuming
a charged basic battery.

## Configuration

The synchronized server config includes `automation.batteryChargePerSecond` (default 20,
range 1–200). This is the combined rate across both sockets.
`automation.energyPerBiofuel` controls fuel yield for both generator and charging station.
`automation.enabled`, the existing purifier/sprinkler costs and per-machine Pause/Resume
also apply. Registration and recipes remain available when automation is disabled.

## Test locally

1. Restart client and server with the new jar; keep one Raft Ocean version installed.
2. Check JEI, craft both battery sizes and place the charger using its ghost preview.
   Expect empty batteries, colored inventory models and readable English/German names.
3. Put two batteries in the charger and add biofuel. Expect both percentages to rise;
   remove a partially charged cell and confirm its tooltip keeps that charge.
4. Pause charging, remove/reinsert a battery and save/reload. Charge and remaining fuel
   should remain intact. Full batteries should stop fuel use.
5. Insert a charged battery into an electric purifier or sprinkler without cables.
   Expect normal production/irrigation, gradual discharge and an empty reusable cell.
6. Connect a generator and compare: wired energy should be used first.
7. In co-op, have two players open the same charger and move batteries. Expect shared
   slots and charge, with no duplication or deleted batteries.

Existing handheld receiver behavior is unchanged. The charger is a mounted raft station,
not a vanilla world block. Only loaded rafts process charging. Removing the station drops
stored battery items through the normal station-inventory handling; remaining converted
biofuel energy belongs to that station and is lost on removal.

Automated verification includes actual native crafting/menu operations, shared storage,
charge clamping, item and fuel save/load, two-socket fairness, exact energy conservation,
standalone/network charging, disabled/paused operation and portable machine power.
Actual client visuals and human connected co-op latency still need local testing.

Final isolated build and native bootstrap passed, including 25 battery-specific checks
and native charger inventory interactions (661 total survival assertions).
Model parser, 121 GUI silhouette checks, 4,845 machine geometry/fill checks and all
packaged asset/localization/story/audio audits passed. Ocean suite passed 741 assertions.
Log: `build/batteries-final.log`; earlier ocean pass: `build/batteries-first.log`.
