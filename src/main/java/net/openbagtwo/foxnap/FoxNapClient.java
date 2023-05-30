package net.openbagtwo.foxnap;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;
import static net.openbagtwo.foxnap.FoxNap.MOD_NAME;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.config.Config;

public class FoxNapClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    Identifier placeholder = new Identifier(MOD_ID, "item/placeholder");
    Config config = Config.loadConfiguration();

    ModelLoadingRegistry.INSTANCE.registerResourceProvider(
        manager -> (resourceId, context) -> {
          for (
              int i = config.getNumDiscs();
              i < config.getNumDiscs();
              i++
          ) {
            Identifier matchMe = new Identifier(MOD_ID, String.format("item/track_%d", i + 1));
            if (resourceId.equals(matchMe)) {
              return context.loadModel(placeholder);
            }
          }
          return null;
        }
    );

    LOGGER.info(MOD_NAME + " Client Initialization Complete");
  }
}
