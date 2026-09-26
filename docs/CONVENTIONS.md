# Conventions

## Language

Everything in git is English: code, comments, commits, pull requests, documents. Russian
exists only as the UI translation in `shared/core/strings/src/commonMain/composeResources/values/`
and `apps/iosApp/Swishy/ru.lproj/`.

## User-facing text

- No text in code: every string is a resource, added to `values/` and `values-en/` together.
- A text next to an icon or another text in a row gets `Modifier.weight(1f, fill = false)`,
  `maxLines` and an ellipsis. Titles take up to two lines.

## UI

- Screens are built from `shared/design-system` components. A missing component is added
  there first, on the theme tokens, with a `@Preview`, and only then used by a screen.
- Material is not used anywhere; detekt rejects its imports.
- Color means swipe direction: `keep` (blue), `trash` (red), `move` (amber). Buttons and
  headings are never painted in them.
- Every gesture has a text alternative, and the direction reads without color (icon and word).
- Reduce Motion is respected: no tilt, growth or spring.

## Comments

- A type gets one line saying what it is for.
- Inside code, a comment only where the code would be misread without it: one line, why.
- Functions, properties and parameters are not documented; this is an app, not a library.
- No dates, stories of how a bug was found, or links to working notes.
- Checked by `scripts/comment-check.py`; the share of comment lines stays under 5 %.

## Privacy

The app never connects to the internet: no `INTERNET` permission, no network library. A pull
request that adds either is out of scope for this project.

Nothing private goes into git: no keys, signing data, machine paths or personal emails.
`scripts/publish-check.py` runs before every commit and push and in CI.
