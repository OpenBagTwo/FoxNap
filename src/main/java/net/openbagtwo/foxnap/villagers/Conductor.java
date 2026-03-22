package net.openbagtwo.foxnap.villagers;

import static net.openbagtwo.foxnap.FoxNap.LOGGER;
import static net.openbagtwo.foxnap.FoxNap.MOD_ID;


import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Blocks;
import net.openbagtwo.foxnap.FoxNap;
import net.openbagtwo.foxnap.instruments.InstrumentRegistry;

/**
 * The villager who will sell you all these goodies
 */
public class Conductor {

  public static final SoundEvent CONDUCTOR_WORK_SOUND = InstrumentRegistry.registerInstrumentSound(
      "condutor_baton");

  private static VillagerProfession makeConductor() {

    Identifier poi_id = Identifier.fromNamespaceAndPath(MOD_ID, "conductor_poi");
    PoiHelper.register(poi_id, 1, 1,
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
            CONDUCTOR_WORK_SOUND,
            Int2ObjectMap.ofEntries(
                Int2ObjectMap.entry(1, createConductorTradeSet(1)),
                Int2ObjectMap.entry(2, createConductorTradeSet(2)),
                Int2ObjectMap.entry(3, createConductorTradeSet(3)),
                Int2ObjectMap.entry(4, createConductorTradeSet(4)),
                Int2ObjectMap.entry(5, createConductorTradeSet(5))
            )
        )
    );
  }


  /**
   * Register FoxNap's custom villager profession and all their trades.
   *
   */
  public static void init() {

    makeConductor();

    LOGGER.info(String.format("Registered %s's %s villager", FoxNap.MOD_NAME, "Conductor"));
  }

  private static ResourceKey<TradeSet> createConductorTradeSet(int level) {
    return ResourceKey.create(
        Registries.TRADE_SET,
        Identifier.fromNamespaceAndPath(MOD_ID, String.format("conductor/level_%d", level))
    );
  }
}
