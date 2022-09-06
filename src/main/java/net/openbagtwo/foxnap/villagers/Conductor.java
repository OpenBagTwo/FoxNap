package net.openbagtwo.foxnap.villagers;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.openbagtwo.foxnap.FoxNap;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;
import net.openbagtwo.foxnap.instruments.SecretlyJustAGoatHorn;
import net.openbagtwo.foxnap.integration.BetterEnd;

/**
 * The villager who will sell you all these goodies
 */
public class Conductor {

  public static final SoundEvent CONDUCTOR_WORK_SOUND = InstrumentRegistry.registerInstrumentSound(
      "condutor_baton");
  public static final VillagerProfession CONDUCTOR = makeConductor();

  private static VillagerProfession makeConductor() {

    Identifier poi_id = new Identifier(MOD_ID, "conductor_poi");
    PointOfInterestHelper.register(poi_id, 1, 1,
        // hoping this is in chunks?
        ImmutableSet.copyOf(Blocks.JUKEBOX.getStateManager().getStates())
    );

    return VillagerProfessionBuilder.create().id(new Identifier(MOD_ID, "conductor"))
        .workstation(RegistryKey.of(Registry.POINT_OF_INTEREST_TYPE_KEY, poi_id))
        .workSound(CONDUCTOR_WORK_SOUND).build();
  }


  /**
   * Register FoxNap's custom villager profession and all their trades.
   *
   * @param instruments The list of custom instruments created by this mod, as returned by
   *                    InstrumentRegistry.init()
   * @param records     The list of custom records created by this mod, as returned by
   *                    DiscRegistry.init()
   */
  public static void init(List<SecretlyJustAGoatHorn> instruments, List<MusicDiscItem> records) {

    List<TradeOffers.Factory> level1Trades = Arrays.asList(
        MusicAndArts.BUY_TONEWOOD,
        MusicAndArts.sellInstrument(instruments, 1)
    );

    List<TradeOffers.Factory> level2Trades = Arrays.asList(
        MusicAndArts.BUY_NOTEBLOCK,
        MusicAndArts.sellInstrument(instruments, 2)
    );

    List<TradeOffers.Factory> level3Trades = Arrays.asList(
        MusicAndArts.BUY_SHOFAR,
        MusicAndArts.sellInstrument(instruments, 3)
    );

    List<TradeOffers.Factory> level4Trades = Arrays.asList(
        MusicAndArts.buyMusicDisc(
            Arrays.asList(
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_CAT,
                Items.DISC_FRAGMENT_5
            )
        ),
        MusicAndArts.sellInstrument(instruments, 4)
    );

    List<TradeOffers.Factory> level5Trades = new ArrayList<>();
    for (MusicDiscItem disc : records) {
      level5Trades.add(MusicAndArts.sellMusicDisc(disc));
    }

    if (FabricLoader.getInstance().isModLoaded("betterend")) {
      List<Item> endTonewoods = BetterEnd.getTonewoods();
      if (endTonewoods.size() > 0) {
        level3Trades = new ArrayList<>(level3Trades);
        level3Trades.add(
            new MusicAndArts.BuyItemFromPoolForOneEmeraldFactory(
                endTonewoods,
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
      }
      List<Item> endDiscs = BetterEnd.getMusicDiscs();
      if (endDiscs.size() > 0) {
        level4Trades = new ArrayList<>(level4Trades);
        level4Trades.add(
            MusicAndArts.buyMusicDisc(endDiscs)
        );
        LOGGER.info(
            String.format(
                "Integrating BetterEnd music discs into %s's %s trades",
                FoxNap.MOD_NAME,
                "Conductor"
            )
        );
      }
    }

    Registry.register(Registry.VILLAGER_PROFESSION, CONDUCTOR.id(), CONDUCTOR);
    TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(
        CONDUCTOR, TradeOffers.copyToFastUtilMap(ImmutableMap.of(
                1, level1Trades.toArray(new TradeOffers.Factory[1]),
                2, level2Trades.toArray(new TradeOffers.Factory[1]),
                3, level3Trades.toArray(new TradeOffers.Factory[1]),
                4, level4Trades.toArray(new TradeOffers.Factory[1]),
                5, level5Trades.toArray(new TradeOffers.Factory[1])
            )
        )
    );

    LOGGER.info(String.format("Registered %s's %s villager", FoxNap.MOD_NAME, "Conductor"));
  }
}
