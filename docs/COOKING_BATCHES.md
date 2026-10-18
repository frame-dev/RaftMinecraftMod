# Deliberate cooking batches — 0.13.6 preview

Press **Cook** to prepare one cooking-pot batch, then stop. This follows the
explicit-start portion of [Raft's cooking-pot mechanic](https://raft.fandom.com/wiki/Cooking_Pot),
using this mod's existing original recipes. Four-food preparation pads and collecting
finished dishes with a bowl afterward remain future work.

## Preparing a batch

1. Supply the existing recipe's base food, vegetable, fresh water, bowl and fuel.
   JEI continues to show all five pot recipes; no recipe format changes.
2. Leave room for the empty cup and finished meal. Fresh water may already be in
   the reservoir or supplied through the full-cup slot.
3. Press **Cook** in the header. A successful press queues exactly one batch.
   The press itself consumes no items, water or fuel.
4. The next one-second machine update starts processing. A queued batch without
   fuel waits for fuel. Pause/Resume preserves progress and the burn reserve.
5. Collect the meal normally. Spare supplies do not start another batch. Press
   Cook again for another meal. Eating the prepared meal still returns its bowl.

## Inventory and saves

Cook validates the recipe, water and output/cup-return capacity. Failed presses
consume nothing. Duplicate presses cannot queue extra batches. Foreign menu owners,
spectators, distant players, stale menus and presses with a carried item are refused.

Foods, the bowl and reservoir water commit at completion after output fit is checked.
Cup pouring still exchanges a full cup for reservoir water and an empty cup.
Output blockage and pause preserve the fuel reserve. Changing recipes cancels the
old request and resets progress; removing required food cancels it when rechecked.
Fuel or reservoir-water shortage pauses the selected batch. Press Cook after fixing
an invalid recipe. Failure feedback persists until another attempt.

Queue, pause, progress, fuel and supplies survive native raft saving/loading.
Older pots with progress resume one batch; older idle pots wait for Cook.
Storage indices, recipes, food bonuses and cup/bowl handling remain compatible.

## Automatic compatibility mode

`survival.manualCooking = true` is the synced default. Set it to false to restore
continuous automatic cooking, including existing automation setups. Cook is disabled
in that mode. Existing processing-speed and fuel settings remain effective.

No new items, models or crafting recipes are added. Addon API3 remains unchanged.
All clients and the server need0.13.6 with handshake13.6 for the new menu data.
The story, harder research, custom navigation speeds and predator delays are retained.

## Local test

- Supply two meals: one press must cook only one, leaving spare supplies untouched.
- Pause midway, relog, resume, and verify the same batch finishes once.
- Fill the meal output or empty-cup return; Cook must refuse without item loss.
- Change recipes during cooking; the old cooking progress must not carry over.
- Try simultaneous Cook presses with a crewmate and test automatic compatibility mode.

Live GUI appearance, real co-op latency and the user's older saves still need testing.
