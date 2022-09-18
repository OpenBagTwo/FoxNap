package net.openbagtwo.foxnap.discs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.openbagtwo.foxnap.FoxNap;

/**
 * A custom music disc created by this mod
 */
public class Disc extends MusicDiscItem {

  /**
   * Simple toggle to control client-side overrides
   */
  public boolean isPlaceholder = false;

  /**
   * Create a new music disc
   *
   * @param comparatorOutput  the output signal a comparator should read from a jukebox with this
   *                          disc loaded
   * @param sound             the sound (track) a jukebox with this disc loaded should play
   * @param trackLength       This parameter is ignored prior to 1.19.1
   * @param creativeInventory whether the disc should appear in the creative inventory
   */
  public Disc(int comparatorOutput, SoundEvent sound, int trackLength, boolean creativeInventory) {
    super(comparatorOutput, sound, generateSettings(creativeInventory));
  }

  @Override
  @Environment(EnvType.CLIENT)
  public String getTranslationKey() {
    if (isPlaceholder) {
      // TODO: make translation-friendly
      return "item." + FoxNap.MOD_ID + ".placeholder_disc";
    }
// now here's an idea:
//    return "item." + FoxNap.MOD_ID + ".music_disc"
    return super.getTranslationKey();
  }

  @Override
  @Environment(EnvType.CLIENT)
  public MutableText getDescription() {
    if (isPlaceholder) {
      // TODO: make translation-friendly
      return Text.literal("Joe Box - 4.33");
    }
    return super.getDescription();
  }

  private static Item.Settings generateSettings(boolean creativeInventory) {
    Item.Settings settings = new Item.Settings().rarity(Rarity.RARE).maxCount(1);
    if (creativeInventory) {
      settings = settings.group(ItemGroup.MISC);
    }
    return settings;
  }
}
