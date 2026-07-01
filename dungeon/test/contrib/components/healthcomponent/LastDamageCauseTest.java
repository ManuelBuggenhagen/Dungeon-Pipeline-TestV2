package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import core.Entity;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link HealthComponent#receiveHit(Damage)}. */
@SuppressWarnings("unchecked")
public class LastDamageCauseTest {

  HealthComponent healthComponent;

  @BeforeEach
  void setup() {
    healthComponent = new HealthComponent();
    BiConsumer<Entity, Damage> callable = mock(BiConsumer.class);
    healthComponent.onHit(callable);
  }

  @Test
  void lastDamageCause_lastCauseNotNull_returnsLastCause() {
    Damage damage = mock(Damage.class);
    Entity firstCause = mock(Entity.class);
    when(damage.cause()).thenReturn(firstCause);
    healthComponent.receiveHit(damage);
    Entity secondCause = mock(Entity.class);

    when(damage.cause()).thenReturn(secondCause);
    healthComponent.receiveHit(damage);

    assertEquals(Optional.of(secondCause), healthComponent.lastDamageCause());
  }

  @Test
  void lastDamageCause_lastCauseIsNull_returnedOptionalIsEmpty() {

    assertEquals(Optional.empty(), healthComponent.lastDamageCause());
  }
}
