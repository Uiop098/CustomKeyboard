---
description: Create a new GitHub release
agent: github-agent
model: anthropic/claude-sonnet-4-6
---

Create a new GitHub release.

```bash
gh release create $1 --title "$2" --notes "$3" --generate-notes
```

Provide version tag (e.g., v1.0.0), release title, and optional release notes.