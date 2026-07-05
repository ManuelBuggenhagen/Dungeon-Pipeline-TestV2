package testingUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Component;
import core.Entity;
import core.Game;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link GameTestBase}. */
class GameTestBaseTest extends GameTestBase {

  /** A minimal marker component used to filter for the spawned entity. */
  private static final class DummyComponent implements Component {}

  @Test
  void spawnAttachesComponentsAndRegistersEntity() {
    DummyComponent component = new DummyComponent();

    Entity entity = spawn(component);

    assertEquals(
        component,
        entity.fetch(DummyComponent.class).orElseThrow(),
        "spawn should attach the given component to the entity");
    assertTrue(
        Game.levelEntities(Set.of(DummyComponent.class)).anyMatch(e -> e == entity),
        "spawn should register the entity so it is queryable by its component filter");
  }
}
