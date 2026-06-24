package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import core.Entity;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Test für HeroController.changeMainSkill() */
public class ChangeMainSkillTest {
  @Mock private Entity mockHero;

  @Mock private SkillComponent mockSkillComponent;

  private Entity heroWithRealSkillComponent;
  private SkillComponent realSkillComponent;
  private Skill firstSkill;
  private Skill lastSkill;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);

    firstSkill = mock(Skill.class);
    lastSkill = mock(Skill.class);
    Skill middleSkill = mock(Skill.class);

    realSkillComponent = new SkillComponent(firstSkill, middleSkill, lastSkill);
    heroWithRealSkillComponent = mock(Entity.class);

    when(heroWithRealSkillComponent.fetch(SkillComponent.class))
        .thenReturn(Optional.of(realSkillComponent));
    when(heroWithRealSkillComponent.id()).thenReturn(1);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void changeMainSkill_WithSkillComponentAndNextSkill_CallsNextMainSkill() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockHero.id()).thenReturn(1);

    // Act
    HeroController.changeMainSkill(mockHero, true);

    // Assert
    verify(mockSkillComponent, times(1)).nextMainSkill();
    verify(mockSkillComponent, never()).prevMainSkill();
  }

  @Test
  void changeMainSkill_WithSkillComponentAndPrevSkill_CallsPrevMainSkill() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockHero.id()).thenReturn(1);

    // Act
    HeroController.changeMainSkill(mockHero, false);

    // Assert
    verify(mockSkillComponent, times(1)).prevMainSkill();
    verify(mockSkillComponent, never()).nextMainSkill();
  }

  @Test
  void changeMainSkill_WithoutSkillComponent_DoesNothing() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.empty());
    when(mockHero.id()).thenReturn(1);

    // Act
    HeroController.changeMainSkill(mockHero, true);

    // Assert
    verify(mockHero, times(1)).fetch(SkillComponent.class);
    verifyNoInteractions(mockSkillComponent);
  }

  @Test
  void changeMainSkill_MultipleCalls_InvokesMethodsCorrectly() {
    // Arrange
    when(mockHero.fetch(SkillComponent.class)).thenReturn(Optional.of(mockSkillComponent));
    when(mockHero.id()).thenReturn(1);

    // Act
    HeroController.changeMainSkill(mockHero, true);
    HeroController.changeMainSkill(mockHero, true);
    HeroController.changeMainSkill(mockHero, false);

    // Assert
    verify(mockSkillComponent, times(2)).nextMainSkill();
    verify(mockSkillComponent, times(1)).prevMainSkill();
  }

  @Test
  void changeMainSkill_LastSkillAndNextSkill_WrapsFirstSkill() {
    // Arrange
    realSkillComponent.nextMainSkill(); // letzter Skill

    // Act
    HeroController.changeMainSkill(heroWithRealSkillComponent, true);

    // Assert
    assertSame(firstSkill, realSkillComponent.activeMainSkill().orElseThrow());
  }

  @Test
  void changeMainSkill_FirstSkillAndPrevSkill_WrapToLastSkill() {
    // Act
    HeroController.changeMainSkill(heroWithRealSkillComponent, false);

    // Assert
    assertSame(lastSkill, realSkillComponent.activeMainSkill().orElseThrow());
  }

  @Test
  void changeMainSkill_WithNullHero_ThrowsNullPointerException() {
    // Arrange
    Entity nullHero = null;

    // Act & Assert
    assertThrows(NullPointerException.class, () -> HeroController.changeMainSkill(nullHero, true));
  }
}
