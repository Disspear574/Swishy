# Git and GitHub

How work reaches `main`. The build is in [BUILD.md](BUILD.md), code rules in
[CONVENTIONS.md](CONVENTIONS.md).

## Issues

Every change to `main` starts from an issue created with a form from `.github/ISSUE_TEMPLATE`.
Blank issues are disabled.

| Label | Meaning |
| --- | --- |
| `epic` | A goal made of several stories, with a clear point where it is done |
| `story` | Something checked by hand on a device |
| `task` | Technical work inside a story or an epic |
| `bug` | Behaves differently from what its story promised |

Tasks and bugs are attached to their story or epic as sub-issues.

## Branches

- `main` is the truth; history is linear.
- A branch per theme, from `main`: `features/<N>-<slug>`, or `fix/<N>-<slug>` for a bug,
  where `<N>` is its main issue. Examples: `features/3-process`, `fix/12-sheet`.
- One level of nesting. A branch lives while its pull request is open.

## Commits

[Conventional Commits](https://www.conventionalcommits.org):

```
type(scope): imperative description

Body wrapped at 72 characters: what changed and why.
```

- Types: `feat`, `fix`, `perf`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `revert`.
- Scopes: `deck`, `months`, `trash`, `duplicates`, `albums`, `settings`, `media`,
  `decisions`, `design`, `l10n`, `android`, `ios`, `build`, `ci`, `git`. Omit the scope for a
  change across the project.
- The header is at most 72 characters, lowercase after the colon, no trailing period, and
  reads as "If applied, this commit will …".
- A blank line after the header. `feat`, `fix`, `perf` and `refactor` need a body.
- No trailers: no `Signed-off-by`, `Generated with` or tool links, in any spelling. The one
  exception is a `Co-Authored-By` trailer naming Claude (a model name may follow) with the
  `noreply@anthropic.com` address, on a commit written together with Claude.
- The issue number belongs to the branch and the pull request, not the commit.
- No `wip` and no header that names nothing (`update`, `fixes`, `misc`).
- A commit is a slice that builds and can be described in one sentence.

```
fix(deck): keep the card when no album is chosen

Closing the album picker used to count as a move. The verdict now
waits for the picker and returns the card to the deck on cancel.
```

## Pull requests

- A pull request covers a theme, not a single issue: small issues of one theme (a screen
  polish, a round of UI fixes, a tooling change) share one branch and one pull request with a
  `Closes #N` line each. Risky work (data, migrations) and large features get their own.
- Title in the same form as a commit header.
- The body follows `.github/pull_request_template.md`: Summary, Changes, How to test,
  Screenshots, Data, Risk, Checklist.
- A task is closed by `Closes #N`. A story is never closed by a pull request: `Part of #N`.
- Before merge: green `check` and `privacy` workflows, and the change run on a simulator or
  emulator with the result in the pull request. iOS is not built in CI, so say whether it
  was built locally.
- Merge is done by a person, with **Rebase and merge**; squash and merge commits are turned
  off. The branch is deleted on merge.

## Hooks

```bash
git config core.hooksPath scripts/git-hooks
```

- `commit-msg` checks the commit form above; the scope list is read from this file, so a
  new scope is added here. Merges, reverts, `fixup!`, `squash!` and `amend!` are skipped.
- `pre-commit` runs `publish-check.py --staged` and the comment rule on staged sources.
- `pre-push` runs `publish-check.py --range` over every commit the remote does not have
  yet: added files and lines, messages, and author and committer emails. A tag or a branch
  from before a history rewrite is stopped here.

`--no-verify` is not used.

## Before a commit

```bash
./scripts/check-conventions.sh
./gradlew detekt :shared:core:media:testAndroidHostTest \
    :shared:core:decisions:testAndroidHostTest :shared:design-system:testAndroidHostTest
```

Plus a build of the affected platform.

## Not committed

- Signing: `apps/iosApp/Signing.xcconfig`; only its `.example` is committed.
- `apps/iosApp/Swishy.xcodeproj/`, generated from `project.yml`.
- Build output: `build/`, `DerivedData/`, `.gradle/`, `.kotlin/`.
- Agent working files: `.claude/`, `CLAUDE.md`, `CLAUDE.local.md`.

Committed on purpose: the Gradle wrapper jar, so a clean clone builds, and the Room schema
snapshots in `shared/core/decisions/schemas/`.

## Version

The version changes only in a release commit, never in a separate "bump" commit.
Files are listed in [BUILD.md](BUILD.md).
