package com.daveestar.alltheitems;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.daveestar.alltheitems.utils.Config;

/**
 * All the Items Java Plugin.
 */
public class Main extends JavaPlugin {
    public static Main instance;
    public static ATI ATI;

    private static final Logger LOGGER = Logger.getLogger("alltheitems");

    @Override
    public void onEnable() {
        LOGGER.info("ALL THE ITEMS ENABLED");

        instance = this;

        // initialize the ati model
        this._initATI();

        // initialize the ati commands

        // initialize the ati event listeners
    }

    @Override
    public void onDisable() {
        LOGGER.info("ALL THE ITEMS DISABLED");
    }

    public static String getPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.YELLOW + "ATI" + ChatColor.GRAY + "] ";
    }

    private void _initATI() {
        Config atiConfig = new Config("ati_config.yml", getDataFolder());
        ATI = new ATI(atiConfig);
    }
}
