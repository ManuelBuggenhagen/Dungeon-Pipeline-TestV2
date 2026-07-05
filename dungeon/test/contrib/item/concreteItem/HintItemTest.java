package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import contrib.hud.DialogUtils;
import contrib.item.ItemRegistry;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Unit tests for {@link HintItem}. */
public class HintItemTest {

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

  /** Issue #156 G1: full constructor with valid paths constructs and assigns values correctly. */
  @Test
  public void testFourArgConstructorValid() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    IPath worldSprite = new SimpleIPath("item/key/gold_key.png");
    HintItem item = new HintItem(imagePath, worldSprite, "Test name", "Test description");

    assertSame(imagePath, item.imagePath());
    assertEquals("Test name", item.displayName());
    assertEquals("Test description", item.description());
    assertEquals(
        Optional.of(worldSprite.pathString()),
        item.worldAnimation().sourcePath().map(IPath::pathString));
    assertEquals(
        Optional.of(worldSprite.pathString()),
        item.inventoryAnimation().sourcePath().map(IPath::pathString));
  }

  /** Issue #156 U1: null imagePath with valid worldSprite throws NullPointerException. */
  @Test
  public void testFourArgConstructorNullImagePath() {
    IPath worldSprite = new SimpleIPath("animation/missing_texture.png");
    assertThrows(
        NullPointerException.class,
        () -> new HintItem(null, worldSprite, "Test name", "Test description"));
  }

  /** Issue #156 U2: valid imagePath with null worldSprite throws NullPointerException. */
  @Test
  public void testFourArgConstructorNullWorldSprite() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    assertThrows(
        NullPointerException.class,
        () -> new HintItem(imagePath, null, "Test name", "Test description"));
  }

  /** Issue #157 G1: three-arg constructor reuses imagePath as worldSprite. */
  @Test
  public void testThreeArgConstructorValid() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    HintItem item = new HintItem(imagePath, "Test name", "Test description");

    assertSame(imagePath, item.imagePath());
    assertEquals(imagePath.pathString(), item.itemData().get(HintItem.DATA_KEY_WORLD_SPRITE));
  }

  /** Issue #157 U1: null imagePath throws NullPointerException. */
  @Test
  public void testThreeArgConstructorNullImagePath() {
    assertThrows(
        NullPointerException.class, () -> new HintItem(null, "Test name", "Test description"));
  }

  /** Issue #158 G1: one-arg constructor applies default name and description. */
  @Test
  public void testOneArgConstructorValid() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    HintItem item = new HintItem(imagePath);

    assertEquals("Hint", item.displayName());
    assertEquals("A note with an image. [Use] to view the image.", item.description());
  }

  /** Issue #158 U1: null imagePath throws NullPointerException. */
  @Test
  public void testOneArgConstructorNullImagePath() {
    assertThrows(NullPointerException.class, () -> new HintItem(null));
  }

  /** Issue #159 G1: ensureRegistration() runs without error and registers the item. */
  @Test
  public void testEnsureRegistration() {
    assertDoesNotThrow(HintItem::ensureRegistration);
    assertTrue(ItemRegistry.idFor(HintItem.class).isPresent());
  }

  /** Issue #160 G1: imagePath() returns exactly the reference passed at construction. */
  @Test
  public void testImagePathReturnsSameReference() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    HintItem item = new HintItem(imagePath);

    assertSame(imagePath, item.imagePath());
  }

  /** Issue #161 G1: itemData() returns a LinkedHashMap with the 4 expected entries. */
  @Test
  public void testItemData() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    IPath worldSprite = new SimpleIPath("item/key/gold_key.png");
    HintItem item = new HintItem(imagePath, worldSprite, "Test name", "Test description");

    Map<String, String> data = item.itemData();

    assertInstanceOf(LinkedHashMap.class, data);
    assertEquals(4, data.size());
    assertEquals(imagePath.pathString(), data.get(HintItem.DATA_KEY_IMAGE_PATH));
    assertEquals(worldSprite.pathString(), data.get(HintItem.DATA_KEY_WORLD_SPRITE));
    assertEquals(item.displayName(), data.get(HintItem.DATA_KEY_NAME));
    assertEquals(item.description(), data.get(HintItem.DATA_KEY_DESCRIPTION));
  }

  /** Issue #162 G1: use() with a valid entity calls showImagePopUp with the image path and id. */
  @Test
  public void testUseWithValidEntity() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    HintItem item = new HintItem(imagePath);
    Entity entity = new Entity();

    try (MockedStatic<DialogUtils> dialogUtils = mockStatic(DialogUtils.class)) {
      item.use(entity);
      dialogUtils.verify(
          () -> DialogUtils.showImagePopUp(imagePath.pathString(), entity.id()), times(1));
    }
  }

  /** Issue #162 G2: use() with a null entity calls showImagePopUp with only the image path. */
  @Test
  public void testUseWithNullEntity() {
    IPath imagePath = new SimpleIPath("animation/missing_texture.png");
    HintItem item = new HintItem(imagePath);

    try (MockedStatic<DialogUtils> dialogUtils = mockStatic(DialogUtils.class)) {
      item.use(null);
      dialogUtils.verify(() -> DialogUtils.showImagePopUp(imagePath.pathString()), times(1));
    }
  }
}
