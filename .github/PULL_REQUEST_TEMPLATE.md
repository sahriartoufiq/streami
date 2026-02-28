Create a .github/PULL_REQUEST_TEMPLATE.md file with this structure:

## What Changed
<!-- Brief description of the changes -->

## Why
<!-- Motivation, link to issue/ticket -->

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Refactor
- [ ] Infrastructure/config
- [ ] Documentation

## How to Test
<!-- Steps to verify this PR -->

## Checklist
- [ ] Tests added/updated for all changes
- [ ] Domain layer has no framework imports
- [ ] Liquibase changesets include rollback sections
- [ ] No modifications to already-applied changesets
- [ ] gRPC proto file is backward compatible
- [ ] Javadoc added for public methods
- [ ] No Lombok usage
- [ ] Conventional commit messages used

## Migration Notes
<!-- If DB changes: describe the changeset, rollback plan, and any data impact -->

## Breaking Changes
<!-- List any breaking changes to the API or data model -->
