package contrib.item.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import contrib.item.Item;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StacksizeTest {

  private static final Animation ANIMATION =
      new Animation(new SimpleIPath("animation/missing_texture.png"));

  private Item item;

  @BeforeEach
  public void setUp() {
    item = new Item("Test", "Test description", ANIMATION);
  }

  // G1: valid stack size (0–64)

  @Test
  public void testStackSize_validLowerBound() {
    item.stackSize(0);
    assertEquals(0, item.stackSize());
  }

  @Test
  public void testStackSize_validUpperBound() {
    item.stackSize(64);
    assertEquals(64, item.stackSize());
  }

  @Test
  public void testStackSize_validMidValue() {
    item.stackSize(32);
    assertEquals(32, item.stackSize());
  }

  // G2: valid maximum stack size (1–64)

  @Test
  public void testMaxStackSize_validLowerBound() {
    item.maxStackSize(1);
    assertEquals(1, item.maxStackSize());
  }

  @Test
  public void testMaxStackSize_validUpperBound() {
    item.maxStackSize(64);
    assertEquals(64, item.maxStackSize());
  }

  @Test
  public void testMaxStackSize_validMidValue() {
    item.maxStackSize(32);
    assertEquals(32, item.maxStackSize());
  }

  // U1: invalid stack size (< 0 or > 64)

  @Test
  public void testStackSize_belowLowerBound() {
    assertThrows(IllegalArgumentException.class, () -> item.stackSize(-1));
  }

  @Test
  public void testStackSize_aboveUpperBound() {
    assertThrows(IllegalArgumentException.class, () -> item.stackSize(65));
  }

  // U2: invalid maximum stack size (< 1 or > 64)

  @Test
  public void testMaxStackSize_belowLowerBound() {
    assertThrows(IllegalArgumentException.class, () -> item.maxStackSize(0));
  }

  @Test
  public void testMaxStackSize_aboveUpperBound() {
    assertThrows(IllegalArgumentException.class, () -> item.maxStackSize(65));
  }
}
