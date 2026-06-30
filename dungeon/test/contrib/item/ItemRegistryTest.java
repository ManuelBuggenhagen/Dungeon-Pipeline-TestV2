package contrib.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import core.utils.components.draw.animation.Animation;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test class */
public class ItemRegistryTest {

  @BeforeEach
  void setup() {
    ItemRegistry.clearMapsForTests();
  }

  @Test
  void lookup_withExistingId_shouldReturnClass() {
    ItemRegistry.register("TestID", Item.class);

    assertEquals(Item.class, ItemRegistry.lookup("TestID").orElseThrow());
  }

  @Test
  void lookup_withMissingId_shouldReturnEmpty() {
    assertTrue(ItemRegistry.lookup("missing").isEmpty());
  }

  @Test
  void lookup_withNullId_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> ItemRegistry.lookup(null));
  }

  @Test
  void lookup_withBlankId_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> ItemRegistry.lookup("   "));
  }

  @Test
  void register_withValidId_shouldRegisterClass() {

    ItemRegistry.register("TestID", Item.class);

    assertTrue(ItemRegistry.lookup("TestID").isPresent());
  }

  @Test
  void register_withNullClass_shouldThrowException() {

    assertThrows(NullPointerException.class, () -> ItemRegistry.register("TestID", null));
  }

  @Test
  void register_withNullId_shouldThrowException() {

    assertThrows(IllegalArgumentException.class, () -> ItemRegistry.register(null, Item.class));
  }

  @Test
  void register_withClassOnly_shouldUseSimpleName() {

    ItemRegistry.register(Item.class);

    assertTrue(ItemRegistry.lookup("Item").isPresent());
  }

  @Test
  void register_sameClassTwice_shouldNotThrow() {

    ItemRegistry.register("ID", Item.class);

    assertDoesNotThrow(() -> ItemRegistry.register("ID", Item.class));
  }

  @Test
  void register_withFactory_shouldCreateItem() {

    ItemRegistry.register(
        "ID", Item.class, data -> new Item("Test", "Description", (Animation) null));

    assertTrue(ItemRegistry.create("ID", Map.of()).isPresent());
  }

  @Test
  void registerFactory_shouldRegisterFactory() {

    ItemRegistry.register("ID", Item.class);

    ItemRegistry.registerFactory("ID", data -> new Item("Test", "Description", (Animation) null));

    assertTrue(ItemRegistry.create("ID", Map.of()).isPresent());
  }

  @Test
  void registerFactory_withNullFactory_shouldThrow() {

    assertThrows(NullPointerException.class, () -> ItemRegistry.registerFactory("ID", null));
  }

  @Test
  void create_withoutFactory_shouldReturnEmpty() {

    ItemRegistry.register("ID", Item.class);

    assertTrue(ItemRegistry.create("ID", Map.of()).isEmpty());
  }

  @Test
  void create_withNullData_shouldWork() {

    ItemRegistry.register(
        "ID", Item.class, data -> new Item("Test", "Description", (Animation) null));

    assertTrue(ItemRegistry.create("ID", null).isPresent());
  }

  @Test
  void create_factoryReturningNull_shouldThrow() {

    ItemRegistry.register("ID", Item.class, data -> null);

    assertThrows(IllegalStateException.class, () -> ItemRegistry.create("ID", Map.of()));
  }

  @Test
  void entries_shouldContainRegisteredItem() {

    ItemRegistry.register("ID", Item.class);

    assertEquals(Item.class, ItemRegistry.entries().get("ID"));
  }

  @Test
  void idForClass_shouldReturnId() {

    ItemRegistry.register("ID", Item.class);

    assertEquals("ID", ItemRegistry.idFor(Item.class).orElseThrow());
  }

  @Test
  void idForUnknownClass_shouldReturnEmpty() {

    assertTrue(ItemRegistry.idFor(Item.class).isEmpty());
  }

  @Test
  void idForItem_shouldReturnId() {

    ItemRegistry.register("ID", Item.class);

    Item item = new Item("Test", "Description", (Animation) null);

    assertEquals("ID", ItemRegistry.idFor(item));
  }

  @Test
  void idForUnregisteredItem_shouldThrow() {

    Item item = new Item("Test", "Description", (Animation) null);

    assertThrows(IllegalArgumentException.class, () -> ItemRegistry.idFor(item));
  }

  @Test
  void isRegistered_existingClass_shouldReturnTrue() {

    ItemRegistry.register("ID", Item.class);

    assertTrue(ItemRegistry.isRegistered(Item.class));
  }

  @Test
  void isRegistered_missingClass_shouldReturnFalse() {

    assertFalse(ItemRegistry.isRegistered(Item.class));
  }
}
