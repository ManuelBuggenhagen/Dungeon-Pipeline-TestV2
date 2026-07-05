package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.fight.AIMeleeBehaviour;
import contrib.utils.components.ai.fight.AIRangeBehaviour;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnApproach;
import contrib.utils.components.ai.transition.ProtectOnAttack;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import testingUtils.TestGame;

/**
 * Tests for {@link AIFactory}.
 *
 * <p>The equivalence classes of each method under test are grouped into their own {@link Nested}
 * class so the {@code G}/{@code U} numbering stays unambiguous.
 */
class AIFactoryTest {

  /**
   * Reads a private {@code float} constant of {@link AIFactory} so ranges stay in sync with it.
   *
   * @param name the name of the constant field to read.
   * @return the value of the constant.
   */
  private static float floatConstant(final String name) {
    try {
      Field field = AIFactory.class.getDeclaredField(name);
      field.setAccessible(true);
      return field.getFloat(null);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("AIFactory is missing the constant " + name, e);
    }
  }

  /**
   * Reads a private {@code int} constant of {@link AIFactory} so ranges stay in sync with it.
   *
   * @param name the name of the constant field to read.
   * @return the value of the constant.
   */
  private static int intConstant(final String name) {
    try {
      Field field = AIFactory.class.getDeclaredField(name);
      field.setAccessible(true);
      return field.getInt(null);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("AIFactory is missing the constant " + name, e);
    }
  }

  /** Repetitions used to exercise random selection when several valid monsters exist. */
  private static final int RANDOM_DRAWS = 100;

  /**
   * Creates a valid monster: an entity with both a {@link HealthComponent} and {@link AIComponent}.
   *
   * @return a new entity that satisfies the monster criteria.
   */
  private static Entity monster() {
    Entity entity = new Entity();
    entity.add(new HealthComponent());
    entity.add(new AIComponent());
    return entity;
  }

  /**
   * Creates an invalid entity that only has a {@link HealthComponent}.
   *
   * @return a new entity that is missing the {@link AIComponent}.
   */
  private static Entity healthOnly() {
    Entity entity = new Entity();
    entity.add(new HealthComponent());
    return entity;
  }

  /**
   * Creates an invalid entity that only has an {@link AIComponent}.
   *
   * @return a new entity that is missing the {@link HealthComponent}.
   */
  private static Entity aiOnly() {
    Entity entity = new Entity();
    entity.add(new AIComponent());
    return entity;
  }

  /**
   * Creates an invalid entity that has neither a {@link HealthComponent} nor an {@link
   * AIComponent}.
   *
   * @return a new entity without any monster-relevant component.
   */
  private static Entity plain() {
    return new Entity();
  }

  /**
   * Tests for {@link AIFactory#randomMonster()}.
   *
   * <p>{@code randomMonster()} collects every entity of the current level that has both a {@link
   * HealthComponent} and an {@link AIComponent}, then returns a random one of them as an {@link
   * Optional}, or an empty {@link Optional} if no such entity exists.
   *
   * <p>The level-entity stream is the only {@link core.Game} dependency of the method, so it is
   * isolated with {@link TestGame#withEntities(Entity...)}.
   */
  @Nested
  class RandomMonster {

    /** G1: exactly one valid and no invalid entity present -> that monster is returned. */
    @Test
    void returnsTheMonster_whenExactlyOneValidEntityExists() {
      Entity monster = monster();

      Optional<Entity> result;
      try (var scope = TestGame.withEntities(monster)) {
        result = AIFactory.randomMonster();
      }

      assertEquals(
          Optional.of(monster), result, "the single monster in the level should be returned");
    }

    /** G2: one valid monster among several invalid entities -> that monster is returned. */
    @Test
    void returnsTheMonster_whenOneValidAmongInvalidEntities() {
      Entity monster = monster();

      Optional<Entity> result;
      try (var scope = TestGame.withEntities(healthOnly(), monster, aiOnly(), plain())) {
        result = AIFactory.randomMonster();
      }

      assertEquals(
          Optional.of(monster),
          result,
          "the only entity with both a HealthComponent and an AIComponent should be returned");
    }

    /** G3: several valid and no invalid entity present -> one of the monsters is returned. */
    @Test
    void returnsOneOfTheMonsters_whenMultipleValidEntitiesExist() {
      Entity first = monster();
      Entity second = monster();
      Entity third = monster();
      Set<Entity> monsters = Set.of(first, second, third);

      try (var scope = TestGame.withEntities(first, second, third)) {
        for (int draw = 0; draw < RANDOM_DRAWS; draw++) {
          Optional<Entity> result = AIFactory.randomMonster();

          assertTrue(result.isPresent(), "a monster should be returned when valid monsters exist");
          assertTrue(
              monsters.contains(result.orElseThrow()),
              "the returned entity should be one of the present monsters");
        }
      }
    }

