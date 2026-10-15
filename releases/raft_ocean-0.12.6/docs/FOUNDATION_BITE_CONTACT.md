# Shark foundation contact — 0.11.2

The old raft bite position used the normal swimming depth (1.08 blocks below
raft origin). The visible jaw consequently closed below the deck. Proximity to
the target was enough to start and complete damage, even without jaw contact.

A raft-bound attack now stages underwater, turns gradually, then rises to put
the mouth at the midpoint of the 0.25-block foundation side. The authoritative
body target remains offshore to leave room for its conservative physical hitbox.
The synchronized foundation-bite pose extends the visible model by 0.315 blocks,
with a shared jaw-forward/height calculation used by the server contact check.
Cosmetic bob, roll and whole-body pitch are suppressed during this pose so they
cannot shift the mouth away from the wood; jaw and tail animation continue.
The visual extension fades during recovery.

Damage requires an exposed target, existing aggression/difficulty settings,
windup completion, alignment and a jaw point within 0.15 blocks of the targeted
foundation side midpoint. The shark continues tracking the raft rather than
applying offshore damage. Circling, bait and swimmer chasing retain normal
underwater targets and their previous bite poses. A removed or protected target
is not damaged. The open-ended story and recipe progression are unchanged.

Regression checks record jaw height/edge distance at an actual native damage
transition, verify the body stays outside raft colliders, and check contact
geometry on four faces at six hull angles. Existing gradual-turn, embedded-shark
recovery, bait, difficulty and combat checks remain. Logs build/foundation-bite-build.log and build/foundation-bite-ocean-check.log.
Both native server runs passed, including 704 Ocean World assertions.

Install matching 0.11.2 clients/server and restart. Development clients need a
normal rebuild after closing Minecraft. Artifact build/libs/raft_ocean-0.11.2.jar.

- Watch a shark approach an exposed foundation while anchored: it should rise,
  open its mouth at the wood edge, close, damage that foundation and retreat.
- Repeat while drifting and turning; check the bite follows the moving edge.
- Remove the targeted foundation during approach, place bait, fight the shark,
  and try Peaceful or disabled raft damage; protected targets should stay intact.
- Check with two players. Damage remains server authoritative.

This does not add torn-wood fragments or dented foundation geometry. Extremely
narrow concave gaps may not admit the shark's physical body. Cosmetic alignment
and network interpolation under real multiplayer latency still need local tests.
