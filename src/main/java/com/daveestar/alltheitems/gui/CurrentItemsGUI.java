package com.daveestar.alltheitems.gui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

public class CurrentItemsGUI {
  private static final String _GUI_TITLE_PREFIX = ChatColor.YELLOW + "" + ChatColor.BOLD + "» ";
  private static final String _GUI_ITEM_PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "» " + ChatColor.YELLOW;
  private static final String _GUI_LORE_PREFIX = ChatColor.YELLOW + "» " + ChatColor.GRAY;

  private static final String _KEY_CURRENT_ITEM = "currentItem";

  private static final int _GUI_ROWS = 2;
  private static final int _CURRENT_ITEM_SLOT = 4;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public CurrentItemsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  public void displayCurrentItemsGUI(Player p) {
    Map<String, ItemStack> entries = new LinkedHashMap<>();
    entries.put(_KEY_CURRENT_ITEM, _createCurrentItemStateItem());

    Map<String, Integer> customSlots = new LinkedHashMap<>();
    customSlots.put(_KEY_CURRENT_ITEM, _CURRENT_ITEM_SLOT);

    CustomGUI currentItemsGUI = new CustomGUI(
        _plugin,
        p,
        _GUI_TITLE_PREFIX + "Current Items",
        entries,
        _GUI_ROWS,
        customSlots,
        null,
        Collections.singleton(CustomGUI.Option.DISABLE_PAGE_BUTTON));

    Map<String, CustomGUI.ClickAction> actions = new LinkedHashMap<>();
    currentItemsGUI.setClickActions(actions);

    currentItemsGUI.open(p);
  }

  private ItemStack _createCurrentItemStateItem() {
    String currentItemName = _allTheItemsManager.getCurrentItem();
    boolean isComplete = _allTheItemsManager.isComplete();

    if (isComplete) {
      return _createItem(
          Material.NETHER_STAR,
          _GUI_ITEM_PREFIX + "Completed",
          List.of(
              "",
              _GUI_LORE_PREFIX + "Congratulations!",
              _GUI_LORE_PREFIX + "All items have been collected."));
    }

    Material currentMaterial = Material.matchMaterial(currentItemName);

    if (currentMaterial == null) {
      return _createItem(
          Material.BARRIER,
          _GUI_ITEM_PREFIX + "Unknown Item",
          List.of(
              "",
              _GUI_LORE_PREFIX + "Something went wrong while fetching the current item."));
    }

    String translatedItemKey = currentMaterial.getItemTranslationKey();

    Component itemNameComponent = Component.translatable(translatedItemKey);
    String itemName = PlainTextComponentSerializer.plainText()
        .serialize(itemNameComponent);

    return _createItem(
        currentMaterial,
        _GUI_ITEM_PREFIX + itemName,
        List.of(
            "",
            _GUI_LORE_PREFIX + "#1",
            _GUI_LORE_PREFIX + "Collect this item to progress."));
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

  private CustomGUI.ClickAction _clickAction(Consumer<Player> onLeft, Consumer<Player> onRight,
      Consumer<Player> onShiftLeft, Consumer<Player> onShiftRight) {
    return new CustomGUI.ClickAction() {
      @Override
      public void onLeftClick(Player p) {
        if (onLeft != null) {
          onLeft.accept(p);
        }
      }

      @Override
      public void onRightClick(Player p) {
        if (onRight != null) {
          onRight.accept(p);
        }
      }

      @Override
      public void onShiftLeftClick(Player p) {
        if (onShiftLeft != null) {
          onShiftLeft.accept(p);
        }
      }

      @Override
      public void onShiftRightClick(Player p) {
        if (onShiftRight != null) {
          onShiftRight.accept(p);
        }
      }
    };
  }
}
