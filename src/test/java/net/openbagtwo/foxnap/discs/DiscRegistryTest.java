package net.openbagtwo.foxnap.discs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.minecraft.item.MusicDiscItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DiscRegistryTest {

  static List<MusicDiscItem> discs;

  @BeforeAll
  static void setup() {
    discs = DiscRegistry.init();
  }

  @Test
  void testRegistryIsNotEmpty() {
    assertThat(discs).isNotEmpty();
  }

  @Test
  void testAllDiscsGiveComparatorOutput() {
    assertThat(discs).allSatisfy((disc) -> assertThat(disc.getComparatorOutput()).isGreaterThan(0));
  }
}