# Local compatibility testing

Place matching Minecraft 26.3 NeoForge mod jars and their required dependencies
directly in this folder. Development client, server and isolated server checks
include these jars through the optional Gradle runtime file tree. No jars are
downloaded or bundled automatically. Restart a running client after changes.

Do not put Raft Ocean, Minecraft, NeoForge or a second copy of the development
JEI jar here. Older Minecraft mod jars cannot load on 26.3.

See `docs/MOD_COMPATIBILITY.md` for supported features and current limits.
