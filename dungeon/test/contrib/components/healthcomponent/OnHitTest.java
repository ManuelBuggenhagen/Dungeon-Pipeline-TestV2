package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import core.Entity;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for {@link HealthComponent#onHit(BiConsumer)}. */
@SuppressWarnings("unchecked")
public class OnHitTest {

  HealthComponent healthComponent;

  Damage damage;

  BiConsumer<Entity, Damage> callback;

  @BeforeEach
  void setup() {
    healthComponent = new HealthComponent();
    damage = Mockito.mock(Damage.class);
    callback = Mockito.mock(BiConsumer.class);
  }

  @Test
  void onHit_passingValidCallbackAndNoCallbackSet_callbackSuccessfullySet() {
    healthComponent.onHit(callback);
    healthComponent.receiveHit(damage);

    verify(callback, times(1)).accept(null, damage);
  }

  @Test
  void onHit_passingValidCallbackAndCallbackSet_callbackSuccessfullyOverwritten() {
    BiConsumer<Entity, Damage> overwriteCallback = Mockito.mock(BiConsumer.class);
    healthComponent.onHit(callback);
    healthComponent.onHit(overwriteCallback);

    healthComponent.receiveHit(damage);

    verify(callback, times(0)).accept(null, damage);
    verify(overwriteCallback, times(1)).accept(null, damage);
  }

  @Test
  void onHit_passingNull_callbackSetToNull() {
    healthComponent.onHit(null);

    assertThrows(NullPointerException.class, () -> healthComponent.receiveHit(damage));
  }
}
