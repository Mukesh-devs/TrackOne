## Git workflow (applies to every update)

- Never commit directly to main/master. For every update (feature, fix, refactor), first create a new branch from the latest main:
  `git switch main && git pull && git switch -c <type>/<short-name>`  (types: feature, fix, chore, refactor)
- Commit in small logical steps. Messages are short, imperative, and describe the change (e.g. "Add bank sender allowlist").
- Push the branch after each group of commits: `git push -u origin <branch>` the first time, then `git push`.
- Commit messages, PR titles/descriptions, branch names and tags must NOT mention Claude, Anthropic or AI.
  Do NOT add `Co-Authored-By` trailers, "Generated with ..." lines, or session links. Commits are authored only by the repo's configured git user.
- Never force-push, never rewrite pushed history, never open a PR unless asked.
- Never commit secrets (keys, tokens, keystores, .env) or real SMS / customer data.

### Commit-message guard (run once after cloning)

`.githooks/commit-msg` rejects commit messages that break the rules above. Enable it with:

```
git config core.hooksPath .githooks
```