    /**
     * G4: several valid and several invalid entities present -> only a valid monster is returned.
     */
    @Test
    void returnsOnlyValidMonsters_whenValidAndInvalidEntitiesExist() {
      Entity first = monster();
      Entity second = monster();
      Set<Entity> monsters = Set.of(first, second);

      try (var scope =
          TestGame.withEntities(healthOnly(), first, aiOnly(), second, plain(), healthOnly())) {
        for (int draw = 0; draw < RANDOM_DRAWS; draw++) {
          Optional<Entity> result = AIFactory.randomMonster();

          assertTrue(result.isPresent(), "a monster should be returned when valid monsters exist");
          assertTrue(
              monsters.contains(result.orElseThrow()),
              "randomMonster must only ever return an entity that is a valid monster");
        }
      }
    }

    /** U1: no entities in the level -> an empty Optional is returned. */
    @Test
    void returnsEmpty_whenNoEntitiesExist() {
      Optional<Entity> result;
      try (var scope = TestGame.withEntities()) {
        result = AIFactory.randomMonster();
      }

      assertTrue(result.isEmpty(), "an empty level should yield no monster");
    }

    /** U2: entities present but none is a monster -> an empty Optional is returned. */
    @Test
    void returnsEmpty_whenNoEntityMeetsMonsterCriteria() {
      Optional<Entity> result;
      try (var scope = TestGame.withEntities(healthOnly(), aiOnly(), plain())) {
        result = AIFactory.randomMonster();
      }

      assertTrue(
          result.isEmpty(),
          "an entity must have both a HealthComponent and an AIComponent to count as a monster");
    }
  }

  /**
   * Tests for {@link AIFactory#randomIdleAI()}.
   *
   * <p>{@code randomIdleAI()} randomly picks one of the supported idle behaviours ({@link
   * PatrolWalk}, {@link RadiusWalk}, {@link StaticRadiusWalk}) and constructs it with parameters
   * drawn from fixed ranges. The concrete behaviours expose none of these parameters and transform
   * some of them in their constructor, so {@link Mockito#mockConstruction(Class,
   * org.mockito.MockedConstruction.MockInitializer)} is used to intercept construction and capture
   * the raw arguments before any transformation. Sampling many times makes every branch (index 0,
   * 1, 2) occur with practical certainty.
   */
  @Nested
  class RandomIdleAI {

    /** Number of draws; large enough that each of the three branches occurs with certainty. */
    private static final int IDLE_SAMPLES = 500;

    /**
     * The constructor arguments captured for each produced idle behaviour, grouped by type.
     *
     * @param patrolWalks captured constructor arguments of every produced {@link PatrolWalk}.
     * @param radiusWalks captured constructor arguments of every produced {@link RadiusWalk}.
     * @param staticRadiusWalks captured constructor arguments of every produced {@link
     *     StaticRadiusWalk}.
     */
    private record IdleSamples(
        List<Object[]> patrolWalks, List<Object[]> radiusWalks, List<Object[]> staticRadiusWalks) {}

    /**
     * Calls {@link AIFactory#randomIdleAI()} {@code iterations} times while intercepting the
     * construction of all three supported idle behaviours, and records their constructor arguments.
     * Also asserts on every draw that the returned behaviour is one of the supported types.
     *
     * @param iterations how often {@code randomIdleAI()} is called.
     * @return the captured constructor arguments grouped by behaviour type.
     */
    private IdleSamples sampleIdleAi(final int iterations) {
      List<Object[]> patrolWalks = new ArrayList<>();
      List<Object[]> radiusWalks = new ArrayList<>();
      List<Object[]> staticRadiusWalks = new ArrayList<>();

      try (var patrol =
              Mockito.mockConstruction(
                  PatrolWalk.class,
                  (mock, context) -> patrolWalks.add(context.arguments().toArray()));
          var radius =
              Mockito.mockConstruction(
                  RadiusWalk.class,
                  (mock, context) -> radiusWalks.add(context.arguments().toArray()));
          var staticRadius =
              Mockito.mockConstruction(
                  StaticRadiusWalk.class,
                  (mock, context) -> staticRadiusWalks.add(context.arguments().toArray()))) {

        for (int draw = 0; draw < iterations; draw++) {
          Consumer<Entity> idleAi = AIFactory.randomIdleAI();

          assertTrue(
              idleAi instanceof PatrolWalk
                  || idleAi instanceof RadiusWalk
                  || idleAi instanceof StaticRadiusWalk,
              "randomIdleAI must only ever return a supported idle AI type");
        }
      }

      return new IdleSamples(patrolWalks, radiusWalks, staticRadiusWalks);
    }

