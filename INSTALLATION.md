# Install Raft Ocean 0.13.0

This is a preview release for Minecraft Java 26.3, NeoForge 26.3.0.48-beta
and 64-bit Java 25. It does not run on Fabric or older Minecraft versions.

## Client

1. Install Java 25 and create a Minecraft 26.3 NeoForge profile using the
   pinned NeoForge version above.
2. Put only `raft_ocean-0.13.0.jar` in that profile's `mods` folder. Remove
   the previous Raft Ocean jar so exactly one version is installed.
3. Start the profile and verify **Raft Ocean 0.13.0** in the Mods screen.
4. Create a world and select the **Ocean World** preset. You should begin
   on a starter raft in the ocean.

The sources jar is for developers and must not be installed in `mods`.
The release does not bundle Minecraft, NeoForge, Java, JEI or technology mods.

## Cooperative dedicated server

Install matching Minecraft/NeoForge/Java versions and the same mod jar on the
server and every client. Review and accept Minecraft's EULA yourself before
running the server. For a new Ocean World, set these server properties before
the world is created:

```properties
level-type=raft_ocean:ocean
generate-structures=true
```

Changing `level-type` does not convert an existing vanilla world. Choose a
new world directory when creating an Ocean World. Keep online authentication
enabled for a normal public server; isolated development tests use a separate
local server configuration.

Players can use the shared-raft/co-op systems. Crew permissions, shared
research and rescue settings are configurable. All peers need 0.13.0 because
the movement protocol is versioned.

## Updating an existing world

Back up the world and configuration, stop the game/server, and replace the
older mod jar on every peer. The 0.11.41 save format is retained. Furniture
and model fixes apply to existing rafts; enlarged/new island layouts appear
only in chunks that have not been generated. Do not delete world data to
force island regeneration.

## Optional JEI and other mods

JEI 31.9.0.58 for Minecraft 26.3/NeoForge is the pinned tested integration
target. Install its matching jar separately if wanted. Research-locked recipes
are discovered through the research table rather than appearing immediately.

Other mods must target the same Minecraft version and loader. Material tags,
energy charging and addon adapters are supported; compatibility with an entire
third-party modpack has not been verified. See `docs/MOD_COMPATIBILITY.md`.

## First playtest

- Walk, jump and use stairs while the raft drifts and turns.
- Try foundations, edge nets, walls, beds and tabletop decorations.
- Collect debris, fill/drink cups, cook food and dry wet bricks in sunlight.
- Research a machine, craft it, and check its inventory/processing UI.
- Test engine forward/reverse, sails and the anchor.
- Save/reload, then repeat with another player on the same raft.
- Check a resource island, trading outpost and story destination in new terrain.

If an issue occurs, include the mod version, reproduction steps and relevant
client/server log or crash report. Redact private server/account information.
