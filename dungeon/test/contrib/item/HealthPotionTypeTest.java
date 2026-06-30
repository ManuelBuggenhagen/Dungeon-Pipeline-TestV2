package contrib.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Test class */
public class HealthPotionTypeTest {

  @Test
  void getHealAmount_shouldReturnCorrectValues() {
    assertEquals(7, HealthPotionType.WEAK.getHealAmount());
    assertEquals(15, HealthPotionType.NORMAL.getHealAmount());
    assertEquals(30, HealthPotionType.GREATER.getHealAmount());
  }

  @Test
  void fromHealAmount_withInput7_shouldReturnCorrectType() {
    assertEquals(Optional.of(HealthPotionType.WEAK), HealthPotionType.fromHealAmount(7));
  }

  @Test
  void fromHealAmount_withInput15_shouldReturnCorrectType() {
    assertEquals(Optional.of(HealthPotionType.NORMAL), HealthPotionType.fromHealAmount(15));
  }

  @Test
  void fromHealAmount_withInput30_shouldReturnCorrectType() {
    assertEquals(Optional.of(HealthPotionType.GREATER), HealthPotionType.fromHealAmount(30));
  }

  @Test
  void fromHealAmount_withNegativeInputValue_shouldReturnEmpty() {
    assertTrue(HealthPotionType.fromHealAmount(-1).isEmpty());
  }

  @Test
  void fromHealAmount_with0InputValue_shouldReturnEmpty() {
    assertTrue(HealthPotionType.fromHealAmount(0).isEmpty());
  }

  @Test
  void fromHealAmount_withHighInputValue_shouldReturnEmpty() {
    assertTrue(HealthPotionType.fromHealAmount(999).isEmpty());
  }
}
