package net.openbagtwo.foxnap.discs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.openbagtwo.foxnap.FoxNap;

/**
 * A sound event played by a custom music disc.
 */
public class Track extends SoundEvent {

  /**
   * Simple toggle to control client-side overrides
   */
  public boolean isPlaceholder = false;

  public Track(Identifier id) {
    super(id, 16.0f, false);
  }

  @Override
  @Environment(EnvType.CLIENT)
  public Identifier getId() {
    if (isPlaceholder) {
      return getPlaceholderTrackId();
    }
    return super.getId();
  }

  public static Identifier getPlaceholderTrackId() {
    return Identifier.of(FoxNap.MOD_ID, "placeholder");
  }

  public static SoundEvent getPlaceholderTrack() {
    return Registries.SOUND_EVENT.get(getPlaceholderTrackId());
  }
}
