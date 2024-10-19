package net.openbagtwo.foxnap.discs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.openbagtwo.foxnap.FoxNap;

/**
 * Dynamic registry of music discs created by this mod
 */
public class DiscRegistry {


  /**
   * Create a new music disc, and register the item and its sound event
   *
   * @param track The sound event (read: jukebox song) to tie to the disc
   * @return the fully instantiated and registered music disc
   */
  public static Item registerDisc(SoundEvent track) {
    return registerDisc(track, track.id().getPath());
  }

  /**
   * Create a new music disc, and register the item and its sound event
   *
   * @param track     The sound event (read: jukebox song) to tie to the disc
   * @param trackName The identifier for the music disc (if distinct from the track ID)
   * @return the fully instantiated and registered music disc
   */
  public static Item registerDisc(SoundEvent track, String trackName) {
    Item disc = new Item(
        new Item.Settings()
            .registryKey(
                RegistryKey.of(RegistryKeys.ITEM, Identifier.of(FoxNap.MOD_ID, trackName))
            )
            .translationKey("item.minecraft.music_disc_cat")
            .maxCount(1)
            .rarity(Rarity.RARE)
            .jukeboxPlayable(
                RegistryKey.of(RegistryKeys.JUKEBOX_SONG, track.id())
            )
    );
    Registry.register(Registries.ITEM, Identifier.of(FoxNap.MOD_ID, trackName), disc);

    FoxNap.LOGGER.debug("Registered " + disc);
    return disc;
  }

  private static RegistryEntry.Reference<SoundEvent> registerTrack(SoundEvent track,
      Identifier trackId) {
    return Registry.registerReference(Registries.SOUND_EVENT, trackId, track);
  }

  /**
   * Create and register a procedurally-generated set of music discs named track_i (each registered
   * with a sound event named track_i and emitting a comparator signal of strength i), starting at i
   * = 1
   *
   * @param numberOfDiscs The length (in seconds) of the tracks that will be made available on the
   *                      use on the server (read: for the Maestro).
   * @return A list of fully instantiated and registered music discs
   */
  public static List<Item> init(int numberOfDiscs) {
    ArrayList<Item> discs = new ArrayList<>();
    for (int i = 1; i <= numberOfDiscs; i++) {
      Identifier trackId = Identifier.of(FoxNap.MOD_ID, String.format("track_%d", i));
      SoundEvent track = new SoundEvent(trackId, Optional.of(16.0f));
      registerTrack(track, trackId);
      Item disc = registerDisc(track);
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
   * @param maxNumDiscs   The maximum number of discs (the number of disc items that will be created
   *                      in the namespace)
   * @return The list of discs that should actually be available for use
   */
  public static List<Item> init(int numberOfDiscs, int maxNumDiscs) {
    Identifier placeholderId = Identifier.of(FoxNap.MOD_ID, "placeholder");
    SoundEvent placeholder = new SoundEvent(placeholderId, Optional.of(16.0f));

    registerTrack(placeholder, placeholderId);

    int placeholderCount = 0;
    for (int i = numberOfDiscs + 1; i <= maxNumDiscs; i++) {
      registerDisc(placeholder, String.format("track_%d", i));
      placeholderCount++;
    }
    FoxNap.LOGGER.debug(String.format("Registered %d placeholder discs", placeholderCount));

    return init(numberOfDiscs);
  }
}
