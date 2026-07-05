package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import java.lang.reflect.Field;
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
      data.get(itemDataKey("DATA_KEY_POTION_TYPE")));
    assertEquals(
      Integer.toString(HealthPotionType.WEAK.getHealAmount()),
      data.get(itemDataKey("DATA_KEY_HEAL_AMOUNT")));
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

  private static String itemDataKey(String fieldName) {
    try {
      Field field = Item.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (String) field.get(null);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Could not read item data key: " + fieldName, e);
    }
  }

  private static HealthPotionType typeWithDifferentHealAmount() {
    for (HealthPotionType type : HealthPotionType.values()) {
      if (type.getHealAmount() != HealthPotionType.WEAK.getHealAmount()) {
        return type;
      }
    }
    throw new AssertionError("No HealthPotionType with different heal amount found.");
  }
}
