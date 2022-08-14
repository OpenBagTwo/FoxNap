package net.openbagtwo.foxnap.discs;

import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

public class DiscRegistry {

    private int comparator_output = 0;

    public final MusicDiscItem TOBU_COLORS = this.registerDisc("tobu_colors");

    public MusicDiscItem registerDisc(String track_name) {
        MusicDiscItem disc = Registry.register(
                Registry.ITEM,
                new Identifier(FoxNap.MOD_ID, track_name),
                new Disc((++this.comparator_output) % 15 + 1, registerTrack(track_name)));
        FoxNap.LOGGER.info("Registered " + track_name + " with comparator signal " + disc.getComparatorOutput());
        return disc;
    }
    public static SoundEvent registerTrack(String track_name) {
        Identifier track_id = new Identifier(FoxNap.MOD_ID, track_name);
        return Registry.register(Registry.SOUND_EVENT, track_id, new SoundEvent(track_id));
    }

    public static void init() {}
}
