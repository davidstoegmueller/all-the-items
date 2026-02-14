package com.daveestar.alltheitems;

import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import com.daveestar.alltheitems.commands.AllTheItemsCommand;
import com.daveestar.alltheitems.enums.Permissions;
import com.daveestar.alltheitems.manager.AllTheItemsManager;
import com.daveestar.alltheitems.utils.Config;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
  public static Main _mainInstance;
  private static final Logger LOGGER = Logger.getLogger("alltheitems");
  private static final int BSTATS_PLUGIN_ID = 29352;

  private AllTheItemsManager _allTheItemsManager;

  @Override
  public void onEnable() {
    _mainInstance = this;

    Config stateConfig = new Config("state.yml", getDataFolder());
    Config settingsConfig = new Config("settings.yml", getDataFolder(), this, true);

    _allTheItemsManager = new AllTheItemsManager(stateConfig, settingsConfig);
    _allTheItemsManager.initGamemode();

    _registerCommands();
    _registerEvents();

    LOGGER.info("All the Items - ENABLED");

    // enable bStats metrics
    new Metrics(this, BSTATS_PLUGIN_ID);
  }

  @Override
  public void onDisable() {
    LOGGER.info("All the Items - DISABLED");
  }

  public static Main getInstance() {
    return _mainInstance;
  }

  public AllTheItemsManager getAllTheItemsManager() {
    return _allTheItemsManager;
  }

  // --------------------------
  // REGISTER COMMANDS & EVENTS
  // --------------------------

  private void _registerCommands() {
    getCommand("alltheitems").setExecutor(new AllTheItemsCommand());
  }

  private void _registerEvents() {

  }

  // -------------------------
  // CHAT TEMPLATES / PREFIXES
  // -------------------------

  public static String getPrefix() {
    return ChatColor.GRAY + "[" + ChatColor.YELLOW + "ATI" + ChatColor.GRAY + "] ";
  }

  public static String getNoPlayerMessage() {
    return getPrefix() + ChatColor.RED + "This command can only be run by a player.";
  }

  public static String getNoPermissionMessage(Permissions permission) {
    return getPrefix() + ChatColor.RED + "Sorry! You do not have permission to use this. " + ChatColor.GRAY + "("
        + ChatColor.RED + permission.getName() + ChatColor.GRAY + ")";
  }
}
