---
description: Create a new GitHub issue
agent: github-agent
model: anthropic/claude-sonnet-4-6
---

Create a new GitHub issue.

```bash
gh issue create --title "$1" --body "$2" --label "$3"
```

If no labels provided, use appropriate defaults based on issue type.