package net.openbagtwo.foxnap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.MusicDiscItem;
import net.openbagtwo.foxnap.config.Config;
import net.openbagtwo.foxnap.discs.DiscRegistry;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;
import net.openbagtwo.foxnap.instruments.SecretlyJustAGoatHorn;
import net.openbagtwo.foxnap.villagers.Conductor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FoxNap implements ModInitializer {

  public static final String MOD_ID = "foxnap";
  public static final String MOD_NAME = "Fox Nap";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    Config config = Config.loadConfiguration();
    LOGGER.info("Registering " + MOD_NAME);
    List<SecretlyJustAGoatHorn> instruments = InstrumentRegistry.init();
    List<MusicDiscItem> custom_discs = DiscRegistry.init(
        config.getNumDiscs(),
        config.getTrackLengths()
    );
    if (config.getMaestroEnabled()) {
      Conductor.init(instruments, custom_discs);
    }
    LOGGER.info(MOD_NAME + " Initialization Complete");
  }
}
