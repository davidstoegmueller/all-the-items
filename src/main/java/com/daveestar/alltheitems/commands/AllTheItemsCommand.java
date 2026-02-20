package com.daveestar.alltheitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.gui.CurrentItemsGUI;

import net.md_5.bungee.api.ChatColor;

public class AllTheItemsCommand implements CommandExecutor {
  private final CurrentItemsGUI _currentItemsGUI;

  public AllTheItemsCommand() {
    _currentItemsGUI = new CurrentItemsGUI();
  }

  @Override
  public boolean onCommand(CommandSender cs, Command c, String label, String[] args) {
    if (!(cs instanceof Player p)) {
      cs.sendMessage(Main.getNoPlayerMessage());
      return true;
    }

    if (args.length > 0) {
      p.sendMessage(Main.getPrefix() + ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/alltheitems");
      return true;
    }

    _currentItemsGUI.displayCurrentItemsGUI(p);

    return true;
  }
}
