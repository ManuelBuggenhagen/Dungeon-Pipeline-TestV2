package testingUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Entity;
import core.System;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link TestGame}. */
class TestGameTest {

  /** A trivial system that records the entities it is given on {@code execute()}. */
  private static final class CollectingSystem extends System {
    private List<Entity> seen = List.of();

    @Override
    public void execute() {
      seen = filteredEntityStream().toList();
    }
  }

  @Test
  void systemSeesExactlyTheMockedEntities() {
    Entity a = new Entity("a");
    Entity b = new Entity("b");
    CollectingSystem system = new CollectingSystem();

    try (var scope = TestGame.withEntities(a, b)) {
      system.execute();
    }

    assertEquals(
        List.of(a, b),
        system.seen,
        "the system's filtered entity stream should yield exactly the mocked entities");
  }
}
