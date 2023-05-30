package net.openbagtwo.foxnap.config;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;
import static net.openbagtwo.foxnap.FoxNap.MOD_NAME;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * FoxNap's configuration reader / writer
 */
public class Config {

  /**
   * Path to the config file
   */
  private static final Path config_path = FabricLoader.getInstance().getConfigDir()
      .resolve(MOD_ID + ".yaml").toAbsolutePath();


  /**
   * The number of discs available to the client
   */
  private int numDiscs;

  /**
   * The length of each track in seconds
   */
  private List<Integer> trackLengths;

  /**
   * Get the number of discs that the client has registered
   */
  public int getNumDiscs() {
    return this.numDiscs;
  }

  /**
   * Get the length (in seconds) to register for each track on the server side
   */
  public List<Integer> getTrackLengths() {
    return new ArrayList<>(trackLengths);
  }

  /**
   * Get the number of discs to be added to the server-side item registry
   */
  public int getMaximumNumberOfDiscs() {
    return this.trackLengths.size();
  }

  /**
   * Default values (that aren't directly accessed by the mod)
   */
  private static final int DEFAULT_N_DISCS = 7;
  private static final int DEFAULT_MAX_DISCS = 64;
  private static final int DEFAULT_TRACK_LENGTH = 600;  // technically default default track length

  /**
   * Load the mod configuration, however you have to
   */
  public static Config loadConfiguration() {
    Config config;
    try {
      config = net.openbagtwo.foxnap.config.Config.fromConfigFile();
      LOGGER.debug("Loaded " + MOD_NAME + " configuration.");
    } catch (FileNotFoundException e) {
      LOGGER.warn("No " + MOD_NAME + " configuration file found.");
      try {
        net.openbagtwo.foxnap.config.Config.writeDefaultConfigFile();
      } catch (ConfigException writee) {
        LOGGER.error("Could not write " + MOD_NAME + " configuration:\n" + writee);
      }
      config = net.openbagtwo.foxnap.config.Config.getDefaultConfiguration();
    } catch (ConfigException e) {
      LOGGER.error(MOD_NAME + " configuration is invalid:\n" + e);
      config = net.openbagtwo.foxnap.config.Config.getDefaultConfiguration();
    }
    return config;
  }

  private static final DumperOptions configFormat = new DumperOptions() {{
    this.setIndent(2);
    this.setPrettyFlow(true);
    this.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
  }};

  /**
   * If the configuration cannot be read in from file for whatever reason, generate and return the
   * default configuration
   */
  private static Config getDefaultConfiguration() {
    LOGGER.info("Loading default " + MOD_NAME + " configuration");
    Config config = new Config();
    config.numDiscs = DEFAULT_N_DISCS;
    config.trackLengths = new ArrayList<>();
    for (int i = 0; i < DEFAULT_MAX_DISCS; i++) {
      config.trackLengths.add(DEFAULT_TRACK_LENGTH);
    }
    return config;
  }


  /**
   * Write a new configuration file with default options
   *
   * @throws ConfigException If the writer encounters any sort of IO error (permissions?)
   */
  private static void writeDefaultConfigFile() throws ConfigException {
    FileWriter configWriter;
    try {
      configWriter = new FileWriter(config_path.toFile());
    } catch (IOException e) {
      throw new ConfigException(
          "Could not open " + config_path + " for writing.", e
      );
    }
    Map<String, Object> writeme = new LinkedHashMap<>();
    writeme.put("n_discs", DEFAULT_N_DISCS);
    writeme.put("default_track_length", DEFAULT_TRACK_LENGTH);

    (new Yaml(configFormat)).dump(writeme, configWriter);
    LOGGER.info("Wrote " + MOD_NAME + " configuration file to " + config_path);
  }

  /**
   * Load the settings for the mod, either from file or from defaults
   *
   * @return map of str key to the configuration value
   * @throws ConfigException if the configuration cannot be parsed
   */
  private static Config fromConfigFile() throws FileNotFoundException, ConfigException {

    LOGGER.info("Reading " + MOD_NAME + " configuration from " + config_path);
    FileInputStream configReader = new FileInputStream(config_path.toFile());
    HashMap<String, Object> settings = new HashMap<>((new Yaml()).load(configReader));

    try {
      // Now we actually construct the thing
      int numDiscs = Integer.parseInt(settings.getOrDefault("n_discs", DEFAULT_N_DISCS).toString());
      int defaultTrackLength = Integer.parseInt(settings.getOrDefault("default_track_length",
          DEFAULT_TRACK_LENGTH).toString());
      int maxNumDiscs = Integer.parseInt(
          settings.getOrDefault("max_discs", DEFAULT_MAX_DISCS).toString());
      ArrayList<Integer> trackLengths = new ArrayList<>();
      for (int i = 0; i < maxNumDiscs; i++) {
        trackLengths.add(defaultTrackLength);
      }

      if (settings.containsKey("track_lengths")) {
        Object trackSpecs = settings.get("max_discs");
        if (trackSpecs instanceof Map trackMap) {
          for (Object key : trackMap.keySet()) {
            trackLengths.set(
                Integer.parseInt(key.toString()) - 1,
                Integer.parseInt(trackMap.get(key).toString())
            );
          }
        } else if (trackSpecs instanceof List trackList) {
          for (int i = 0; i < trackList.size(); i++) {
            trackLengths.set(i, Integer.parseInt(trackList.get(i).toString()));
          }
        }
      }
      Config config = new Config();
      config.numDiscs = numDiscs;
      config.trackLengths = trackLengths;
      return config;

    } catch (Exception e) {
      throw new ConfigException(config_path + " is not a valid " + MOD_NAME + " configuration.", e);
    }
  }

  private static class ConfigException extends Exception {

    ConfigException(String message, Exception e) {
      super(message, e);
    }
  }

}
