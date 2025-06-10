package net.openbagtwo.foxnap.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.openbagtwo.foxnap.FoxNap;

public class ConfigScreen extends GameOptionsScreen {

  public Config config;

  public ConfigScreen(Screen previous) {
    super(previous, MinecraftClient.getInstance().options, Text.of(FoxNap.MOD_NAME));
    this.config = Config.loadConfiguration();
  }

  @Override
  protected void addOptions() {
    if (this.body == null) {
      return;
    }
    this.body.addSingleOptionEntry(
        SimpleOption.ofBoolean("Enable Maestro",
            this.config.getMaestroEnabled(), (value) -> {
              this.config.setMaestroEnabled(value);
            }));
    this.body.addSingleOptionEntry(
        new SimpleOption<>(
            "Number of Discs",
            SimpleOption.constantTooltip(
                Text.of("The size of the Maestro's music disc trade pool")),
            (optionText, value) -> GameOptions.getGenericValueText(optionText,
                Text.of(String.valueOf(value))),
            new SimpleOption.ValidatingIntSliderCallbacks(0, 64, false),
            this.config.getNumDiscs(),
            value -> {
              this.config.setNumDiscs(value);
            }));
  }

  @Override
  protected void initFooter() {
    this.layout.addFooter(
        ButtonWidget.builder(Text.of("Changes will apply after restart"),
            (button) -> this.close()).width(200).build());
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
