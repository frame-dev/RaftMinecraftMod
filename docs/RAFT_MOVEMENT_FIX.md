# Raft walking correction — 0.11.13

The raft landing handler runs during the native server player tick. It placed feet
on the deck, but did not update the server connection's movement baseline.
Minecraft then restored its pre-tick position, undoing the landing. A restored
position inside the deck can trigger overlap recovery and further disagreement
with the client. Landing now records its platform displacement and resets that
baseline, as raft carry and overlap recovery already do. Native collision, speed
validation and teleport acknowledgements remain active.

## Verification

The native tick regression failed before the fix: landing Y347.251 was restored
to Y347.270. After the fix, the same regression and below-deck variants pass.
The smoke fixture uses Minecraft's real movement handler instead of FakePlayer's
normal no-op connection. 1210 checks cover landings plus walking with client hull
interpolation, drift, bobbing and rotation at six headings, near crafting tables,
chests, sprinklers, battery chargers and stairs. The existing runtime checks,
build and four packaged asset audits also pass. See build/movement-fixed.log.
This does not replace testing an actual connected client or co-op under latency.

## Local test

1. Fully close the old test client and restart it with the updated source, or install
   build/libs/raft_ocean-0.11.13.jar in a matching instance, replacing the older mod jar.
2. Walk and sprint around stations on an anchored raft.
3. Repeat while drifting, under engine power and while steering.
4. Jump onto the deck from water, walk up stairs, and cross upper floors.
5. In co-op, repeat with both players on the same raft and the same mod version.

Expected: landing stays on the deck, walls still block walking, and ordinary
walking does not repeatedly snap back. If a correction remains, report whether
it happens anchored or moving and share the new client/server logs.