    /**
     * U1: every call produces exactly one of the three supported idle behaviours and nothing else.
     */
    @Test
    void returnsOnlySupportedIdleAiTypes() {
      IdleSamples samples = sampleIdleAi(IDLE_SAMPLES);

      int produced =
          samples.patrolWalks().size()
              + samples.radiusWalks().size()
              + samples.staticRadiusWalks().size();
      assertEquals(
          IDLE_SAMPLES,
          produced,
          "each call must construct exactly one PatrolWalk, RadiusWalk or StaticRadiusWalk");
    }

    /** G1 and U2&ndash;U5: index 0 produces a PatrolWalk with every parameter within its range. */
    @Test
    void createsPatrolWalkWithParametersInRange() {
      float radiusLow = floatConstant("PATROL_RADIUS_LOW");
      float radiusHigh = floatConstant("PATROL_RADIUS_HIGH");
      int checkpointsLow = intConstant("CHECKPOINTS_LOW");
      int checkpointsHigh = intConstant("CHECKPOINTS_HIGH");
      int pauseLow = intConstant("PAUSE_TIME_LOW");
      int pauseHigh = intConstant("PAUSE_TIME_HIGH");
      Set<PatrolWalk.MODE> validModes = Set.of(PatrolWalk.MODE.values());

      IdleSamples samples = sampleIdleAi(IDLE_SAMPLES);

      assertFalse(
          samples.patrolWalks().isEmpty(), "index 0 should produce at least one PatrolWalk");
      for (Object[] args : samples.patrolWalks()) {
        float radius = (Float) args[0];
        int checkpoints = (Integer) args[1];
        int pauseTime = (Integer) args[2];
        PatrolWalk.MODE mode = (PatrolWalk.MODE) args[3];

        // U2
        assertTrue(
            radius >= radiusLow && radius <= radiusHigh, "patrol radius out of range: " + radius);
        // U3
        assertTrue(
            checkpoints >= checkpointsLow && checkpoints <= checkpointsHigh,
            "checkpoint count out of range: " + checkpoints);
        // U4
        assertTrue(
            pauseTime >= pauseLow && pauseTime <= pauseHigh,
            "pause time out of range: " + pauseTime);
        // U5
        assertTrue(validModes.contains(mode), "mode must be a valid PatrolWalk.MODE: " + mode);
      }
    }

    /** G2 and U6&ndash;U7: index 1 produces a RadiusWalk with every parameter within its range. */
    @Test
    void createsRadiusWalkWithParametersInRange() {
      float radiusLow = floatConstant("RADIUS_WALK_LOW");
      float radiusHigh = floatConstant("RADIUS_WALK_HIGH");
      int breakLow = intConstant("BREAK_TIME_LOW");
      int breakHigh = intConstant("BREAK_TIME_HIGH");

      IdleSamples samples = sampleIdleAi(IDLE_SAMPLES);

      assertFalse(
          samples.radiusWalks().isEmpty(), "index 1 should produce at least one RadiusWalk");
      for (Object[] args : samples.radiusWalks()) {
        float radius = (Float) args[0];
        int breakTime = (Integer) args[1];

        // U6
        assertTrue(radius >= radiusLow && radius <= radiusHigh, "radius out of range: " + radius);
        // U7
        assertTrue(
            breakTime >= breakLow && breakTime <= breakHigh,
            "break time out of range: " + breakTime);
      }
    }

    /**
     * G3 and U8&ndash;U9: index 2 produces a StaticRadiusWalk with every parameter within its
     * range.
     */
    @Test
    void createsStaticRadiusWalkWithParametersInRange() {
      float radiusLow = floatConstant("STATIC_RADIUS_WALK_LOW");
      float radiusHigh = floatConstant("STATIC_RADIUS_WALK_HIGH");
      int breakLow = intConstant("STATIC_BREAK_TIME_LOW");
      int breakHigh = intConstant("STATIC_BREAK_TIME_HIGH");

      IdleSamples samples = sampleIdleAi(IDLE_SAMPLES);

      assertFalse(
          samples.staticRadiusWalks().isEmpty(),
          "index 2 should produce at least one StaticRadiusWalk");
      for (Object[] args : samples.staticRadiusWalks()) {
        float radius = (Float) args[0];
        int breakTime = (Integer) args[1];

        // U8
        assertTrue(radius >= radiusLow && radius <= radiusHigh, "radius out of range: " + radius);
        // U9
        assertTrue(
            breakTime >= breakLow && breakTime <= breakHigh,
            "break time out of range: " + breakTime);
      }
    }
  }

  /**
   * Tests for {@link AIFactory#randomMonsterOrMe(Entity)}.
   *
   * <p>{@code randomMonsterOrMe(me)} returns a random monster from the current level, or the given
   * fallback entity {@code me} when the level contains no monster. A monster is an entity with both
   * a {@link HealthComponent} and an {@link AIComponent}. The method delegates to {@code
   * randomMonster()}, so the level-entity stream is isolated with {@link
   * TestGame#withEntities(Entity...)}. The fallback {@code me} is deliberately kept out of the
   * level stream, so a returned monster is always distinguishable from {@code me}.
   */
  @Nested
  class RandomMonsterOrMe {

