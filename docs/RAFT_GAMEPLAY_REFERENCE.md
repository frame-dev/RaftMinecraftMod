# Raft gameplay reference and implementation priorities

Initially reviewed 2026-10-18 against the 0.13.1 source. This is a comparison and
implementation direction, not a claim that the features below are implemented.
The user wants the mod's survival loop and progression to feel closer to Raft.

The requested [German wiki homepage](https://raft.fandom.com/de/wiki/Raft_Wiki)
could not be fetched (payment/robots restrictions). Search-indexed German and
English gameplay articles were accessible. Facts below are limited to those
articles; this is not an exhaustive audit of every wiki page. Articles can lag
behind the game, so check the relevant mechanic again before implementing it.
The comparisons with the mod and the priority order are our engineering assessment.

## Preserve the user's choices

- Original assets, models, sounds, names and lore; an open-ended story.
- Engine/sail/paddle nominal base speeds: 1.5/1.0/1.8 m/s. Do not substitute
  wiki engine speeds without a new user instruction.
- Configurable ten-minute shark raft-attack delay and five-minute bird-theft delay.
  Peaceful remains free of shark foundation attacks.
- Harder 2–4-sample research batches, with prior research retained.
- Large, irregular story islands with increasing stage footprints and extra notes.
- Server-authoritative moving rafts, co-op, existing saves and the addon API.

## Implemented follow-up

0.13.5 implements priority3's [shared sails and engine load feedback/bands](SAILING_AND_ENGINE_LOAD.md),
preserving the user's nominal speeds. It includes a legacy engine-scaling toggle;
its averaged propulsion and slow-band speed intentionally differ from Raft's exact
engine speed formula. Native control/physics verification is recorded in RELEASE_VERIFICATION.md.

0.13.4 adds the persistent personal [expedition notebook](EXPEDITION_NOTEBOOK.md),
with field-note archiving, prior journey records and side-route logs. Numeric code
entry and multi-signal radar remain future work.

0.13.3 adds the mounted battery-powered receiver/antennas and radar portion of
priority2; the notebook follows in0.13.4.
See [mounted receiver](MOUNTED_RECEIVER.md).

0.13.2 implements the first priority: seven story schematics, permanent discovery,
material-plus-discovery research gates, older-journal recovery and crew sharing.
See [story schematics](STORY_SCHEMATICS.md). The comparisons below describe the
0.13.1 audit baseline; the remaining parts of priority2 and priorities4–6 remain future work.

## 1. Exploration should unlock technology

In Raft, research consumes a material once; recipes become learnable when their
required materials have been studied. Blueprint discoveries add technology to
the research system automatically, so retaining the physical blueprint is not
required. The engine article also describes a story-location blueprint gate.
Sources: [Forschungstisch](https://raft.fandom.com/de/wiki/Forschungstisch),
[Baupläne](https://raft.fandom.com/de/wiki/Baupl%C3%A4ne?theme=wiki),
[Motor](https://raft.fandom.com/de/wiki/Motor).

Current mod: ResearchCatalog/Research implement 30 material-based projects and
60 unlocks. Story journals advance JOURNEY, but Research.state checks material
masks and learned projects, not discovered island schematics. Thus players can
research the engine without finding an original story-island technology cache.
The higher sample costs make material gathering harder, but do not provide this
exploration gate.

Recommended first implementation: stable original schematic IDs discovered at
appropriate existing story sites, followed by the existing material/recipe learning
steps. Persist discovery at acquisition, keep it if the paper is lost, support crew
sharing and an operator unlock, and preserve existing learned recipes. No starter
water/food or initial receiver gate may depend on reaching a later island. Define
and test the entire dependency graph before adding gates; do not reset old saves.
Implemented in0.13.2; it was not present in the0.13.1 audit baseline.

## 2. Receiver and notebook should connect exploration

The wiki receiver is placed on the raft, powered by a battery and used with three
spaced antennas. Recorded destination codes guide subsequent story travel.
Source: [Receiver](https://raft.fandom.com/wiki/Receiver).

Current mod: ReceiverItem is a handheld locator with an upgraded-channel option;
JournalItem updates the next destination and prints the main journal to chat.
The added side notes are readable books. There is no mounted three-antenna setup
in this receiver path.

Next feature after schematic progression: mounted receiver, original radar UI,
antenna placement feedback, power use and a persistent journal/notebook recording
visited sites, discovered notes and signal clues. Scale spacing to this mod's
one-meter grid instead of assuming a Raft foundation has Minecraft dimensions.
Retain a migration path for existing handheld receivers and journey progress.

## 3. Navigation should preserve limits, not reward sail stacking

The wiki describes a sail as wind-limited steering; additional sails do not
increase speed, and a sail cannot propel directly against the wind. Engines
have a foundation-count capacity and can stall when overloaded.
Sources: [Sail](https://raft.fandom.com/wiki/Sail),
[Motor](https://raft.fandom.com/de/wiki/Motor).

Current mod: RaftNavigation.target adds thrust from every enabled sail, so
multiple sails can add propulsion. Engines have a configured per-engine
foundation scale that reduces thrust, rather than the wiki's exact stall bands.
Wind-aware trimming, directional engine mounting, fuel and anchors already exist.

Recommended navigation pass: test duplicate and opposed sails, cap effective
sail drive, and expose engine capacity/overload status with clear feedback.
Preserve the requested nominal speeds, fine steering controls and fuel settings;
exact original-game speed formulas are not the project's current default.

## 4. Recycling and fishing should drive trading

The wiki links recyclers to tradeable resource cubes, and special-bait fishing
to coins and reputation. Reputation unlocks higher trading tiers.
Source: [Trading Post](https://raft.fandom.com/wiki/Trading_Post).

Current mod: OutpostTrading uses saved, limited-stock native merchant offers
from trading/outpost.json. Offers exchange fish or valuables directly for goods;
the merchant reports zero XP and no progress bar. This path does not implement
the wiki's cube/coin/reputation economy. No recycler implementation was found in
the current source/resource search.

Recommended economic milestone: an original powered salvage compactor and trade
tokens, followed by special catches, bait tiers, saved reputation and shop tiers.
Retain transactional stock handling and crew safety. Do not add a currency without
renewable acquisition paths and meaningful surplus-material sinks.

## 5. Cooking should reward preparing for an expedition

The wiki cooking pot takes four food portions, fuel and an explicit start action;
finished dishes are collected with a bowl. Prepared dishes provide longer-lasting
food benefits. Finding a recipe card is not required to cook a known combination.
Source: [Cooking Pot](https://raft.fandom.com/wiki/Cooking_Pot).

Current mod: SurvivalMachines uses automatic processing and a four-slot recipe
input that substitutes fresh water for one slot. Existing prepared meals have
bonuses, but this is not the same four-food/start/serve interaction.

Recommended survival pass: visible ingredient pads, recipe guidance, deliberate
start/stop, serving-container handling and clear ready/full/missing-input states.
Keep existing recipes and inventory safety through migration. A recipe card can
be guidance without becoming an arbitrary crafting lock.

## 6. Building should be controlled through the hammer

The wiki hammer opens a building selection menu and includes repair. Nets act as
walkable sea-level collection pieces; a full basic net allows debris to pass.
Sources: [Building Hammer](https://raft.fandom.com/wiki/Building_Hammer),
[Simple Collection Net](https://raft.fandom.com/wiki/Simple_Collection_Net).

Current mod: individual buildable items select placement, while the hammer
repairs/removes and a brace reinforces foundations. Shared ghosts, support,
collision and resource handling already exist. The hammer is registered as a
simple item; there is no hammer build-selection interface in that registration.

Recommended building pass: categorized hammer palette, material costs and
unlocks before placement, consistent rotation and ghosts, clear support feedback,
and repair/reinforcement modes. Reuse the server's existing placement validation
and test stairs, corners, upper floors and moving co-op placement.

## Delivery order and acceptance

Implement one bounded feature at a time in the order above, with appropriate
server tests and a client playtest between milestones. This audit changes no
runtime mechanics or balance. For each gameplay change, verify actual behavior
against the cited reference and explicitly retain any user-selected differences.
Avoid claiming the entire mod matches Raft because a recipe or model resembles it.

First acceptance target: a new player cannot learn a gated engine merely by
studying materials; acquiring its island schematic records discovery permanently,
then material research permits learning. Existing researched engines, creative/admin
access, starter survival, saves and shared-raft permissions must continue to work.
