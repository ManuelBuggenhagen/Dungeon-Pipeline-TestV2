package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FogSystemTest {

  FogSystem fs;

  @BeforeEach
  void setUp() {
    fs = new FogSystem();
  }

  @SuppressWarnings("unchecked")
  private Map<Tile, Integer> getDarkenedTiles(FogSystem fog)
      throws NoSuchFieldException, IllegalAccessException {
    Field f = FogSystem.class.getDeclaredField("darkenedTiles");
    f.setAccessible(true);
    return (Map<Tile, Integer>) f.get(fog);
  }

  private static class TestTile extends Tile {

    public TestTile(int x, int y, DesignLabel label) {
      super(null, new Coordinate(x, y), label);
    }
  }

  @Test
  void set_and_get_currentViewDistance_with_legal_argument() {
    FogSystem.currentViewDistance(7);
    assertEquals(FogSystem.currentViewDistance(), 7);
    FogSystem.currentViewDistance(10);
    assertEquals(FogSystem.currentViewDistance(), 10);
  }

  @Test
  void set_and_get_currentViewDistance_with_negative_value_throws_runtime_exception() {
    FogSystem.currentViewDistance(7);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FogSystem.currentViewDistance(-10);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FogSystem.currentViewDistance(-1);
        });
  }

  @Test
  void set_and_get_currentViewDistance_with_negative_value_throws_runtime_exception_check_Message()
      throws NoSuchFieldException, IllegalAccessException {
    Field field = FogSystem.class.getDeclaredField("MAX_VIEW_DISTANCE");
    field.setAccessible(true);
    int max_view_distance = (int) field.get(fs);
    String message = "View distance must be between 0 and " + max_view_distance;
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              FogSystem.currentViewDistance(-1);
              ;
            });
    assertEquals(message, ex.getMessage());
  }

  @Test
  void set_and_get_currentViewDistance_with_min_legal_value()
      throws IllegalAccessException, NoSuchFieldException {
    FogSystem.currentViewDistance(0);
    assertEquals(FogSystem.currentViewDistance(), 0);
  }

  @Test
  void set_and_get_currentViewDistance_with_max_legal_value()
      throws IllegalAccessException, NoSuchFieldException {
    Field field = FogSystem.class.getDeclaredField("MAX_VIEW_DISTANCE");
    field.setAccessible(true);
    int max_view_distance = (int) field.get(fs);
    FogSystem.currentViewDistance(max_view_distance);
    assertEquals(FogSystem.currentViewDistance(), max_view_distance);
  }

  @Test
  void set_and_get_currentViewDistance_with_too_high_value_throws_runtime_exception()
      throws IllegalAccessException, NoSuchFieldException {
    Field field = FogSystem.class.getDeclaredField("MAX_VIEW_DISTANCE");
    field.setAccessible(true);
    int max_view_distance = (int) field.get(fs);
    int over_max_view_distance = max_view_distance + 1;
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FogSystem.currentViewDistance(over_max_view_distance);
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FogSystem.currentViewDistance(over_max_view_distance + 20);
        });
  }

  @Test
  void set_and_get_currentViewDistance_with_too_high_value_throws_runtime_exception_check_Message()
      throws NoSuchFieldException, IllegalAccessException {
    Field field = FogSystem.class.getDeclaredField("MAX_VIEW_DISTANCE");
    field.setAccessible(true);
    int max_view_distance = (int) field.get(fs);
    String message = "View distance must be between 0 and " + max_view_distance;
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              FogSystem.currentViewDistance(max_view_distance + 1);
            });
    assertEquals(message, ex.getMessage());
  }

  @Test
  void set_and_get_active_true() {
    fs.active(true);
    assertTrue(fs.active());
  }

  @Test
  void set_and_get_active_false() {
    fs.active(false);
    assertFalse(fs.active());
  }

  @Test
  void set_and_get_active_toggle_to_true() {
    fs.active(false);
    fs.active(true);
    assertTrue(fs.active());
  }

  @Test
  void set_and_get_active_toggle_to_false() {
    fs.active(true);
    fs.active(false);
    assertFalse(fs.active());
  }

  @Test
  void updateTile_both_tiles_not_in_darkenedTiles()
      throws NoSuchFieldException, IllegalAccessException {
    Map<Tile, Integer> map = getDarkenedTiles(fs);
    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);

    fs.updateTile(oldTile, newTile);
    assertTrue(map.isEmpty());
  }

  @Test
  void updateTile_oldtile_is_inside_darkenedTiles() throws Exception {
    Map<Tile, Integer> map = getDarkenedTiles(fs);

    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);

    map.put(oldTile, 100);

    fs.updateTile(oldTile, newTile);

    assertFalse(map.containsKey(oldTile));
    assertTrue(map.containsKey(newTile));
    assertEquals(100, map.get(newTile));
  }

  @Test
  void updateTile_newtile_is_inside_darkenedTiles() throws Exception {
    Map<Tile, Integer> map = getDarkenedTiles(fs);

    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);

    map.put(newTile, 200);

    fs.updateTile(oldTile, newTile);

    assertTrue(map.containsKey(newTile));
    assertEquals(200, map.get(newTile));
    assertFalse(map.containsKey(oldTile));
  }

  @Test
  void updateTile_both_are_inside_of_darkenedTiles() throws Exception {
    Map<Tile, Integer> map = getDarkenedTiles(fs);

    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);

    map.put(oldTile, 100);
    map.put(newTile, 999);

    fs.updateTile(oldTile, newTile);

    assertFalse(map.containsKey(oldTile));
    assertTrue(map.containsKey(newTile));
    assertEquals(100, map.get(newTile));
    assertEquals(1, map.size());
  }

  @Test
  void updateTile_emptyMap() throws Exception {
    Map<Tile, Integer> map = getDarkenedTiles(fs);

    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);

    fs.updateTile(oldTile, newTile);

    assertTrue(map.isEmpty());
  }

  @Test
  void updateTile_otherTilesUnaffected() throws Exception {
    Map<Tile, Integer> map = getDarkenedTiles(fs);

    Tile oldTile = new TestTile(1, 1, DesignLabel.DEFAULT);
    Tile newTile = new TestTile(2, 2, DesignLabel.DEFAULT);
    Tile otherTile = new TestTile(3, 3, DesignLabel.DEFAULT);

    map.put(oldTile, 100);
    map.put(otherTile, 300);

    fs.updateTile(oldTile, newTile);

    assertFalse(map.containsKey(oldTile));
    assertTrue(map.containsKey(newTile));
    assertEquals(100, map.get(newTile));
    assertTrue(map.containsKey(otherTile));
    assertEquals(300, map.get(otherTile));
    assertEquals(2, map.size());
  }
}
