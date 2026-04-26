---
name: project-context
description: Captures VocabDaily-specific conventions, architecture, and delivery expectations. Use when implementing features or making code changes in this repository.
---

# VocabDaily Project Context

## Purpose

Keep changes aligned with this repository's app goals, terminology, and product direction.

## How To Use This Skill

1. Read `App_Plan.md` before implementing non-trivial features.
2. Reuse the terminology in the plan consistently across code and docs.
3. Prefer small, focused changes that map to one user-facing behavior.
4. Call out assumptions whenever requirements are missing.

## Project Facts To Fill In

Replace placeholders with concrete project details:

- Primary users: `<who uses VocabDaily>`
- Core user outcome: `<what users should accomplish each day>`
- Main architecture: `<frontend/backend/data stack>`
- Key constraints: `<performance/security/offline/hosting constraints>`

## Implementation Guardrails

- Keep naming aligned with the product language in `App_Plan.md`.
- Avoid introducing new dependencies unless clearly justified.
- Add or update tests for behavior changes when test infrastructure exists.
- Include a short verification plan in responses after making changes.

## Done Criteria

A change is ready when:

- It matches current product direction in `App_Plan.md`.
- It does not break existing behavior.
- Any new assumptions are documented.
- Follow-up steps are explicit if work is intentionally partial.