    /** G1: at least one monster present -> a monster (and not {@code me}) is returned. */
    @Test
    void returnsAMonster_whenAtLeastOneMonsterExists() {
      Entity me = plain();
      Entity theMonster = monster();

      Entity result;
      try (var scope = TestGame.withEntities(theMonster)) {
        result = AIFactory.randomMonsterOrMe(me);
      }

      assertNotSame(me, result, "a present monster must be preferred over the fallback entity");
      assertSame(theMonster, result, "the only monster in the level should be returned");
      assertTrue(
          result.isPresent(HealthComponent.class) && result.isPresent(AIComponent.class),
          "the returned entity must be a valid monster");
    }

    /** G2: no monster present -> the fallback entity {@code me} is returned. */
    @Test
    void returnsMe_whenNoMonsterExists() {
      Entity me = plain();

      Entity result;
      try (var scope = TestGame.withEntities(healthOnly(), aiOnly())) {
        result = AIFactory.randomMonsterOrMe(me);
      }

      assertSame(me, result, "without a monster the fallback entity must be returned");
    }

    /** U1: monsters present -> {@code me} must never be returned; a monster must be. */
    @Test
    void neverReturnsMe_whenMonstersExist() {
      Entity me = plain();
      Entity first = monster();
      Entity second = monster();
      Entity third = monster();
      Set<Entity> monsters = Set.of(first, second, third);

      try (var scope = TestGame.withEntities(first, second, third)) {
        for (int draw = 0; draw < RANDOM_DRAWS; draw++) {
          Entity result = AIFactory.randomMonsterOrMe(me);

          assertNotSame(me, result, "me must not be returned while monsters exist");
          assertTrue(monsters.contains(result), "the returned entity must be one of the monsters");
        }
      }
    }

    /** U2: monsters and non-monsters present -> only a valid monster may be returned. */
    @Test
    void returnsOnlyValidMonsters_whenMonstersAndNonMonstersExist() {
      Entity me = plain();
      Entity first = monster();
      Entity second = monster();
      Set<Entity> monsters = Set.of(first, second);

      try (var scope = TestGame.withEntities(healthOnly(), first, aiOnly(), second, plain())) {
        for (int draw = 0; draw < RANDOM_DRAWS; draw++) {
          Entity result = AIFactory.randomMonsterOrMe(me);

          assertNotSame(me, result, "me must not be returned while monsters exist");
          assertTrue(
              monsters.contains(result),
              "randomMonsterOrMe must only ever return a valid monster, never a non-monster");
          assertTrue(
              result.isPresent(HealthComponent.class) && result.isPresent(AIComponent.class),
              "the returned entity must have both a HealthComponent and an AIComponent");
        }
      }
    }

    /** U3: no monster present -> exactly {@code me} is returned and no other entity. */
    @Test
    void returnsExactlyMe_whenNoMonsterExists() {
      Entity me = plain();
      Entity nonMonster = healthOnly();

      Entity result;
      try (var scope = TestGame.withEntities(nonMonster, aiOnly(), plain())) {
        result = AIFactory.randomMonsterOrMe(me);
      }

      assertSame(
          me, result, "exactly the passed entity me must be returned when no monster exists");
      assertNotSame(nonMonster, result, "no entity other than me may be returned");
    }
  }

  /**
   * Tests for {@link AIFactory#randomTransition(Entity)}.
   *
   * <p>{@code randomTransition(entity)} randomly picks one of the supported transition behaviours
   * ({@link RangeTransition}, {@link SelfDefendTransition}, {@link ProtectOnApproach}, {@link
   * ProtectOnAttack}) and constructs it with parameters drawn from fixed ranges. The two protect
   * behaviours receive {@code randomMonsterOrMe(entity)} as their target entity. The behaviours
   * expose none of their parameters, so {@link Mockito#mockConstruction(Class,
   * org.mockito.MockedConstruction.MockInitializer)} intercepts construction and captures the raw
   * arguments.
   *
   * <p>To make the target deterministic, the level is set up with exactly one monster while the
   * passed {@code entity} is kept out of the level. {@code randomMonsterOrMe(entity)} then always
   * returns that monster, which lets the tests assert that the target is the monster and not the
   * passed entity.
   */
  @Nested
  class RandomTransition {

    /** Number of draws; large enough that each of the four branches occurs with certainty. */
    private static final int TRANSITION_SAMPLES = 500;

    /**
     * The constructor arguments captured for each produced transition, grouped by type.
     *
     * @param rangeTransitions captured constructor arguments of every produced {@link
     *     RangeTransition}.
     * @param selfDefends captured constructor arguments of every produced {@link
     *     SelfDefendTransition}.
     * @param protectOnApproaches captured constructor arguments of every produced {@link
     *     ProtectOnApproach}.
     * @param protectOnAttacks captured constructor arguments of every produced {@link
     *     ProtectOnAttack}.
     */
    private record TransitionSamples(
        List<Object[]> rangeTransitions,
        List<Object[]> selfDefends,
        List<Object[]> protectOnApproaches,
        List<Object[]> protectOnAttacks) {}

