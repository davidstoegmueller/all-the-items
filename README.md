# All the Items 🎯

A progression-first Paper plugin built for SMP servers that want a clear shared goal.
Collect every obtainable item once and celebrate every milestone together. ✨

👑 Core Feature: Open the in-game gamemode/admin menu with `/alltheitems` (alias `/ati`) to manage challenge flow, progression controls, and celebration tools.

[![GitHub release](https://img.shields.io/github/v/release/davidstoegmueller/all-the-items?style=flat-round)](https://github.com/davidstoegmueller/all-the-items/releases)
[![MIT license](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-round)](https://github.com/davidstoegmueller/all-the-items/blob/master/LICENSE)

Works standalone, but it is **best combined with BetterVanilla SMP** for extra QoL features:
[BetterVanilla on Modrinth](https://modrinth.com/plugin/bettervanilla-smp)

Tested on Paper 1.21.11

## Table of Contents

- [Why All the Items?](#why-all-the-items)
- [Highlights](#highlights)
- [Installation](#installation)
- [Commands](#commands)
- [Settings](#settings)
- [Permissions](#permissions)

## Why All the Items?

Vanilla survival can lose direction after the early game.
All the Items gives your server a clear, always-active objective with visible progress,
shared momentum, and satisfying completion effects.

## Highlights

### Core Challenge Flow

- 🎯 **Live Current Target** - One server-wide target item at a time.
- 🔄 **Queue Preview** - See upcoming targets before they become active.
- 🧠 **Automatic Progression Checks** - Progress updates on pickup, inventory click, inventory drag, crafting, and player join.
- ⚖️ **Queue Normalization & Refill** - Stable target flow even over long-running worlds.
- 🎲 **Smart Skip Handling** - Automatically skips invalid/blocked targets and announces updates.

### GUIs & Controls

- 🖥️ **Current Items GUI** - Current target, queue preview, and progress panel.
- 📚 **Remaining Items GUI** - Searchable list of everything still missing.
- ✅ **Collected Items GUI** - Searchable completed list with timestamps (newest first).
- ⚙️ **Gamemode / Admin Menu** - Open with `/alltheitems` or `/ati` to:
  - Toggle gamemode
  - Reset challenge progress
  - Trigger manual fireworks
  - Set queue amount through the in-game dialog

### Immersion & Feedback

- 📣 **Server Broadcasts** - Collected item, skipped item, new current item, and full completion announcements.
- 🎆 **Celebration System** - Progress particles/sounds plus enhanced completion firework show.
- 📊 **Bossbar Progress Indicator** - Live visual completion progress.

### Data & Performance

- 💾 **Persistent State** - Saved in `state.yml` (`remaining`, `queue`, `current`, `collected`, `complete`).
- 🔧 **Simple Config** - Tune behavior in `settings.yml`.
- 📈 **bStats Integration** - Anonymous usage metrics support ongoing improvements.

## Installation

1. Download the latest release jar.
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Run `/alltheitems` (alias `/ati`) to open the gamemode menu.

## Commands

### Core Command

- `/alltheitems` - Opens the All the Items gamemode/admin menu.
- `/ati` - Alias of `/alltheitems`.

## Settings

Main config file: `settings.yml`

- `gamemode.enabled` - Enable or disable challenge mode.
- `queue.itemsAmount` - Number of items shown in the upcoming queue.
- `items.excluded` - Materials excluded from the challenge pool.

## Permissions

- `alltheitems.admin` - Access the command and gamemode/admin menu.
