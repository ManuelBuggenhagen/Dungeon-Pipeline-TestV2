package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import contrib.components.HealthComponent;
import core.Entity;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link HealthComponent#triggerOnDeath}. */
public class TriggerOnDeathTest {

  private Entity entity;

  private HealthComponent healthComponent;

  @BeforeEach
  void setup() {
    entity = mock(Entity.class);
  }

  @Test
  void triggerOnDeath_entitySetAndOnDeathCallbackSet_callbackExecutedOnEntity() {
    AtomicReference<Entity> received = new AtomicReference<>();
    Consumer<Entity> callback = received::set;
    healthComponent = new HealthComponent(1, callback);

    healthComponent.triggerOnDeath(entity);

    assertSame(entity, received.get());
  }

  @Test
  void triggerOnDeath_entityIsNullAndOnDeathCallbackSet_callbackExecutedOnNull() {
    AtomicReference<Entity> received = new AtomicReference<>();
    Consumer<Entity> callback = received::set;
    healthComponent = new HealthComponent(1, callback);

    healthComponent.triggerOnDeath(null);

    assertNull(received.get());
  }

  @Test
  void triggerOnDeath_onDeathCallbackIsNull_throwsNullPointerException() {
    healthComponent = new HealthComponent(1, null);

    assertThrows(NullPointerException.class, () -> healthComponent.triggerOnDeath(entity));
  }
}
