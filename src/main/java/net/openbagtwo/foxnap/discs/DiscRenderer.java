package net.openbagtwo.foxnap.discs;

import static net.openbagtwo.foxnap.FoxNap.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.config.Config;

@Environment(EnvType.CLIENT)
public class DiscRenderer implements ModelLoadingPlugin {

  private final int placeholderStart;
  private final int maxNumDiscs;

  public DiscRenderer(Config config) {
    this.placeholderStart = config.getNumDiscs();
    this.maxNumDiscs = config.getMaximumNumberOfDiscs();
  }

  @Override
  public void onInitializeModelLoader(Context pluginContext) {
    Identifier placeholder = Identifier.of(MOD_ID, "item/placeholder");
    pluginContext.modifyModelOnLoad().register((original, context) -> {
      for (int i = this.placeholderStart; i < this.maxNumDiscs; i++) {
        Identifier matchMe = Identifier.of(MOD_ID, String.format("item/track_%d", i + 1));
        if (context.topLevelId().equals(matchMe)) {
          return context.getOrLoadModel(placeholder);
        }
      }
      return original;
    });
  }
}
