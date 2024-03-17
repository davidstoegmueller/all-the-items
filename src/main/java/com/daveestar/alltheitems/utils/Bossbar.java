package com.daveestar.alltheitems.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Bossbar {
  private BossBar bossbar;

  public Bossbar() {
    this.bossbar = Bukkit.createBossBar(null, BarColor.YELLOW, BarStyle.SOLID);
  }

  /**
   * Sync the bossbar with all players.
   */
  public void sync() {
    this.bossbar.removeAll();

    for (Player p : Bukkit.getOnlinePlayers()) {
      this.bossbar.addPlayer(p);
    }
  }

  /**
   * Destory the bossbar.
   */
  public void destroy() {
    this.bossbar.removeAll();
  }

  /**
   * Modify the bossbar.
   * Set the title, progress, ...
   */
  public void modifyBossbar(String text) {
    this.bossbar.setTitle(text);

    // execute sync after modifying the bossbar
    this.sync();
  }
}
