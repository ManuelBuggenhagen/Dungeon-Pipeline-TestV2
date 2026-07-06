package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.item.HealthPotionType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ItemPotionHealthTest {

  @Test
  void type_ShouldReturnPotionType() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(HealthPotionType.WEAK, potion.type());
  }

  @Test
  void healAmount_ShouldReturnHealAmountOfPotionType() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(HealthPotionType.WEAK.getHealAmount(), potion.healAmount());
  }

  @Test
  void itemData_ShouldContainPotionTypeAndHealAmountWithCorrectKeys() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    Map<String, String> data = potion.itemData();

    assertEquals(2, data.size());
    assertEquals(
      HealthPotionType.WEAK.name(),
      data.get(TestableItemPotionHealth.potionTypeKey()));
    assertEquals(
      Integer.toString(HealthPotionType.WEAK.getHealAmount()),
      data.get(TestableItemPotionHealth.healAmountKey()));
  }

  @Test
  void equals_ShouldReturnTrueForSameObject() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertTrue(potion.equals(potion));
  }

  @Test
  void equals_ShouldReturnTrueForSameHealAmount() {
    ItemPotionHealth potion1 = new ItemPotionHealth(HealthPotionType.WEAK);
    ItemPotionHealth potion2 = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(potion1, potion2);
  }

  @Test
  void equals_ShouldReturnFalseForDifferentHealAmount() {
    ItemPotionHealth potion1 = new ItemPotionHealth(HealthPotionType.WEAK);
    ItemPotionHealth potion2 = new ItemPotionHealth(typeWithDifferentHealAmount());

    assertNotEquals(potion1, potion2);
  }

  @Test
  void equals_ShouldReturnFalseForDifferentClass() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertFalse(potion.equals("not a potion"));
  }

  @Test
  void equals_ShouldReturnFalseForNull() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertFalse(potion.equals(null));
  }

  @Test
  void hashCode_ShouldBeSameForSameHealAmount() {
    ItemPotionHealth potion1 = new ItemPotionHealth(HealthPotionType.WEAK);
    ItemPotionHealth potion2 = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(potion1.hashCode(), potion2.hashCode());
  }

  private static HealthPotionType typeWithDifferentHealAmount() {
    for (HealthPotionType type : HealthPotionType.values()) {
      if (type.getHealAmount() != HealthPotionType.WEAK.getHealAmount()) {
        return type;
      }
    }
    throw new AssertionError("No HealthPotionType with different heal amount found.");
  }

  private static class TestableItemPotionHealth extends ItemPotionHealth {

    private TestableItemPotionHealth(HealthPotionType type) {
      super(type);
    }

    private static String potionTypeKey() {
      return DATA_KEY_POTION_TYPE;
    }

    private static String healAmountKey() {
      return DATA_KEY_HEAL_AMOUNT;
    }
  }
}
