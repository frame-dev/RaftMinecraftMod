# Sailing and engine load — 0.13.5 preview

This update focuses on everyday raft navigation. It uses the wiki's
[sail](https://raft.fandom.com/wiki/Sail) and [engine](https://raft.fandom.com/wiki/Engine)
mechanics as references. The developer also confirms that
[additional sails do not increase speed](https://support.redbeetinteractive.com/hc/en-us/articles/360007241132-Will-more-than-one-sail-make-my-raft-go-faster).
The formulas below describe this mod, including its deliberate differences.

## Sails

Deployed sails share one drive budget: their wind-dependent thrust vectors are
averaged. Two identically trimmed sails give the same drive as one. Opposed
crosswind trims counteract each other; coordinate trims or fold unwanted sails.
A deployed sail that faces upwind contributes zero while still participating in
the average. Fold it to stop it diluting other sails.

Sails permit travel across the wind and with it, but provide no upwind drive.
Paddles or engines can still move against the wind. Wind strength, rain and current
continue to affect actual travel. The configured sail multiplier remains effective.
Sails no longer have a cargo-mass slowdown; larger structures do not require a
stack of sails to recover sailing drive. Buoyancy and paddle mass handling are unchanged.

The sail screen displays folded, wind drive, headwind, no-wind or configured-off feedback. Its
Deploy/Fold button explains shared drive. Existing one-degree trimming, steering
wheel, wind streamer, hull rotation and placement orientation remain available.

## Engines

Only enabled engines with a burn reserve or usable fuel count toward capacity.
Collection nets, upper floors, walls and furniture do not count as foundations.
At the default100-foundation capacity per powered engine:

| Powered engines | Full drive | Half drive | Stalled |
| --- | --- | --- | --- |
| 1 | up to100 foundations | 101–110 |111+ |
| 2 | up to200 |201–220 |221+ |
| 3–5 | up to engines ×100 | through110% of capacity |above110% |
| 6+ | Any supported raft size | none |none from foundation load |

Aligned engines share a 1.5 m/s nominal engine-drive budget. Extra engines provide
capacity without multiplying that base speed. The half-drive band gives0.75 m/s
engine drive at default fuel/config ratings. The original game's minimum/maximum
engine speed formula is not copied: the user's selected base speed is retained.
Native fuel ratings, configured thrust multipliers, other propulsion/current and
the overall speed cap can affect the displayed travel speed.

Mounting direction, hull yaw and the Forward/Reverse lever determine each engine's
thrust. Opposed engines cancel their force, so align their arrows. Both still count
as powered units for the load check. Dry, anchored, stopped, thrust-disabled or
stalled engines consume no fuel. Stalling preserves existing burn reserves and
unburned items; this conservation rule is intentional. Natural current, a legal
sail trim and paddles remain available when engines stall. Anchors override all drive.

The engine screen shows foundation load versus current fueled capacity, including
an infinity symbol for the six-engine bypass, and stopped/fuel/full/half/stalled
feedback. Its Start/Stop tooltip explains the limits. If capacity drops because
another engine loses fuel, the load band updates automatically.

## Configuration and existing saves

`navigation.engineFoundationCapacity` keeps its existing range1–1024 and default100.
`navigation.engineLoadLimits = true` enables the new bands and shared engine drive.
Set it tofalse to restore the previous gradual, additive engine scaling. This
compatibility option changes engine behavior only; sails still use shared drive.
Existing hulls, fuel, trim, direction and anchors load without conversion.

Engine/sail/paddle nominal bases remain1.5/1.0/1.8 m/s. The shark ten-minute delay,
bird five-minute delay, harder research, Peaceful behavior and story are unchanged.
All peers need0.13.5 with handshake13.5; addon API3 is unchanged.

## Test in your client

1. Compare one versus two aligned sails; deploying the second must not add drive.
2. Try crosswind and upwind trims; only crosswind/with-wind sails provide drive.
3. With zero test current, try one fueled engine on100,110 and111 foundations.
   Expect full drive, half drive and stall. Stalled fuel must remain untouched.
4. Fuel a second engine to recover capacity, reverse it to counteract forward drive,
   then align it again. Confirm the load/status display follows changes.
5. Lower the anchor: coordinates hold and fuel is preserved. Raise it to resume.
6. Check the controls and player movement with a crewmate on a moving raft.

Live co-op latency and visual text layout still require local playtesting. This
change does not claim to eliminate all previously reported collision limitations.
