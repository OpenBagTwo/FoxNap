package net.openbagtwo.foxnap.discs;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
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
  public static Item registerDisc(String trackName, int comparatorOutput, int trackLength) {

    JukeboxSong jukeboxSong = new JukeboxSong(
        registerTrack(trackName),
        Text.translatable(
            Util.createTranslationKey(
                "jukebox_song",
                RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(FoxNap.MOD_ID, trackName))
                    .getValue()
            )
        ),
        (float) trackLength,
        comparatorOutput
    );
    Item disc = new Item((new Item.Settings()).maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(
        RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(FoxNap.MOD_ID, trackName)))
    );

    FoxNap.LOGGER.debug(
        "Registered " + trackName + " with comparator signal " + jukeboxSong.comparatorOutput());
    return disc;
  }

  private static RegistryEntry.Reference<SoundEvent> registerTrack(String trackName) {
    Identifier track_id = Identifier.of(FoxNap.MOD_ID, trackName);
    return Registry.registerReference(Registries.SOUND_EVENT, track_id, new Track(track_id));
  }

  /**
   * Create and register a procedurally-generated set of music discs named track_i (each registered
   * with a sound event name track_i and emitting a comparator signal of strength i), starting at i
   * = 1
   *
   * @param trackLengths The length (in seconds) of the tracks that will be made available on the
   *                     use on the server (read: for the Maestro).
   * @return A list of fully instantiated and registered music discs
   */
  public static List<Item> init(List<Integer> trackLengths) {
    ArrayList<Item> discs = new ArrayList<>();
    for (int i = 1; i <= trackLengths.size(); i++) {
      Item disc = registerDisc(
          String.format("track_%d", i),
          (i - 1) % 15 + 1,
          trackLengths.get(i - 1)
      );
      discs.add(disc);
      ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(disc));
    }
    return discs;
  }

  /**
   * In addition to the procedurally-generated set of usable music discs, also generate extra
   * dummy/placeholder discs to prevent client/server conflicts
   *
   * @param numberOfDiscs The number of discs that will actually be available for use
   * @param trackLengths  The length (in seconds) of the tracks that need to be registered
   *                      server-side (the size of this array de facto sets the maximum number of
   *                      discs)
   * @return The list of discs that should actually be available for use
   */
  public static List<Item> init(int numberOfDiscs, List<Integer> trackLengths) {
    registerTrack("placeholder");  // really tempting to make placeholder a placeholder
    int placeholderCount = 0;
    for (int i = numberOfDiscs + 1; i <= trackLengths.size(); i++) {
      Item disc = registerDisc(
          String.format("track_%d", i),
          0,
          trackLengths.get(i - 1)
      );
      disc.isPlaceholder = true;
      ((Track) disc.getSound()).isPlaceholder = true;
      placeholderCount++;
    }
    FoxNap.LOGGER.debug(String.format("Registered %d placeholder discs", placeholderCount));

    return init(trackLengths.subList(0, Math.min(numberOfDiscs, trackLengths.size())));
  }
}
