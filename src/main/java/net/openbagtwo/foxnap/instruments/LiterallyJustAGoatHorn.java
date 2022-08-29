package net.openbagtwo.foxnap.instruments;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instruments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LiterallyJustAGoatHorn extends GoatHornItem {

  private final SoundEvent soundEvent;
  private final int cooldown;

  public LiterallyJustAGoatHorn(SoundEvent soundEvent, int cooldown) {
    super(
        new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1).group(ItemGroup.TOOLS),
        TagKey.of(Registry.INSTRUMENT_KEY, Instruments.YEARN_GOAT_HORN.getValue())
    );
    this.soundEvent = soundEvent;
    this.cooldown = cooldown;
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        /*
       TODO:
             - implement handed instruments (can only be held in right or left hand)
             - implement two-handed instruments (require other hand to be empty)
             - implement two-item instruments (e.g. violin + bow)
         */
    ItemStack itemStack = user.getStackInHand(hand);
    user.setCurrentHand(hand);

    playSound(world, user, this.soundEvent);
    user.getItemCooldownManager().set(this, this.cooldown);
    return TypedActionResult.consume(itemStack);
  }

  private static void playSound(World world, PlayerEntity player, SoundEvent soundEvent) {
    world.playSoundFromEntity(
        player,
        player,
        soundEvent,
        SoundCategory.RECORDS,
        4.0F,
        1.0F
    );
    world.emitGameEvent(
        GameEvent.INSTRUMENT_PLAY,
        player.getPos(),
        GameEvent.Emitter.of(player)
    );
  }

}
