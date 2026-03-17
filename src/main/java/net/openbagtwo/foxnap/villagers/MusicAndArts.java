package net.openbagtwo.foxnap.villagers;

import static java.lang.Math.max;
import static net.openbagtwo.foxnap.FoxNap.LOGGER;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for generating a trade from a pool of items (so that a Conductor doesn't just have all
 * instrument sells)
 */
public class MusicAndArts implements VillagerTrades.ItemListing {

  private final boolean isBuy;
  private final int price;  // items / emerald or emeralds / item
  private final int maxUses;
  private final int xp;  // this is evidently the XP the *villager* earns?!
  private final Supplier<List<? extends Item>> poolProvider;

  private MusicAndArts(Supplier<List<? extends Item>> poolProvider, int price, int maxUses,
      int xp) {
    this.poolProvider = poolProvider;
    if (price > 0) {
      this.price = price;
      this.isBuy = true;
    } else if (price < 0) {
      this.price = -price;
      this.isBuy = false;
    } else {
      // private constructor, so this should be unreachable. Still...
      LOGGER.error(
          "Sorry, PooledTradeFactories do not support acts of charity."
              + " This PooledTradeFactory will do nothing."
      );
      this.isBuy = false;
      this.price = 0;
    }
    this.maxUses = maxUses;
    this.xp = xp;
  }

  @Nullable
  @Override
  public MerchantOffer getOffer(ServerLevel world, Entity entity, RandomSource random) {
    List<? extends Item> itemPool = this.poolProvider.get();
    if (this.price == 0 || itemPool.isEmpty()) {
      return null;
    }
    Item selectedItem = itemPool.get(random.nextInt(itemPool.size()));
    if (isBuy) {
      return new MerchantOffer(new ItemCost(selectedItem, this.price), new ItemStack(Items.EMERALD),
          this.maxUses, this.xp, 0.05F);
    } else {
      return new MerchantOffer(new ItemCost(Items.EMERALD, this.price),
          new ItemStack(selectedItem.asItem()),
          this.maxUses, this.xp, 0.05F);
    }
  }

  /**
   * A trade factory for selling one of a pool of related items to a villager
   */
  public static class BuyItemFromPoolForOneEmeraldFactory extends MusicAndArts {

    /**
     * @param poolProvider A method that returns the items to choose from
     * @param price        The base number of items the player will need to offer to receive one
     *                     emerald
     * @param maxUses      The maximum number of this trade a villager can make before restocking
     * @param xp           The XP the *villager* will earn through this trade
     */
    public BuyItemFromPoolForOneEmeraldFactory(
        Supplier<List<? extends Item>> poolProvider,
        int price,
        int maxUses,
        int xp
    ) {
      super(poolProvider, max(price, 0), maxUses, xp);
    }
  }

  /**
   * A trade factory for buying one of a pool of related items to a villager
   */
  public static class SellOneItemFromPoolFactory extends MusicAndArts {

    /**
     * @param poolProvider A method that returns the items to choose from
     * @param price        The base number of emeralds the player will need to offer to receive one
     *                     of this item
     * @param maxUses      The maximum number of this trade a villager can make before restocking
     * @param xp           The XP the *villager* will earn through this trade
     */
    public SellOneItemFromPoolFactory(
        Supplier<List<? extends Item>> poolProvider,
        int price,
        int maxUses,
        int xp
    ) {
      super(poolProvider, -max(price, 0), maxUses, xp);
    }
  }

  /**
   * Tonewoods, as the name implies, are woods that are highly sought after for use in making
   * musical instruments, due to their specific acoustic properties.
   */
  public static final List<Item> TONEWOODS = Arrays.asList(
      Items.STRIPPED_SPRUCE_WOOD,
      Items.STRIPPED_ACACIA_WOOD,
      Items.STRIPPED_DARK_OAK_WOOD,
      Items.STRIPPED_MANGROVE_WOOD
  );

