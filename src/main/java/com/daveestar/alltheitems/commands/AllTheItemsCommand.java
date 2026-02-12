package com.daveestar.alltheitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.enums.Permissions;

import net.md_5.bungee.api.ChatColor;

public class AllTheItemsCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender cs, Command c, String label, String[] args) {
    if (!(cs instanceof Player)) {
      cs.sendMessage(Main.getNoPlayerMessage());
      return true;
    }

    Player p = (Player) cs;

    if (!p.hasPermission(Permissions.ADMIN.getName())) {
      p.sendMessage(Main.getNoPermissionMessage(Permissions.ADMIN));
      return true;
    }

    if (args.length > 0) {
      p.sendMessage(Main.getPrefix() + ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/alltheitems");
      return true;
    }

    if (Main.getInstance().getAllTheItemsManager().isComplete()) {
      p.sendMessage(
          Main.getPrefix() + ChatColor.RED + "All items are already completed. No further action is possible.");
      return true;
    }

    String nextItem = Main.getInstance().getAllTheItemsManager().setRandomNextItem();
    String nextItemLabel = nextItem == null ? "<none>" : nextItem;
    Main.getInstance().getLogger().info("Debug switched current item to " + nextItemLabel);
    p.sendMessage(Main.getPrefix() + ChatColor.YELLOW + "Debug: next item is " + nextItemLabel);

    return true;
  }
}
