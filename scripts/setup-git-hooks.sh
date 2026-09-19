#!/usr/bin/env sh

set -eu

REPO_ROOT="$(git rev-parse --show-toplevel)"

git -C "$REPO_ROOT" config core.hooksPath .githooks
chmod +x "$REPO_ROOT/.githooks/pre-push" "$REPO_ROOT/scripts/setup-git-hooks.sh"

echo "Git hooks configurados em $REPO_ROOT/.githooks."
