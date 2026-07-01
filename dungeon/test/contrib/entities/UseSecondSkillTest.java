package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
import core.Entity;
import core.utils.Point;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link HeroController#useSecondSkill(Entity, Point)} */
public class UseSecondSkillTest {

  @Mock private Entity mockHero;

  @Mock private SkillComponent mockSkillComponent;

  @Mock private CursorSkill mockCursorSkill;

  @Mock private ProjectileSkill mockProjectileSkill;

  @Mock private Skill mockGenericSkill;

  @Captor private ArgumentCaptor<Supplier<Point>> supplierCaptor;

  private Point validTarget;
  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    validTarget = new Point(10, 20);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void useSecondSkill_WithCursorSkillAndValidTarget_SetsCursorPositionAndExecutes() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.of(mockCursorSkill));

    HeroController.useSecondSkill(mockHero, validTarget);

    verify(mockCursorSkill).cursorPositionSupplier(supplierCaptor.capture());
    assertEquals(
        validTarget,
        supplierCaptor.getValue().get(),
        "Cursor skill should set the target position to the provided valid target point");
    verify(mockCursorSkill, times(1)).execute(mockHero);
  }

  @Test
  void useSecondSkill_WithProjectileSkillAndValidTarget_SetsEndpointAndExecutes() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.of(mockProjectileSkill));

    HeroController.useSecondSkill(mockHero, validTarget);

    verify(mockProjectileSkill).endPointSupplier(supplierCaptor.capture());
    assertEquals(
        validTarget,
        supplierCaptor.getValue().get(),
        "Projectile skill should set the endpoint to the provided valid target point");
    verify(mockProjectileSkill, times(1)).execute(mockHero);
  }

  @Test
  void useSecondSkill_WithGenericSkillType_ExecutesWithoutTargetSetting() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.of(mockGenericSkill));

    HeroController.useSecondSkill(mockHero, validTarget);

    verify(mockGenericSkill, times(1)).execute(mockHero);
    verifyNoInteractions(mockCursorSkill, mockProjectileSkill);
  }

  @Test
  void useSecondSkill_WithNoActiveSkill_ExecutesNothing() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.empty());

    HeroController.useSecondSkill(mockHero, validTarget);

    verify(mockGenericSkill, never()).execute(any());
    verify(mockCursorSkill, never()).execute(any());
    verify(mockProjectileSkill, never()).execute(any());
  }

  @Test
  void useSecondSkill_WithoutSkillComponent_ExecutesNothing() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.empty());

    HeroController.useSecondSkill(mockHero, validTarget);

    verify(mockGenericSkill, never()).execute(any());
    verify(mockCursorSkill, never()).execute(any());
    verify(mockProjectileSkill, never()).execute(any());
  }

  @Test
  void useSecondSkill_WithNullHero_ThrowsNullPointerException() {
    Entity nullHero = null;

    assertThrows(
        NullPointerException.class,
        () -> HeroController.useSecondSkill(nullHero, validTarget),
        "NullPointerException should be thrown when hero parameter is null");
  }

  @Test
  void useSecondSkill_WithCursorSkillAndNullTarget_SetsNullSupplierAndExecutes() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.of(mockCursorSkill));

    HeroController.useSecondSkill(mockHero, null);

    verify(mockCursorSkill).cursorPositionSupplier(supplierCaptor.capture());
    assertNull(
        supplierCaptor.getValue().get(),
        "Cursor skill supplier should return null when null target is provided");
    verify(mockCursorSkill, times(1)).execute(mockHero);
  }

  @Test
  void useSecondSkill_WithProjectileSkillAndNullTarget_SetsNullSupplierAndExecutes() {
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockSkillComponent.activeSecondSkill()).thenReturn(Optional.of(mockProjectileSkill));

    HeroController.useSecondSkill(mockHero, null);

    verify(mockProjectileSkill).endPointSupplier(supplierCaptor.capture());
    assertNull(
        supplierCaptor.getValue().get(),
        "Projectile skill supplier should return null when null target is provided");
    verify(mockProjectileSkill, times(1)).execute(mockHero);
  }
}
