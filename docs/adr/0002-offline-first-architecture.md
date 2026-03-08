# 2. Offline-First Architecture

Date: 2026-03-08

## Status

Accepted

## Context

Day Keeper is a personal life-management app covering calendars, tasks, lists, and contacts. Users
interact with this data throughout their day — often in places with unreliable or no network
connectivity (underground, rural areas, airplane mode).

The backend is self-hosted on a home Kubernetes cluster, which adds another potential point of
unavailability (maintenance windows, ISP outages, power loss).

The app must feel instant and work regardless of network state.

## Decision

We adopt an **offline-first** architecture:

- **Room (SQLite)** serves as the local source of truth on-device.
- All reads and writes go against the local database first.
- A background sync mechanism (WorkManager) pushes local changes to the server and pulls remote
  changes using a REST-based push/pull protocol.
- Sync uses a **monotonic cursor** and **tombstone soft-deletes** (`deleted_at`) for consistency.
- Conflict resolution follows **last-write-wins** (v1), with room for future refinement.

## Consequences

- The app is fully functional without network access.
- Users never wait on a network round-trip for basic CRUD operations.
- We must handle sync conflicts and maintain `created_at`, `updated_at`, and `deleted_at` timestamps
  on every syncable entity.
- The local schema mirrors a subset of the server schema, adding complexity to schema migrations.
- Background sync must be battery-efficient (WorkManager constraints).
