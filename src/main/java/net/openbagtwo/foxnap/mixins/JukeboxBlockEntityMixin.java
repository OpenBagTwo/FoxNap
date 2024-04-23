package net.openbagtwo.foxnap.mixins;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {

  @Shadow
  ItemStack recordStack;

  @Shadow
  abstract void updateState(@Nullable Entity entity, boolean hasRecord);

  @Unique
  private static boolean isMusicDisc(ItemStack maybeDisc) {
    return (maybeDisc.getItem() instanceof MusicDiscItem) || maybeDisc.isIn(ItemTags.MUSIC_DISCS);
  }


  /**
   * @author OpenBagTwo
   * @reason Force-allow all mod music discs to be valid for interacting with a jukebox
   */
  @Overwrite
  public boolean isValid(int slot, ItemStack stack) {
    return isMusicDisc(stack) && ((JukeboxBlockEntity) (Object) this).getStack(slot).isEmpty();
  }

  /**
   * @author OpenBagTwo
   * @reason Force all mod music discs to interact with a jukebox
   */
  @Overwrite
  public void setStack(ItemStack stack) {
    JukeboxBlockEntity this_jukebox = (JukeboxBlockEntity) (Object) this;

    if (isMusicDisc(stack) && this_jukebox.getWorld() != null) {
      this.recordStack = stack;
      this.updateState(null, true);
      this_jukebox.startPlaying();
    } else if (stack.isEmpty()) {
      this_jukebox.decreaseStack(1);
    }
  }
}