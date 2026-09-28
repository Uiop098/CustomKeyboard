---
description: Create a new GitHub pull request with the current branch
agent: github-agent
model: anthropic/claude-sonnet-4-6
---

Create a pull request for the current branch.

```bash
gh pr create --title "$1" --body "$2" --base main --head $(git branch --show-current)
```

If no title is provided, use the branch name. If no body is provided, use a default template.