package net.openbagtwo.foxnap;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_NAME;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.openbagtwo.foxnap.config.Config;
import net.openbagtwo.foxnap.discs.DiscRenderer;

public class FoxNapClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    Config config = Config.loadConfiguration();

    ModelLoadingPlugin.register(new DiscRenderer(config));

    LOGGER.info(MOD_NAME + " Client Initialization Complete");
  }
}
