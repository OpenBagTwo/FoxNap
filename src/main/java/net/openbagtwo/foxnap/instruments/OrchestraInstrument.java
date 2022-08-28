package net.openbagtwo.foxnap.instruments;

import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;

import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class OrchestraInstrument extends Item {

    private final SoundEvent soundEvent;
    private final int cooldown;

    public OrchestraInstrument(SoundEvent soundEvent, int cooldown) {
        super(new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1).group(ItemGroup.TOOLS));
        this.soundEvent = soundEvent;
        this.cooldown = cooldown;
    }



    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        playSound(world, user, this.soundEvent);
        user.getItemCooldownManager().set(this, this.cooldown);
        return TypedActionResult.consume(itemStack);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN;
    }

    private static void playSound(World world, PlayerEntity player, SoundEvent soundEvent) {
        world.playSoundFromEntity(player, player, soundEvent, SoundCategory.RECORDS, 4.0F, 1.0F);
        world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }
}
