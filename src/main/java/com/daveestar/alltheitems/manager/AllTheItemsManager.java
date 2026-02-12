package com.daveestar.alltheitems.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.daveestar.alltheitems.utils.Config;

public class AllTheItemsManager {
  private static final String KEY_REMAINING = "remaining";
  private static final String KEY_COLLECTED = "collected";
  private static final String KEY_CURRENT = "current";
  private static final String KEY_COMPLETE = "complete";
  private static final String KEY_COLLECTED_NAME = "name";
  private static final String KEY_COLLECTED_TIMESTAMP = "timestamp";

  private static final Set<Material> _NON_OBTAINABLE_ITEMS = Set.of(
      Material.BARRIER,
      Material.COMMAND_BLOCK,
      Material.CHAIN_COMMAND_BLOCK,
      Material.REPEATING_COMMAND_BLOCK,
      Material.COMMAND_BLOCK_MINECART,
      Material.STRUCTURE_BLOCK,
      Material.STRUCTURE_VOID,
      Material.JIGSAW,
      Material.DEBUG_STICK,
      Material.LIGHT);

  private final Config _config;
  private final FileConfiguration _fileConfig;

  public AllTheItemsManager(Config config) {
    _config = config;
    _fileConfig = config.getFileConfig();
  }

  public void initGamemode() {
    if (_isComplete()) {
      return;
    }

    List<String> remainingItems = _getRemainingItems();

    if (remainingItems.isEmpty()) {
      resetGamemode();
    }
  }

  public void resetGamemode() {
    List<Material> allItems = _getAllItems();
    List<String> allItemNames = allItems.stream()
        .map(Material::name)
        .collect(Collectors.toList());

    List<String> remainingItems = new ArrayList<>(allItemNames);
    Map<String, CollectedItem> collectedItems = new HashMap<>();
    String currentItem = _pickRandomRemainingItem(remainingItems);

    _saveState(remainingItems, collectedItems, currentItem);
  }

  public String setRandomNextItem() {
    if (_isComplete()) {
      return null;
    }

    List<String> remainingItems = _getRemainingItems();
    Map<String, CollectedItem> collectedItems = _getCollectedItems();

    if (remainingItems.isEmpty()) {
      _saveState(remainingItems, collectedItems, null);
      return null;
    }

    String currentItem = _getCurrentItem();
    remainingItems.remove(currentItem);

    String uid = UUID.randomUUID().toString();
    collectedItems.put(uid, new CollectedItem(currentItem, System.currentTimeMillis()));

    if (remainingItems.isEmpty()) {
      _saveState(remainingItems, collectedItems, null);
      return null;
    }

    String nextItem = _pickRandomRemainingItem(remainingItems);

    _saveState(remainingItems, collectedItems, nextItem);

    return nextItem;
  }

  public boolean isComplete() {
    return _isComplete();
  }

  // ----------------
  // PICK RANDOM ITEM
  // ----------------

  private String _pickRandomRemainingItem(List<String> remainingItems) {
    return remainingItems.get(ThreadLocalRandom.current().nextInt(remainingItems.size()));
  }

  // ------------------
  // GET CONFIG ENTRIES
  // ------------------

  private List<String> _getRemainingItems() {
    List<String> remainingItems = _fileConfig.getStringList(KEY_REMAINING);
    return new ArrayList<>(remainingItems);
  }

  private Map<String, CollectedItem> _getCollectedItems() {
    Map<String, CollectedItem> collectedItems = new HashMap<>();
    ConfigurationSection collectedSection = _fileConfig.getConfigurationSection(KEY_COLLECTED);

    if (collectedSection == null) {
      return collectedItems;
    }

    for (String uid : collectedSection.getKeys(false)) {
      ConfigurationSection collectedItemSection = collectedSection.getConfigurationSection(uid);

      String itemName = collectedItemSection.getString(KEY_COLLECTED_NAME, "");
      long timestamp = collectedItemSection.getLong(KEY_COLLECTED_TIMESTAMP, 0L);

      collectedItems.put(uid, new CollectedItem(itemName, timestamp));
    }

    return collectedItems;
  }

  private String _getCurrentItem() {
    return _fileConfig.getString(KEY_CURRENT);
  }

  private boolean _isComplete() {
    return _fileConfig.getBoolean(KEY_COMPLETE, false);
  }

  // --------------
  // SAVE TO CONFIG
  // --------------

  private void _saveState(List<String> remainingItems, Map<String, CollectedItem> collectedItems, String currentItem) {
    _fileConfig.set(KEY_REMAINING, new ArrayList<>(remainingItems));
    _fileConfig.set(KEY_CURRENT, currentItem);
    _fileConfig.set(KEY_COMPLETE, remainingItems.isEmpty());
    _fileConfig.set(KEY_COLLECTED, null);

    ConfigurationSection collectedSection = _fileConfig.createSection(KEY_COLLECTED);
    for (Entry<String, CollectedItem> collectedItem : collectedItems.entrySet()) {
      ConfigurationSection itemSection = collectedSection.createSection(collectedItem.getKey());

      itemSection.set(KEY_COLLECTED_NAME, collectedItem.getValue().getName());
      itemSection.set(KEY_COLLECTED_TIMESTAMP, collectedItem.getValue().getTimestamp());
    }

    _config.save();
  }

  private List<Material> _getAllItems() {
    // List<Material> materials = Arrays.stream(Material.values())
    // .filter(material -> material.isItem() &&
    // !_NON_OBTAINABLE_ITEMS.contains(material))
    // .collect(Collectors.toList());

    // define manual list of materials for testing purposes
    List<Material> materials = Arrays.asList(
        Material.STONE,
        Material.GRASS_BLOCK,
        Material.DIRT,
        Material.COBBLESTONE,
        Material.OAK_LOG,
        Material.SPRUCE_LOG,
        Material.BIRCH_LOG,
        Material.JUNGLE_LOG,
        Material.ACACIA_LOG,
        Material.DARK_OAK_LOG);

    return materials;
  }

  // -----
  // TYPES
  // -----

  private static class CollectedItem {
    private final String name;
    private final long timestamp;

    private CollectedItem(String name, long timestamp) {
      this.name = name;
      this.timestamp = timestamp;
    }

    public String getName() {
      return name;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
