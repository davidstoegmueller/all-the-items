package com.daveestar.alltheitems;

import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
  public static Main _mainInstance;
  private static final Logger LOGGER = Logger.getLogger("alltheitems");
  private static final int BSTATS_PLUGIN_ID = 29352;

  @Override
  public void onEnable() {
    _mainInstance = this;

    LOGGER.info("All the Items - ENABLED");

    // enable bStats metrics
    new Metrics(this, BSTATS_PLUGIN_ID);
  }

  @Override
  public void onDisable() {
    LOGGER.info("All the Items - DISABLED");
  }

  public static String getPrefix() {
    return ChatColor.GRAY + "[" + ChatColor.YELLOW + "ATI" + ChatColor.GRAY + "] ";
  }
}
