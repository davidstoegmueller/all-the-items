package com.daveestar.alltheitems.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.daveestar.alltheitems.Main;
import com.daveestar.alltheitems.utils.Config;
import com.daveestar.alltheitems.utils.CompletionFireworkShow;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class AllTheItemsManager {
  private static final String _KEY_REMAINING = "remaining";
  private static final String _KEY_COLLECTED = "collected";
  private static final String _KEY_CURRENT = "current";
  private static final String _KEY_QUEUE = "queue";
  private static final String _KEY_COMPLETE = "complete";
  private static final String _KEY_COLLECTED_NAME = "name";
  private static final String _KEY_COLLECTED_TIMESTAMP = "timestamp";
  private static final String _KEY_QUEUE_ITEMS_AMOUNT = "queue.itemsAmount";
  private static final int _DEFAULT_QUEUE_ITEMS_AMOUNT = 2;
  private static final int _MIN_QUEUE_ITEMS_AMOUNT = 0;
  private static final int _MAX_QUEUE_ITEMS_AMOUNT = 8;

  private static final String _KEY_EXCLUDED_ITEMS = "items.excluded";
  private static final String _KEY_GAMEMODE_ENABLED = "gamemode.enabled";

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

  private final Main _plugin;
  private final Config _stateConfig;
  private final Config _settingsConfig;
  private final FileConfiguration _stateFileConfig;
  private final FileConfiguration _settingsFileConfig;

  public AllTheItemsManager(Config stateConfig, Config settingsConfig) {
    this._plugin = Main.getInstance();

    this._stateConfig = stateConfig;
    this._settingsConfig = settingsConfig;

    this._stateFileConfig = stateConfig.getFileConfig();
    this._settingsFileConfig = settingsConfig.getFileConfig();
  }

  public void initGamemode() {
    if (!isGamemodeEnabled() || isComplete()) {
      _refreshBossBar();
      return;
    }

    List<String> remainingItems = getRemainingItems();

    if (remainingItems.isEmpty()) {
      resetGamemode();
      return;
    }

    _refreshBossBar();
  }

  public void resetGamemode() {
    if (!isGamemodeEnabled()) {
      _refreshBossBar();
      return;
    }

    List<Material> allItems = _getAllItems();
    List<String> allItemNames = allItems.stream()
        .map(Material::name)
        .collect(Collectors.toList());

    List<String> remainingItems = new ArrayList<>(allItemNames);
    Map<String, CollectedItem> collectedItems = new HashMap<>();
    List<String> itemQueue = new ArrayList<>();
    _fillQueue(itemQueue, remainingItems, null);

    _saveState(remainingItems, collectedItems, itemQueue);
    _refreshBossBar();
  }

  public String nextItem() {
    if (!isGamemodeEnabled()) {
      _refreshBossBar();
      return null;
    }

    if (isComplete()) {
      _refreshBossBar();
      return null;
    }

    List<String> remainingItems = getRemainingItems();
    Map<String, CollectedItem> collectedItems = getCollectedItems();
    List<String> itemQueue = _getNormalizedQueue(remainingItems);

    if (itemQueue.isEmpty()) {
      _saveState(remainingItems, collectedItems, itemQueue);
      _refreshBossBar();
      return null;
    }

    String currentItem = itemQueue.remove(0);

    if (currentItem == null || !remainingItems.contains(currentItem)) {
      _fillQueue(itemQueue, remainingItems, null);
      _saveState(remainingItems, collectedItems, itemQueue);
      _refreshBossBar();
      return itemQueue.isEmpty() ? null : itemQueue.get(0);
    }

    remainingItems.remove(currentItem);

    String uid = UUID.randomUUID().toString();
    collectedItems.put(uid, new CollectedItem(currentItem, System.currentTimeMillis()));

    _fillQueue(itemQueue, remainingItems, null);
    _saveState(remainingItems, collectedItems, itemQueue);
    _refreshBossBar();

    return itemQueue.isEmpty() ? null : itemQueue.get(0);
  }

  public String skipItem() {
    if (!isGamemodeEnabled()) {
      _refreshBossBar();
      return null;
    }

    if (isComplete()) {
      _refreshBossBar();
      return null;
    }

    List<String> remainingItems = getRemainingItems();
    Map<String, CollectedItem> collectedItems = getCollectedItems();
    List<String> itemQueue = _getNormalizedQueue(remainingItems);

    if (itemQueue.isEmpty()) {
      _saveState(remainingItems, collectedItems, itemQueue);
      _refreshBossBar();
      return null;
    }

    String skippedItem = itemQueue.remove(0);

    _fillQueue(itemQueue, remainingItems, skippedItem);
    _saveState(remainingItems, collectedItems, itemQueue);
    _refreshBossBar();

    return itemQueue.isEmpty() ? null : itemQueue.get(0);
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
    List<String> itemQueue = getQueue();
    return itemQueue.isEmpty() ? null : itemQueue.get(0);
  }

  public List<String> getQueue() {
    List<String> remainingItems = getRemainingItems();
    List<String> itemQueue = _getNormalizedQueue(remainingItems);

    if (!itemQueue.equals(_stateFileConfig.getStringList(_KEY_QUEUE))) {
      _saveState(remainingItems, getCollectedItems(), itemQueue);
    }

    return itemQueue;
  }

  public boolean isComplete() {
    return _stateFileConfig.getBoolean(_KEY_COMPLETE, false);
  }

  public boolean isGamemodeEnabled() {
    return _settingsFileConfig.getBoolean(_KEY_GAMEMODE_ENABLED, true);
  }

  public void setGamemodeEnabled(boolean enabled) {
    _settingsFileConfig.set(_KEY_GAMEMODE_ENABLED, enabled);
    _settingsConfig.save();
    _refreshBossBar();
  }

  public int getQueueItemsAmount() {
    int queueItemsAmount = _settingsFileConfig.getInt(_KEY_QUEUE_ITEMS_AMOUNT, _DEFAULT_QUEUE_ITEMS_AMOUNT);
    return _normalizeQueueItemsAmount(queueItemsAmount);
  }

  public int getMinQueueItemsAmount() {
    return _MIN_QUEUE_ITEMS_AMOUNT;
  }

  public int getMaxQueueItemsAmount() {
    return _MAX_QUEUE_ITEMS_AMOUNT;
  }

  public void setQueueItemsAmount(int queueItemsAmount) {
    int normalizedQueueItemsAmount = _normalizeQueueItemsAmount(queueItemsAmount);

    _settingsFileConfig.set(_KEY_QUEUE_ITEMS_AMOUNT, normalizedQueueItemsAmount);
    _settingsConfig.save();

    List<String> remainingItems = getRemainingItems();
    List<String> itemQueue = _getNormalizedQueue(remainingItems);
    _saveState(remainingItems, getCollectedItems(), itemQueue);
    _refreshBossBar();
  }

  public void syncBossBarForPlayer(Player p) {
    if (_plugin.getBossBarUtils() == null || p == null) {
      return;
    }

    String currentItem = getCurrentItem();
    if (!_shouldDisplayBossBar(currentItem)) {
      _plugin.getBossBarUtils().removePlayer(p);
      return;
    }

    _plugin.getBossBarUtils().update(_buildBossBarTitle(currentItem), getProgressPercentage() / 100.0);
    _plugin.getBossBarUtils().addPlayer(p);
    _plugin.getBossBarUtils().setVisible(true);
  }

  public void clearBossBarForPlayer(Player p) {
    if (_plugin.getBossBarUtils() == null || p == null) {
      return;
    }

    _plugin.getBossBarUtils().removePlayer(p);
  }

  public boolean isDevMode() {
    return _DEV_MODE;
  }

  public int getTotalItemsAmount() {
    return getRemainingItemsAmount() + getCollectedItemsAmount();
  }

  public int getRemainingItemsAmount() {
    return getRemainingItems().size();
  }

  public int getCollectedItemsAmount() {
    return getCollectedItems().size();
  }

  public double getProgressPercentage() {
    int totalItems = getTotalItemsAmount();
    if (totalItems <= 0) {
      return 0.0;
    }

    double progress = ((double) getCollectedItemsAmount() / (double) totalItems) * 100.0;

    if (progress < 0.0) {
      return 0.0;
    }

    if (progress > 100.0) {
      return 100.0;
    }

    return progress;
  }

  public String getTranslatedItemName(Material material) {
    if (material == null) {
      return "Unknown";
    }

    String translatedItemKey = material.getItemTranslationKey();
    Component itemNameComponent = Component.translatable(translatedItemKey);

    return PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
  }

  public String getTranslatedItemName(String itemName) {
    if (itemName == null || itemName.isBlank()) {
      return itemName;
    }

    Material material = Material.matchMaterial(itemName);
    if (material == null) {
      return itemName;
    }

    return getTranslatedItemName(material);
  }

  // ----------------
  // PLAYER MESSAGES
  // ----------------

  public void broadcastCurrentItemCollected(String itemName) {
    _broadcastToAll(ChatColor.GRAY + "Current item " + ChatColor.YELLOW + itemName + ChatColor.GRAY + " was collected.",
        BroadcastCelebration.COLLECTED);
  }

  public void broadcastCurrentItemSkipped(String itemName) {
    _broadcastToAll(ChatColor.GRAY + "Current item " + ChatColor.YELLOW + itemName + ChatColor.GRAY + " was skipped.",
        BroadcastCelebration.SKIPPED);
  }

  public void broadcastNewItem(String itemName) {
    _broadcastToAll(ChatColor.GRAY + "New item is " + ChatColor.YELLOW + itemName + ChatColor.GRAY + ".",
        BroadcastCelebration.NEW_ITEM);
  }

  public void broadcastAllItemsCompleted() {
    _broadcastToAll(ChatColor.GREEN + "All items have been collected.", BroadcastCelebration.COMPLETE);
  }

  // ------------------------
  // QUEUE & PICK RANDOM ITEM
  // ------------------------

  private String _pickRandomRemainingItem(List<String> remainingItems) {
    return remainingItems.get(ThreadLocalRandom.current().nextInt(remainingItems.size()));
  }

  private void _fillQueue(List<String> itemQueue, List<String> remainingItems, String avoidItem) {
    int targetQueueSize = _getTargetQueueSize(remainingItems);

    while (itemQueue.size() < targetQueueSize && itemQueue.size() < remainingItems.size()) {
      List<String> candidates = remainingItems.stream()
          .filter(item -> !itemQueue.contains(item))
          .collect(Collectors.toList());

      if (candidates.isEmpty()) {
        return;
      }

      if (avoidItem != null) {
        List<String> candidatesWithoutAvoided = candidates.stream()
            .filter(item -> !avoidItem.equals(item))
            .collect(Collectors.toList());

        if (!candidatesWithoutAvoided.isEmpty()) {
          candidates = candidatesWithoutAvoided;
        }
      }

      itemQueue.add(_pickRandomRemainingItem(candidates));
    }
  }

  private List<String> _getNormalizedQueue(List<String> remainingItems) {
    List<String> storedQueue = _stateFileConfig.getStringList(_KEY_QUEUE);
    Set<String> normalizedQueueSet = new LinkedHashSet<>();

    for (String itemName : storedQueue) {
      if (itemName == null || itemName.isBlank()) {
        continue;
      }

      String normalizedName = itemName.trim();
      if (!remainingItems.contains(normalizedName)) {
        continue;
      }

      normalizedQueueSet.add(normalizedName);
    }

    List<String> itemQueue = new ArrayList<>(normalizedQueueSet);

    int targetQueueSize = _getTargetQueueSize(remainingItems);
    if (targetQueueSize <= 0) {
      return new ArrayList<>();
    }

    if (itemQueue.size() > targetQueueSize) {
      itemQueue = new ArrayList<>(itemQueue.subList(0, targetQueueSize));
    }

    if (itemQueue.isEmpty()) {
      String legacyCurrent = _stateFileConfig.getString(_KEY_CURRENT);
      if (legacyCurrent != null && !legacyCurrent.isBlank() && remainingItems.contains(legacyCurrent)) {
        itemQueue.add(legacyCurrent);
      }
    }

    if (itemQueue.size() > targetQueueSize) {
      itemQueue = new ArrayList<>(itemQueue.subList(0, targetQueueSize));
    }

    _fillQueue(itemQueue, remainingItems, null);

    return itemQueue;
  }

  private int _getTargetQueueSize(List<String> remainingItems) {
    int totalItemsInQueue = 1 + getQueueItemsAmount();
    return Math.min(totalItemsInQueue, remainingItems.size());
  }

  private int _normalizeQueueItemsAmount(int queueItemsAmount) {
    return Math.max(_MIN_QUEUE_ITEMS_AMOUNT, Math.min(_MAX_QUEUE_ITEMS_AMOUNT, queueItemsAmount));
  }

  // --------------
  // CONFIG HELPERS
  // --------------

  private void _saveState(List<String> remainingItems, Map<String, CollectedItem> collectedItems,
      List<String> itemQueue) {
    String currentItem = itemQueue.isEmpty() ? null : itemQueue.get(0);

    _stateFileConfig.set(_KEY_REMAINING, new ArrayList<>(remainingItems));
    _stateFileConfig.set(_KEY_QUEUE, new ArrayList<>(itemQueue));
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
        .map(materialName -> Material.matchMaterial(materialName))
        .filter(material -> material != null)
        .collect(Collectors.toSet());
  }

  private List<Material> _getAllItems() {
    if (_DEV_MODE) {
      return new ArrayList<>(_DEV_MODE_ITEMS);
    }

    Set<Material> excludedItems = _getExcludedItemsFromConfig();

    return Arrays.stream(Material.values())
        .filter(material -> material.isItem() && !excludedItems.contains(material))
        .collect(Collectors.toList());
  }

  // -----------------
  // BROADCAST HELPERS
  // -----------------

  private void _broadcastToAll(String message, BroadcastCelebration celebration) {
    String prefixedMessage = Main.getPrefix() + message;

    for (Player p : Bukkit.getOnlinePlayers()) {
      p.sendMessage(prefixedMessage);

      if (celebration != null) {
        _playBroadcastCelebration(p, celebration);
      }
    }
  }

  private void _playBroadcastCelebration(Player p, BroadcastCelebration celebration) {
    switch (celebration) {
      case COLLECTED:
      case SKIPPED:
      case NEW_ITEM:
        _playCollectedCelebration(p);
        break;
      case COMPLETE:
        _playCompleteCelebration(p);
        break;
      default:
        break;
    }
  }

  // ------------
  // CELEBRATIONS
  // ------------

  private void _playCollectedCelebration(Player p) {
    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.7F, 1.25F);
    p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.45F);

    _spawnParticles(p, Particle.TOTEM_OF_UNDYING, 24, 0.45, 0.8, 0.45, 0.02);
    _spawnParticles(p, Particle.HAPPY_VILLAGER, 18, 0.55, 0.9, 0.55, 0.06);
    _spawnParticles(p, Particle.FLAME, 14, 0.4, 0.7, 0.4, 0.01);
  }

  private void _playCompleteCelebration(Player p) {
    p.playSound(p, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9F, 1.0F);
    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.7F);

    _spawnParticles(p, Particle.TOTEM_OF_UNDYING, 36, 0.65, 1.1, 0.65, 0.03);
    _spawnParticles(p, Particle.END_ROD, 28, 0.75, 1.15, 0.75, 0.04);
    _spawnParticles(p, Particle.HAPPY_VILLAGER, 24, 0.75, 1.0, 0.75, 0.08);
    _spawnParticles(p, Particle.FLAME, 20, 0.55, 0.95, 0.55, 0.01);
    _spawnParticles(p, Particle.PORTAL, 26, 0.75, 1.05, 0.75, 0.3);

    CompletionFireworkShow.play(_plugin, p);

    Bukkit.getScheduler().runTaskLater(_plugin, () -> {
      if (!p.isOnline()) {
        return;
      }

      p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.75F, 1.65F);
      _spawnParticles(p, Particle.TOTEM_OF_UNDYING, 20, 0.55, 0.95, 0.55, 0.02);
      _spawnParticles(p, Particle.END_ROD, 18, 0.55, 0.95, 0.55, 0.03);
    }, 8L);
  }

  private void _spawnParticles(Player p, Particle particle, int count, double offsetX, double offsetY,
      double offsetZ, double extra) {
    p.spawnParticle(particle, p.getLocation().add(0, 1.0, 0), count, offsetX, offsetY, offsetZ, extra);
  }

  // -------
  // BOSSBAR
  // -------

  private void _refreshBossBar() {
    if (_plugin.getBossBarUtils() == null) {
      return;
    }

    String currentItem = getCurrentItem();
    if (!_shouldDisplayBossBar(currentItem)) {
      _plugin.getBossBarUtils().hide();
      return;
    }

    _plugin.getBossBarUtils().update(_buildBossBarTitle(currentItem), getProgressPercentage() / 100.0);
    _plugin.getBossBarUtils().showToAll();
  }

  private boolean _shouldDisplayBossBar(String currentItem) {
    return isGamemodeEnabled() && !isComplete() && currentItem != null && !currentItem.isBlank();
  }

  private String _buildBossBarTitle(String currentItem) {
    String itemDisplayName = getTranslatedItemName(currentItem);
    String progressPercentageDisplay = String.format(Locale.ROOT, "%.1f%%", getProgressPercentage());
    return Main.getBossBarPrefix() + "Item: " + ChatColor.YELLOW + itemDisplayName + ChatColor.GRAY
        + " (" + getCollectedItemsAmount() + "/" + getTotalItemsAmount() + " » "
        + progressPercentageDisplay + ")";
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

  private enum BroadcastCelebration {
    COLLECTED,
    SKIPPED,
    NEW_ITEM,
    COMPLETE
  }
}
