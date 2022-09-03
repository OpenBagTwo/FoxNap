package net.openbagtwo.foxnap.villagers;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.world.poi.PointOfInterestType;
import net.openbagtwo.foxnap.FoxNap;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;

/**
 * The villager who will sell you all these goodies
 */
public class Conductor {

  public static final SoundEvent CONDUCTOR_WORK_SOUND = InstrumentRegistry.registerInstrumentSound(
      "condutor_baton");
  public static final VillagerProfession CONDUCTOR = makeConductor();
  private static final int[] xpMap = {2, 5, 10, 15, 20};

  private static VillagerProfession makeConductor() {

    Identifier poi_id = new Identifier(MOD_ID, "conductor_poi");
    PointOfInterestType poi = PointOfInterestHelper.register(poi_id, 1, 1,
        // hoping this is in chunks?
        ImmutableSet.copyOf(Blocks.JUKEBOX.getStateManager().getStates()));

    LOGGER.info(RegistryKey.of(Registry.POINT_OF_INTEREST_TYPE_KEY, poi_id).toString());
    return VillagerProfessionBuilder.create().id(new Identifier(MOD_ID, "conductor"))
        .workstation(RegistryKey.of(Registry.POINT_OF_INTEREST_TYPE_KEY, poi_id))
        .workSound(CONDUCTOR_WORK_SOUND).build();
  }

  /**
   * Specify trades for "tonewoods"
   *
   * @param woodType A stripped wood item that could make a good tonewood
   * @return Trade factory for the villager buying four stripped woods for one emerald
   */
  public static TradeOffers.Factory buyTonewood(Item woodType) {
    return new TradeOffers.BuyForOneEmeraldFactory(woodType, 4, 16, 2);
  }

  /**
   * Specify goat horn trades. The villager should accept any goat horn (but, interestingly, not our
   * instruments which extend the GoatHornItem class!)
   *
   * @return Trade factory for the villager buying one goat horn for one emerald
   */
  public static TradeOffers.Factory buyShofar() {
    return new TradeOffers.BuyForOneEmeraldFactory(Items.GOAT_HORN, 1, 12, 20);
  }

  /**
   * Specify note block trades
   *
   * @return Trade factory for the villager buying three note blocks for one emerald
   */
  public static TradeOffers.Factory buyNoteblock() {
    return new TradeOffers.BuyForOneEmeraldFactory(Items.NOTE_BLOCK, 3, 8, 25);
  }

  /**
   * Specify trades for selling common music disks to the villager
   *
   * @param disc The disc
   * @return Trade factory for the villager buying one music disc for one emerald
   */
  public static TradeOffers.Factory buyMusicDisc(Item disc) {
    return new TradeOffers.BuyForOneEmeraldFactory(disc, 1, 8, 30);
  }

  /**
   * Specify trades for buying instruments from the villager. Like with librarians and enchanted
   * books, the idea is that the villager will offer multiple trades of this type across multiple
   * levels. This is, after all, the main reason why this villager exists.
   *
   * @param instrument The instrument the villager will sell
   * @param level      The villager level this trade is being registered for (1-5 inclusive)
   * @return Trade factory where the villager will sell 1 instrument for 12 emeralds
   */
  public static TradeOffers.Factory sellInstrument(Item instrument, int level) {
    return new TradeOffers.SellItemFactory(instrument, 12, 1, xpMap[level - 1]);
  }

  /**
   * Specify trades for buying mod-custom music discs from the villager, which is intended as the
   * only way for these discs to be available outside of commands and the creative inventory.
   *
   * @param disc  The disc the villager will sell
   * @param level The villager level this trade is being registered for (1-5 inclusive)
   * @return Trade factory where the villager will sell 1 disc for 24 emeralds
   */
  public static TradeOffers.Factory sellMusicDisc(Item disc, int level) {
    return new TradeOffers.SellItemFactory(disc, 24, 1, xpMap[level - 1]);
  }

  /**
   * Register FoxNap's custom villager profession and all its trades.
   *
   * @param instruments The list of custom instruments created by this mod, as returned by
   *                    InstrumentRegistry.init()
   * @param records     The list of custom records created by this mod, as returned by
   *                    DiscRegistry.init()
   */
  public static void init(List<Item> instruments, List<MusicDiscItem> records) {

    ArrayList<TradeOffers.Factory> level1Trades = new ArrayList<>();
    level1Trades.addAll(
        Arrays.asList(
            buyTonewood(Items.STRIPPED_SPRUCE_WOOD),
            buyTonewood(Items.STRIPPED_ACACIA_WOOD),
            buyTonewood(Items.STRIPPED_DARK_OAK_WOOD),
            buyTonewood(Items.STRIPPED_MANGROVE_WOOD)
        )
    );
    for (Item instrument : instruments) {
      level1Trades.add(sellInstrument(instrument, 1));
    }

    ArrayList<TradeOffers.Factory> level2Trades = new ArrayList<>();
    level2Trades.add(buyShofar());
    for (Item instrument : instruments) {
      level2Trades.add(sellInstrument(instrument, 2));
    }

    ArrayList<TradeOffers.Factory> level3Trades = new ArrayList<>();
    level3Trades.addAll(
        Arrays.asList(
            buyMusicDisc(Items.MUSIC_DISC_13),
            buyMusicDisc(Items.MUSIC_DISC_CAT),
            buyMusicDisc(Items.DISC_FRAGMENT_5)
        )
    );
    for (Item instrument : instruments) {
      level3Trades.add(sellInstrument(instrument, 3));
    }

    ArrayList<TradeOffers.Factory> level4Trades = new ArrayList<>();
    if (FabricLoader.getInstance().isModLoaded("betterend")) {
      for (String end_disc_name : Arrays.asList(
          "strange_and_alien",
          "grasping_at_stars",
          "endseeker",
          "eo_dracona"
      )) {
        Optional<Item> endDisc = Registry.ITEM.getOrEmpty(
            new Identifier("betterend", "music_disc_" + end_disc_name)
        );
        if (endDisc.isPresent()) {
          level4Trades.add(buyMusicDisc(endDisc.get()));
        }
      }
    }
    level4Trades.add(buyNoteblock());
    for (Item instrument : instruments) {
      level4Trades.add(sellInstrument(instrument, 4));
    }

    ArrayList<TradeOffers.Factory> level5Trades = new ArrayList<>();
    for (Item disc : records) {
      level5Trades.add(sellMusicDisc(disc, 5));
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