    /**
     * Calls {@link AIFactory#randomTransition(Entity)} {@code iterations} times while intercepting
     * the construction of all four supported transitions, and records their constructor arguments.
     * The level entities control what {@code randomMonsterOrMe(entity)} returns.
     *
     * @param iterations how often {@code randomTransition(entity)} is called.
     * @param entity the entity passed to {@code randomTransition}.
     * @param levelEntities the entities the mocked level stream should yield.
     * @return the captured constructor arguments grouped by transition type.
     */
    private TransitionSamples sampleTransition(
        final int iterations, final Entity entity, final Entity... levelEntities) {
      List<Object[]> rangeTransitions = new ArrayList<>();
      List<Object[]> selfDefends = new ArrayList<>();
      List<Object[]> protectOnApproaches = new ArrayList<>();
      List<Object[]> protectOnAttacks = new ArrayList<>();

      try (var game = TestGame.withEntities(levelEntities);
          var range =
              Mockito.mockConstruction(
                  RangeTransition.class,
                  (mock, context) -> rangeTransitions.add(context.arguments().toArray()));
          var selfDefend =
              Mockito.mockConstruction(
                  SelfDefendTransition.class,
                  (mock, context) -> selfDefends.add(context.arguments().toArray()));
          var protectApproach =
              Mockito.mockConstruction(
                  ProtectOnApproach.class,
                  (mock, context) -> protectOnApproaches.add(context.arguments().toArray()));
          var protectAttack =
              Mockito.mockConstruction(
                  ProtectOnAttack.class,
                  (mock, context) -> protectOnAttacks.add(context.arguments().toArray()))) {

        for (int draw = 0; draw < iterations; draw++) {
          Function<Entity, Boolean> transition = AIFactory.randomTransition(entity);

          assertTrue(
              transition instanceof RangeTransition
                  || transition instanceof SelfDefendTransition
                  || transition instanceof ProtectOnApproach
                  || transition instanceof ProtectOnAttack,
              "randomTransition must only ever return a supported transition AI type");
        }
      }

      return new TransitionSamples(
          rangeTransitions, selfDefends, protectOnApproaches, protectOnAttacks);
    }

    /** U1: every call produces exactly one of the four supported transitions and nothing else. */
    @Test
    void returnsOnlySupportedTransitionTypes() {
      TransitionSamples samples = sampleTransition(TRANSITION_SAMPLES, plain(), monster());

      int produced =
          samples.rangeTransitions().size()
              + samples.selfDefends().size()
              + samples.protectOnApproaches().size()
              + samples.protectOnAttacks().size();
      assertEquals(
          TRANSITION_SAMPLES,
          produced,
          "each call must construct exactly one RangeTransition, SelfDefendTransition, "
              + "ProtectOnApproach or ProtectOnAttack");
    }

    /** G1 and U2: index 0 produces a RangeTransition with its distance within range. */
    @Test
    void createsRangeTransitionWithDistanceInRange() {
      float low = floatConstant("RANGE_TRANSITION_LOW");
      float high = floatConstant("RANGE_TRANSITION_HIGH");

      TransitionSamples samples = sampleTransition(TRANSITION_SAMPLES, plain(), monster());

      assertFalse(
          samples.rangeTransitions().isEmpty(),
          "index 0 should produce at least one RangeTransition");
      for (Object[] args : samples.rangeTransitions()) {
        float distance = (Float) args[0];

        // U2
        assertTrue(
            distance >= low && distance <= high,
            "range transition distance out of range: " + distance);
      }
    }

    /** G2: index 1 produces a SelfDefendTransition (constructed without arguments). */
    @Test
    void createsSelfDefendTransition() {
      TransitionSamples samples = sampleTransition(TRANSITION_SAMPLES, plain(), monster());

      assertFalse(
          samples.selfDefends().isEmpty(),
          "index 1 should produce at least one SelfDefendTransition");
      for (Object[] args : samples.selfDefends()) {
        assertEquals(0, args.length, "SelfDefendTransition must be constructed without arguments");
      }
    }

