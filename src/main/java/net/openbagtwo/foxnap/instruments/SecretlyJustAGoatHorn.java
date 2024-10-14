package net.openbagtwo.foxnap.instruments;

import java.util.List;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SecretlyJustAGoatHorn extends GoatHornItem {

  private final SoundEvent soundEvent;
  private final int cooldown;

  public SecretlyJustAGoatHorn(SoundEvent soundEvent, int cooldown) {
    super(
        null,
        new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, soundEvent.id()))
            .rarity(Rarity.UNCOMMON).maxCount(1)
    );
    this.soundEvent = soundEvent;
    this.cooldown = cooldown;
  }

  @Override
  public void appendTooltip(
      ItemStack stack,
      Item.TooltipContext context,
      List<Text> tooltip,
      TooltipType type
  ) {
  }

  @Override
  public ActionResult use(World world, PlayerEntity user, Hand hand) {
        /*
       TODO:
             - implement handed instruments (can only be held in right or left hand)
             - implement two-handed instruments (require other hand to be empty)
             - implement two-item instruments (e.g. violin + bow)
         */
    ItemStack itemStack = user.getStackInHand(hand);

    user.setCurrentHand(hand);

    playSound(world, user, this.soundEvent);
    user.getItemCooldownManager().set(itemStack, MathHelper.floor(this.cooldown));
    user.incrementStat(Stats.USED.getOrCreateStat(this));
    return ActionResult.CONSUME;
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
