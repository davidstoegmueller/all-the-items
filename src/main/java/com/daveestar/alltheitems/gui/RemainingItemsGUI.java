package com.daveestar.alltheitems.gui;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.manager.AllTheItemsManager;
import com.daveestar.alltheitems.utils.CustomGUI;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class RemainingItemsGUI {
  private static final String _KEY_ITEM_PREFIX = "remaining::";

  private static final int _GUI_ROWS = 6;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public RemainingItemsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  public void displayRemainingItemsGUI(Player p, CustomGUI parentGUI) {
    Map<String, ItemStack> entries = _createRemainingItemEntries();

    CustomGUI remainingItemsGUI = new CustomGUI(
        _plugin,
        p,
        Main.getPrefix() + Main.getGuiTitlePrefix() + "Remaining Items",
        entries,
        _GUI_ROWS,
        null,
        parentGUI,
        EnumSet.of(CustomGUI.Option.ENABLE_SEARCH));

    remainingItemsGUI.setClickActions(Map.of());

    remainingItemsGUI.open(p);
  }

  private Map<String, ItemStack> _createRemainingItemEntries() {
    List<String> remainingItems = _allTheItemsManager.getRemainingItems();
    Map<String, ItemStack> entries = new LinkedHashMap<>();

    for (String itemName : remainingItems) {
      if (itemName == null || itemName.isBlank()) {
        continue;
      }

      String normalizedItemName = itemName.trim();
      String key = _KEY_ITEM_PREFIX + normalizedItemName;
      entries.put(key, _createRemainingItem(normalizedItemName));
    }

    return entries;
  }

  // -----
  // ITEMS
  // -----

  private ItemStack _createRemainingItem(String itemName) {
    Material material = Material.matchMaterial(itemName);

    if (material == null) {
      return _createItem(
          Material.BARRIER,
          Main.getGuiItemPrefix() + itemName,
          false,
          List.of(
              "",
              Main.getGuiLorePrefix() + "Invalid material in remaining list."));
    }

    return _createItem(
        material,
        Main.getGuiItemPrefix() + _allTheItemsManager.getTranslatedItemName(material),
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + ChatColor.RED + "NOT COLLECTED"));
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
