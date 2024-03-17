package com.daveestar.alltheitems;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.daveestar.alltheitems.commands.SkipItemCommand;
import com.daveestar.alltheitems.events.PlayerJoin;
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

        // initialize the ati event listeners
        this._initEventHandlers();

        // initialize the ati commands
        this._initCommandExecutors();
    }

    @Override
    public void onDisable() {
        LOGGER.info("ALL THE ITEMS DISABLED");

        ATI.bossbar.destroy();
    }

    public static String getPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.YELLOW + "ATI" + ChatColor.GRAY + "] ";
    }

    // ---------------
    // PRIVATE METHODS
    // ---------------

    private void _initATI() {
        Config atiConfig = new Config("ati_config.yml", getDataFolder());
        ATI = new ATI(atiConfig);
    }

    private void _initEventHandlers() {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new PlayerJoin(), this);
    }

    private void _initCommandExecutors() {
        getCommand("skipitem").setExecutor(new SkipItemCommand());
    }
}
