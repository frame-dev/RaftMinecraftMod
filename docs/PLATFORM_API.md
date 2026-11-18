# Platform inspection API — 0.13.11 preview

Addons can inspect wooden foundations and sea-level collection nets through
dev.raftocean.api.RaftPlatforms. These are additive API3 methods available
starting with Raft Ocean 0.13.11. Declare [0.13.11,0.14) as the main-mod
dependency range when calling them.

Call on the owning game thread. The server provides authoritative health and
protection. Client values reflect the last synchronized state. Resolve a new
snapshot when refreshing a panel or processing a server tick.

## Query one cell

Cells use raft-local block coordinates. Foundations and exposed collection nets
are at Y=0. A machine on the deck commonly has an anchor at Y=1.

~~~java
import dev.raftocean.api.RaftMachineContext;
import dev.raftocean.api.RaftPlatforms;

public final class PlatformMonitor {
    private PlatformMonitor() { }

    public static void update(RaftMachineContext context) {
        if (!context.isInstalled()) {
            return;
        }
        var platform = RaftPlatforms.inspect(context.raft(), context.anchor().below());
        context.state().setInt("support_health", platform.map(RaftPlatforms.Status::health).orElse(0));
        context.state().setInt("support_protected",
                platform.filter(RaftPlatforms.Status::protectionActive).isPresent() ? 1 : 0);
    }
}
~~~

The inspect method returns an empty Optional for a removed raft or absent platform.
Call context.valid(player) before opening an addon menu or accepting a player
command. An inspection result does not grant crew permissions.

## Inspect the whole raft

~~~java
var platforms = RaftPlatforms.snapshot(context.raft());
long damaged = platforms.stream().filter(platform -> platform.health() < 100).count();
long vulnerableEdges = platforms.stream()
        .filter(RaftPlatforms.Status::exposed)
        .filter(platform -> !platform.protectionActive())
        .count();
~~~

The returned list and copied cell coordinates are immutable. Snapshots stay at
their original values when the raft changes. A removed raft returns an empty list.
An installed machine may retain a snapshot for display, then replace it on the
next server update.

| Status field | Meaning |
| --- | --- |
| cell() | Immutable raft-local platform coordinate |
| collectionNet() | True for a sea-level collection net |
| health() | Remaining platform health, from 1 through 100 |
| armored() | Armor is installed and saved on this cell |
| protectionActive() | Installed armor currently protects this cell under the synced configuration |
| exposed() | At least one cardinal neighbor lacks a sea-level platform |

Exposure describes geometry. Actual shark attacks also depend on difficulty,
Peaceful mode, delays, aggression and the raft damage configuration.
An armored platform can remain installed while protection is disabled.

## Compile and test

Build the main mod with Java25, then compile against
build/libs/raft_ocean-0.13.11.jar with the pinned project dependencies.
The existing example addon reads the main-mod version from gradle.properties.
To build it from this checkout, run:

~~~powershell
.\gradlew.bat -p examples/raft-addon build --offline --no-parallel
~~~

Offline use requires cached dependencies. For an addon that uses these new
methods, update its neoforge.mods.toml version range to [0.13.11,0.14).
Install matching main-mod and addon versions on the client and server.

Local tests should cover a damaged net, an armored net, disabled reinforcement,
save/reload, dismantling and a net that connects two raft sections.
The core runtime check is RaftNetArmorCheck.
