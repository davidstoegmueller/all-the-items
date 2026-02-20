# Changelog

All notable changes to this project are documented in this file.

## [1.0.0] - 2026-02-19

- Initial Paper plugin release for the All the Items challenge.
- Main command `/alltheitems` with alias `/ati`.
- Permission model with `alltheitems.admin` to access gamemode settings.
- Current Items GUI with:
  - Current target display
  - Queue preview
  - Info panel with progress overview
- Remaining Items GUI (search-enabled).
- Collected Items GUI (search-enabled, timestamp display, newest first).
- Settings GUI with admin controls:
  - Toggle game mode
  - Reset game mode/progress
  - Trigger manual firework show
- Automatic inventory-driven progression checks on:
  - Item pickup
  - Inventory click
  - Inventory drag
  - Crafting
  - Player join
- Persistent state management via `state.yml`:
  - `remaining`, `queue`, `current`, `collected`, `complete`
- Configurable settings via `settings.yml`:
  - `gamemode.enabled`
  - `items.excluded`
  - `queue.itemsAmount`
- Queue normalization and refill logic to maintain stable target flow.
- Broadcast system for:
  - Item collected
  - Item skipped
  - New current item
  - Full completion
- Celebration effects:
  - Particles and sounds for progression
  - Enhanced completion celebration
  - Firework show utility
- Added bossbar to indicate progress
- Added CustomDialog to set queue item amount
- bStats metrics integration.
