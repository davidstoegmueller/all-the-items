package com.daveestar.alltheitems.gui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.enums.Permissions;
import com.daveestar.alltheitems.manager.AllTheItemsManager;
import com.daveestar.alltheitems.utils.CustomGUI;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class CurrentItemsGUI {
  private static final String _KEY_CURRENT_ITEM = "currentItem";
  private static final String _KEY_QUEUE_ITEM_PREFIX = "queueItem::";
  private static final String _KEY_REMAINING_ITEMS = "action::openRemainingItems";
  private static final String _KEY_COLLECTED_ITEMS = "action::openCollectedItems";
  private static final String _KEY_SETTINGS_ITEM = "action::openSettings";
  private static final String _KEY_INFO_ITEM = "action::info";
  private static final String _KEY_TOP_PLACEHOLDER_PREFIX = "topPlaceholder::";

  private static final int _GUI_ROWS = 2;
  private static final int _TOP_ROW_SIZE = 9;
  private static final int _CURRENT_ITEM_SLOT = 4;
  private static final int _REMAINING_ITEMS_SLOT = (_GUI_ROWS - 1) * 9 + 0;
  private static final int _COLLECTED_ITEMS_SLOT = (_GUI_ROWS - 1) * 9 + 1;
  private static final int _SETTINGS_ITEM_SLOT = (_GUI_ROWS - 1) * 9 + 7;
  private static final int _INFO_ITEM_SLOT = (_GUI_ROWS - 1) * 9 + 8;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;
  private final RemainingItemsGUI _remainingItemsGUI;
  private final CollectedItemsGUI _collectedItemsGUI;
  private final SettingsGUI _settingsGUI;

  public CurrentItemsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
    _remainingItemsGUI = new RemainingItemsGUI();
    _collectedItemsGUI = new CollectedItemsGUI();
    _settingsGUI = new SettingsGUI();
  }

  public void displayCurrentItemsGUI(Player p) {
    Map<String, ItemStack> entries = new LinkedHashMap<>();
    Map<String, Integer> customSlots = new LinkedHashMap<>();

    _addCurrentAndQueueEntries(p, entries, customSlots);
    _addPlaceholders(entries, customSlots);

    CustomGUI currentItemsGUI = new CustomGUI(
        _plugin,
        p,
        Main.getPrefix() + Main.getGuiTitlePrefix() + "Current Items",
        entries,
        _GUI_ROWS,
        customSlots,
        null,
        EnumSet.of(CustomGUI.Option.DISABLE_PAGE_BUTTON));

    Map<String, CustomGUI.ClickAction> actions = new LinkedHashMap<>();
    actions.put(_KEY_CURRENT_ITEM, _clickAction(
        null,
        null,
        player -> _handleSkipItem(player),
        player -> _handleNextItem(player)));

    actions.put(_KEY_REMAINING_ITEMS, _clickAction(
        player -> {
          if (_isGamemodeDisabled(player)) {
            return;
          }
          _remainingItemsGUI.displayRemainingItemsGUI(player, currentItemsGUI);
        },
        null,
        null,
        null));

    actions.put(_KEY_COLLECTED_ITEMS, _clickAction(
        player -> {
          if (_isGamemodeDisabled(player)) {
            return;
          }
          _collectedItemsGUI.displayCollectedItemsGUI(player, currentItemsGUI);
        },
        null,
        null,
        null));

    if (p.hasPermission(Permissions.ADMIN.getName())) {
      actions.put(_KEY_SETTINGS_ITEM, _clickAction(
          player -> _settingsGUI.displaySettingsGUI(player, currentItemsGUI),
          null,
          null,
          null));
    }

    currentItemsGUI.addFooterEntry(_KEY_REMAINING_ITEMS, _createOpenRemainingItemsItem(), _REMAINING_ITEMS_SLOT);
    currentItemsGUI.addFooterEntry(_KEY_COLLECTED_ITEMS, _createOpenCollectedItemsItem(), _COLLECTED_ITEMS_SLOT);
    if (p.hasPermission(Permissions.ADMIN.getName())) {
      currentItemsGUI.addFooterEntry(_KEY_SETTINGS_ITEM, _createOpenSettingsItem(), _SETTINGS_ITEM_SLOT);
    }
    currentItemsGUI.addFooterEntry(_KEY_INFO_ITEM, _createInfoItem(), _INFO_ITEM_SLOT);
    currentItemsGUI.setClickActions(actions);

    currentItemsGUI.open(p);
  }

  private void _addCurrentAndQueueEntries(Player p, Map<String, ItemStack> entries,
      Map<String, Integer> customSlots) {
    List<String> itemQueue = _allTheItemsManager.getQueue();
    int visibleQueueItems = Math.min(_TOP_ROW_SIZE - 1, Math.max(itemQueue.size() - 1, 0));
    DynamicSlots dynamicSlots = _getDynamicSlots(itemQueue);

    entries.put(_KEY_CURRENT_ITEM, _createCurrentItemStateItem(p));
    customSlots.put(_KEY_CURRENT_ITEM, dynamicSlots.currentItemSlot());

    for (int queueOffset = 1; queueOffset <= visibleQueueItems; queueOffset++) {
      int queueIndex = queueOffset;
      int queueIndexDisplay = queueIndex + 1;

      if (itemQueue.size() <= queueIndex) {
        break;
      }

      int queueSlot = dynamicSlots.firstQueueItemSlot() + (queueOffset - 1);
      if (queueSlot >= _TOP_ROW_SIZE) {
        break;
      }

      String queueItemName = itemQueue.get(queueIndex);
      String queueKey = _KEY_QUEUE_ITEM_PREFIX + queueIndex;

      entries.put(queueKey, _createQueueItemStateItem(queueItemName, queueIndexDisplay));
      customSlots.put(queueKey, queueSlot);
    }
  }

  private DynamicSlots _getDynamicSlots(List<String> itemQueue) {
    int availableQueueItems = Math.max(itemQueue.size() - 1, 0);
    int visibleItemCount = 1 + Math.min(_TOP_ROW_SIZE - 1, availableQueueItems);
    int slotShift = (visibleItemCount - 1) / 2;

    int currentItemSlot = _CURRENT_ITEM_SLOT - slotShift;
    int firstQueueItemSlot = currentItemSlot + 1;

    return new DynamicSlots(currentItemSlot, firstQueueItemSlot);
  }

  // -----
  // ITEMS
  // -----

  private ItemStack _createCurrentItemStateItem(Player p) {
    List<String> itemQueue = _allTheItemsManager.getQueue();
    String currentItemName = itemQueue.isEmpty() ? null : itemQueue.get(0);
    boolean isComplete = _allTheItemsManager.isComplete();

    if (isComplete) {
      return _createItem(
          Material.NETHER_STAR,
          Main.getGuiItemPrefix() + "Completed",
          true, List.of(
              "",
              Main.getGuiLorePrefix() + "Congratulations!",
              "",
              Main.getGuiLorePrefix() + "All items have been collected."));
    }

    Material currentMaterial = Material.matchMaterial(currentItemName);

    if (currentMaterial == null) {
      return _createItem(
          Material.BARRIER,
          Main.getGuiItemPrefix() + "Unknown Item",
          true,
          List.of(
              "",
              Main.getGuiLorePrefix() + "Something went wrong while fetching the current item."));
    }

    List<String> lore = new ArrayList<>(List.of(
        "",
        Main.getGuiLorePrefix() + "#1 - Current Item",
        Main.getGuiLorePrefix() + "Collect this item to progress."));

    if (p.hasPermission(Permissions.ADMIN.getName())) {
      lore.add("");
      lore.add(Main.getGuiLorePrefix() + "Shift + Left-Click: Skip for later");
      lore.add(Main.getGuiLorePrefix() + "Shift + Right-Click: Collect & Next Item");

    }

    return _createItem(
        currentMaterial,
        Main.getGuiItemPrefix() + _allTheItemsManager.getTranslatedItemName(currentMaterial),
        true,
        lore);
  }

  private ItemStack _createQueueItemStateItem(String queueItemName, int queueIndex) {
    Material queueMaterial = Material.matchMaterial(queueItemName);

    if (queueMaterial == null) {
      return _createItem(
          Material.BARRIER,
          Main.getGuiItemPrefix() + "Unknown Queue Item",
          false,
          List.of(
              "",
              Main.getGuiLorePrefix() + "#" + queueIndex + " - Queue Item",
              Main.getGuiLorePrefix() + "Something went wrong while fetching this queue item."));
    }

    return _createItem(
        queueMaterial,
        Main.getGuiItemPrefix() + _allTheItemsManager.getTranslatedItemName(queueMaterial),
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "#" + queueIndex + " - Queue Item",
            Main.getGuiLorePrefix() + "Upcoming item."));
  }

  private ItemStack _createOpenRemainingItemsItem() {
    return _createItem(
        Material.CHEST,
        Main.getGuiItemPrefix() + "Remaining Items",
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "Open all remaining items.",
            "",
            Main.getGuiLorePrefix() + "Left-Click: Open"));
  }

  private ItemStack _createOpenCollectedItemsItem() {
    return _createItem(
        Material.ENDER_CHEST,
        Main.getGuiItemPrefix() + "Collected Items",
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "Open all collected items.",
            "",
            Main.getGuiLorePrefix() + "Left-Click: Open"));
  }

  private ItemStack _createInfoItem() {
    int totalItemsAmount = _allTheItemsManager.getTotalItemsAmount();
    int remainingItemsAmount = _allTheItemsManager.getRemainingItemsAmount();
    int collectedItemsAmount = _allTheItemsManager.getCollectedItemsAmount();
    double progressPercentage = _allTheItemsManager.getProgressPercentage();
    String progressPercentageDisplay = String.format(Locale.ROOT, "%.1f%%", progressPercentage);

    String currentItem = _allTheItemsManager.getCurrentItem();
    Material currentMaterial = currentItem == null ? null : Material.matchMaterial(currentItem);
    String currentItemName = _allTheItemsManager.isComplete()
        ? "Completed"
        : currentMaterial == null ? "None" : _allTheItemsManager.getTranslatedItemName(currentMaterial);

    return _createItem(
        Material.BOOK,
        Main.getGuiItemPrefix() + "Info",
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "Goal: Collect every item once.",
            Main.getGuiLorePrefix() + "Collect the current item to progress.",
            Main.getGuiLorePrefix() + "Auto-marks item as collected when obtained.",
            Main.getGuiLorePrefix() + "The queue shows your next items.",
            "",
            ChatColor.GRAY + "Progress Overview:",
            Main.getGuiLorePrefix() + "Total Items: " + ChatColor.YELLOW + totalItemsAmount,
            Main.getGuiLorePrefix() + "Items Remaining: " + ChatColor.YELLOW + remainingItemsAmount,
            Main.getGuiLorePrefix() + "Items Collected: " + ChatColor.YELLOW + collectedItemsAmount,
            Main.getGuiLorePrefix() + "Progress: " + ChatColor.YELLOW + progressPercentageDisplay,
            Main.getGuiLorePrefix() + "Current Item: " + ChatColor.YELLOW + currentItemName));
  }

  private ItemStack _createOpenSettingsItem() {
    return _createItem(
        Material.COMPARATOR,
        Main.getGuiItemPrefix() + "Settings",
        false,
        List.of(
            "",
            Main.getGuiLorePrefix() + "Open game mode settings.",
            "",
            Main.getGuiLorePrefix() + "Left-Click: Open"));
  }

  private ItemStack _createPlaceholderItem() {
    return _createItem(Material.YELLOW_STAINED_GLASS_PANE, ChatColor.YELLOW + "*", false, null);
  }

  private void _addPlaceholders(Map<String, ItemStack> entries, Map<String, Integer> customSlots) {
    Set<Integer> reservedSlots = new HashSet<>(customSlots.values());

    for (int slot = 0; slot < 9; slot++) {
      if (reservedSlots.contains(slot)) {
        continue;
      }

      String key = _KEY_TOP_PLACEHOLDER_PREFIX + slot;
      entries.put(key, _createPlaceholderItem());
      customSlots.put(key, slot);
    }
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

  // --------------
  // ACTION HANDLER
  // --------------

  private void _handleSkipItem(Player p) {
    if (_isGamemodeDisabled(p)) {
      return;
    }

    if (!p.hasPermission(Permissions.ADMIN.getName())) {
      return;
    }

    String currentItem = _allTheItemsManager.getCurrentItem();
    String currentItemName = _getItemDisplayName(currentItem);

    String nextItem = _allTheItemsManager.skipItem();
    if (nextItem == null) {
      _allTheItemsManager.broadcastAllItemsCompleted();
    } else {
      String nextName = _getItemDisplayName(nextItem);

      _allTheItemsManager.broadcastCurrentItemSkipped(currentItemName);
      _allTheItemsManager.broadcastNewItem(nextName);
    }

    displayCurrentItemsGUI(p);
  }

  private void _handleNextItem(Player p) {
    if (_isGamemodeDisabled(p)) {
      return;
    }

    if (!p.hasPermission(Permissions.ADMIN.getName())) {
      return;
    }

    String currentItem = _allTheItemsManager.getCurrentItem();
    String currentItemName = _getItemDisplayName(currentItem);

    String nextItem = _allTheItemsManager.nextItem();
    if (nextItem == null) {
      _allTheItemsManager.broadcastAllItemsCompleted();
    } else {
      String nextName = _getItemDisplayName(nextItem);

      _allTheItemsManager.broadcastCurrentItemCollected(currentItemName);
      _allTheItemsManager.broadcastNewItem(nextName);
    }

    displayCurrentItemsGUI(p);
  }

  // -------
  // HELPERS
  // -------

  private boolean _isGamemodeDisabled(Player p) {
    if (_allTheItemsManager.isGamemodeEnabled()) {
      return false;
    }

    p.sendMessage(Main.getPrefix() + ChatColor.RED + "Game mode is disabled. Action blocked.");
    return true;
  }

  private String _getItemDisplayName(String itemName) {
    Material material = itemName == null ? null : Material.matchMaterial(itemName);
    return material == null ? itemName : _allTheItemsManager.getTranslatedItemName(material);
  }

  // -------------
  // ACTION HELPER
  // -------------

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

  private record DynamicSlots(int currentItemSlot, int firstQueueItemSlot) {
  }
}
