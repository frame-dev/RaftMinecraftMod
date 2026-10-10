# Textures and animations in 0.9.5

These animations run only on the client. They do not move the deck, change
collision, alter inventory transactions, or replace server movement authority.
Existing worlds remain save-compatible. Version 0.9.3 added synchronized creature
animation data; the current build requires raft-movement-9.5 on client and server.
See [movement and gradual approach fixes](MOVEMENT_FIXES.md) for version 0.9.5.

## Shark bites

The server publishes foundation bite windup and a timestamp when foundation or
swimmer damage succeeds. The jaws open during the existing 40-tick foundation
windup, then snap shut over three ticks. A short forward thrust, tail kick, and
eight-tick recoil complete the 12-tick impact animation. Swimmer bites trigger
the same snap/recoil once the shark has turned to face the swimmer, with unchanged
damage and cooldown settings.
The shark faces the foundation or swimmer at impact. Windup ends when a target
is lost, bait intervenes, attacks are disabled, or the shark is struck. Being
struck also interrupts the remaining bite pose. Completed impacts remain in
synchronized data long enough to render after the attack phase changes.

Lunging is a render-only offset, limited to 0.18 blocks, with no collider movement
or extra damage. Windup packet interpolation is bounded; high latency can delay
the visible snap relative to server damage. Attack animations are transient and
are not restored from saved worlds.

Version 0.9.2 adds original 64px wood grain and fasteners, woven sail cloth with
stitched hems, distinct shark back/belly/fin materials, feather patterns, fur and
hooves, visible eyes, and an animated toothed jaw. Shark heads and bodies have
stepped tapering sections. The jaw opens downward and flexes through a bite.
Regenerate these assets with `python tools/generate_visual_assets.py` after the
base asset scripts; this preserves the revised textures over base placeholders.

Sharks swim deeper so their body and dorsal fin clear the deck. Bite approach
uses a missing cardinal neighbor of an exposed foundation, including concave
and rotated layouts. If raft movement puts a shark inside a collider, the server
recovers it into verified water below that collider, without forcing chunks.
Ocean World excludes drowned from its natural spawn table, refuses drowned
join/load/conversion, and discards existing loaded drowned without death drops.
Other world presets retain their normal drowned spawning.

Sharks have speed-responsive swimming, delayed tail flex, moving fins, banking,
and an opening jaw during the synchronized bite phase. Birds have segmented
wings, a smooth transition to gliding during dives, head pecking, and tail motion.
Island grazers walk with alternating legs, turn and lower their heads when idle,
twitch their ears, and wag their tails. Living creatures react to damage and roll
over during death; floating bait gently bobs and tilts.

Sails raise and lower smoothly. Four cloth strips ripple along the sail, while
trim and wind streamers turn smoothly across the 360-degree boundary. Steering
wheels ease toward their target. Engine cranks accelerate and coast to a stop,
keeping their angle instead of snapping back to zero.

## Local test checklist

- Sail forward and turn: inspect the cloth, streamer, and steering wheel. Change
  sail trim through north to check that it takes the shortest rotation.
- Start an engine with fuel, then stop it: the crank should speed up and coast
  down smoothly. Raise and lower the sail and anchor several times.
- Watch sharks orbit, chase bait, and attack: tails and fins should move, and
  biting sharks should open their jaws. Watch birds orbit and dive at crops.
- During a foundation attack, watch the jaw open, snap, and recoil once when the
  foundation takes damage. Check swimmer bites too. Strike the shark or throw
  bait during windup: the unfinished bite should stop without damaging the deck.
- With two clients, check that both see the same shark bite sequence. Update
  both client and server to 0.9.5 before joining.
- Turn or build a raft over a swimming shark. It should escape below the deck
  and continue swimming. Test a large raft and an irregular foundation outline.
- Reopen an Ocean World containing drowned: they should disappear without loot.
  New drowned should not spawn there, including from eggs or zombie conversion.
- Watch island grazers standing, walking, and taking damage. Their feet should
  settle when stopped, and head/ear/tail movements should remain visible.
- Walk on a moving raft while watching these animations. Deck collision and
  player carrying should behave as before. Check with a second player if possible.

## Limits

Meshes remain original geometric placeholders. Mouth and head movements are
cosmetic and do not define hitboxes. Brief attack phases may be less visible
under poor network conditions. Storage lids and arbitrary vanilla block-entity
animations are not implemented. Live appearance and multiplayer need user testing.
