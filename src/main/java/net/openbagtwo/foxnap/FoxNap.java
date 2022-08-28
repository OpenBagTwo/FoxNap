package net.openbagtwo.foxnap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.openbagtwo.foxnap.discs.DiscRegistry;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FoxNap implements ModInitializer {

  public static final String MOD_ID = "foxnap";
  public static final String MOD_NAME = "Fox Nap";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LOGGER.info("Registering " + MOD_NAME);
    List<Item> instruments = InstrumentRegistry.init();
    List<MusicDiscItem> custom_discs = DiscRegistry.init();
    LOGGER.info(MOD_NAME + " Initialization Complete");
  }
}
