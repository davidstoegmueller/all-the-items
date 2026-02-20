package com.daveestar.alltheitems.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
  private FileConfiguration _fileConfiguration;
  private File _file;

  public Config(String configName, File path) {
    this(configName, path, null, false);
  }

  public Config(String configName, File path, JavaPlugin plugin, boolean copyDefaultResource) {
    _file = new File(path, configName);

    if (!_file.exists()) {
      path.mkdirs();

      if (copyDefaultResource && plugin != null) {
        _copyResourceOrCreateEmptyFile(plugin, configName);
      } else {
        _createEmptyFile();
      }
    }

    _fileConfiguration = new YamlConfiguration();

    try {
      _fileConfiguration.load(_file);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }

  public File getFile() {
    return _file;
  }

  public FileConfiguration getFileConfig() {
    return _fileConfiguration;
  }

  public void save() {
    try {
      _fileConfiguration.save(_file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void reload() {
    try {
      _fileConfiguration.load(_file);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }

  private void _copyResourceOrCreateEmptyFile(JavaPlugin plugin, String configName) {
    try {
      plugin.saveResource(configName, false);
    } catch (IllegalArgumentException e) {
      _createEmptyFile();
    }
  }

  private void _createEmptyFile() {
    try {
      _file.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
