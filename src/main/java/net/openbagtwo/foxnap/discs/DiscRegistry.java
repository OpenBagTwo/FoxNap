package net.openbagtwo.foxnap.discs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

/**
 * Dynamic registry of music discs created by this mod
 */
public class DiscRegistry {


  // TODO: load from config file
  private static final int NUMBER_OF_DISCS = 2;

  /**
   * Create a new music disc, and register the item and its sound event
   *
   * @param track_name        The name of the sound event (as specified in sounds.json)
   * @param comparator_output The output signal a comparator should read from a jukebox with this
   *                          disc loaded
   * @return the fully instantiated and registered music disc
   */
  public static Disc registerDisc(String track_name, int comparator_output) {
    Disc disc = Registry.register(
        Registry.ITEM,
        new Identifier(FoxNap.MOD_ID, track_name),
        new Disc(comparator_output, registerTrack(track_name))
    );
    FoxNap.LOGGER.info(
        "Registered " + track_name
            + " with comparator signal " + disc.getComparatorOutput()
    );
    return disc;
  }

  private static SoundEvent registerTrack(String track_name) {
    Identifier track_id = new Identifier(FoxNap.MOD_ID, track_name);
    return Registry.register(Registry.SOUND_EVENT, track_id, new SoundEvent(track_id));
  }

  /**
   * Create and register a procedurally-generated set of music discs named track_i (each registered
   * with a sound event name track_i and emitting a comparator signal of strength i), starting at i
   * = 1
   *
   * @return A list of fully instantiated and registered music discs
   */
  public static List<MusicDiscItem> init() {
    ArrayList<MusicDiscItem> discs = new ArrayList<>();
    for (int i = 1; i <= NUMBER_OF_DISCS; i++) {
      discs.add(registerDisc(String.format("track_%d", i), (i - 1) % 15 + 1));
    }
    return discs;
  }
}
