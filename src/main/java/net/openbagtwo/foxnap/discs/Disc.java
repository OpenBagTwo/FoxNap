package net.openbagtwo.foxnap.discs;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.Rarity;

/**
 * A custom music disc created by this mod
 */
public class Disc extends MusicDiscItem {
    /**
     * Create a new music disc
     *
     * @param comparatorOutput the output signal a comparator should read from a jukebox with
     *                         this disc loaded
     * @param sound            the sound (track) a jukebox with this disc loaded should play
     */
    public Disc(int comparatorOutput, SoundEvent sound) {
        super(comparatorOutput, sound, generateSettings());
    }

    private static Item.Settings generateSettings() {
        return new Item.Settings().rarity(Rarity.RARE).maxCount(1).group(ItemGroup.MISC);
    }
}
