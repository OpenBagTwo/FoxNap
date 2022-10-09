package net.openbagtwo.foxnap.config;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;
import static net.openbagtwo.foxnap.FoxNap.MOD_NAME;

import com.google.common.collect.ImmutableMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Extremely simple reader for an extremely simple config file
 */
public class Config {

  private static final Path config_path = FabricLoader.getInstance().getConfigDir()
      .resolve(MOD_ID + ".yaml").toAbsolutePath();

  public static final ImmutableMap<String, Object> DEFAULTS = ImmutableMap.of(
      "n_discs", 7
  );

  private static final DumperOptions configFormat = new DumperOptions() {{
    this.setIndent(2);
    this.setPrettyFlow(true);
    this.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }};


  /**
   * Write a new configuration file with default options
   *
   * @throws ConfigException If the writer encounters any sort of IO error (permissions?)
   */
  public static void writeDefaultConfigFile() throws ConfigException {
    FileWriter configWriter;
    try {
      configWriter = new FileWriter(config_path.toFile());
    } catch (IOException e) {
      throw new ConfigException(
          "Could not open " + config_path + " for writing.", e
      );
    }
    (new Yaml(configFormat)).dump(DEFAULTS, configWriter);
    LOGGER.info("Created " + MOD_NAME + " configuration file at " + config_path);
  }

  /**
   * Load the settings for the mod, either from file or from defaults
   *
   * @return map of str key to the configuration value
   */
  public static Map<String, Object> readModSettings() {
    HashMap<String, Object> settings = new HashMap<>(DEFAULTS);
    LOGGER.info("Reading " + MOD_NAME + " configuration from " + config_path);
    FileInputStream configReader;
    try {
      configReader = new FileInputStream(config_path.toFile());
    } catch (FileNotFoundException e) {
      LOGGER.warn("No " + MOD_NAME + " configuration file found.");
      try {
        writeDefaultConfigFile();
      } catch (ConfigException ex) {
        LOGGER.error(e.toString());
      }
      LOGGER.info("Loading " + MOD_NAME + " default settings.");
      return settings;
    }
    settings.putAll((new Yaml()).load(configReader));
    return settings;
  }

  private static class ConfigException extends Exception {

    ConfigException(String message, IOException e) {
      super(message, e);
    }
  }

}
