package net.openbagtwo.foxnap.discs;

import java.util.Optional;
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

  public Track(String trackName) {
    super(Identifier.of(FoxNap.MOD_ID, trackName), Optional.of(16.0f));
  }

  @Override
  @Environment(EnvType.CLIENT)
  public Identifier id() {
    if (isPlaceholder) {
      return getPlaceholderTrackId();
    }
    return super.id();
  }

  public static Identifier getPlaceholderTrackId() {
    return Identifier.of(FoxNap.MOD_ID, "placeholder");
  }

  public static SoundEvent getPlaceholderTrack() {
    return Registries.SOUND_EVENT.get(getPlaceholderTrackId());
  }
}
