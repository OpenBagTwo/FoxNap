package net.openbagtwo.foxnap;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;
import static net.openbagtwo.foxnap.FoxNap.MOD_NAME;

import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.config.Config;

public class FoxNapClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    Identifier placeholder = new Identifier(MOD_ID, "item/placeholder");
    Map<String, Object> settings = Config.readModSettings();

    ModelLoadingRegistry.INSTANCE.registerResourceProvider(
        manager -> (resourceId, context) -> {
          for (
              int i = (int) settings.get("n_discs") + 1;
              i <=  (int) settings.get("max_discs");
              i++
          ) {
            Identifier matchMe = new Identifier(MOD_ID, String.format("item/track_%d", i));
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