    /**
     * G3 and U3: index 2 produces a ProtectOnApproach with its range within bounds and {@code
     * randomMonsterOrMe(entity)} as its target.
     */
    @Test
    void createsProtectOnApproachWithRangeInBoundsAndMonsterTarget() {
      float low = floatConstant("PROTECT_RANGE_LOW");
      float high = floatConstant("PROTECT_RANGE_HIGH");
      Entity me = plain();
      Entity theMonster = monster();

      TransitionSamples samples = sampleTransition(TRANSITION_SAMPLES, me, theMonster);

      assertFalse(
          samples.protectOnApproaches().isEmpty(),
          "index 2 should produce at least one ProtectOnApproach");
      for (Object[] args : samples.protectOnApproaches()) {
        float range = (Float) args[0];
        Entity target = (Entity) args[1];

        // U3
        assertTrue(range >= low && range <= high, "protect range out of range: " + range);
        assertSame(
            theMonster, target, "the target must be the result of randomMonsterOrMe(entity)");
        assertNotSame(me, target, "the target must be the monster, not the passed entity");
      }
    }

    /**
     * G4: index 3 produces a ProtectOnAttack with {@code randomMonsterOrMe(entity)} as its target.
     */
    @Test
    void createsProtectOnAttackWithMonsterTarget() {
      Entity me = plain();
      Entity theMonster = monster();

      TransitionSamples samples = sampleTransition(TRANSITION_SAMPLES, me, theMonster);

      assertFalse(
          samples.protectOnAttacks().isEmpty(),
          "index 3 should produce at least one ProtectOnAttack");
      for (Object[] args : samples.protectOnAttacks()) {
        Entity target = (Entity) args[0];

        assertSame(
            theMonster, target, "the target must be the result of randomMonsterOrMe(entity)");
        assertNotSame(me, target, "the target must be the monster, not the passed entity");
      }
    }
  }

  /**
   * Tests for {@link AIFactory#randomFightAI()}.
   *
   * <p>{@code randomFightAI()} randomly picks one of the supported fight behaviours ({@link
   * AIChaseBehaviour}, {@link AIRangeBehaviour}, {@link AIMeleeBehaviour}) and constructs it with
   * parameters drawn from fixed ranges; the two skill-using behaviours receive a {@link
   * FireballSkill} configured with {@link AIFactory#FIREBALL_COOL_DOWN}. The behaviours expose none
   * of their parameters, so {@link Mockito#mockConstruction(Class,
   * org.mockito.MockedConstruction.MockInitializer)} intercepts construction and captures the raw
   * arguments. The {@link FireballSkill} construction is intercepted as well, which both records
   * the cooldown argument and avoids building a real, asset-loading skill.
   *
   * <p>{@link AIMeleeBehaviour} extends {@link AIChaseBehaviour}, but construction mocking keys on
   * the exact instantiated type, so the per-type counts stay disjoint (verified by {@link
   * #returnsOnlySupportedFightAiTypes()}).
   */
  @Nested
  class RandomFightAI {

    /** Number of draws; large enough that each of the three branches occurs with certainty. */
    private static final int FIGHT_SAMPLES = 500;

    /**
     * The constructor arguments captured for each produced fight behaviour and skill.
     *
     * @param chaseBehaviours captured constructor arguments of every produced {@link
     *     AIChaseBehaviour}.
     * @param rangeBehaviours captured constructor arguments of every produced {@link
     *     AIRangeBehaviour}.
     * @param meleeBehaviours captured constructor arguments of every produced {@link
     *     AIMeleeBehaviour}.
     * @param fireballSkills captured constructor arguments of every produced {@link FireballSkill}.
     */
    private record FightSamples(
        List<Object[]> chaseBehaviours,
        List<Object[]> rangeBehaviours,
        List<Object[]> meleeBehaviours,
        List<Object[]> fireballSkills) {}

    /**
     * Calls {@link AIFactory#randomFightAI()} {@code iterations} times while intercepting the
     * construction of all three fight behaviours and the {@link FireballSkill}, recording their
     * constructor arguments. Also asserts on every draw that the returned behaviour is supported.
     *
     * @param iterations how often {@code randomFightAI()} is called.
     * @return the captured constructor arguments grouped by type.
     */
    private FightSamples sampleFightAi(final int iterations) {
      List<Object[]> chaseBehaviours = new ArrayList<>();
      List<Object[]> rangeBehaviours = new ArrayList<>();
      List<Object[]> meleeBehaviours = new ArrayList<>();
      List<Object[]> fireballSkills = new ArrayList<>();

      try (var chase =
              Mockito.mockConstruction(
                  AIChaseBehaviour.class,
                  (mock, context) -> chaseBehaviours.add(context.arguments().toArray()));
          var range =
              Mockito.mockConstruction(
                  AIRangeBehaviour.class,
                  (mock, context) -> rangeBehaviours.add(context.arguments().toArray()));
          var melee =
              Mockito.mockConstruction(
                  AIMeleeBehaviour.class,
                  (mock, context) -> meleeBehaviours.add(context.arguments().toArray()));
          var fireball =
              Mockito.mockConstruction(
                  FireballSkill.class,
                  (mock, context) -> fireballSkills.add(context.arguments().toArray()))) {

        for (int draw = 0; draw < iterations; draw++) {
          Consumer<Entity> fightAi = AIFactory.randomFightAI();

          assertTrue(
              fightAi instanceof AIChaseBehaviour
                  || fightAi instanceof AIRangeBehaviour
                  || fightAi instanceof AIMeleeBehaviour,
              "randomFightAI must only ever return a supported fight AI type");
        }
      }

      return new FightSamples(chaseBehaviours, rangeBehaviours, meleeBehaviours, fireballSkills);
    }

