# Known limitations — 0.13.8 preview

- Juicers use native portable batteries, four food slots and separate glass serving.
  Wired/foreign energy, bonus-water reserves and invalid-recipe leftovers are absent.
  Dismantling spills an unserved portion. Glasses are intentionally reusable.
  Live new-screen/JEI appearance and user-save/co-op testing remain outstanding.

- Salvage recycling adds a portable-battery compactor and five bale purchases.
  Coins, shop reputation, special trading fish and recycling JEI category are future
  work. Existing fish/scrap offers remain usable; the input tiers use data-pack tags.

- Cooking pots now wait for Cook and stop after one batch. Existing automation can
  retain continuous processing with survival.manualCooking=false. Four-food pads
  and bowl collection after cooking remain future work; existing recipes are retained.

- Automated checks pass, but live client appearance, real network latency,
  large-raft performance and a complete multiplayer playthrough still require
  local testing. Earlier reported movement/collision issues need user confirmation.
- Moving geometry uses conservative axis-aligned collision strips; this is an
  approximation of a rotating raft. Very large builds increase physics/render cost.
- New island terrain appears only in new chunks. Existing islands do not resize;
  mixed old/new generation can meet at explored chunk boundaries.
- Repeated copies of a story destination share its authored template. The
  current seven-destination story deliberately ends with unresolved clues;
  additional chapters are not yet playable.
- Foreign machines need dedicated moving-raft adapters. Common material/energy
  integration does not establish compatibility with every technology mod.
- Lamp lighting on moving raft surfaces is visual; it does not place temporary
  world light blocks or guarantee normal block-light effects on mob spawning.
- The matching NeoForge build is beta. This package is a preview release,
  not a claim of a bug-free production mod or fully completed visual playtesting.

- Notebook archives are personal. Legacy field-note books must be retrieved once
  to archive them; prior journey and side-route records remain readable. Numeric
  receiver codes and multi-signal radar are not implemented.

- Engine capacity and wind-limited sailing intentionally change earlier propulsion.
  Existing overloaded rafts can stall. Add fueled aligned engines, change the saved
  foundation capacity, or use engineLoadLimits=false for legacy engine scaling.
  The exact original-game engine speed formula is not reproduced; custom base
  speeds are preserved. See docs/SAILING_AND_ENGINE_LOAD.md.

These are release limitations, not failures hidden by the automated checks.
