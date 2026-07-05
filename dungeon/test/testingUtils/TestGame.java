package testingUtils;

import core.Component;
import core.Entity;
import core.Game;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Helper for unit-testing a single {@link core.System} in full isolation ("TestGame Stream
 * mocken").
 *
 * <p>Every system reads its entities through {@link core.System#filteredEntityStream()}, which
 * delegates to the static call {@link Game#levelEntities(java.util.Set)}. Because that call is
 * static and {@code final}, it cannot be overridden in a test subclass. This helper stubs it with
 * Mockito's static mocking, so a system's {@code execute()} can be driven against a controlled set
 * of entities without a running game, a loaded level or headless graphics.
 *
 * <p>The unfiltered overload {@link Game#levelEntities()} is stubbed with the same entities. That
 * overload does not delegate to {@link Game#levelEntities(java.util.Set)}, so under a static {@link
 * Game} mock it would otherwise return {@code null}. Stubbing it lets helpers and factories that
 * read <em>all</em> level entities (for example {@link contrib.entities.AIFactory}) be unit-tested
 * the same way as systems.
 *
 * <p>Prefer this over {@link GameTestBase} when you want a true unit test that isolates the logic
 * of the system under test and mocks everything else. Use {@link GameTestBase} when you
 * deliberately want to exercise the real ECS.
 *
 * <p>Example:
 *
 * <pre>{@code
 * Entity hero = new Entity();
 * hero.add(new HealthComponent());
 * try (var scope = TestGame.withEntities(hero)) {
 *   new HealthSystem().execute();
 * }
 * assertEquals(..., hero.fetch(HealthComponent.class).orElseThrow()...);
 * }</pre>
 */
public final class TestGame {

  private TestGame() {}

  /**
   * Opens a scope in which every {@link core.System} sees exactly the given entities.
   *
   * <p>Both {@link Game#levelEntities()} and {@link Game#levelEntities(java.util.Set)} are stubbed
   * to yield the given entities; each call returns a fresh stream, so the code under test may query
   * the stream more than once. The returned {@link MockedStatic} must be closed to restore normal
   * {@link Game} behaviour; use it in a try-with-resources block. While the scope is open, all
   * other static {@link Game} methods return their Mockito defaults, so the code under test should
   * only rely on its entity stream.
   *
   * @param entities the entities the mocked entity stream should yield, in encounter order
   * @return the active static {@link Game} mock; close it to end the isolation scope
   */
  public static MockedStatic<Game> withEntities(final Entity... entities) {
    final List<Entity> snapshot = List.of(entities);
    final MockedStatic<Game> mocked = Mockito.mockStatic(Game.class);
    mocked
        .when(() -> Game.levelEntities(Mockito.<Class<? extends Component>>anySet()))
        .thenAnswer(invocation -> snapshot.stream());
    mocked.when(() -> Game.levelEntities()).thenAnswer(invocation -> snapshot.stream());
    return mocked;
  }
}