    /**
     * Asserts that every captured {@link FireballSkill} was built with {@link
     * AIFactory#FIREBALL_COOL_DOWN}.
     *
     * @param fireballSkills captured {@link FireballSkill} constructor arguments.
     */
    private void assertFireballCooldowns(final List<Object[]> fireballSkills) {
      assertFalse(fireballSkills.isEmpty(), "a FireballSkill should have been constructed");
      for (Object[] args : fireballSkills) {
        long cooldown = ((Number) args[1]).longValue();
        assertEquals(
            AIFactory.FIREBALL_COOL_DOWN,
            cooldown,
            "the FireballSkill must use FIREBALL_COOL_DOWN as its cooldown");
      }
    }

    /**
     * U1: every call produces exactly one of the three supported fight behaviours and nothing else.
     */
    @Test
    void returnsOnlySupportedFightAiTypes() {
      FightSamples samples = sampleFightAi(FIGHT_SAMPLES);

      int produced =
          samples.chaseBehaviours().size()
              + samples.rangeBehaviours().size()
              + samples.meleeBehaviours().size();
      assertEquals(
          FIGHT_SAMPLES,
          produced,
          "each call must construct exactly one AIChaseBehaviour, AIRangeBehaviour or "
              + "AIMeleeBehaviour");
    }

    /** G1 and U2: index 0 produces an AIChaseBehaviour with its range within bounds. */
    @Test
    void createsChaseBehaviourWithRangeInBounds() {
      float low = floatConstant("RUSH_RANGE_LOW");
      float high = floatConstant("RUSH_RANGE_HIGH");

      FightSamples samples = sampleFightAi(FIGHT_SAMPLES);

      assertFalse(
          samples.chaseBehaviours().isEmpty(),
          "index 0 should produce at least one AIChaseBehaviour");
      for (Object[] args : samples.chaseBehaviours()) {
        float chaseRange = (Float) args[0];

        // U2
        assertTrue(
            chaseRange >= low && chaseRange <= high, "chase range out of range: " + chaseRange);
      }
    }

    /**
     * G2 and U3&ndash;U5: index 1 produces an AIRangeBehaviour with attack range and distance
     * within bounds and a correctly configured FireballSkill.
     */
    @Test
    void createsRangeBehaviourWithParametersInBoundsAndFireball() {
      float attackLow = floatConstant("ATTACK_RANGE_LOW");
      float attackHigh = floatConstant("ATTACK_RANGE_HIGH");
      float distanceLow = floatConstant("DISTANCE_LOW");
      float distanceHigh = floatConstant("DISTANCE_HIGH");

      FightSamples samples = sampleFightAi(FIGHT_SAMPLES);

      assertFalse(
          samples.rangeBehaviours().isEmpty(),
          "index 1 should produce at least one AIRangeBehaviour");
      for (Object[] args : samples.rangeBehaviours()) {
        float attackRange = (Float) args[0];
        float distance = (Float) args[1];
        Object skill = args[2];

        // U3
        assertTrue(
            attackRange >= attackLow && attackRange <= attackHigh,
            "attack range out of range: " + attackRange);
        // U4
        assertTrue(
            distance >= distanceLow && distance <= distanceHigh,
            "distance out of range: " + distance);
        // U5
        assertTrue(skill instanceof FireballSkill, "the range behaviour must use a FireballSkill");
      }
      assertFireballCooldowns(samples.fireballSkills());
    }

    /**
     * G3 and U6&ndash;U8: index 2 produces an AIMeleeBehaviour with a chase range within bounds, an
     * attack range of exactly {@code 1f} and a correctly configured FireballSkill.
     */
    @Test
    void createsMeleeBehaviourWithParametersAndFireball() {
      float rushLow = floatConstant("RUSH_RANGE_LOW");
      float rushHigh = floatConstant("RUSH_RANGE_HIGH");

      FightSamples samples = sampleFightAi(FIGHT_SAMPLES);

      assertFalse(
          samples.meleeBehaviours().isEmpty(),
          "index 2 should produce at least one AIMeleeBehaviour");
      for (Object[] args : samples.meleeBehaviours()) {
        float chaseRange = (Float) args[0];
        float attackRange = (Float) args[1];
        Object skill = args[2];

        // U6
        assertTrue(
            chaseRange >= rushLow && chaseRange <= rushHigh,
            "chase range out of range: " + chaseRange);
        // U7
        assertEquals(1f, attackRange, "the melee attack range must be exactly 1f");
        // U8
        assertTrue(skill instanceof FireballSkill, "the melee behaviour must use a FireballSkill");
      }
      assertFireballCooldowns(samples.fireballSkills());
    }
  }

