package net.openbagtwo.foxnap.villagers;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.openbagtwo.foxnap.FoxNap;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;
import net.openbagtwo.foxnap.integration.LighterEnd;

/**
 * The villager who will sell you all these goodies
 */
public class Conductor {

  public static final SoundEvent CONDUCTOR_WORK_SOUND = InstrumentRegistry.registerInstrumentSound(
      "condutor_baton");

  private static VillagerProfession makeConductor() {

    Identifier poi_id = Identifier.fromNamespaceAndPath(MOD_ID, "conductor_poi");
    PointOfInterestHelper.register(poi_id, 1, 1,
        // hoping this is in chunks?
        ImmutableSet.copyOf(Blocks.JUKEBOX.getStateDefinition().getPossibleStates())
    );

    return Registry.register(
        BuiltInRegistries.VILLAGER_PROFESSION,
        Identifier.fromNamespaceAndPath(MOD_ID, "conductor"),
        new VillagerProfession(
            Component.literal("conductor"),
            entry -> entry.is(
                ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), poi_id)
            ),
            entry -> entry.is(
                ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), poi_id)
            ),
            ImmutableSet.of(),
            ImmutableSet.of(),
            CONDUCTOR_WORK_SOUND
        )
    );
  }


  /**
   * Register FoxNap's custom villager profession and all their trades.
   *
   * @param instruments The list of custom instruments created by this mod, as returned by
   *                    InstrumentRegistry.init()
   * @param records     The list of custom records created by this mod, as returned by
   *                    DiscRegistry.init()
   */
  public static void init(List<Item> instruments, List<Item> records) {

    List<VillagerTrades.ItemListing> level1Trades = Arrays.asList(
        MusicAndArts.BUY_TONEWOOD,
        MusicAndArts.sellInstrument(instruments, 1)
    );

    List<VillagerTrades.ItemListing> level2Trades = Arrays.asList(
        MusicAndArts.BUY_NOTEBLOCK,
        MusicAndArts.sellInstrument(instruments, 2)
    );

    List<VillagerTrades.ItemListing> level3Trades = Arrays.asList(
        MusicAndArts.BUY_SHOFAR,
        MusicAndArts.sellInstrument(instruments, 3)
    );

    List<VillagerTrades.ItemListing> level4Trades = Arrays.asList(
        MusicAndArts.buyMusicDisc(
            Arrays.asList(
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_CAT,
                Items.DISC_FRAGMENT_5
            )
        ),
        MusicAndArts.sellInstrument(instruments, 4)
    );

    List<VillagerTrades.ItemListing> level5Trades = new ArrayList<>();
    for (Item disc : records) {
      level5Trades.add(MusicAndArts.sellMusicDisc(disc));
    }

    if (FabricLoader.getInstance().isModLoaded(LighterEnd.MOD_ID)) {
      level3Trades = new ArrayList<>(level3Trades);
      level3Trades.add(
          new MusicAndArts.BuyItemFromPoolForOneEmeraldFactory(
              LighterEnd::getTonewoods,
              3,
              16,
              16
          )
      );
      LOGGER.info(
          String.format(
              "Integrating BetterEnd woods into %s's %s trades",
              FoxNap.MOD_NAME,
              "Conductor"
          )
      );

      level4Trades = new ArrayList<>(level4Trades);
      level4Trades.add(MusicAndArts.buyMusicDisc(LighterEnd::getMusicDiscs));
      LOGGER.info(
          String.format(
              "Integrating BetterEnd music discs into %s's %s trades",
              FoxNap.MOD_NAME,
              "Conductor"
          )
      );
    }

    makeConductor();

    VillagerTrades.TRADES.put(
        ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.fromNamespaceAndPath(MOD_ID, "conductor")),
        VillagerTrades.toIntMap(ImmutableMap.of(
                1, level1Trades.toArray(new VillagerTrades.ItemListing[1]),
                2, level2Trades.toArray(new VillagerTrades.ItemListing[1]),
                3, level3Trades.toArray(new VillagerTrades.ItemListing[1]),
                4, level4Trades.toArray(new VillagerTrades.ItemListing[1]),
                5, level5Trades.toArray(new VillagerTrades.ItemListing[1])
            )
        )
    );

    LOGGER.info(String.format("Registered %s's %s villager", FoxNap.MOD_NAME, "Conductor"));
  }
}
