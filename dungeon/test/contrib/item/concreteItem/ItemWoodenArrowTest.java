package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.InventoryComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link ItemWoodenArrow}. */
public class ItemWoodenArrowTest {

  /** Sets up a level so items can be dropped into the game world. */
  @BeforeEach
  public void before() {
    Game.add(new LevelSystem());

    DungeonLevel level =
        new DungeonLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR
              },
              new LevelElement[] {
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR
              },
              new LevelElement[] {
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR
              },
              new LevelElement[] {
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR
              },
              new LevelElement[] {
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR
              }
            },
            DesignLabel.DEFAULT);

    for (Tile t : new ArrayList<>(level.exitTiles())) {
      level.changeTileElementType(t, LevelElement.FLOOR);
    }
    Game.currentLevel(level);
  }

  /** Cleans up game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  /** A valid amount sets the stack size and max stack size, and updates the display name. */
  @Test
  public void constructor_withValidAmount_setsStackSizeAndMax() {
    ItemWoodenArrow arrow = new ItemWoodenArrow(5);

    assertEquals(5, arrow.stackSize());
    assertEquals(16, arrow.maxStackSize());
    assertEquals("5 x Wooden Arrow", arrow.displayName());
  }

  /** An amount above the max stack size is stored as-is; the constructor performs no clamping. */
  @Test
  public void constructor_withAmountAboveMax_storesAmountNoValidation() {
    ItemWoodenArrow arrow = new ItemWoodenArrow(20);

    assertEquals(20, arrow.stackSize());
  }

  /** A negative amount is stored as-is; the constructor performs no validation. */
  @Test
  public void constructor_withNegativeAmount_storesNegative() {
    ItemWoodenArrow arrow = new ItemWoodenArrow(-1);

    assertEquals(-1, arrow.stackSize());
  }

  /** The parameterless constructor defaults to a stack size of one. */
  @Test
  public void defaultConstructor_setsStackSizeOneAndMaxSixteen() {
    ItemWoodenArrow arrow = new ItemWoodenArrow();

    assertEquals(1, arrow.stackSize());
    assertEquals(16, arrow.maxStackSize());
    assertEquals("Wooden Arrow", arrow.displayName());
  }

  /** Using the arrow with a position and inventory drops it and removes it from the inventory. */
  @Test
  public void use_withPositionAndInventory_dropsAndRemovesStack() {
    ItemWoodenArrow arrow = new ItemWoodenArrow();
    Entity user = new Entity();
    user.add(new PositionComponent(new Point(3, 3)));
    InventoryComponent inventoryComponent = new InventoryComponent(2);
    user.add(inventoryComponent);
    inventoryComponent.add(arrow);
    long countBefore = Game.levelEntities().count();

    arrow.use(user);

    assertFalse(Arrays.asList(inventoryComponent.items()).contains(arrow));
    assertEquals(countBefore + 1, Game.levelEntities().count());
  }

  /** Using the arrow with a position but no inventory still drops it without throwing. */
  @Test
  public void use_withPositionNoInventory_dropsNoException() {
    ItemWoodenArrow arrow = new ItemWoodenArrow();
    Entity user = new Entity();
    user.add(new PositionComponent(new Point(3, 3)));
    long countBefore = Game.levelEntities().count();

    arrow.use(user);

    assertEquals(countBefore + 1, Game.levelEntities().count());
  }

  /** Using the arrow without a position does nothing: no drop and no inventory removal. */
  @Test
  public void use_withoutPosition_doesNothing() {
    ItemWoodenArrow arrow = new ItemWoodenArrow();
    Entity user = new Entity();
    InventoryComponent inventoryComponent = new InventoryComponent(2);
    user.add(inventoryComponent);
    inventoryComponent.add(arrow);
    long countBefore = Game.levelEntities().count();

    arrow.use(user);

    assertEquals(countBefore, Game.levelEntities().count());
    assertTrue(Arrays.asList(inventoryComponent.items()).contains(arrow));
  }

  /** Using the arrow with a null user throws a NullPointerException. */
  @Test
  public void use_nullUser_throwsNPE() {
    ItemWoodenArrow arrow = new ItemWoodenArrow();

    assertThrows(NullPointerException.class, () -> arrow.use(null));
  }
}