  /**
   * Tests for {@link AIFactory#randomAI(Entity)}.
   *
   * <p>{@code randomAI(entity)} builds an {@link AIComponent} from the results of {@link
   * AIFactory#randomFightAI()}, {@link AIFactory#randomIdleAI()} and {@link
   * AIFactory#randomTransition(Entity)}, without attaching the component to any entity.
   */
  @Nested
  class RandomAI {

    /** Number of draws for the null-safety check; large enough to exercise every branch. */
    private static final int AI_SAMPLES = 300;

    /**
     * G1: the produced {@link AIComponent} wraps exactly the references returned by the three
     * helper methods, and is not attached to the given entity.
     */
    @Test
    void composesComponentFromHelperResults() {
      Consumer<Entity> fightStub = ignored -> {};
      Consumer<Entity> idleStub = ignored -> {};
      Function<Entity, Boolean> transitionStub = ignored -> false;
      Entity entity = plain();

      AIComponent result;
      try (MockedStatic<AIFactory> factory =
          Mockito.mockStatic(AIFactory.class, Mockito.CALLS_REAL_METHODS)) {
        factory.when(AIFactory::randomFightAI).thenReturn(fightStub);
        factory.when(AIFactory::randomIdleAI).thenReturn(idleStub);
        factory.when(() -> AIFactory.randomTransition(entity)).thenReturn(transitionStub);

        result = AIFactory.randomAI(entity);
      }

      assertNotNull(result, "randomAI must return a non-null AIComponent");
      assertSame(
          fightStub,
          result.fightBehavior(),
          "the component must use the result of randomFightAI()");
      assertSame(
          idleStub, result.idleBehavior(), "the component must use the result of randomIdleAI()");
      assertSame(
          transitionStub,
          result.shouldFight(),
          "the component must use the result of randomTransition(entity)");
      assertFalse(
          entity.isPresent(AIComponent.class),
          "randomAI must not attach the produced component to the given entity");
    }

    /**
     * U1: across many draws, the produced component always carries a non-null, supported fight,
     * idle and transition behaviour, and is never attached to the given entity.
     *
     * <p>Construction of every concrete fight/idle/transition class is intercepted so the draws
     * need neither a running game loop (idle AIs read {@code Game.frameRate()} in their real
     * constructor) nor level state beyond what {@link TestGame#withEntities(Entity...)} provides
     * (the protect transitions resolve their target via {@code randomMonsterOrMe(entity)}, which
     * reads the level-entity stream).
     */
    @Test
    void neverProducesAnIncompleteComponent() {
      Entity entity = plain();

      try (var scope = TestGame.withEntities(entity);
          var chase = Mockito.mockConstruction(AIChaseBehaviour.class);
          var range = Mockito.mockConstruction(AIRangeBehaviour.class);
          var melee = Mockito.mockConstruction(AIMeleeBehaviour.class);
          var fireball = Mockito.mockConstruction(FireballSkill.class);
          var patrol = Mockito.mockConstruction(PatrolWalk.class);
          var radiusWalk = Mockito.mockConstruction(RadiusWalk.class);
          var staticRadiusWalk = Mockito.mockConstruction(StaticRadiusWalk.class);
          var rangeTransition = Mockito.mockConstruction(RangeTransition.class);
          var selfDefend = Mockito.mockConstruction(SelfDefendTransition.class);
          var protectApproach = Mockito.mockConstruction(ProtectOnApproach.class);
          var protectAttack = Mockito.mockConstruction(ProtectOnAttack.class)) {

        for (int draw = 0; draw < AI_SAMPLES; draw++) {
          AIComponent result = AIFactory.randomAI(entity);

          assertNotNull(result, "randomAI must never return null");

          Consumer<Entity> fight = result.fightBehavior();
          assertTrue(
              fight instanceof AIChaseBehaviour
                  || fight instanceof AIRangeBehaviour
                  || fight instanceof AIMeleeBehaviour,
              "the fight behaviour must never be missing or of an unsupported type");

          Consumer<Entity> idle = result.idleBehavior();
          assertTrue(
              idle instanceof PatrolWalk
                  || idle instanceof RadiusWalk
                  || idle instanceof StaticRadiusWalk,
              "the idle behaviour must never be missing or of an unsupported type");

          Function<Entity, Boolean> transition = result.shouldFight();
          assertTrue(
              transition instanceof RangeTransition
                  || transition instanceof SelfDefendTransition
                  || transition instanceof ProtectOnApproach
                  || transition instanceof ProtectOnAttack,
              "the transition behaviour must never be missing or of an unsupported type");

          assertFalse(
              entity.isPresent(AIComponent.class),
              "randomAI must not attach the produced component to the given entity");
        }
      }
    }
  }
}
