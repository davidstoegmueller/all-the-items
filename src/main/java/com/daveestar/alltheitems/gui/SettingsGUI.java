package com.daveestar.alltheitems.gui;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.manager.AllTheItemsManager;
import com.daveestar.alltheitems.utils.CompletionFireworkShow;
import com.daveestar.alltheitems.utils.CustomGUI;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class SettingsGUI {
  private static final String _GUI_TITLE_PREFIX = ChatColor.YELLOW + "" + ChatColor.BOLD + "» ";
  private static final String _GUI_ITEM_PREFIX = ChatColor.RED + "" + ChatColor.BOLD + "» " + ChatColor.YELLOW;
  private static final String _GUI_LORE_PREFIX = ChatColor.YELLOW + "» " + ChatColor.GRAY;

  private static final String _KEY_TOGGLE_GAMEMODE = "action::toggle";
  private static final String _KEY_RESET_GAMEMODE = "action::reset";
  private static final String _KEY_MANUAL_FIREWORK_SHOW = "action::manualFireworkShow";

  private static final int _GUI_ROWS = 2;
  private static final int _TOGGLE_SLOT = 3;
  private static final int _MANUAL_FIREWORK_SHOW_SLOT = 4;
  private static final int _RESET_SLOT = 5;

  private final Main _plugin;
  private final AllTheItemsManager _allTheItemsManager;

  public SettingsGUI() {
    _plugin = Main.getInstance();
    _allTheItemsManager = _plugin.getAllTheItemsManager();
  }

  public void displaySettingsGUI(Player p, CustomGUI parentGUI) {
    Map<String, ItemStack> entries = new LinkedHashMap<>();
    Map<String, Integer> customSlots = new LinkedHashMap<>();
    entries.put(_KEY_TOGGLE_GAMEMODE, _createToggleGamemodeItem());
    entries.put(_KEY_MANUAL_FIREWORK_SHOW, _createManualFireworkShowItem());
    entries.put(_KEY_RESET_GAMEMODE, _createResetGamemodeItem());
    customSlots.put(_KEY_TOGGLE_GAMEMODE, _TOGGLE_SLOT);
    customSlots.put(_KEY_MANUAL_FIREWORK_SHOW, _MANUAL_FIREWORK_SHOW_SLOT);
    customSlots.put(_KEY_RESET_GAMEMODE, _RESET_SLOT);

    CustomGUI settingsGUI = new CustomGUI(
        _plugin,
        p,
        Main.getPrefix() + _GUI_TITLE_PREFIX + "Settings",
        entries,
        _GUI_ROWS,
        customSlots,
        parentGUI,
        EnumSet.of(CustomGUI.Option.DISABLE_PAGE_BUTTON));

    Map<String, CustomGUI.ClickAction> actions = new LinkedHashMap<>();
    actions.put(_KEY_TOGGLE_GAMEMODE, new CustomGUI.ClickAction() {
      @Override
      public void onLeftClick(Player player) {
        _handleToggleGamemode(player, parentGUI);
      }
    });

    actions.put(_KEY_RESET_GAMEMODE, new CustomGUI.ClickAction() {
      @Override
      public void onLeftClick(Player player) {
        _handleResetGamemode(player);
      }
    });

    actions.put(_KEY_MANUAL_FIREWORK_SHOW, new CustomGUI.ClickAction() {
      @Override
      public void onLeftClick(Player player) {
        _handleManualFireworkShow(player);
      }
    });

    settingsGUI.setClickActions(actions);
    settingsGUI.open(p);
  }

  private void _handleToggleGamemode(Player p, CustomGUI parentGUI) {
    boolean wasEnabled = _allTheItemsManager.isGamemodeEnabled();
    boolean isEnabled = !wasEnabled;
    _allTheItemsManager.setGamemodeEnabled(isEnabled);

    if (isEnabled) {
      _allTheItemsManager.initGamemode();
      p.sendMessage(Main.getPrefix() + ChatColor.GREEN + "Game mode enabled.");
    } else {
      p.sendMessage(Main.getPrefix() + ChatColor.RED + "Game mode disabled. All actions are now blocked.");
    }

    displaySettingsGUI(p, parentGUI);
  }

  private void _handleResetGamemode(Player p) {
    if (!_allTheItemsManager.isGamemodeEnabled()) {
      p.sendMessage(Main.getPrefix() + ChatColor.RED + "Game mode is disabled. Action blocked.");
      return;
    }

    _allTheItemsManager.resetGamemode();
    _allTheItemsManager.initGamemode();

    p.sendMessage(Main.getPrefix() + ChatColor.YELLOW + "Game mode has been reset.");
    p.sendMessage(Main.getPrefix() + ChatColor.GRAY + "All progress was cleared and restarted.");

    new CurrentItemsGUI().displayCurrentItemsGUI(p);
  }

  private void _handleManualFireworkShow(Player p) {
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      CompletionFireworkShow.play(_plugin, onlinePlayer);
    }

    p.closeInventory();
  }

  private ItemStack _createToggleGamemodeItem() {
    boolean isEnabled = _allTheItemsManager.isGamemodeEnabled();

    return _createItem(
        isEnabled ? Material.LIME_DYE : Material.GRAY_DYE,
        _GUI_ITEM_PREFIX + "Game Mode",
        false,
        List.of(
            "",
            _GUI_LORE_PREFIX + "Current State: "
                + (isEnabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"),
            "",
            _GUI_LORE_PREFIX + "Left-Click: Toggle"));
  }

  private ItemStack _createResetGamemodeItem() {
    return _createItem(
        Material.BARRIER,
        _GUI_ITEM_PREFIX + "Reset Game Mode",
        false,
        List.of(
            "",
            _GUI_LORE_PREFIX + ChatColor.RED + ChatColor.BOLD + "ATTENTION: " + ChatColor.GRAY
                + "This will reset all progress.",
            "",
            _GUI_LORE_PREFIX + "The gamemode will be restarted and all progress will be lost.",
            "",
            _GUI_LORE_PREFIX + "Left-Click: Reset game mode"));
  }

  private ItemStack _createManualFireworkShowItem() {
    return _createItem(
        Material.FIREWORK_ROCKET,
        _GUI_ITEM_PREFIX + "Manual Firework Show",
        true,
        List.of(
            "",
            _GUI_LORE_PREFIX + "Because its just an awesome show.",
            "",
            _GUI_LORE_PREFIX + "Left-Click: Start firework show"));
  }

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
