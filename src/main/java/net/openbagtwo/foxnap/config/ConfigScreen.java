package net.openbagtwo.foxnap.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.openbagtwo.foxnap.FoxNap;

public class ConfigScreen extends OptionsSubScreen {

  public Config config;

  public ConfigScreen(Screen previous) {
    super(previous, Minecraft.getInstance().options, Component.nullToEmpty(FoxNap.MOD_NAME));
    this.config = Config.loadConfiguration();
  }

  @Override
  protected void addOptions() {
    if (this.list == null) {
      return;
    }
    this.list.addBig(
        OptionInstance.createBoolean("Enable Maestro",
            this.config.getMaestroEnabled(), (value) -> {
              this.config.setMaestroEnabled(value);
            }));
    this.list.addBig(
        new OptionInstance<>(
            "Number of Discs",
            OptionInstance.cachedConstantTooltip(
                Component.nullToEmpty("The size of the Maestro's music disc trade pool")),
            (optionText, value) -> Options.genericValueLabel(optionText,
                Component.nullToEmpty(String.valueOf(value))),
            new OptionInstance.IntRange(0, 64, false),
            this.config.getNumDiscs(),
            value -> {
              this.config.setNumDiscs(value);
            }));
  }

  @Override
  protected void addFooter() {
    this.layout.addToFooter(
        Button.builder(Component.nullToEmpty("Changes will apply after restart"),
            (button) -> this.onClose()).width(200).build());
  }

  @Override
  public void removed() {
    try {
      this.config.writeConfigToFile();
    } catch (Config.ConfigException e) {
      FoxNap.LOGGER.error(String.valueOf(e));
    }
  }


}
