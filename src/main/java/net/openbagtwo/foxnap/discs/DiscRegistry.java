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

  /**
   * Create a new music disc, and register the item and its sound event
   *
   * @param trackName        The name of the sound event (as specified in sounds.json)
   * @param comparatorOutput The output signal a comparator should read from a jukebox with this
   *                         disc loaded
   * @param trackLength      The length of the track in seconds. This value is currently only used
   *                         by Allays (presumably to determine when to stop dancing and duping?).
   * @return the fully instantiated and registered music disc
   */
  public static Disc registerDisc(String trackName, int comparatorOutput, int trackLength) {
    Disc disc = Registry.register(
        Registry.ITEM,
        new Identifier(FoxNap.MOD_ID, trackName),
        new Disc(comparatorOutput, registerTrack(trackName), trackLength)
    );
    FoxNap.LOGGER.info(
        "Registered " + trackName
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
   * @param number_of_discs The number of discs to generate and register
   * @return A list of fully instantiated and registered music discs
   */
  public static List<MusicDiscItem> init(int number_of_discs) {
    ArrayList<MusicDiscItem> discs = new ArrayList<>();
    for (int i = 1; i <= number_of_discs; i++) {
      discs.add(
          registerDisc(
              String.format("track_%d", i),
              (i - 1) % 15 + 1,
              60
          )
      );
    }
    return discs;
  }
}
