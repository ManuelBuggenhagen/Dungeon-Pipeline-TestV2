package contrib.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.utils.components.skill.Skill;
import core.Entity;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for the {@link SkillComponent}. */
public class SkillComponentTest {

  private static class TestSkill extends Skill {
    TestSkill() {
      super("test", 0);
    }

    @Override
    protected void executeSkill(Entity caster) {}
  }

  private static class SkillA extends Skill {
    SkillA() {
      super("A", 0);
    }

    @Override
    protected void executeSkill(Entity caster) {}
  }

  private static class SkillB extends SkillA {}

  private static void setActiveMainSkill(SkillComponent sc, int value) {
    try {
      Field f = SkillComponent.class.getDeclaredField("activeMainSkill");
      f.setAccessible(true);
      f.setInt(sc, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void setActiveSecondSkill(SkillComponent sc, int value) {
    try {
      Field f = SkillComponent.class.getDeclaredField("activeSecondSkill");
      f.setAccessible(true);
      f.setInt(sc, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static int getActiveMainSkill(SkillComponent sc) {
    try {
      Field f = SkillComponent.class.getDeclaredField("activeMainSkill");
      f.setAccessible(true);
      return f.getInt(sc);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static int getActiveSecondSkill(SkillComponent sc) {
    try {
      Field f = SkillComponent.class.getDeclaredField("activeSecondSkill");
      f.setAccessible(true);
      return f.getInt(sc);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Skill> getInternalSkills(SkillComponent sc) {
    try {
      Field f = SkillComponent.class.getDeclaredField("skills");
      f.setAccessible(true);
      return (List<Skill>) f.get(sc);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  // ---- Issue #119: SkillComponent(Skill...) ----

  /** G1: constructor called with empty varargs. */
  @Test
  public void constructorNoSkills() {
    SkillComponent sc = new SkillComponent();
    assertTrue(sc.getSkills().isEmpty());
    assertTrue(sc.activeMainSkill().isEmpty());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G2: constructor called with exactly one skill. */
  @Test
  public void constructorOneSkill() {
    Skill skill = new TestSkill();
    SkillComponent sc = new SkillComponent(skill);
    assertEquals(List.of(skill), sc.getSkills());
    assertEquals(Optional.of(skill), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G3: constructor called with multiple skills. */
  @Test
  public void constructorMultipleSkills() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2);
    assertEquals(List.of(s1, s2), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertEquals(Optional.of(s2), sc.activeSecondSkill());
  }

  /** U1: explicit null passed as the whole varargs array throws NullPointerException. */
  @Test
  public void constructorNullArrayThrows() {
    assertThrows(NullPointerException.class, () -> new SkillComponent((Skill[]) null));
  }

  /** U2: null elements inside the array are stored as-is, no exception. */
  @Test
  public void constructorNullElementsStored() {
    SkillComponent sc = new SkillComponent(null, null);
    List<Skill> internal = getInternalSkills(sc);
    assertEquals(2, internal.size());
    assertNull(internal.get(0));
    assertNull(internal.get(1));
  }

  // ---- Issue #120: addSkill(Skill) ----

  /** G1: first skill added to an empty list. */
  @Test
  public void addSkillFirst() {
    SkillComponent sc = new SkillComponent();
    Skill skill = new TestSkill();
    sc.addSkill(skill);
    assertEquals(List.of(skill), sc.getSkills());
    assertEquals(Optional.of(skill), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G2: second skill added (exactly one skill present before). */
  @Test
  public void addSkillSecond() {
    Skill s1 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1);
    Skill s2 = new TestSkill();
    sc.addSkill(s2);
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertEquals(Optional.of(s2), sc.activeSecondSkill());
  }

  /** G3: further skill added (already 2 or more present). */
  @Test
  public void addSkillThird() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2);
    Skill s3 = new TestSkill();
    sc.addSkill(s3);
    assertEquals(List.of(s1, s2, s3), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertEquals(Optional.of(s2), sc.activeSecondSkill());
  }

  /** U1: null passed is ignored, no state change. */
  @Test
  public void addSkillNullIgnored() {
    Skill s1 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1);
    sc.addSkill(null);
    assertEquals(List.of(s1), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  // ---- Issue #121: removeSkill(Skill) ----

  /** G1: more than one skill remains after removal. */
  @Test
  public void removeSkillMoreThanOneRemains() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    Skill s3 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2, s3);
    sc.removeSkill(s2);
    assertEquals(List.of(s1, s3), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertEquals(Optional.of(s3), sc.activeSecondSkill());
  }

  /** G2: exactly one skill remains after removal. */
  @Test
  public void removeSkillOneRemains() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2);
    sc.removeSkill(s2);
    assertEquals(List.of(s1), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G3: no skills remain after removal. */
  @Test
  public void removeSkillNoneRemain() {
    Skill s1 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1);
    sc.removeSkill(s1);
    assertTrue(sc.getSkills().isEmpty());
    assertTrue(sc.activeMainSkill().isEmpty());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G4: skill not in list, no change. */
  @Test
  public void removeSkillNotInList() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1);
    sc.removeSkill(s2);
    assertEquals(List.of(s1), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** U1: null passed, no exception, no change. */
  @Test
  public void removeSkillNullNoException() {
    Skill s1 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1);
    assertDoesNotThrow(() -> sc.removeSkill((Skill) null));
    assertEquals(List.of(s1), sc.getSkills());
    assertEquals(Optional.of(s1), sc.activeMainSkill());
  }

  // ---- Issue #122: removeAll() ----

  /** G1: filled list is cleared, indices reset to -1. */
  @Test
  public void removeAllFilledList() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    sc.removeAll();
    assertTrue(sc.getSkills().isEmpty());
    assertTrue(sc.activeMainSkill().isEmpty());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G2: already empty list stays empty/-1. */
  @Test
  public void removeAllAlreadyEmpty() {
    SkillComponent sc = new SkillComponent();
    sc.removeAll();
    assertTrue(sc.getSkills().isEmpty());
    assertTrue(sc.activeMainSkill().isEmpty());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  // ---- Issue #123: removeSkill(Class<? extends Skill>) ----

  /** G1: more than one skill remains after class-based removal. */
  @Test
  public void removeSkillByClassMoreThanOneRemains() {
    SkillA a1 = new SkillA();
    SkillA a2 = new SkillA();
    TestSkill t1 = new TestSkill();
    TestSkill t2 = new TestSkill();
    SkillComponent sc = new SkillComponent(a1, t1, t2, a2);
    sc.removeSkill(SkillA.class);
    assertEquals(List.of(t1, t2), sc.getSkills());
    assertEquals(Optional.of(t1), sc.activeMainSkill());
    assertEquals(Optional.of(t2), sc.activeSecondSkill());
  }

  /** G2: exactly one skill remains after class-based removal. */
  @Test
  public void removeSkillByClassOneRemains() {
    SkillA a1 = new SkillA();
    TestSkill t1 = new TestSkill();
    SkillComponent sc = new SkillComponent(a1, t1);
    sc.removeSkill(SkillA.class);
    assertEquals(List.of(t1), sc.getSkills());
    assertEquals(Optional.of(t1), sc.activeMainSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G3: all matching skills removed, none remain. */
  @Test
  public void removeSkillByClassAllRemoved() {
    SkillComponent sc = new SkillComponent(new SkillA(), new SkillA());
    sc.removeSkill(SkillA.class);
    assertTrue(sc.getSkills().isEmpty());
    assertTrue(sc.activeMainSkill().isEmpty());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G4: no class match, list unchanged but indices still recomputed based on size. */
  @Test
  public void removeSkillByClassNoMatchIndicesRecomputed() {
    TestSkill t1 = new TestSkill();
    TestSkill t2 = new TestSkill();
    TestSkill t3 = new TestSkill();
    SkillComponent sc = new SkillComponent(t1, t2, t3);
    sc.removeSkill(SkillA.class);
    assertEquals(List.of(t1, t2, t3), sc.getSkills());
    assertEquals(Optional.of(t2), sc.activeMainSkill());
    assertEquals(Optional.of(t3), sc.activeSecondSkill());
  }

  /** U1: null class param throws NullPointerException. */
  @Test
  public void removeSkillByClassNullThrows() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    assertThrows(NullPointerException.class, () -> sc.removeSkill((Class<? extends Skill>) null));
  }

  // ---- Issue #124: getSkill(Class<? extends Skill>) ----

  /** G1: exact class match. */
  @Test
  public void getSkillExactMatch() {
    SkillA a = new SkillA();
    SkillComponent sc = new SkillComponent(a);
    assertEquals(Optional.of(a), sc.getSkill(SkillA.class));
  }

  /** G2: subclass match. */
  @Test
  public void getSkillSubclassMatch() {
    SkillB b = new SkillB();
    SkillComponent sc = new SkillComponent(b);
    assertEquals(Optional.of(b), sc.getSkill(SkillA.class));
  }

  /** G3: multiple matches, returns first in insertion order. */
  @Test
  public void getSkillMultipleMatchesReturnsFirst() {
    SkillA a1 = new SkillA();
    SkillA a2 = new SkillA();
    SkillComponent sc = new SkillComponent(a1, a2);
    assertEquals(Optional.of(a1), sc.getSkill(SkillA.class));
  }

  /** G4: no match returns Optional.empty(). */
  @Test
  public void getSkillNoMatch() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    assertTrue(sc.getSkill(SkillA.class).isEmpty());
  }

  /** U1: null class param with non-empty list throws NullPointerException. */
  @Test
  public void getSkillNullClassThrows() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    assertThrows(NullPointerException.class, () -> sc.getSkill(null));
  }

  // ---- Issue #125: activeMainSkill() ----

  /** G1: valid index returns Optional with the skill. */
  @Test
  public void activeMainSkillValidIndex() {
    Skill s = new TestSkill();
    SkillComponent sc = new SkillComponent(s);
    assertEquals(Optional.of(s), sc.activeMainSkill());
  }

  /** G2: index is exactly -1 returns Optional.empty(). */
  @Test
  public void activeMainSkillIndexMinusOne() {
    SkillComponent sc = new SkillComponent();
    assertTrue(sc.activeMainSkill().isEmpty());
  }

  /** G3: index >= skills.size() returns Optional.empty(). */
  @Test
  public void activeMainSkillIndexBeyondSize() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    setActiveMainSkill(sc, 5);
    assertTrue(sc.activeMainSkill().isEmpty());
  }

  /** U1: index < -1 throws IndexOutOfBoundsException. */
  @Test
  public void activeMainSkillNegativeIndexThrows() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    setActiveMainSkill(sc, -2);
    assertThrows(IndexOutOfBoundsException.class, sc::activeMainSkill);
  }

  // ---- Issue #126: activeSecondSkill() ----

  /** G1: valid index returns Optional with the skill. */
  @Test
  public void activeSecondSkillValidIndex() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2);
    assertEquals(Optional.of(s2), sc.activeSecondSkill());
  }

  /** G2: index is exactly -1 returns Optional.empty(). */
  @Test
  public void activeSecondSkillIndexMinusOne() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** G3: index >= skills.size() returns Optional.empty(). */
  @Test
  public void activeSecondSkillIndexBeyondSize() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 5);
    assertTrue(sc.activeSecondSkill().isEmpty());
  }

  /** U1: index < -1 throws IndexOutOfBoundsException. */
  @Test
  public void activeSecondSkillNegativeIndexThrows() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, -2);
    assertThrows(IndexOutOfBoundsException.class, sc::activeSecondSkill);
  }

  // ---- Issue #127: nextMainSkill() ----

  /** G1: advances by one, no collision. */
  @Test
  public void nextMainSkillNoCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 0);
    setActiveSecondSkill(sc, 2);
    sc.nextMainSkill();
    assertEquals(1, getActiveMainSkill(sc));
  }

  /** G2: skips the index occupied by activeSecondSkill. */
  @Test
  public void nextMainSkillSkipsCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 0);
    setActiveSecondSkill(sc, 1);
    sc.nextMainSkill();
    assertEquals(2, getActiveMainSkill(sc));
  }

  /** G3: wraps around from the last index to 0. */
  @Test
  public void nextMainSkillWrapsAround() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 2);
    setActiveSecondSkill(sc, 1);
    sc.nextMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /** G4: exactly 2 skills, loop returns to start, net no-op. */
  @Test
  public void nextMainSkillTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    sc.nextMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /** G5: fewer than 2 skills, blocked by the guard, no-op. */
  @Test
  public void nextMainSkillFewerThanTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    sc.nextMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /** U1: negative start state (< -1) results in the field becoming exactly -1. */
  @Test
  public void nextMainSkillNegativeStartStateBecomesMinusOne() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, -2);
    setActiveSecondSkill(sc, 1);
    sc.nextMainSkill();
    assertEquals(-1, getActiveMainSkill(sc));
  }

  // ---- Issue #128: prevMainSkill() ----

  /** G1: moves back by one, no collision. */
  @Test
  public void prevMainSkillNoCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 1);
    setActiveSecondSkill(sc, 2);
    sc.prevMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /** G2: skips the index occupied by activeSecondSkill. */
  @Test
  public void prevMainSkillSkipsCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 1);
    setActiveSecondSkill(sc, 0);
    sc.prevMainSkill();
    assertEquals(2, getActiveMainSkill(sc));
  }

  /** G3: wraps around from the first index to the last. */
  @Test
  public void prevMainSkillWrapsAround() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, 0);
    setActiveSecondSkill(sc, 1);
    sc.prevMainSkill();
    assertEquals(2, getActiveMainSkill(sc));
  }

  /** G4: exactly 2 skills, loop returns to start, net no-op. */
  @Test
  public void prevMainSkillTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    sc.prevMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /** G5: fewer than 2 skills, blocked by the guard, no-op. */
  @Test
  public void prevMainSkillFewerThanTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    sc.prevMainSkill();
    assertEquals(0, getActiveMainSkill(sc));
  }

  /**
   * U1: strongly negative start state (<= -skills.size()) leaves the field in a corrupted state
   * that causes activeMainSkill() to throw IndexOutOfBoundsException.
   */
  @Test
  public void prevMainSkillStronglyNegativeCausesOutOfBounds() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveMainSkill(sc, -4);
    setActiveSecondSkill(sc, 1);
    sc.prevMainSkill();
    assertEquals(-2, getActiveMainSkill(sc));
    assertThrows(IndexOutOfBoundsException.class, sc::activeMainSkill);
  }

  // ---- Issue #129: nextSecondSkill() ----

  /** G1: advances by one, no collision. */
  @Test
  public void nextSecondSkillNoCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 0);
    setActiveMainSkill(sc, 2);
    sc.nextSecondSkill();
    assertEquals(1, getActiveSecondSkill(sc));
  }

  /** G2: skips the index occupied by activeMainSkill. */
  @Test
  public void nextSecondSkillSkipsCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 0);
    setActiveMainSkill(sc, 1);
    sc.nextSecondSkill();
    assertEquals(2, getActiveSecondSkill(sc));
  }

  /** G3: wraps around from the last index to 0. */
  @Test
  public void nextSecondSkillWrapsAround() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 2);
    setActiveMainSkill(sc, 1);
    sc.nextSecondSkill();
    assertEquals(0, getActiveSecondSkill(sc));
  }

  /** G4: exactly 2 skills, loop returns to start, net no-op. */
  @Test
  public void nextSecondSkillTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    sc.nextSecondSkill();
    assertEquals(1, getActiveSecondSkill(sc));
  }

  /** G5: fewer than 2 skills, blocked by the guard, no-op. */
  @Test
  public void nextSecondSkillFewerThanTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    sc.nextSecondSkill();
    assertEquals(-1, getActiveSecondSkill(sc));
  }

  /** U1: negative start state (< -1) results in the field becoming exactly -1. */
  @Test
  public void nextSecondSkillNegativeStartStateBecomesMinusOne() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, -2);
    setActiveMainSkill(sc, 1);
    sc.nextSecondSkill();
    assertEquals(-1, getActiveSecondSkill(sc));
  }

  // ---- Issue #130: prevSecondSkill() ----

  /** G1: moves back by one, no collision. */
  @Test
  public void prevSecondSkillNoCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 1);
    setActiveMainSkill(sc, 2);
    sc.prevSecondSkill();
    assertEquals(0, getActiveSecondSkill(sc));
  }

  /** G2: skips the index occupied by activeMainSkill. */
  @Test
  public void prevSecondSkillSkipsCollision() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 1);
    setActiveMainSkill(sc, 0);
    sc.prevSecondSkill();
    assertEquals(2, getActiveSecondSkill(sc));
  }

  /** G3: wraps around from the first index to the last. */
  @Test
  public void prevSecondSkillWrapsAround() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, 0);
    setActiveMainSkill(sc, 1);
    sc.prevSecondSkill();
    assertEquals(2, getActiveSecondSkill(sc));
  }

  /** G4: exactly 2 skills, loop returns to start, net no-op. */
  @Test
  public void prevSecondSkillTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill());
    sc.prevSecondSkill();
    assertEquals(1, getActiveSecondSkill(sc));
  }

  /** G5: fewer than 2 skills, blocked by the guard, no-op. */
  @Test
  public void prevSecondSkillFewerThanTwoSkillsNoOp() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    sc.prevSecondSkill();
    assertEquals(-1, getActiveSecondSkill(sc));
  }

  /**
   * U1: strongly negative start state (<= -skills.size()) leaves the field in a corrupted state
   * that causes activeSecondSkill() to throw IndexOutOfBoundsException.
   */
  @Test
  public void prevSecondSkillStronglyNegativeCausesOutOfBounds() {
    SkillComponent sc = new SkillComponent(new TestSkill(), new TestSkill(), new TestSkill());
    setActiveSecondSkill(sc, -4);
    setActiveMainSkill(sc, 1);
    sc.prevSecondSkill();
    assertEquals(-2, getActiveSecondSkill(sc));
    assertThrows(IndexOutOfBoundsException.class, sc::activeSecondSkill);
  }

  // ---- Issue #131: getSkills() ----

  /** G1: filled list returns a list with the same elements in the same order. */
  @Test
  public void getSkillsFilledList() {
    Skill s1 = new TestSkill();
    Skill s2 = new TestSkill();
    SkillComponent sc = new SkillComponent(s1, s2);
    assertEquals(List.of(s1, s2), sc.getSkills());
  }

  /** G2: empty list returns an empty list. */
  @Test
  public void getSkillsEmptyList() {
    SkillComponent sc = new SkillComponent();
    assertTrue(sc.getSkills().isEmpty());
  }

  /** G3: returned list is immutable. */
  @Test
  public void getSkillsIsImmutable() {
    SkillComponent sc = new SkillComponent(new TestSkill());
    List<Skill> skills = sc.getSkills();
    assertThrows(UnsupportedOperationException.class, () -> skills.add(new TestSkill()));
  }

  /** U1: internal list containing a null element causes NullPointerException. */
  @Test
  public void getSkillsWithNullElementThrows() {
    SkillComponent sc = new SkillComponent((Skill) null);
    assertThrows(NullPointerException.class, sc::getSkills);
  }
}
