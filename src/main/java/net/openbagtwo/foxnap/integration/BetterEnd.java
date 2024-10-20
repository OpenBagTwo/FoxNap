package net.openbagtwo.foxnap.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

/**
 * Tonewood and music disc integration for the BetterEnd mod
 */
public class BetterEnd {

  private static final String mod_id = "betterend";

  private static final List<String> tonewoods = Arrays.asList(
      "mossy_glowshroom",
      "lacugrove",
      "dragon_tree",
      "helix_tree",
      "umbrella_tree",
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
   * @return a list of stripped barks from the BetterEnd mod that seem like would make for good
   * tonewood
   */
  public static List<Item> getTonewoods() {
    ArrayList<Item> tonewoodItemList = new ArrayList<>();
    for (String woodType : tonewoods) {
      Item strippedWood = Registries.ITEM.get(
          Identifier.of(mod_id, woodType + "_stripped_bark")
      );
      if (strippedWood != null) {
        tonewoodItemList.add(strippedWood);
      }
    }
    return tonewoodItemList;
  }

  /**
   * @return the list of music discs that the BetterEnd mod makes available as end city loot
   */
  public static List<Item> getMusicDiscs() {
    ArrayList<Item> discList = new ArrayList<>();

    for (String end_disc_name : tracks) {
      Item endDisc = Registries.ITEM.get(
          Identifier.of("betterend", "music_disc_" + end_disc_name)
      );
      if (endDisc != null) {
        discList.add(endDisc);
      }
    }
    return discList;
  }
}
