package com.daveestar.alltheitems.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.daveestar.alltheitems.Main;

public class SkipItemCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
    // generate/set new item as traget/active
    Main.ATI.setNewItem(true);

    return true;
  }
}
