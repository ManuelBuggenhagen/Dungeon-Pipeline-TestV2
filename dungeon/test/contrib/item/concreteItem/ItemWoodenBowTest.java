package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.InventoryComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link ItemWoodenBow}. */
public class ItemWoodenBowTest {

  /** WTF? . */
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

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  private Entity buildGlobalPlayer(boolean withBowSkill) {
    Entity player = new Entity();
    player.add(new PlayerComponent());
    if (withBowSkill) {
      player.add(new SkillComponent(new BowSkill(SkillTools::cursorPositionAsPoint)));
    }
    Game.add(player);
    return player;
  }

  // --- Constructor #224 ---

  /** WTF? . */
  @Test
  public void constructor_setsNameDescriptionAndTexture() {
    ItemWoodenBow bow = new ItemWoodenBow();
    assertEquals("Wooden Bow", bow.displayName());
    assertEquals("It needs arrows as ammunition", bow.description());
    assertEquals("items/weapon/wooden_bow.png", ItemWoodenBow.DEFAULT_TEXTURE.pathString());
  }

  // --- collect(itemEntity, collector) #226 ---

  /** WTF? . */
  @Test
  public void collect_withInventorySpaceAndSkillComponent_addsBowSkillAndReturnsTrue() {
    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> dropped = bow.drop(new Point(3, 3));
    assertTrue(dropped.isPresent());
    Entity itemEntity = dropped.get();
    assertEquals(1, Game.levelEntities().count());

    Entity collector = new Entity();
    collector.add(new InventoryComponent(3));
    collector.add(new SkillComponent());

    assertTrue(bow.collect(itemEntity, collector));

    assertTrue(
        collector.fetch(InventoryComponent.class).map(inv -> inv.hasItem(bow)).orElse(false));
    assertTrue(
        collector
            .fetch(SkillComponent.class)
            .flatMap(sc -> sc.getSkill(BowSkill.class))
            .isPresent());
    assertEquals(0, Game.levelEntities().count());
  }

  /** WTF? . */
  @Test
  public void collect_withInventorySpaceNoSkillComponent_returnsTrueNoSkill() {
    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> dropped = bow.drop(new Point(3, 3));
    assertTrue(dropped.isPresent());
    Entity itemEntity = dropped.get();

    Entity collector = new Entity();
    collector.add(new InventoryComponent(3));

    assertTrue(bow.collect(itemEntity, collector));

    assertTrue(
        collector.fetch(InventoryComponent.class).map(inv -> inv.hasItem(bow)).orElse(false));
    assertEquals(0, Game.levelEntities().count());
    assertFalse(collector.isPresent(SkillComponent.class));
  }

  /** WTF? . */
  @Test
  public void collect_withFullInventory_returnsFalse() {
    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> dropped = bow.drop(new Point(3, 3));
    assertTrue(dropped.isPresent());
    Entity itemEntity = dropped.get();

    Entity collector = new Entity();
    collector.add(new InventoryComponent(0));

    assertFalse(bow.collect(itemEntity, collector));

    assertFalse(
        collector.fetch(InventoryComponent.class).map(inv -> inv.hasItem(bow)).orElse(false));
    assertEquals(1, Game.levelEntities().count());
  }

  /** WTF? . */
  @Test
  public void collect_withoutInventory_returnsFalse() {
    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> dropped = bow.drop(new Point(3, 3));
    assertTrue(dropped.isPresent());
    Entity itemEntity = dropped.get();

    Entity collector = new Entity();

    assertFalse(bow.collect(itemEntity, collector));
    assertEquals(1, Game.levelEntities().count());
  }

  /** WTF? . */
  @Test
  public void collect_nullCollector_throwsNPE() {
    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> dropped = bow.drop(new Point(3, 3));
    assertTrue(dropped.isPresent());
    Entity itemEntity = dropped.get();

    assertThrows(NullPointerException.class, () -> bow.collect(itemEntity, null));
  }

  // --- drop(position) #227 ---

  /** WTF? . */
  @Test
  public void drop_onFloorTileWithPlayerSkill_removesSkillAddsEntity() {
    Entity player = buildGlobalPlayer(true);
    assertTrue(Game.player().isPresent());
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> result = bow.drop(new Point(3, 3));

    assertTrue(result.isPresent());
    assertEquals(before + 1, Game.levelEntities().count());
    assertTrue(
        player
            .fetch(SkillComponent.class)
            .map(sc -> sc.getSkill(BowSkill.class).isEmpty())
            .orElse(false));
  }

  /** WTF? . */
  @Test
  public void drop_onFloorTileNoPlayerSkill_addsEntity() {
    buildGlobalPlayer(false);
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> result = bow.drop(new Point(3, 3));

    assertTrue(result.isPresent());
    assertEquals(before + 1, Game.levelEntities().count());
  }

  /** WTF? . */
  @Test
  public void drop_onNonFloorTile_skillRemovedButNoEntity() {
    Entity player = buildGlobalPlayer(true);
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Optional<Entity> result = bow.drop(new Point(-50, -50));

    assertTrue(result.isEmpty());
    assertEquals(before, Game.levelEntities().count());
    assertTrue(
        player
            .fetch(SkillComponent.class)
            .map(sc -> sc.getSkill(BowSkill.class).isEmpty())
            .orElse(false));
  }

  /** WTF? . */
  @Test
  public void drop_nullPosition_throwsNPE() {
    ItemWoodenBow bow = new ItemWoodenBow();
    assertThrows(NullPointerException.class, () -> bow.drop(null));
  }

  // --- use(user) #228 ---

  /** WTF? . */
  @Test
  public void use_withPositionAndInventory_dropsAndRemovesFromInventory() {
    Entity player = buildGlobalPlayer(true);
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Entity user = new Entity();
    user.add(new PositionComponent(new Point(3, 3)));
    InventoryComponent inventoryComponent = new InventoryComponent(2);
    user.add(inventoryComponent);
    inventoryComponent.add(bow);

    bow.use(user);

    assertFalse(Arrays.asList(inventoryComponent.items()).contains(bow));
    assertEquals(before + 1, Game.levelEntities().count());
    assertTrue(
        player
            .fetch(SkillComponent.class)
            .map(sc -> sc.getSkill(BowSkill.class).isEmpty())
            .orElse(false));
  }

  /** WTF? . */
  @Test
  public void use_withPositionNoInventory_dropsNoException() {
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Entity user = new Entity();
    user.add(new PositionComponent(new Point(3, 3)));

    bow.use(user);

    assertEquals(before + 1, Game.levelEntities().count());
  }

  /** WTF? . */
  @Test
  public void use_withoutPosition_doesNothing() {
    Entity player = buildGlobalPlayer(true);
    long before = Game.levelEntities().count();

    ItemWoodenBow bow = new ItemWoodenBow();
    Entity user = new Entity();
    InventoryComponent inventoryComponent = new InventoryComponent(2);
    user.add(inventoryComponent);
    inventoryComponent.add(bow);

    bow.use(user);

    assertEquals(before, Game.levelEntities().count());
    assertTrue(Arrays.asList(inventoryComponent.items()).contains(bow));
    assertTrue(
        player.fetch(SkillComponent.class).flatMap(sc -> sc.getSkill(BowSkill.class)).isPresent());
  }

  /** WTF? . */
  @Test
  public void use_nullUser_throwsNPE() {
    ItemWoodenBow bow = new ItemWoodenBow();
    assertThrows(NullPointerException.class, () -> bow.use(null));
  }
}
