Create a CLAUDE.md file in the project root with the following rules and conventions:

# Project: DataStream gRPC API

## Tech Stack
- Java 21, Spring Boot 3.3+, gRPC (grpc-spring-boot-starter via net.devh)
- PostgreSQL, Liquibase for database migrations and versioning
- Domain-Driven Design (DDD) architecture
- Maven as build tool (with protobuf-maven-plugin for proto compilation)
- JUnit 5, Mockito, grpc-testing for tests
- Testcontainers for integration tests

## Architecture Rules
- Follow DDD: domain, application, infrastructure, interfaces (grpc) layers
- Domain layer has ZERO framework dependencies (no Spring, no JPA, no proto imports)
- All business logic lives in domain services or aggregates
- Use Value Objects for type safety (e.g., StreamId, UserId)
- Repository interfaces defined in domain, implementations in infrastructure
- Application layer uses Command/Query pattern (CQRS-lite)
- gRPC services are thin adapters in the interfaces layer — no business logic

## Code Conventions
- No Lombok — use Java records for DTOs/Value Objects and manual getters/constructors for entities
- Use constructor injection only (no @Autowired on fields)
- All public methods must have Javadoc
- Exceptions: use domain-specific exceptions, map to gRPC status codes in interceptors
- Naming: *Command, *Query, *Handler, *Repository, *GrpcService
- Package structure: com.datastream.{domain,application,infrastructure,interfaces}

## Testing Rules
- Unit tests for all domain logic (>90% coverage on domain layer)
- Integration tests use Testcontainers (PostgreSQL)
- gRPC endpoint tests use grpc-test InProcessServer
- Test naming: should_ExpectedBehavior_When_Condition
- Every PR must include tests for new/changed logic

## Git & PR Rules
- Branch naming: feat/, fix/, refactor/, chore/, docs/
- Commit messages: conventional commits (feat:, fix:, refactor:, test:, docs:, chore:)
- PR title format: [TYPE] Short description
- PR must have: description, linked issue, test coverage summary, migration notes if DB changes
- No force pushes to main. Squash merge only.
- PRs require description of: What changed, Why, How to test, Breaking changes (if any)

## Migration Rules (Liquibase)
- Liquibase changelogs in src/main/resources/db/changelog/
- Master changelog: db.changelog-master.yaml (includes all changelogs in order)
- Changelog files: YAML format, naming: YYYY-MM-DD-NNN-description.yaml (e.g., 2024-01-15-001-create-streams-table.yaml)
- Every changeset must have a unique id and author
- Every changeset must include a rollback section
- Never modify an already-applied changeset — always create a new one
- Use preconditions where appropriate (e.g., tableExists checks)
- Seed data changelogs prefixed with "seed-" and use context="dev,local" so they don't run in production
- Tag changesets after major schema milestones using <tagDatabase>
