# Propulsion speeds — 0.12.3

Engine: 1.5 m/s per engine at full load capacity and unit fuel speed.
Sail: 1.0 m/s per sail at unit wind, full alignment and full mass factor.
Paddle: 1.8 m/s peak at full mass factor. Repeated strokes are capped at this
paddle contribution; drive decays between strokes and the hull accelerates
smoothly toward its requested velocity.

One block is one meter; all three values are converted using 20 ticks/second.
Existing server propulsion multipliers default to 1.0 and remain configurable.
Current adds drift, wind and load affect sails, fuel/load affect engines,
and multiple drive sources add together. The overall default limit remains
3.2 m/s. The HUD measures actual hull movement, so its number can differ from
a single source's base speed. Obstacles, anchor and server tick rate also matter.

Test one drive source at a time on a small raft. Engine reverse must keep the
same magnitude, the anchor must hold position without burning engine fuel,
and paddle drive should fade when strokes stop. For an isolated comparison,
set navigation.currentMultiplier to 0 and propulsion multipliers to 1.
