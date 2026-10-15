# Milestone 12 — release packaging

0.12.3 is a preview release of the existing ocean-survival mod. It includes
the current save format, larger graded islands, open-ended story, machinery,
co-op, models, collision refinements and corrected conduit-junction meshes.

Deliverables:

- `build/libs/raft_ocean-0.12.3.jar` — installable mod.
- `build/libs/raft_ocean-0.12.3-sources.jar` — developer sources, not a playable jar.
- `releases/raft_ocean-0.12.3.zip` — jar, sources, guides, changelog, notices,
  verification summary, addon examples and per-file checksums.
- `releases/raft_ocean-0.12.3.zip.sha256` — ZIP checksum.

Installation is described in the root `INSTALLATION.md`. The release has been
built and verified locally; it has not been uploaded to a mod-hosting service.
The packaging portion of milestone 12 is complete. Client/co-op sign-off
remains pending; use `KNOWN_ISSUES.md` and the installation playtest checklist.

Regenerate the bundle with `python tools/package_release.py` after completing
the isolated build/checks and updating `RELEASE_VERIFICATION.md`.
