package net.openbagtwo.foxnap.instruments;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Items;
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
  public static Item registerInstrument(String instrumentName) {
    return Items.registerItem(
        ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FoxNap.MOD_ID, instrumentName)),
        settings -> new SecretlyJustAGoatHorn(
            settings,
            registerInstrumentSound(instrumentName),
            20 * INSTRUMENTS.get(instrumentName)
        ),
        new Properties()
    );
  }

  public static SoundEvent registerInstrumentSound(String instrumentName) {
    Identifier playSoundId = Identifier.fromNamespaceAndPath(FoxNap.MOD_ID, instrumentName);
    return Registry.register(BuiltInRegistries.SOUND_EVENT, playSoundId, SoundEvent.createVariableRangeEvent(playSoundId));
  }

  /**
   * Create and register all instruments defined by this mod
   *
   * @return A list of fully instantiated and registered instruments
   */
  public static List<Item> init() {
    ArrayList<Item> instruments = new ArrayList<>();
    for (String instrument : INSTRUMENTS.keySet()) {
      Item tooter = registerInstrument(instrument);
      ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(tooter));
      instruments.add(tooter);
      FoxNap.LOGGER.debug("Registered " + instrument);
    }
    return instruments;


  }
}
