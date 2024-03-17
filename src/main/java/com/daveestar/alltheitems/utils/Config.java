package com.daveestar.alltheitems.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
  private FileConfiguration fileCfgn;
  private File file;

  /*
   * Initialize the configuration from the file.
   * Check if the file/cfgn already exists and load it.
   */
  public Config(String configName, File path) {
    file = new File(path, configName);

    // checck if the file already exists
    if (!file.exists()) {
      // create file directory
      path.mkdirs();

      // try to creaate the new file at the path
      try {
        file.createNewFile();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    fileCfgn = new YamlConfiguration();

    // try to load the yaml file configuration from the file
    try {
      fileCfgn.load(file);
    } catch (IOException | InvalidConfigurationException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get a reference to the file.
   */
  public File getFile() {
    return file;
  }

  /**
   * Get a reference to the file configuration.
   */
  public FileConfiguration getFileCfgrn() {
    return fileCfgn;
  }

  /**
   * Save the configuration into the file.
   */
  public void save() {
    try {
      fileCfgn.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Load the configuration from the file.
   */
  public void load() {
    try {
      fileCfgn.load(file);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }
}
