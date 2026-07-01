package contrib.item.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import contrib.item.Item;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the stack size and maximum stack size validation of {@link Item}. */
public class StacksizeTest {

  private static final Animation ANIMATION =
      new Animation(new SimpleIPath("animation/missing_texture.png"));

  private Item item;

  /** Sets up a fresh {@link Item} instance before each test. */
  @BeforeEach
  public void setUp() {
    item = new Item("Test", "Test description", ANIMATION);
  }

  // G1: valid stack size (0–64)

  /** Tests that the lower bound (0) is accepted as a valid stack size. */
  @Test
  public void testStackSize_validLowerBound() {
    item.stackSize(0);
    assertEquals(0, item.stackSize());
  }

  /** Tests that the upper bound (64) is accepted as a valid stack size. */
  @Test
  public void testStackSize_validUpperBound() {
    item.stackSize(64);
    assertEquals(64, item.stackSize());
  }

  /** Tests that a mid-range value (32) is accepted as a valid stack size. */
  @Test
  public void testStackSize_validMidValue() {
    item.stackSize(32);
    assertEquals(32, item.stackSize());
  }

  // G2: valid maximum stack size (1–64)

  /** Tests that the lower bound (1) is accepted as a valid maximum stack size. */
  @Test
  public void testMaxStackSize_validLowerBound() {
    item.maxStackSize(1);
    assertEquals(1, item.maxStackSize());
  }

  /** Tests that the upper bound (64) is accepted as a valid maximum stack size. */
  @Test
  public void testMaxStackSize_validUpperBound() {
    item.maxStackSize(64);
    assertEquals(64, item.maxStackSize());
  }

  /** Tests that a mid-range value (32) is accepted as a valid maximum stack size. */
  @Test
  public void testMaxStackSize_validMidValue() {
    item.maxStackSize(32);
    assertEquals(32, item.maxStackSize());
  }

  // U1: invalid stack size (< 0 or > 64)

  /**
   * Tests that a stack size below the lower bound (-1) throws an {@link IllegalArgumentException}.
   */
  @Test
  public void testStackSize_belowLowerBound() {
    assertThrows(IllegalArgumentException.class, () -> item.stackSize(-1));
  }

  /**
   * Tests that a stack size above the upper bound (65) throws an {@link IllegalArgumentException}.
   */
  @Test
  public void testStackSize_aboveUpperBound() {
    assertThrows(IllegalArgumentException.class, () -> item.stackSize(65));
  }

  // U2: invalid maximum stack size (< 1 or > 64)

  /**
   * Tests that a maximum stack size below the lower bound (0) throws an {@link
   * IllegalArgumentException}.
   */
  @Test
  public void testMaxStackSize_belowLowerBound() {
    assertThrows(IllegalArgumentException.class, () -> item.maxStackSize(0));
  }

  /**
   * Tests that a maximum stack size above the upper bound (65) throws an {@link
   * IllegalArgumentException}.
   */
  @Test
  public void testMaxStackSize_aboveUpperBound() {
    assertThrows(IllegalArgumentException.class, () -> item.maxStackSize(65));
  }
}
