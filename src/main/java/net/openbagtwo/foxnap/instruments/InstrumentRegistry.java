package net.openbagtwo.foxnap.instruments;

import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registry of all instruments created by this mod
 */
public class InstrumentRegistry {

  private static final Map<String, Integer> INSTRUMENTS = Map.of(
      "trumpet", 11
  );


  /**
   * Create a new instrument, and register the item and its sound event
   *
   * @param instrumentName the name of the instrument (which should also be the name of its sound
   *                       event)
   * @return the fully instantiated and registered instrument
   */
  public static GoatHornItem registerInstrument(String instrumentName) {
    return Registry.register(
        Registry.ITEM,
        new Identifier(FoxNap.MOD_ID, instrumentName),
        new LiterallyJustAGoatHorn(
            registerInstrumentSound(instrumentName),
            20 * INSTRUMENTS.get(instrumentName))
    );
  }

  private static SoundEvent registerInstrumentSound(String instrumentName) {
    Identifier playSoundId = new Identifier(FoxNap.MOD_ID, instrumentName);
    return Registry.register(Registry.SOUND_EVENT, playSoundId, new SoundEvent(playSoundId));
  }

  /**
   * Create and register all instruments defined by this mod
   *
   * @return A list of fully instantiated and registered instruments
   */
  public static List<Item> init() {
    ArrayList<Item> instruments = new ArrayList<>();
    for (String instrument : INSTRUMENTS.keySet()) {
      instruments.add(registerInstrument(instrument));
      FoxNap.LOGGER.info("Registered " + instrument);
    }
    return instruments;


  }
}
