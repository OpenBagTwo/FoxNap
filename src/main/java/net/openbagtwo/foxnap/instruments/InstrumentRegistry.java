package net.openbagtwo.foxnap.instruments;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

public class InstrumentRegistry {

    public static void init() {

        Identifier soundId = new Identifier(FoxNap.MOD_ID, "trumpet");
        SoundEvent trumpetSound = Registry.register(Registry.SOUND_EVENT, soundId, new SoundEvent(soundId));
        Registry.register(Registry.ITEM, new Identifier(FoxNap.MOD_ID, "trumpet"), new OrchestraInstrument(trumpetSound, 20));
        FoxNap.LOGGER.info("Registered " + "trumpet");


    }
}
