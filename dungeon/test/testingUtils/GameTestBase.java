package testingUtils;

import core.Component;
import core.Entity;
import core.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for tests that exercise real {@link Game} state (component and integration tests).
 *
 * <p>The {@link Game} is a static singleton, so its entity, system and level state leaks between
 * tests unless it is reset. Extending this class removes that boilerplate: the shared state is
 * cleared automatically before and after every test, and {@link #spawn(Component...)} creates and
 * registers entities in one line.
 *
 * <p>Use this base class when you want to test against the real ECS (for example a whole {@link
 * core.System}). For pure unit isolation of a single system, prefer {@link TestGame} instead, which
 * mocks the entity stream without touching global state.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class MySystemTest extends GameTestBase {
 *   @Test
 *   void heals() {
 *     Entity hero = spawn(new HealthComponent());
 *     Game.add(new MySystem());
 *     new MySystem().execute();
 *     assertEquals(..., hero.fetch(HealthComponent.class).orElseThrow()...);
 *   }
 * }
 * }</pre>
 */
public abstract class GameTestBase {

  /** Clears all {@link Game} state before each test so tests start from a clean world. */
  @BeforeEach
  protected final void setUpGameState() {
    resetGameState();
  }

  /** Clears all {@link Game} state after each test so nothing leaks into the next test. */
  @AfterEach
  protected final void tearDownGameState() {
    resetGameState();
  }

  private void resetGameState() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  /**
   * Creates an {@link Entity}, attaches the given components to it and registers it with the {@link
   * Game}.
   *
   * <p>After this call the entity is queryable through the {@link Game} entity streams (for example
   * {@link Game#levelEntities(java.util.Set)}), so systems will process it.
   *
   * @param components the components to attach to the new entity
   * @return the freshly created and registered entity
   */
  protected final Entity spawn(final Component... components) {
    final Entity entity = new Entity();
    for (final Component component : components) {
      entity.add(component);
    }
    Game.add(entity);
    return entity;
  }
}
