package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import contrib.components.HealthComponent;
import core.Entity;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for {@link HealthComponent#onDeath(Consumer)}. */
@SuppressWarnings("unchecked")
public class OnDeathTest {

  HealthComponent healthComponent;

  Entity entity;

  Consumer<Entity> callback;

  @BeforeEach
  void setup() {
    healthComponent = new HealthComponent();
    entity = Mockito.mock(Entity.class);
    callback = Mockito.mock(Consumer.class);
  }

  @Test
  void onDeath_passingValidCallbackAndNoCallbackSet_callbackSuccessfullySet() {
    healthComponent.onDeath(callback);
    healthComponent.triggerOnDeath(entity);

    verify(callback, times(1)).accept(entity);
  }

  @Test
  void onDeath_passingValidCallbackAndCallbackSet_callbackSuccessfullyOverwritten() {
    Consumer<Entity> overwriteCallback = Mockito.mock(Consumer.class);
    healthComponent.onDeath(callback);
    healthComponent.onDeath(overwriteCallback);

    healthComponent.triggerOnDeath(entity);

    verify(callback, times(0)).accept(entity);
    verify(overwriteCallback, times(1)).accept(entity);
  }

  @Test
  void onDeath_passingNull_callbackSetToNull() {
    healthComponent.onDeath(null);
    assertThrows(NullPointerException.class, () -> healthComponent.triggerOnDeath(entity));
  }
}
