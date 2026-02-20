package com.daveestar.alltheitems.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.manager.AllTheItemsManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class InventoryCheckEvent implements Listener {
  private static final int _MAX_AUTO_COLLECTIONS_PER_CHECK = 256;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public InventoryCheckEvent() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityPickupItem(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player p)) {
      return;
    }

    _scheduleInventoryCheck(p);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) {
      return;
    }

    _scheduleInventoryCheck(p);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) {
      return;
    }

    _scheduleInventoryCheck(p);
  }

  @EventHandler(ignoreCancelled = true)
  public void onCraftItem(CraftItemEvent e) {
    if (!(e.getWhoClicked() instanceof Player p)) {
      return;
    }

    _scheduleInventoryCheck(p);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player p = (Player) e.getPlayer();

    _allTheItemsManager.syncBossBarForPlayer(p);
    _scheduleInventoryCheck(p);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) {
    _allTheItemsManager.clearBossBarForPlayer(e.getPlayer());
  }

  @EventHandler
  public void onPlayerKick(PlayerKickEvent e) {
    _allTheItemsManager.clearBossBarForPlayer(e.getPlayer());
  }

  private void _scheduleInventoryCheck(Player p) {
    Bukkit.getScheduler().runTask(_plugin, () -> _checkPlayerInventory(p));
  }

  private void _checkPlayerInventory(Player p) {
    if (!_allTheItemsManager.isGamemodeEnabled() || _allTheItemsManager.isComplete()) {
      return;
    }

    for (int i = 0; i < _MAX_AUTO_COLLECTIONS_PER_CHECK; i++) {
      String currentItemName = _allTheItemsManager.getCurrentItem();
      if (currentItemName == null || currentItemName.isBlank()) {
        break;
      }

      Material currentMaterial = Material.matchMaterial(currentItemName);
      if (currentMaterial == null) {
        break;
      }

      if (!p.getInventory().contains(currentMaterial)) {
        break;
      }

      String collectedItemDisplayName = _getTranslatedItemName(currentMaterial);
      _allTheItemsManager.nextItem();

      _allTheItemsManager.broadcastCurrentItemCollected(collectedItemDisplayName);

      if (_allTheItemsManager.isComplete()) {
        _allTheItemsManager.broadcastAllItemsCompleted();
        break;
      }

      String nextItem = _allTheItemsManager.getCurrentItem();
      Material nextMaterial = nextItem == null ? null : Material.matchMaterial(nextItem);
      String nextItemDisplayName = nextMaterial == null ? nextItem : _getTranslatedItemName(nextMaterial);

      _allTheItemsManager.broadcastNewItem(nextItemDisplayName);
    }
  }

  private String _getTranslatedItemName(Material material) {
    String translatedItemKey = material.getItemTranslationKey();
    Component itemNameComponent = Component.translatable(translatedItemKey);

    return PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
  }
}