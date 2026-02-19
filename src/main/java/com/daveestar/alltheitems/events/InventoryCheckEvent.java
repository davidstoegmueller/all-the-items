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

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.manager.AllTheItemsManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class InventoryCheckEvent implements Listener {
  private static final int _MAX_AUTO_COLLECTIONS_PER_CHECK = 256;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public InventoryCheckEvent() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityPickupItem(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    _scheduleInventoryCheck(player);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    _scheduleInventoryCheck(player);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    _scheduleInventoryCheck(player);
  }

  @EventHandler(ignoreCancelled = true)
  public void onCraftItem(CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    _scheduleInventoryCheck(player);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    _scheduleInventoryCheck(event.getPlayer());
  }

  private void _scheduleInventoryCheck(Player player) {
    Bukkit.getScheduler().runTask(_plugin, () -> _checkPlayerInventory(player));
  }

  private void _checkPlayerInventory(Player player) {
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

      if (!player.getInventory().contains(currentMaterial)) {
        break;
      }

      String collectedItemDisplayName = _getTranslatedItemName(currentMaterial);
      _allTheItemsManager.nextItem();

      player.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Current item " + ChatColor.YELLOW
          + collectedItemDisplayName + ChatColor.GRAY + " was collected.");

      if (_allTheItemsManager.isComplete()) {
        player.sendMessage(Main.getPrefix() + ChatColor.GREEN + "All items have been collected.");
        break;
      }

      String nextItem = _allTheItemsManager.getCurrentItem();
      Material nextMaterial = nextItem == null ? null : Material.matchMaterial(nextItem);
      String nextItemDisplayName = nextMaterial == null ? nextItem : _getTranslatedItemName(nextMaterial);

      player.sendMessage(Main.getPrefix() + ChatColor.GRAY + "New item is " + ChatColor.YELLOW
          + nextItemDisplayName + ChatColor.GRAY + ".");
    }
  }

  private String _getTranslatedItemName(Material material) {
    String translatedItemKey = material.getItemTranslationKey();
    Component itemNameComponent = Component.translatable(translatedItemKey);

    return PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
  }
}