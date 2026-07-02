package contrib.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

import contrib.systems.AttachmentSystem;
import core.components.PositionComponent;
import core.utils.Vector2;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests for the {@link AttachmentComponent}. */
public class AttachmentComponentTest {

  /** A non-zero offset is stored as-is and does not enable rotating with the origin. */
  @Test
  public void constructor_nonZeroOffset_storesOffsetNoRotation() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();
      Vector2 offset = Vector2.of(1, 2);

      AttachmentComponent ac = new AttachmentComponent(offset, copy, origin);

      assertEquals(offset, ac.getOffset());
      assertFalse(ac.isRotatingWithOrigin());
      assertFalse(ac.isTextureRotating());
      assertEquals(1f, ac.getScale());
      mocked.verify(() -> AttachmentSystem.registerAttachment(copy, origin), times(1));
    }
  }

  /** Passing the {@link Vector2#ZERO} singleton enables rotating with the origin. */
  @Test
  public void constructor_zeroOffset_enablesRotatingWithOrigin() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();

      AttachmentComponent ac = new AttachmentComponent(Vector2.ZERO, copy, origin);

      assertTrue(ac.isRotatingWithOrigin());
      assertFalse(ac.isTextureRotating());
      assertEquals(1f, ac.getScale());
      assertEquals(Vector2.ZERO, ac.getOffset());
      mocked.verify(() -> AttachmentSystem.registerAttachment(copy, origin), times(1));
    }
  }

  /** The four-argument constructor sets whether the texture rotates with the origin. */
  @Test
  public void constructor_fourArg_setsTextureRotating() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();
      Vector2 offset = Vector2.of(1, 2);

      AttachmentComponent ac = new AttachmentComponent(offset, copy, origin, true);

      assertTrue(ac.isTextureRotating());
      assertEquals(offset, ac.getOffset());
      assertFalse(ac.isRotatingWithOrigin());
      assertEquals(1f, ac.getScale());
      mocked.verify(() -> AttachmentSystem.registerAttachment(copy, origin), times(1));
    }
  }

  /**
   * A null offset is stored as-is, does not enable rotating with the origin and later fails when
   * used as a vector.
   */
  @Test
  public void constructor_nullOffset_storesNullStillRegisters() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();

      AttachmentComponent ac = new AttachmentComponent(null, copy, origin);

      assertNull(ac.getOffset());
      assertFalse(ac.isRotatingWithOrigin());
      mocked.verify(() -> AttachmentSystem.registerAttachment(copy, origin), times(1));
      assertThrows(NullPointerException.class, () -> ac.getOffset().scale(2.0));
    }
  }

  /** A null copy still registers with the {@link AttachmentSystem} and does not throw. */
  @Test
  public void constructor_nullCopy_stillRegistersWithNull() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent origin = new PositionComponent();

      new AttachmentComponent(Vector2.of(1, 2), null, origin);

      mocked.verify(() -> AttachmentSystem.registerAttachment(null, origin), times(1));
    }
  }

  /** A null origin still registers with the {@link AttachmentSystem} and does not throw. */
  @Test
  public void constructor_nullOrigin_stillRegistersWithNull() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();

      new AttachmentComponent(Vector2.of(1, 2), copy, null);

      mocked.verify(() -> AttachmentSystem.registerAttachment(copy, null), times(1));
    }
  }

  /** Setting a non-null offset is reflected by {@link AttachmentComponent#getOffset()}. */
  @Test
  public void setOffset_nonNull_isReturnedByGetOffset() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();
      AttachmentComponent ac = new AttachmentComponent(Vector2.of(1, 2), copy, origin);

      Vector2 newOffset = Vector2.of(3, 4);
      ac.setOffset(newOffset);

      assertEquals(newOffset, ac.getOffset());
    }
  }

  /** Setting a null offset makes {@link AttachmentComponent#getOffset()} return null. */
  @Test
  public void setOffset_null_getOffsetReturnsNull() {
    try (MockedStatic<AttachmentSystem> mocked = Mockito.mockStatic(AttachmentSystem.class)) {
      PositionComponent copy = new PositionComponent();
      PositionComponent origin = new PositionComponent();
      AttachmentComponent ac = new AttachmentComponent(Vector2.of(1, 2), copy, origin);

      ac.setOffset(null);

      assertNull(ac.getOffset());
    }
  }
}
