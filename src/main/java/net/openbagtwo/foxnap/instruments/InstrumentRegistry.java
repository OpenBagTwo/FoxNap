package net.openbagtwo.foxnap.instruments;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.openbagtwo.foxnap.FoxNap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstrumentRegistry {

    private static final Map<String, Integer> INSTRUMENTS = Map.of(
            "trumpet", 11
    );


    public static OrchestraInstrument registerInstrument(String instrumentName) {
        return Registry.register(
                Registry.ITEM,
                new Identifier(FoxNap.MOD_ID, instrumentName),
                new OrchestraInstrument(
                        registerInstrumentSound(instrumentName),
                        20 * INSTRUMENTS.get(instrumentName))
        );
    }

    private static SoundEvent registerInstrumentSound(String instrumentName) {
        Identifier playSoundId = new Identifier(FoxNap.MOD_ID, instrumentName);
        return Registry.register(Registry.SOUND_EVENT, playSoundId, new SoundEvent(playSoundId));
    }

    public static List<OrchestraInstrument> init() {
        ArrayList<OrchestraInstrument> instruments = new ArrayList<>();
        for (String instrument : INSTRUMENTS.keySet()) {
            instruments.add(registerInstrument(instrument));
            FoxNap.LOGGER.info("Registered " + instrument);
        }
        return instruments;


    }
}
