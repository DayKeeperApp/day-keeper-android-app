# 1. Record Architecture Decisions

Date: 2026-03-08

## Status

Accepted

## Context

We need to record the architectural decisions made on this project so that future contributors (and
our future selves) can understand why the codebase is shaped the way it is.

Without a lightweight decision log, context is lost when conversations fade and contributors rotate.

## Decision

We will use Architecture Decision Records (ADRs) as described by
[Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

Each ADR is a short Markdown file in `docs/adr/` numbered sequentially:

```text
docs/adr/NNNN-short-title.md
```

Each record contains:

- **Title** — a short noun phrase
- **Status** — Proposed | Accepted | Deprecated | Superseded
- **Context** — the forces at play
- **Decision** — what we decided
- **Consequences** — what happens as a result

## Consequences

- Decisions are visible, searchable, and version-controlled alongside the code.
- New contributors can read the ADR log to understand past trade-offs.
- We accept a small overhead of writing a short document for each significant decision.
