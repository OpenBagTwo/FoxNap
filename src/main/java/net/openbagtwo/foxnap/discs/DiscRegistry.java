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
    FoxNap.LOGGER.debug(
        "Registered " + trackName
            + " with comparator signal " + disc.getComparatorOutput()
    );
    return disc;
  }

  public static void registerPlaceholderDisc(String trackName) {
    Registry.register(
        Registry.ITEM,
        new Identifier(FoxNap.MOD_ID, trackName),
        new Disc(0, registerTrack(trackName), 0)
    );
  }

  private static SoundEvent registerTrack(String trackName) {
    Identifier track_id = new Identifier(FoxNap.MOD_ID, trackName);
    return Registry.register(Registry.SOUND_EVENT, track_id, new SoundEvent(track_id));
  }

  /**
   * Create and register a procedurally-generated set of music discs named track_i (each registered
   * with a sound event name track_i and emitting a comparator signal of strength i), starting at i
   * = 1
   *
   * @param numberOfDiscs The number of discs to generate and register
   * @return A list of fully instantiated and registered music discs
   */
  public static List<MusicDiscItem> init(int numberOfDiscs) {
    ArrayList<MusicDiscItem> discs = new ArrayList<>();
    for (int i = 1; i <= numberOfDiscs; i++) {
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

  /**
   * In addition to the procedurally-generated set of usable music discs, also generate extra
   * dummy/placeholder discs to prevent client/server conflicts
   *
   * @param numberOfDiscs    The number of discs that will actually be available for use
   * @param maxNumberOfDiscs The total number of discs to register
   * @return The list of discs that should actually be available for use
   */
  public static List<MusicDiscItem> init(int numberOfDiscs, int maxNumberOfDiscs) {
    int placeholderCount = 0;
    for (int i = numberOfDiscs + 1; i <= maxNumberOfDiscs; i++) {
      registerPlaceholderDisc(String.format("track_%d", i));
      placeholderCount++;
    }
    FoxNap.LOGGER.debug(String.format("Registered %d placeholder discs", placeholderCount));

    return init(numberOfDiscs);
  }
}
