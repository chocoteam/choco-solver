#!/bin/bash
# Generate a structured changelog from git commits since the last tag.
# Uses the Claude CLI to analyze commits and produce a CHANGES.md section.

set -e

LAST_TAG=$(git tag --sort=-v:refname | head -1)
if [ -z "$LAST_TAG" ]; then
    echo "ERROR: No previous tag found"
    exit 1
fi

echo "Analyzing commits since ${LAST_TAG}..."

# Collect commit logs with short diffs for context
GIT_DATA=$(git log "${LAST_TAG}..develop" --format="=== COMMIT %h by %aN (%aE) ===%n%s%n%b" --stat)

if [ -z "$GIT_DATA" ]; then
    echo "No commits found since ${LAST_TAG}"
    exit 0
fi

# Collect contributors
CONTRIBUTORS=$(git log "${LAST_TAG}..develop" --format='%aN <%aE>' | sort -u)

# Collect referenced issues/PRs
ISSUES=$(git log "${LAST_TAG}..develop" --format='%s %b' | grep -oE '#[0-9]+' | sort -u || true)

# Build the prompt
PROMPT="You are analyzing git commits for the Choco-solver project (a Java Constraint Programming library).
Generate a structured changelog section in Markdown, following this exact format:

### Major features:

#### Constraints & LCG
- description of change

#### Search & Strategies
- description of change

#### Build, CI & Tooling
- description of change

### Deprecated API (to be removed in next release):
- if any

### Other closed issues and pull requests:

Rules:
- Write in English
- Group related commits into a single bullet point
- Each bullet should describe the user-visible change, not the implementation detail
- Mention issue/PR numbers (#NNN) when referenced in commits
- Skip merge commits and version bumps
- Only include categories that have actual changes
- Be concise: one line per change

Here are the commits since ${LAST_TAG}:

${GIT_DATA}

Here are the referenced issues/PRs: ${ISSUES}

Here are the contributors:
${CONTRIBUTORS}"

# Call Claude to generate the changelog
CHANGELOG=$(echo "$PROMPT" | claude -p --model sonnet 2>/dev/null)

echo ""
echo "$CHANGELOG"
echo ""
echo "#### Contributors to this release:"
echo "$CONTRIBUTORS"
echo ""

if [ -n "$ISSUES" ]; then
    echo "========================================="
    echo "  REFERENCED ISSUES/PRs"
    echo "========================================="
    echo "$ISSUES"
    echo ""
fi