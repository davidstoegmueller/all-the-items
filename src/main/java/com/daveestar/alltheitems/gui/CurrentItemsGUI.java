package com.daveestar.alltheitems.gui;

import java.util.EnumSet;
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
  private static final String _KEY_REMAINING_ITEMS = "action::openRemainingItems";
  private static final String _KEY_COLLECTED_ITEMS = "action::openCollectedItems";

  private static final int _GUI_ROWS = 2;
  private static final int _CURRENT_ITEM_SLOT = 4;
  private static final int _REMAINING_ITEMS_SLOT = 13;
  private static final int _COLLECTED_ITEMS_SLOT = 14;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;
  private final RemainingItemsGUI _remainingItemsGUI;
  private final CollectedItemsGUI _collectedItemsGUI;

  public CurrentItemsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
    _remainingItemsGUI = new RemainingItemsGUI();
    _collectedItemsGUI = new CollectedItemsGUI();
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
        EnumSet.of(CustomGUI.Option.DISABLE_PAGE_BUTTON));

    Map<String, CustomGUI.ClickAction> actions = new LinkedHashMap<>();
    if (_allTheItemsManager.isDevMode()) {
      actions.put(_KEY_CURRENT_ITEM, _clickAction(
          player -> _handleDevAdvanceCurrentItem(player),
          null,
          null,
          null));
    }

    actions.put(_KEY_REMAINING_ITEMS, _clickAction(
        player -> _remainingItemsGUI.displayRemainingItemsGUI(player, currentItemsGUI),
        null,
        null,
        null));

    actions.put(_KEY_COLLECTED_ITEMS, _clickAction(
        player -> _collectedItemsGUI.displayCollectedItemsGUI(player, currentItemsGUI),
        null,
        null,
        null));

    currentItemsGUI.addFooterEntry(_KEY_REMAINING_ITEMS, _createOpenRemainingItemsItem(), _REMAINING_ITEMS_SLOT);
    currentItemsGUI.addFooterEntry(_KEY_COLLECTED_ITEMS, _createOpenCollectedItemsItem(), _COLLECTED_ITEMS_SLOT);
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

    return _createItem(
        currentMaterial,
        _GUI_ITEM_PREFIX + _getTranslatedItemName(currentMaterial),
        List.of(
            "",
            _GUI_LORE_PREFIX + "#1",
            _GUI_LORE_PREFIX + "Collect this item to progress."));
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

  private ItemStack _createOpenRemainingItemsItem() {
    return _createItem(
        Material.CHEST,
        _GUI_ITEM_PREFIX + "Remaining Items",
        List.of(
            "",
            _GUI_LORE_PREFIX + "Open all remaining items.",
            "",
            _GUI_LORE_PREFIX + "Left-Click: Open"));
  }

  private ItemStack _createOpenCollectedItemsItem() {
    return _createItem(
        Material.ENDER_CHEST,
        _GUI_ITEM_PREFIX + "Collected Items",
        List.of(
            "",
            _GUI_LORE_PREFIX + "Open all collected items.",
            "",
            _GUI_LORE_PREFIX + "Left-Click: Open"));
  }

  private void _handleDevAdvanceCurrentItem(Player p) {
    if (!_allTheItemsManager.isDevMode()) {
      return;
    }

    String nextItem = _allTheItemsManager.setRandomNextItem();
    if (nextItem == null) {
      p.sendMessage(Main.getPrefix() + ChatColor.GREEN + "All items are now completed.");
    } else {
      Material nextMaterial = Material.matchMaterial(nextItem);
      String nextName = nextMaterial == null ? nextItem : _getTranslatedItemName(nextMaterial);

      p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Next item is now " + ChatColor.YELLOW + nextName
          + ChatColor.GRAY + ".");
    }

    displayCurrentItemsGUI(p);
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
