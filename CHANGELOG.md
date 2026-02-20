# 📜 Changelog

All notable changes to this project are documented in this file.

## 🚀 [1.0.0] - 20.02.2026

- 🎉 Initial Paper plugin release for the All the Items challenge.
- 🧭 Main command `/alltheitems` with alias `/ati`.
- 🔐 Permission model with `alltheitems.admin` to access gamemode settings.
- 🧱 Current Items GUI with target display, queue preview, and progress info panel.
- 🔎 Remaining Items GUI (search-enabled).
- ✅ Collected Items GUI (search-enabled, timestamp display, newest first).
- ⚙️ Settings GUI admin controls: toggle game mode, reset progress, and trigger manual fireworks.
- 🧪 Automatic inventory-driven progression checks on pickup, click, drag, crafting, and join events.
- 💾 Persistent state management via `state.yml` with `remaining`, `queue`, `current`, `collected`, and `complete`.
- 🛠️ Configurable `settings.yml` options: `gamemode.enabled`, `items.excluded`, and `queue.itemsAmount`.
- 🔄 Queue normalization and refill logic to maintain stable target flow.
- 📣 Broadcast system for item collected, item skipped, new current item, and full completion.
- 🎊 Celebration effects include progression particles/sounds, enhanced completion celebration, and fireworks.
- 📈 Added bossbar to indicate progress
- 🧩 Added CustomDialog to set queue item amount
- 📊 bStats metrics integration.