  /**
   * Factory to enable a villager to buy tonewood at a base rate of 4 blocks / 1 emerald
   */
  public static final VillagerTrades.ItemListing BUY_TONEWOOD = new BuyItemFromPoolForOneEmeraldFactory(
      () -> TONEWOODS, 4, 16, 3);

  /**
   * Factory to enable a villager to buy noteblocks at a base rate of 2 blocks / 1 emerald
   */
  public static final VillagerTrades.ItemListing BUY_NOTEBLOCK = new BuyItemFromPoolForOneEmeraldFactory(
      () -> Collections.singletonList(Items.NOTE_BLOCK),
      2,
      12,
      15);
  /**
   * Factory to enable a villager to buy a goat horn (like, a real goat horn, not an instrument from
   * this mod) at a rate of 1 horn / 1 emerald
   */
  public static final VillagerTrades.ItemListing BUY_SHOFAR = new BuyItemFromPoolForOneEmeraldFactory(
      () -> Collections.singletonList(Items.GOAT_HORN),
      1,
      8,
      20
  );

  /**
   * Create a trade factory for selling (preferably common) music discs to a villager.
   *
   * @param discProvider A method to fetch the list of discs to choose from
   * @return Trade factory that will enable the villager to buy one specific music disc for one
   * emerald
   */
  public static VillagerTrades.ItemListing buyMusicDisc(Supplier<List<? extends Item>> discProvider) {
    return new BuyItemFromPoolForOneEmeraldFactory(discProvider, 1, 8, 30);
  }

  /**
   * Create a trade factory for selling (preferably common) music discs to a villager.
   *
   * @param musicDiscs The discs to choose from
   * @return Trade factory that will enable the villager to buy one specific music disc for one
   * emerald
   */
  public static VillagerTrades.ItemListing buyMusicDisc(List<Item> musicDiscs) {
    return buyMusicDisc(() -> musicDiscs);
  }

  private static final int[] xpMap = {2, 5, 10, 15, 20};

  /**
   * Create a trade factory for buying instruments from a villager (this is intended to be the only
   * way to obtain instruments outside of commands). Like with librarians and enchanted books, the
   * idea is that the villager will offer multiple trades of this type across multiple levels. This
   * is, after all, the main reason why this villager exists.
   *
   * @param instruments The instruments the villager might sell
   * @param level       The villager level this trade is being registered for (1-5 inclusive)
   * @return Trade factory that will enable the villager to sell an instrument at a base rate of 12
   * emeralds per instrument
   */
  public static VillagerTrades.ItemListing sellInstrument(List<Item> instruments,
      int level) {
    return new SellOneItemFromPoolFactory(() -> instruments, 12, 12, xpMap[level - 1]);
  }

  /**
   * Create a trade factory for buying mod-custom music discs from the villager. These are intended
   * to be max-level trades exclusively. Use this method (supplying a list of discs) if you want the
   * villager's two max-level trade slots to pull from separate disc pools or if you want each
   * Conductor to only sell one disc (you sadist)
   *
   * @param discs The discs a villager might sell
   * @return Trade factory that will enable the villager to sell a music disc at a base rate of 32
   * emeralds per disc
   */
  public static VillagerTrades.ItemListing sellMusicDisc(List<Item> discs) {
    return new SellOneItemFromPoolFactory(() -> discs, 32, 3, 30);
  }

  /**
   * Create a trade factory for buying mod-custom music discs from the villager. These are intended
   * to be max-level trades exclusively. Creating a TradeOffers.Factory by using this method
   * (providing a single disc at a time) and looping over all available discs ensures that the
   * Conductor will offer two randomly selected records (without replacement).
   *
   * @param disc A disc a villager might sell
   * @return Trade factory that will enable the villager to sell a music disc at a base rate of 32
   * emeralds per disc
   */
  public static VillagerTrades.ItemListing sellMusicDisc(Item disc) {
    return new SellOneItemFromPoolFactory(() -> Collections.singletonList(disc), 32, 3, 30);
  }

}
