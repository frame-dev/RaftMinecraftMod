# Milestone 12 — release packaging

0.13.0 is a preview release of the existing ocean-survival mod. It includes
the current save format, larger graded islands, open-ended story, machinery,
co-op, models, collision refinements, corrected conduit-junction meshes,
the machine/navigation/salvage quality patch, and refined utility models/motion,
plus quick machine collection and contextual hints.

Deliverables:

- `build/libs/raft_ocean-0.13.0.jar` — installable mod.
- `build/libs/raft_ocean-0.13.0-sources.jar` — developer sources, not a playable jar.
- `releases/raft_ocean-0.13.0.zip` — jar, sources, guides, changelog, notices,
  verification summary, addon examples and per-file checksums.
- `releases/raft_ocean-0.13.0.zip.sha256` — ZIP checksum.

Installation is described in the root `INSTALLATION.md`. The release has been
built and verified locally; it has not been uploaded to a mod-hosting service.
The packaging portion of milestone 12 is complete. Client/co-op sign-off
remains pending; use `KNOWN_ISSUES.md` and the installation playtest checklist.

Regenerate the bundle with `python tools/package_release.py` after completing
the isolated build/checks and updating `RELEASE_VERIFICATION.md`.

API v3 includes the standalone raft-addon-sdk-0.13.0.zip; see API_V3_UTILITIES.md.
