package com.daveestar.alltheitems;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.daveestar.alltheitems.utils.Bossbar;
import com.daveestar.alltheitems.utils.Config;

public class ATI {
  public Bossbar bossbar;

  private Config atiConfig;
  private FileConfiguration fileCfgn;

  public ATI(Config atiConfig) {
    this.atiConfig = atiConfig;
    this.fileCfgn = atiConfig.getFileCfgrn();

    this._init();
  }

  /**
   * Set a new target item.
   * With the paremter "skipItem" an items can be skipped but will not be marked
   * as done.
   * 
   * When setting a new item:
   * The current item will be removed from the open list.
   * The current item will be added to the done list.
   */
  public void setNewItem(Boolean skipItem) {
    // get all open items
    List<String> openItems = this._getOpenItems();
    System.out.println("before manipulating items: " + this._getOpenItems().size());

    // get active item
    String activeItem = this._getActiveItem();
    int activeItemIdx = openItems.indexOf(activeItem);
    System.out.println("active item: " + activeItem);

    // get done items
    List<String> doneItems = this._getDoneItems();

    // if the item should not be skipped we remove it from the open items before we
    // calculate the new and add the item to the done items
    // if an item should be skipped we want to keep it in the open items list
    if (skipItem == false && activeItemIdx > -1) {
      // remove from open items
      openItems.remove(activeItemIdx);
      this.fileCfgn.set("open", openItems);

      // add to done items
      doneItems.add(activeItem);
      this.fileCfgn.set("done", doneItems);
    }

    // chose a random new item
    int newItemIdx = ThreadLocalRandom.current().nextInt(0, openItems.size() + 1);
    String newItem = openItems.get(newItemIdx);
    System.out.println("new item: " + newItem);

    // set the new item as active
    this.fileCfgn.set("active", newItem);

    // save the current configuration to the file
    this.atiConfig.save();

    // set the new item as bossbar text
    this.bossbar.modifyBossbar(newItem);

    System.out.println("after manipulating items: " + this._getOpenItems().size());
  }

  /**
   * Reset the current game and start a new one.
   */
  public void reset() {
    List<String> itemList = this._getAllItemList();

    this.fileCfgn.set("active", null);
    this.fileCfgn.set("open", itemList);
    this.fileCfgn.set("done", null);

    this.atiConfig.save();

    this.setNewItem(false);
  }

  /**
   * Handle actions to unload the ati gamemode.
   */
  public void unload() {
    this.bossbar.destroy();
  }

  // ---------------
  // PRIVATE METHODS
  // ---------------

  /**
   * Initialize the gamemode.
   * Check if the mode is already running.
   * If its a new run - generate the item list and choose a random item.
   */
  private void _init() {
    // check if the gamemode is already running
    // if not we will reset the cconfiguration which create a new game
    if (this._getActiveItem() == null) {
      this.reset();
    }

    // create the bossbar
    bossbar = new Bossbar();

    // set the current item in bossbar text
    this.bossbar.modifyBossbar(this._getActiveItem());
  }

  /**
   * Get a list of all items excluding items on the block-list.
   */
  private List<String> _getAllItemList() {
    // define a list of blocked items eg. not obtainable in survival
    List<Material> blockedMaterials = Arrays.asList(Material.AIR, Material.SPAWNER, Material.SUSPICIOUS_GRAVEL,
        Material.SUSPICIOUS_SAND, Material.FARMLAND, Material.BARRIER, Material.LIGHT, Material.STRUCTURE_VOID,
        Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.PLAYER_HEAD, Material.DEBUG_STICK,
        Material.BUDDING_AMETHYST, Material.FROGSPAWN, Material.BUNDLE);

    // filter all the materials check if its an item and NOT on the blocklist
    List<Material> materials = Arrays.stream(Material.values())
        .filter(material -> material.isItem() && !blockedMaterials.contains(material))
        .collect(Collectors.toList());

    // return items as a string list
    return materials.stream().map(material -> material.name()).collect(Collectors.toList());
  }

  /**
   * Get the current active item name as a string.
   */
  private String _getActiveItem() {
    return this.fileCfgn.getString("active");
  }

  /**
   * Get all open item names as a string list.
   */
  private List<String> _getOpenItems() {
    return this.fileCfgn.getStringList("open");
  }

  /**
   * Get all done item names as a string list.
   */
  private List<String> _getDoneItems() {
    return this.fileCfgn.getStringList("done");
  }
}
