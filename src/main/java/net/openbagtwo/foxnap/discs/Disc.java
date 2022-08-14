package net.openbagtwo.foxnap.discs;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.Rarity;


public class Disc extends MusicDiscItem {
    public Disc(int comparatorOutput, SoundEvent sound) {
        super(comparatorOutput, sound, generateSettings());
    }

    public static Item.Settings generateSettings(){
        return new Item.Settings().rarity(Rarity.RARE).maxCount(1).group(ItemGroup.MISC);
    }
}
