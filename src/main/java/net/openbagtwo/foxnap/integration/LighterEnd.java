package net.openbagtwo.foxnap.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Tonewood and music disc integration for the LighterEnd mod
 */
public class LighterEnd {

  public static final String MOD_ID = "lighterend";

  private static final List<String> tonewoods = Arrays.asList(
      "umbrella",
      "end_lotus",
      "mossy_glowshroom",
      "lacugrove",
      "dragon",
      "helix",
      "jellyshroom",
      "lucernia"
  );

  private static final List<String> tracks = Arrays.asList(
      "strange_and_alien",
      "grasping_at_stars",
      "endseeker",
      "eo_dracona"
  );

  /**
   * @return a list of stripped woods from the mod that seem like would make for good tonewood
   */
  public static List<Item> getTonewoods() {
    ArrayList<Item> tonewoodItemList = new ArrayList<>();
    for (String woodType : tonewoods) {

      if (BuiltInRegistries.ITEM.containsKey(
          Identifier.fromNamespaceAndPath(MOD_ID, woodType + "_stripped_wood")
      )) {
        tonewoodItemList.add(BuiltInRegistries.ITEM.getValue(
            Identifier.fromNamespaceAndPath(MOD_ID, woodType + "_stripped_wood")));
      }
    }
    return tonewoodItemList;
  }

  /**
   * @return the list of music discs that the mod makes available as end city loot
   */
  public static List<Item> getMusicDiscs() {
    ArrayList<Item> discList = new ArrayList<>();
    for (String end_disc_name : tracks) {
      if (BuiltInRegistries.ITEM.containsKey(
          Identifier.fromNamespaceAndPath(MOD_ID, "music_disc_" + end_disc_name)
      )) {
        discList.add(BuiltInRegistries.ITEM.getValue(
            Identifier.fromNamespaceAndPath(MOD_ID, "music_disc_" + end_disc_name)
        ));
      }
    }
    return discList;
  }
}
