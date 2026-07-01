package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link HealthComponent#receiveHit(Damage)}. */
public class ReceiveHitTest {

  HealthComponent healthComponent;
  Damage damage;
  DamageType damageType;
  Entity entity;

  BiConsumer<Entity, Damage> onHitCallback;
  AtomicReference<Entity> callbackEntity;
  AtomicReference<Damage> callbackDamage;

  @BeforeEach
  void setup() {
    healthComponent = new HealthComponent();
    entity = mock(Entity.class);

    damage = mock(Damage.class);
    damageType = mock(DamageType.class);

    when(damage.damageType()).thenReturn(damageType);
    when(damage.damageAmount()).thenReturn(0);

    callbackEntity = new AtomicReference<>();
    callbackDamage = new AtomicReference<>();

    onHitCallback =
        (entity, damage) -> {
          callbackEntity.set(entity);
          callbackDamage.set(damage);
        };
  }

  @Test
  void receiveHit_callbackSetAndDamageHasCause_callbackExecutedAndHitProcessed() {
    when(damage.cause()).thenReturn(entity);
    healthComponent.onHit(onHitCallback);

    healthComponent.receiveHit(damage);

    assertSame(entity, callbackEntity.get());
    assertSame(damage, callbackDamage.get());
    assertEquals(0, healthComponent.calculateDamageOf(damageType));
    assertEquals(Optional.of(entity), healthComponent.lastDamageCause());
  }

  @Test
  void receiveHit_callbackSetAndDamageCauseIsNull_lastCauseRemainsUnchanged() {
    Entity firstCause = mock(Entity.class);
    when(damage.cause()).thenReturn(firstCause);
    healthComponent.onHit(onHitCallback);
    healthComponent.receiveHit(damage);

    when(damage.cause()).thenReturn(null);
    healthComponent.receiveHit(damage);

    assertNull(callbackEntity.get());
    assertSame(damage, callbackDamage.get());
    assertEquals(0, healthComponent.calculateDamageOf(damageType));
    assertEquals(Optional.of(firstCause), healthComponent.lastDamageCause());
  }

  @Test
  void receiveHit_damageIsNull_accessToDamageCauseThrowsNullPointerException() {
    healthComponent.onHit(onHitCallback);

    assertThrows(NullPointerException.class, () -> healthComponent.receiveHit(null));
  }

  @Test
  void receiveHit_onHitCallbackIsNull_throwsNullPointerException() {
    healthComponent.onHit(null);

    assertThrows(NullPointerException.class, () -> healthComponent.receiveHit(damage));
  }
}
