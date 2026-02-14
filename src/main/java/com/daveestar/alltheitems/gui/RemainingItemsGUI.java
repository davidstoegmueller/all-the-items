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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class RemainingItemsGUI {
  private static final String _GUI_TITLE_PREFIX = ChatColor.YELLOW + "" + ChatColor.BOLD + "» ";
  private static final String _GUI_ITEM_PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "» " + ChatColor.YELLOW;
  private static final String _GUI_LORE_PREFIX = ChatColor.YELLOW + "» " + ChatColor.GRAY;

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
        Main.getPrefix() + _GUI_TITLE_PREFIX + "Remaining Items",
        entries,
        _GUI_ROWS,
        null,
        parentGUI,
        EnumSet.of(CustomGUI.Option.ENABLE_SEARCH));

    Map<String, CustomGUI.ClickAction> actions = new LinkedHashMap<>();
    remainingItemsGUI.setClickActions(actions);

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

  private ItemStack _createRemainingItem(String itemName) {
    Material material = Material.matchMaterial(itemName);

    if (material == null) {
      return _createItem(
          Material.BARRIER,
          _GUI_ITEM_PREFIX + itemName,
          List.of(
              "",
              _GUI_LORE_PREFIX + "Invalid material in remaining list."));
    }

    return _createItem(
        material,
        _GUI_ITEM_PREFIX + _getTranslatedItemName(material),
        List.of(
            "",
            _GUI_LORE_PREFIX + ChatColor.RED + "NOT COLLECTED"));
  }

  private String _getTranslatedItemName(Material material) {
    String translatedItemKey = material.getItemTranslationKey();
    Component itemNameComponent = Component.translatable(translatedItemKey);

    return PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
  }

  private ItemStack _createItem(Material material, String displayName, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta == null) {
      return item;
    }

    meta.displayName(Component.text(displayName));
    meta.lore(lore.stream().map(Component::text).toList());
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    item.setItemMeta(meta);

    return item;
  }
}
