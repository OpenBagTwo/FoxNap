package net.openbagtwo.foxnap.instruments;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.FoxNap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registry of all instruments created by this mod
 */
public class InstrumentRegistry {

  private static final Map<String, Integer> INSTRUMENTS = Map.ofEntries(
      Map.entry("bassoon", 8),
      Map.entry("cello", 10),
      Map.entry("clarinet", 5),
      Map.entry("flute", 6),
      Map.entry("oboe", 8),
      Map.entry("saxophone", 4),
      Map.entry("trombone", 3),
      Map.entry("trumpet", 10),
      Map.entry("tuba", 5),
      Map.entry("viola", 9),
      Map.entry("violin", 5)
  );


  /**
   * Create a new instrument, and register the item and its sound event
   *
   * @param instrumentName the name of the instrument (which should also be the name of its sound
   *                       event)
   * @return the fully instantiated and registered instrument
   */
  public static SecretlyJustAGoatHorn registerInstrument(String instrumentName) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(FoxNap.MOD_ID, instrumentName),
        new SecretlyJustAGoatHorn(
            registerInstrumentSound(instrumentName),
            20 * INSTRUMENTS.get(instrumentName))
    );
  }

  public static SoundEvent registerInstrumentSound(String instrumentName) {
    Identifier playSoundId = Identifier.of(FoxNap.MOD_ID, instrumentName);
    return Registry.register(Registries.SOUND_EVENT, playSoundId, SoundEvent.of(playSoundId));
  }

  /**
   * Create and register all instruments defined by this mod
   *
   * @return A list of fully instantiated and registered instruments
   */
  public static List<SecretlyJustAGoatHorn> init() {
    ArrayList<SecretlyJustAGoatHorn> instruments = new ArrayList<>();
    for (String instrument : INSTRUMENTS.keySet()) {
      SecretlyJustAGoatHorn tooter = registerInstrument(instrument);
      ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(tooter));
      instruments.add(tooter);
      FoxNap.LOGGER.debug("Registered " + instrument);
    }
    return instruments;


  }
}
