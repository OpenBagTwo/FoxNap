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
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
   * @param track            The sound event (read: jukebox song) to tie to the disc
   * @param comparatorOutput The output signal a comparator should read from a jukebox with this
   *                         disc loaded
   * @param trackLength      The length of the track in seconds. This value is currently only used
   *                         by Allays (presumably to determine when to stop dancing and duping?).
   * @return the fully instantiated and registered music disc
   */
  public static Item registerDisc(Track track, int comparatorOutput, int trackLength
  ) {
    return registerDisc(track, comparatorOutput, trackLength, track.getId().getPath());
  }

  /**
   * Create a new music disc, and register the item and its sound event
   *
   * @param track            The sound event (read: jukebox song) to tie to the disc
   * @param comparatorOutput The output signal a comparator should read from a jukebox with this
   *                         disc loaded
   * @param trackLength      The length of the track in seconds. This value is currently only used
   *                         by Allays (presumably to determine when to stop dancing and duping?).
   * @param trackName        The identifier for the music disc (if distinct from the track ID)
   * @return the fully instantiated and registered music disc
   */
  public static Item registerDisc(
      Track track,
      int comparatorOutput,
      int trackLength,
      String trackName
  ) {
    JukeboxSong jukeboxSong = new JukeboxSong(
        registerTrack(track),
        Text.translatable(
            Util.createTranslationKey(
                "jukebox_song",
                RegistryKey.of(RegistryKeys.JUKEBOX_SONG, track.getId())
                    .getValue()
            )
        ),
        (float) trackLength,
        comparatorOutput
    );
    Item disc = Registry.register(
        Registries.ITEM,
        Identifier.of(FoxNap.MOD_ID, trackName),
        new Item(
            (new Item.Settings()).maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(
                RegistryKey.of(RegistryKeys.JUKEBOX_SONG, track.getId())
            )
        )
    );

    FoxNap.LOGGER.info(
        "Registered " + disc + " with comparator signal "
            + jukeboxSong.comparatorOutput());
    return disc;
  }

  private static RegistryEntry.Reference<SoundEvent> registerTrack(Track track) {
    return Registry.registerReference(Registries.SOUND_EVENT, track.getId(), track);
  }

  /**
   * Create and register a procedurally-generated set of music discs named track_i (each registered
   * with a sound event named track_i and emitting a comparator signal of strength i), starting at i
   * = 1
   *
   * @param trackLengths The length (in seconds) of the tracks that will be made available on the
   *                     use on the server (read: for the Maestro).
   * @return A list of fully instantiated and registered music discs
   */
  public static List<Item> init(List<Integer> trackLengths) {
    ArrayList<Item> discs = new ArrayList<>();
    for (int i = 1; i <= trackLengths.size(); i++) {
      Track track = new Track(String.format("track_%d", i));
      Item disc = registerDisc(
          track,
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
    Track placeholder = new Track("placeholder");
    placeholder.isPlaceholder = true;
    registerTrack(placeholder);

    int placeholderCount = 0;
    for (int i = numberOfDiscs + 1; i <= trackLengths.size(); i++) {
      registerDisc(
          placeholder,
          0,
          trackLengths.get(i - 1),
          String.format("track_%d", i)
      );
      placeholderCount++;
    }
    FoxNap.LOGGER.info(String.format("Registered %d placeholder discs", placeholderCount));

    return init(trackLengths.subList(0, Math.min(numberOfDiscs, trackLengths.size())));
  }
}
