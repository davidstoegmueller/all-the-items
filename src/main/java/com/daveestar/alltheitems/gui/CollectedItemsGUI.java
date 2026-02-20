package com.daveestar.alltheitems.gui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.manager.AllTheItemsManager;
import com.daveestar.alltheitems.manager.AllTheItemsManager.CollectedItem;
import com.daveestar.alltheitems.utils.CustomGUI;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class CollectedItemsGUI {
  private static final String _KEY_ITEM_PREFIX = "collected::";

  private static final int _GUI_ROWS = 6;

  private static final DateTimeFormatter _TIMESTAMP_FORMATTER = DateTimeFormatter
      .ofPattern("dd.MM.yyyy HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public CollectedItemsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  public void displayCollectedItemsGUI(Player p, CustomGUI parentGUI) {
    Map<String, ItemStack> entries = _createCollectedItemEntries();

    CustomGUI collectedItemsGUI = new CustomGUI(
        _plugin,
        p,
        Main.getPrefix() + Main.getGuiTitlePrefix() + "Collected Items",
        entries,
        _GUI_ROWS,
        null,
        parentGUI,
        EnumSet.of(CustomGUI.Option.ENABLE_SEARCH));

    collectedItemsGUI.setClickActions(Map.of());

    collectedItemsGUI.open(p);
  }

  private Map<String, ItemStack> _createCollectedItemEntries() {
    Map<String, CollectedItem> collectedItems = _allTheItemsManager.getCollectedItems();

    List<Entry<String, CollectedItem>> sortedCollectedItems = collectedItems.entrySet().stream()
        .sorted(Comparator.comparingLong((Entry<String, CollectedItem> entry) -> entry.getValue().getTimestamp())
            .reversed())
        .toList();

    Map<String, ItemStack> entries = new LinkedHashMap<>();
    for (Entry<String, CollectedItem> collectedEntry : sortedCollectedItems) {
      CollectedItem collectedItem = collectedEntry.getValue();
      String itemName = collectedItem.getName();

      if (itemName == null || itemName.isBlank()) {
        continue;
      }

      String normalizedItemName = itemName.trim();
      String key = _KEY_ITEM_PREFIX + collectedEntry.getKey();
      entries.put(key, _createCollectedItem(normalizedItemName, collectedItem.getTimestamp()));
    }

    return entries;
  }

  // ----
  // ITEM
  // ----

  private ItemStack _createCollectedItem(String itemName, long collectedTimestamp) {
    Material material = Material.matchMaterial(itemName);

    if (material == null) {
      return _createItem(
          Material.BARRIER,
          Main.getGuiItemPrefix() + itemName,
          false,
          List.of(
              "",
              Main.getGuiLorePrefix() + "Collected At: " + ChatColor.YELLOW + _formatTimestamp(collectedTimestamp),
              "",
              Main.getGuiLorePrefix() + "Invalid material in collected list."));
    }

    return _createItem(
        material,
        Main.getGuiItemPrefix() + _allTheItemsManager.getTranslatedItemName(material),
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "Collected At: " + ChatColor.YELLOW + _formatTimestamp(collectedTimestamp),
            "",
            Main.getGuiLorePrefix() + ChatColor.GREEN + "COLLECTED"));
  }

  // -------
  // HELPERS
  // -------

  private String _formatTimestamp(long timestamp) {
    if (timestamp <= 0L) {
      return "Unknown";
    }

    return _TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(timestamp));
  }

  // -----------
  // ITEM HELPER
  // -----------

  private ItemStack _createItem(Material material, String displayName, boolean setEnchanted, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta == null) {
      return item;
    }

    meta.displayName(Component.text(displayName));

    if (lore != null) {
      meta.lore(lore.stream().map(Component::text).toList());
    }

    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    meta.setEnchantmentGlintOverride(setEnchanted);
    item.setItemMeta(meta);

    return item;
  }
}
