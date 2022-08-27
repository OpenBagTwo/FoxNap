package net.openbagtwo.foxnap.discs;

import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

public class DiscRegistry {

    public static final int NUMBER_OF_DISCS = 2;

    public static MusicDiscItem registerDisc(String track_name, int comparator_output) {
        MusicDiscItem disc = Registry.register(
                Registry.ITEM,
                new Identifier(FoxNap.MOD_ID, track_name),
                new Disc(comparator_output, registerTrack(track_name)));
        FoxNap.LOGGER.info("Registered " + track_name + " with comparator signal " + disc.getComparatorOutput());
        return disc;
    }
    public static SoundEvent registerTrack(String track_name) {
        Identifier track_id = new Identifier(FoxNap.MOD_ID, track_name);
        return Registry.register(Registry.SOUND_EVENT, track_id, new SoundEvent(track_id));
    }

    public static void init() {
        for (int i=1; i<= NUMBER_OF_DISCS; i++){
            registerDisc(String.format("track_%d", i), i % 15 + 1);
        }
    }
}
