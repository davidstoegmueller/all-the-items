package com.daveestar.alltheitems.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class CustomBossBar {
  private final BossBar _bossBar;

  public CustomBossBar(String title, BarColor color, BarStyle style) {
    _bossBar = Bukkit.createBossBar(title, color, style);
    _bossBar.setVisible(false);
  }

  public void update(String title, double progress) {
    if (title != null) {
      _bossBar.setTitle(title);
    }

    _bossBar.setProgress(_sanitizeProgress(progress));
  }

  public void setVisible(boolean visible) {
    _bossBar.setVisible(visible);
  }

  public void showToAll() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      addPlayer(p);
    }

    _bossBar.setVisible(true);
  }

  public void addPlayer(Player p) {
    if (p == null || !p.isOnline() || _bossBar.getPlayers().contains(p)) {
      return;
    }

    _bossBar.addPlayer(p);
  }

  public void removePlayer(Player p) {
    if (p == null) {
      return;
    }

    _bossBar.removePlayer(p);
  }

  public void hide() {
    _bossBar.removeAll();
    _bossBar.setVisible(false);
  }

  public void shutdown() {
    hide();
  }

  private double _sanitizeProgress(double progress) {
    if (progress < 0.0) {
      return 0.0;
    }

    if (progress > 1.0) {
      return 1.0;
    }

    return progress;
  }
}