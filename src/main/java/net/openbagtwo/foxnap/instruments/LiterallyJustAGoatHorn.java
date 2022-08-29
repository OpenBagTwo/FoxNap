package net.openbagtwo.foxnap.instruments;

import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instruments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class LiterallyJustAGoatHorn extends GoatHornItem {

  public LiterallyJustAGoatHorn() {
    super(
        new Item.Settings().rarity(Rarity.UNCOMMON).maxCount(1).group(ItemGroup.TOOLS),
        TagKey.of(Registry.INSTRUMENT_KEY, Instruments.YEARN_GOAT_HORN.getValue())
    );
  }

}
