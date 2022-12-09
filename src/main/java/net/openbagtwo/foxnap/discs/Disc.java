package net.openbagtwo.foxnap.discs;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.Rarity;

/**
 * A custom music disc created by this mod
 */
public class Disc extends MusicDiscItem {

  /**
   * Create a new music disc
   *
   * @param comparatorOutput  the output signal a comparator should read from a jukebox with this
   *                          disc loaded
   * @param sound             the sound (track) a jukebox with this disc loaded should play
   * @param trackLength       The length of the track in seconds. This value is currently only used
   *                          by Allays (presumably to determine when to stop dancing and duping?).
   * @param creativeInventory whether the disc should appear in the creative inventory
   */
  public Disc(int comparatorOutput, SoundEvent sound, int trackLength, boolean creativeInventory) {
    super(comparatorOutput, sound, generateSettings(creativeInventory), trackLength);
  }

  private static Item.Settings generateSettings(boolean creativeInventory) {
    Item.Settings settings = new Item.Settings().rarity(Rarity.RARE).maxCount(1);
    if (creativeInventory) {
      settings = settings.group(ItemGroup.MISC);
    }
    return settings;
  }
}
