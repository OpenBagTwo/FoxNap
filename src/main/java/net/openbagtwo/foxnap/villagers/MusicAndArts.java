package net.openbagtwo.foxnap.villagers;

import static java.lang.Math.max;
import static net.openbagtwo.foxnap.FoxNap.LOGGER;

import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradeOffers.BuyForOneEmeraldFactory;
import net.minecraft.village.TradeOffers.SellItemFactory;
import net.openbagtwo.foxnap.instruments.SecretlyJustAGoatHorn;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for generating a trade from a pool of items (so that a Conductor doesn't just have all
 * instrument sells)
 */
public class MusicAndArts implements TradeOffers.Factory {

  private final boolean isBuy;
  private final int price;  // items / emerald or emeralds / item
  private final int maxUses;
  private final int xp;  // this is evidently the XP the *villager* earns?!
  private final List<? extends Item> itemPool;

  private MusicAndArts(List<? extends Item> itemPool, int price, int maxUses, int xp) {
    this.itemPool = itemPool;
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
  public TradeOffer create(Entity entity, Random random) {
    if (this.price == 0 || this.itemPool.size() == 0) {
      return null;
    }
    Item selectedItem = this.itemPool.get(random.nextInt(this.itemPool.size()));
    if (isBuy) {
      return new BuyForOneEmeraldFactory(selectedItem, this.price, this.maxUses, this.xp).create(
          entity, random);
    } else {
      return new SellItemFactory(selectedItem, this.price, 1, this.maxUses, this.xp).create(entity,
          random);
    }
  }

  /**
   * A trade factory for selling one of a pool of related items to a villager
   */
  public static class BuyItemFromPoolForOneEmeraldFactory extends MusicAndArts {

    /**
     * @param itemPool The items to choose from
     * @param price    The base number of items the player will need to offer to receive one
     *                 emerald
     * @param maxUses  The maximum number of this trade a villager can make before restocking
     * @param xp       The XP the *villager* will earn through this trade
     */
    public BuyItemFromPoolForOneEmeraldFactory(
        List<? extends Item> itemPool,
        int price,
        int maxUses,
        int xp
    ) {
      super(itemPool, max(price, 0), maxUses, xp);
    }
  }

  /**
   * A trade factory for buying one of a pool of related items to a villager
   */
  public static class SellOneItemFromPoolFactory extends MusicAndArts {

    /**
     * @param itemPool The items to choose from
     * @param price    The base number of emeralds the player will need to offer to receive one of
     *                 this item
     * @param maxUses  The maximum number of this trade a villager can make before restocking
     * @param xp       The XP the *villager* will earn through this trade
     */
    public SellOneItemFromPoolFactory(
        List<? extends Item> itemPool,
        int price,
        int maxUses,
        int xp
    ) {
      super(itemPool, -max(price, 0), maxUses, xp);
    }
  }

  /**
   * Tonewoods, as the name implplies, are woods that are highly sought after for use in making
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
  public static final TradeOffers.Factory BUY_TONEWOOD = new BuyItemFromPoolForOneEmeraldFactory(
      TONEWOODS, 4, 16, 8);

  /**
   * Factory to enable a villager to buy noteblocks at a base rate of 2 blocks / 1 emerald
   */
  public static final TradeOffers.Factory BUY_NOTEBLOCK = new TradeOffers.BuyForOneEmeraldFactory(
      Items.NOTE_BLOCK,
      2,
      12,
      15);
  /**
   * Factory to enable a villager to buy a goat horn (like, a real goat horn, not an instrument from
   * this mod) at a rate of 1 horn / 1 emerald
   */
  public static final TradeOffers.Factory BUY_SHOFAR = new BuyForOneEmeraldFactory(
      Items.GOAT_HORN,
      1,
      8,
      20
  );

  /**
   * Create a trade factory for selling (preferably common) music discs to a villager.
   *
   * @param musicDiscs The discs to choose from
   * @return Trade factory that will enable the villager to buy one specific music disc for one
   * emerald
   */
  public static TradeOffers.Factory buyMusicDisc(List<Item> musicDiscs) {
    return new BuyItemFromPoolForOneEmeraldFactory(musicDiscs, 1, 8, 30);
  }

  private static final int[] xpMap = {2, 5, 10, 15, 20};

  /**
   * Create a trade factory for buying instruments from a villager (this is intended to be the only
   * way to obtain instruments outside of commands. Like with librarians and enchanted books, the
   * idea is that the villager will offer multiple trades of this type across multiple levels. This
   * is, after all, the main reason why this villager exists.
   *
   * @param instruments The instruments the villager might sell
   * @param level       The villager level this trade is being registered for (1-5 inclusive)
   * @return Trade factory that will enable the villager to sell an instrument at a base rate of 12
   * emeralds per instrument
   */
  public static TradeOffers.Factory sellInstrument(List<SecretlyJustAGoatHorn> instruments,
      int level) {
    return new SellOneItemFromPoolFactory(instruments, 12, 12, xpMap[level - 1]);
  }

  /**
   * Create a trade factory for buying mod-custom music discs from the villager, which is intended
   * as the only way for these discs to be obtainable outside of commands and the creative
   * inventory. The intention is for these to be exclusively max-level trades.
   *
   * @param discs The discs a villager might sell
   * @return Trade factory that will enable the villager to sell an instrument at a base rate of 32
   * emeralds per instrument
   */
  public static TradeOffers.Factory sellMusicDisc(List<MusicDiscItem> discs) {
    return new SellOneItemFromPoolFactory(discs, 32, 3, 30);
  }

  public static TradeOffers.Factory sellMusicDisc(MusicDiscItem disc) {
    return new SellItemFactory(disc, 32, 1, 3, 30);
  }

}
