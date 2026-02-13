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
  private static final String _KEY_REMAINING = "remaining";
  private static final String _KEY_COLLECTED = "collected";
  private static final String _KEY_CURRENT = "current";
  private static final String _KEY_COMPLETE = "complete";
  private static final String _KEY_COLLECTED_NAME = "name";
  private static final String _KEY_COLLECTED_TIMESTAMP = "timestamp";

  private static final String _KEY_EXCLUDED_ITEMS = "items.excluded";

  private static final boolean _DEV_MODE = true;
  private static final List<Material> _DEV_MODE_ITEMS = Arrays.asList(
      Material.STONE,
      Material.OAK_LOG,
      Material.IRON_INGOT,
      Material.BREAD,
      Material.TORCH,
      Material.COBBLESTONE,
      Material.DIRT,
      Material.CRAFTING_TABLE,
      Material.APPLE,
      Material.GLASS);

  private final Config _stateConfig;
  private final FileConfiguration _stateFileConfig;
  private final FileConfiguration _settingsFileConfig;

  public AllTheItemsManager(Config stateConfig, Config settingsConfig) {
    this._stateConfig = stateConfig;

    this._stateFileConfig = stateConfig.getFileConfig();
    this._settingsFileConfig = settingsConfig.getFileConfig();
  }

  public void initGamemode() {
    if (isComplete()) {
      return;
    }

    List<String> remainingItems = getRemainingItems();

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
    if (isComplete()) {
      return null;
    }

    List<String> remainingItems = getRemainingItems();
    Map<String, CollectedItem> collectedItems = getCollectedItems();

    if (remainingItems.isEmpty()) {
      _saveState(remainingItems, collectedItems, null);
      return null;
    }

    String currentItem = getCurrentItem();

    if (currentItem == null || !remainingItems.contains(currentItem)) {
      String nextItem = _pickRandomRemainingItem(remainingItems);
      _saveState(remainingItems, collectedItems, nextItem);
      return nextItem;
    }

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

  // ------------------
  // GET CONFIG ENTRIES
  // ------------------

  public List<String> getRemainingItems() {
    List<String> remainingItems = _stateFileConfig.getStringList(_KEY_REMAINING);
    return new ArrayList<>(remainingItems);
  }

  public Map<String, CollectedItem> getCollectedItems() {
    Map<String, CollectedItem> collectedItems = new HashMap<>();
    ConfigurationSection collectedSection = _stateFileConfig.getConfigurationSection(_KEY_COLLECTED);

    if (collectedSection == null) {
      return collectedItems;
    }

    for (String uid : collectedSection.getKeys(false)) {
      ConfigurationSection collectedItemSection = collectedSection.getConfigurationSection(uid);

      if (collectedItemSection == null) {
        continue;
      }

      String itemName = collectedItemSection.getString(_KEY_COLLECTED_NAME, "");
      long timestamp = collectedItemSection.getLong(_KEY_COLLECTED_TIMESTAMP, 0L);

      collectedItems.put(uid, new CollectedItem(itemName, timestamp));
    }

    return collectedItems;
  }

  public String getCurrentItem() {
    return _stateFileConfig.getString(_KEY_CURRENT);
  }

  public boolean isComplete() {
    return _stateFileConfig.getBoolean(_KEY_COMPLETE, false);
  }

  public boolean isDevMode() {
    return _DEV_MODE;
  }

  // ----------------
  // PICK RANDOM ITEM
  // ----------------

  private String _pickRandomRemainingItem(List<String> remainingItems) {
    return remainingItems.get(ThreadLocalRandom.current().nextInt(remainingItems.size()));
  }

  // --------------
  // SAVE TO CONFIG
  // --------------

  private void _saveState(List<String> remainingItems, Map<String, CollectedItem> collectedItems, String currentItem) {
    _stateFileConfig.set(_KEY_REMAINING, new ArrayList<>(remainingItems));
    _stateFileConfig.set(_KEY_CURRENT, currentItem);
    _stateFileConfig.set(_KEY_COMPLETE, remainingItems.isEmpty());
    _stateFileConfig.set(_KEY_COLLECTED, null);

    ConfigurationSection collectedSection = _stateFileConfig.createSection(_KEY_COLLECTED);
    for (Entry<String, CollectedItem> collectedItem : collectedItems.entrySet()) {
      ConfigurationSection itemSection = collectedSection.createSection(collectedItem.getKey());

      itemSection.set(_KEY_COLLECTED_NAME, collectedItem.getValue().getName());
      itemSection.set(_KEY_COLLECTED_TIMESTAMP, collectedItem.getValue().getTimestamp());
    }

    _stateConfig.save();
  }

  private Set<Material> _getExcludedItemsFromConfig() {
    return _settingsFileConfig.getStringList(_KEY_EXCLUDED_ITEMS)
        .stream()
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(this::_parseMaterialOrNull)
        .filter(material -> material != null)
        .collect(Collectors.toSet());
  }

  private Material _parseMaterialOrNull(String materialName) {
    return Material.matchMaterial(materialName);
  }

  private List<Material> _getAllItems() {
    if (_DEV_MODE) {
      return new ArrayList<>(_DEV_MODE_ITEMS);
    }

    Set<Material> excludedItems = _getExcludedItemsFromConfig();

    List<Material> materials = Arrays.stream(Material.values())
        .filter(material -> material.isItem() && !excludedItems.contains(material))
        .collect(Collectors.toList());

    return materials;
  }

  // -----
  // TYPES
  // -----

  public static class CollectedItem {
    private final String _name;
    private final long _timestamp;

    public CollectedItem(String name, long timestamp) {
      this._name = name;
      this._timestamp = timestamp;
    }

    public String getName() {
      return _name;
    }

    public long getTimestamp() {
      return _timestamp;
    }
  }
}
