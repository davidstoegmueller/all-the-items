package com.daveestar.alltheitems.gui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class CurrentItemsGUI {
  private static final String _GUI_TITLE_PREFIX = ChatColor.YELLOW + "" + ChatColor.BOLD + "» ";
  private static final String _GUI_ITEM_PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "» " + ChatColor.YELLOW;
  private static final String _GUI_LORE_PREFIX = ChatColor.YELLOW + "» " + ChatColor.GRAY;

  private static final String _KEY_CURRENT_ITEM = "currentItem";
  private static final String _KEY_QUEUE_ITEM_PREFIX = "queueItem::";
  private static final String _KEY_REMAINING_ITEMS = "action::openRemainingItems";
  private static final String _KEY_COLLECTED_ITEMS = "action::openCollectedItems";
  private static final String _KEY_INFO_ITEM = "action::info";
  private static final String _KEY_TOP_PLACEHOLDER_PREFIX = "topPlaceholder::";

  private static final int _GUI_ROWS = 2;
  private static final int _CURRENT_ITEM_SLOT = 4;
  private static final int _MAX_VISIBLE_QUEUE_ITEMS = 2;
  private static final int _REMAINING_ITEMS_SLOT = (_GUI_ROWS - 1) * 9 + 0;
  private static final int _COLLECTED_ITEMS_SLOT = (_GUI_ROWS - 1) * 9 + 1;
  private static final int _INFO_ITEM_SLOT = (_GUI_ROWS - 1) * 9 + 8;

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
    Map<String, Integer> customSlots = new LinkedHashMap<>();

    _addCurrentAndQueueEntries(p, entries, customSlots);
    _addPlaceholders(entries, customSlots);

    CustomGUI currentItemsGUI = new CustomGUI(
        _plugin,
        p,
        Main.getPrefix() + _GUI_TITLE_PREFIX + "Current Items",
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
    currentItemsGUI.addFooterEntry(_KEY_INFO_ITEM, _createInfoItem(), _INFO_ITEM_SLOT);
    currentItemsGUI.setClickActions(actions);

    currentItemsGUI.open(p);
  }

  private void _addCurrentAndQueueEntries(Player player, Map<String, ItemStack> entries,
      Map<String, Integer> customSlots) {
    List<String> itemQueue = _allTheItemsManager.getQueue();
    DynamicSlots dynamicSlots = _getDynamicSlots(itemQueue);

    entries.put(_KEY_CURRENT_ITEM, _createCurrentItemStateItem(player));
    customSlots.put(_KEY_CURRENT_ITEM, dynamicSlots.currentItemSlot());

    for (int queueOffset = 1; queueOffset <= _MAX_VISIBLE_QUEUE_ITEMS; queueOffset++) {
      int queueIndex = queueOffset;
      int queueIndexDisplay = queueIndex + 1;

      if (itemQueue.size() <= queueIndex) {
        break;
      }

      int queueSlot = dynamicSlots.firstQueueItemSlot() + (queueOffset - 1);
      if (queueSlot >= 9) {
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
    int visibleItemCount = 1 + Math.min(_MAX_VISIBLE_QUEUE_ITEMS, availableQueueItems);
    int slotShift = (visibleItemCount - 1) / 2;

    int currentItemSlot = _CURRENT_ITEM_SLOT - slotShift;
    int firstQueueItemSlot = currentItemSlot + 1;

    return new DynamicSlots(currentItemSlot, firstQueueItemSlot);
  }

  // ------------
  // CREATE ITEMS
  // ------------

  private ItemStack _createCurrentItemStateItem(Player player) {
    List<String> itemQueue = _allTheItemsManager.getQueue();
    String currentItemName = itemQueue.isEmpty() ? null : itemQueue.get(0);
    boolean isComplete = _allTheItemsManager.isComplete();

    if (isComplete) {
      return _createItem(
          Material.NETHER_STAR,
          _GUI_ITEM_PREFIX + "Completed",
          true, List.of(
              "",
              _GUI_LORE_PREFIX + "Congratulations!",
              "",
              _GUI_LORE_PREFIX + "All items have been collected."));
    }

    Material currentMaterial = Material.matchMaterial(currentItemName);

    if (currentMaterial == null) {
      return _createItem(
          Material.BARRIER,
          _GUI_ITEM_PREFIX + "Unknown Item",
          true,
          List.of(
              "",
              _GUI_LORE_PREFIX + "Something went wrong while fetching the current item."));
    }

    List<String> lore = new ArrayList<>(List.of(
        "",
        _GUI_LORE_PREFIX + "#1 - Current Item",
        _GUI_LORE_PREFIX + "Collect this item to progress."));

    if (player.hasPermission(Permissions.ADMIN.getName())) {
      lore.add("");
      lore.add(_GUI_LORE_PREFIX + "Shift + Left-Click: Skip for later");
      lore.add(_GUI_LORE_PREFIX + "Shift + Right-Click: Collect & Next Item");

    }

    return _createItem(
        currentMaterial,
        _GUI_ITEM_PREFIX + _getTranslatedItemName(currentMaterial),
        true,
        lore);
  }

  private ItemStack _createQueueItemStateItem(String queueItemName, int queueIndex) {
    Material queueMaterial = Material.matchMaterial(queueItemName);

    if (queueMaterial == null) {
      return _createItem(
          Material.BARRIER,
          _GUI_ITEM_PREFIX + "Unknown Queue Item",
          false,
          List.of(
              "",
              _GUI_LORE_PREFIX + "#" + queueIndex + " - Queue Item",
              _GUI_LORE_PREFIX + "Something went wrong while fetching this queue item."));
    }

    return _createItem(
        queueMaterial,
        _GUI_ITEM_PREFIX + _getTranslatedItemName(queueMaterial),
        false,
        List.of(
            "",
            _GUI_LORE_PREFIX + "#" + queueIndex + " - Queue Item",
            _GUI_LORE_PREFIX + "Upcoming item."));
  }

  private ItemStack _createOpenRemainingItemsItem() {
    return _createItem(
        Material.CHEST,
        _GUI_ITEM_PREFIX + "Remaining Items",
        false,
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
        false,
        List.of(
            "",
            _GUI_LORE_PREFIX + "Open all collected items.",
            "",
            _GUI_LORE_PREFIX + "Left-Click: Open"));
  }

  private ItemStack _createInfoItem() {
    int totalItemsAmount = _allTheItemsManager.getTotalItemsAmount();
    int remainingItemsAmount = _allTheItemsManager.getRemainingItemsAmount();
    int collectedItemsAmount = _allTheItemsManager.getCollectedItemsAmount();

    String currentItem = _allTheItemsManager.getCurrentItem();
    Material currentMaterial = currentItem == null ? null : Material.matchMaterial(currentItem);
    String currentItemName = _allTheItemsManager.isComplete()
        ? "Completed"
        : currentMaterial == null ? "None" : _getTranslatedItemName(currentMaterial);

    return _createItem(
        Material.BOOK,
        _GUI_ITEM_PREFIX + "Info",
        false,
        List.of(
            "",
            _GUI_LORE_PREFIX + "Goal: Collect every item once.",
            _GUI_LORE_PREFIX + "Collect the current item to progress.",
            _GUI_LORE_PREFIX + "Auto-marks item as collected when obtained.",
            _GUI_LORE_PREFIX + "The queue shows your next items.",
            "",
            ChatColor.GRAY + "Progress Overview:",
            _GUI_LORE_PREFIX + "Total Items: " + ChatColor.YELLOW + totalItemsAmount,
            _GUI_LORE_PREFIX + "Items Remaining: " + ChatColor.YELLOW + remainingItemsAmount,
            _GUI_LORE_PREFIX + "Items Collected: " + ChatColor.YELLOW + collectedItemsAmount,
            _GUI_LORE_PREFIX + "Current Item: " + ChatColor.YELLOW + currentItemName));
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

  private String _getTranslatedItemName(Material material) {
    String translatedItemKey = material.getItemTranslationKey();
    Component itemNameComponent = Component.translatable(translatedItemKey);

    return PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
  }

  // --------------------
  // CLICK ACTION HANDLER
  // --------------------

  private void _handleSkipItem(Player p) {
    if (!p.hasPermission(Permissions.ADMIN.getName())) {
      return;
    }

    String currentItem = _allTheItemsManager.getCurrentItem();
    Material currentMaterial = currentItem == null ? null : Material.matchMaterial(currentItem);
    String currentItemName = currentMaterial == null ? currentItem : _getTranslatedItemName(currentMaterial);

    String nextItem = _allTheItemsManager.skipItem();
    if (nextItem == null) {
      p.sendMessage(Main.getPrefix() + ChatColor.GREEN + "All items are completed.");
    } else {
      Material nextMaterial = nextItem == null ? null : Material.matchMaterial(nextItem);
      String nextName = nextMaterial == null ? nextItem : _getTranslatedItemName(nextMaterial);

      p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Skipped current item " + ChatColor.YELLOW + currentItemName
          + ChatColor.GRAY + ".");
      p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Next item is now " + ChatColor.YELLOW + nextName
          + ChatColor.GRAY + ".");
    }

    displayCurrentItemsGUI(p);
  }

  private void _handleNextItem(Player p) {
    if (!p.hasPermission(Permissions.ADMIN.getName())) {
      return;
    }

    String currentItem = _allTheItemsManager.getCurrentItem();
    Material currentMaterial = currentItem == null ? null : Material.matchMaterial(currentItem);
    String currentItemName = currentMaterial == null ? currentItem : _getTranslatedItemName(currentMaterial);

    String nextItem = _allTheItemsManager.nextItem();
    if (nextItem == null) {
      p.sendMessage(Main.getPrefix() + ChatColor.GREEN + "All items are completed.");
    } else {
      Material nextMaterial = nextItem == null ? null : Material.matchMaterial(nextItem);
      String nextName = nextMaterial == null ? nextItem : _getTranslatedItemName(nextMaterial);

      p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Collected current item " + ChatColor.YELLOW + currentItemName
          + ChatColor.GRAY + ".");
      p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "Next item is now " + ChatColor.YELLOW + nextName
          + ChatColor.GRAY + ".");
    }

    displayCurrentItemsGUI(p);
  }

  // -------------------
  // CLICK ACTION HELPER
  // -------------------

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
