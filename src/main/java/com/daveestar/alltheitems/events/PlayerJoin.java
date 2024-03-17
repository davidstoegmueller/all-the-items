package com.daveestar.alltheitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.daveestar.alltheitems.Main;

public class PlayerJoin implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    // sync the ati bossbar with all players on player join
    Main.ATI.bossbar.sync();
  }
}