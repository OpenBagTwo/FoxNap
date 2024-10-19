package net.openbagtwo.foxnap.discs;

import static net.openbagtwo.foxnap.FoxNap.MOD_ID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.minecraft.client.render.model.ItemModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.config.Config;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DiscRenderer implements ModelResolver {

  private final int placeholderStart;
  private final int maxNumDiscs;

  public DiscRenderer(Config config) {
    this.placeholderStart = config.getNumDiscs();
    this.maxNumDiscs = config.getMaximumNumberOfDiscs();
  }

  @Override
  public @Nullable UnbakedModel resolveModel(Context context) {
    Identifier placeholder = Identifier.of(MOD_ID, "item/placeholder");
    for (int i = placeholderStart; i < maxNumDiscs; i++) {
      Identifier matchMe = Identifier.of(MOD_ID, String.format("item/track_%d", i + 1));
      if (context.id().equals(matchMe)) {
        return new ItemModel(placeholder);
      }
    }
    return null;
  }
}
