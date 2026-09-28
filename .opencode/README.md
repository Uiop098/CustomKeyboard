# opencode GitHub Agent

This directory contains the opencode configuration for GitHub integration.

## Setup

1. **Install opencode**: `npm install -g opencode-ai`
2. **Set GitHub Token**: Export your GitHub token as environment variable:
   ```bash
   export GITHUB_TOKEN=your_github_token
   ```
   Or add it to your shell profile (`.bashrc`, `.zshrc`, etc.)

2. **Start opencode**: Run `opencode` in the project directory

## Configuration

- `opencode.json` - Main configuration with GitHub MCP server
- `.opencode/agent/github-agent.md` - GitHub specialist agent
- `.opencode/command/` - Custom commands for GitHub operations

## Available Commands

| Command | Description |
|---------|-------------|
| `pr` | Create a new pull request |
| `issue` | Create a new GitHub issue |
| `review` | Request code review for current PR |
| `release` | Create a new GitHub release |

## GitHub Agent

The `github-agent` is a specialized agent for GitHub operations:
- Repository management
- Pull request creation and management
- Issue creation and triage
- Code reviews
- Release management

## GitHub MCP Server

The configuration includes a GitHub MCP server for advanced operations:
- Complex multi-API queries
- Bulk operations
- Advanced GraphQL queries
- Cross-repository searches

## Usage

1. Start opencode: `opencode`
2. Use `@github-agent` to invoke the GitHub specialist
3. Use `/pr`, `/issue`, `/review`, `/release` commands for common operations

## Environment Variables

Required:
- `GITHUB_TOKEN` - GitHub personal access token with appropriate scopes

Optional:
- `GITHUB_REPOSITORY` - Default repository (owner/repo)