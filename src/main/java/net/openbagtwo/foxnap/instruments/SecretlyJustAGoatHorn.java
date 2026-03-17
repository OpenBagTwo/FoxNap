package net.openbagtwo.foxnap.instruments;

import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class SecretlyJustAGoatHorn extends InstrumentItem {

  private final SoundEvent soundEvent;
  private final int cooldown;

  public SecretlyJustAGoatHorn(Properties settings, SoundEvent soundEvent, int cooldown) {
    super(
        settings.useItemDescriptionPrefix().rarity(Rarity.UNCOMMON).stacksTo(1)
    );
    this.soundEvent = soundEvent;
    this.cooldown = cooldown;
  }

  @Override
  public InteractionResult use(Level world, Player user, InteractionHand hand) {
        /*
       TODO:
             - implement handed instruments (can only be held in right or left hand)
             - implement two-handed instruments (require other hand to be empty)
             - implement two-item instruments (e.g. violin + bow)
         */
    ItemStack itemStack = user.getItemInHand(hand);

    user.startUsingItem(hand);

    playSound(world, user, this.soundEvent);
    user.getCooldowns().addCooldown(itemStack, Mth.floor(this.cooldown));
    user.awardStat(Stats.ITEM_USED.get(this));
    return InteractionResult.CONSUME;
  }

  private static void playSound(Level world, Player player, SoundEvent soundEvent) {
    world.playSound(
        player,
        player,
        soundEvent,
        SoundSource.RECORDS,
        4.0F,
        1.0F
    );
    world.gameEvent(
        GameEvent.INSTRUMENT_PLAY,
        player.position(),
        GameEvent.Context.of(player)
    );
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity user) {
    return this.cooldown;
  }
}
