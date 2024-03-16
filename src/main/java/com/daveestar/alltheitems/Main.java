package com.daveestar.alltheitems;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * All the Items Java Plugin.
 */
public class Main extends JavaPlugin {
    public static Main instance;

    private static final Logger LOGGER = Logger.getLogger("alltheitems");

    @Override
    public void onEnable() {
        LOGGER.info("ALL THE ITEMS ENABLED");

        instance = this;
    }

    @Override
    public void onDisable() {
        LOGGER.info("ALL THE ITEMS DISABLED");
    }

    public static Main getInstance() {
        return instance;
    }

    public static String getPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.YELLOW + "ATI" + ChatColor.GRAY + "] ";
    }
}
