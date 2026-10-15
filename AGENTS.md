# Repository workflow

The user authorizes committing and pushing completed project changes to
https://github.com/frame-dev/RaftMinecraftMod after each change task.

- Run the checks appropriate to the changes, review the diff, then commit and
  push the task's changes to the current upstream branch.
- Preserve unrelated user work. Do not force-push, rewrite published history,
  or commit credentials, local worlds, runtime logs, or build caches.
- Report the commit and any failed checks or blocked push honestly.
- Publish GitHub releases when requested. Use the verified versioned artifacts,
  include requirements and known limits, and retain preview status while the
  project is in preview. Do not replace existing tags or released assets silently.
- The current remote is named `master`; `main` tracks `master/main`.

Project versions, milestones and outstanding live testing are recorded in
PROJECT_STATE.md. Keep the story open-ended.
