---
description: Request a code review for the current PR
agent: github-agent
model: anthropic/claude-sonnet-4-6
---

Request a code review for the current pull request.

```bash
gh pr edit $(gh pr list --head $(git branch --show-current) --json number -q '.[0].number') --add-reviewer $1
```

If no reviewer specified, request from team reviewers.