#!/usr/bin/env bash
# Comment rule and comment density over the sources, and publish-check over every tracked file.
# Exit: 0 clean, 1 violations.

set -uo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT" || exit 1

STATUS=0
# Untracked files too: a new file is checked before `git add`.
SOURCES=$(git ls-files --cached --others --exclude-standard '*.kt' '*.kts' '*.swift' |
    while read -r f; do [ -f "$f" ] && printf '%s\n' "$f"; done)

printf '%s\n' "$SOURCES" | xargs python3 scripts/comment-check.py || STATUS=1
printf '%s\n' "$SOURCES" | xargs python3 scripts/comment-check.py --density >/dev/null || {
    printf '%s\n' "$SOURCES" | xargs python3 scripts/comment-check.py --density
    STATUS=1
}
python3 scripts/publish-check.py --tree || STATUS=1

[ "$STATUS" -eq 0 ] && printf '\033[32m✓ project conventions hold\033[0m\n'
exit "$STATUS"
