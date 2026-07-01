package contrib.components.healthcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link HealthComponent#calculateDamageOf(DamageType)} . */
@SuppressWarnings("unchecked")
public class CalculateDamageOfTest {

  HealthComponent healthComponent;
  Damage damage;

  @BeforeEach
  void setup() {
    healthComponent = new HealthComponent();
    damage = mock(Damage.class);
    BiConsumer<Entity, Damage> callable = mock(BiConsumer.class);
    healthComponent.onHit(callable);
  }

  @Test
  void calculateDamageOf_oneValidEntry_valueOfEntryReturned() {
    DamageType damageType = mock(DamageType.class);
    when(damage.damageType()).thenReturn(damageType);
    when(damage.damageAmount()).thenReturn(69);
    healthComponent.receiveHit(damage);

    int result = healthComponent.calculateDamageOf(damageType);

    assertEquals(69, result);
  }

  @Test
  void calculateDamageOf_multipleValidEntries_sumOfEntriesReturned() {
    DamageType damageType = mock(DamageType.class);
    when(damage.damageType()).thenReturn(damageType);
    when(damage.damageAmount()).thenReturn(69);
    healthComponent.receiveHit(damage);
    healthComponent.receiveHit(damage);

    int result = healthComponent.calculateDamageOf(damageType);

    assertEquals(138, result);
  }

  @Test
  void calculateDamageOf_noEntries_zeroReturned() {
    DamageType damageType = mock(DamageType.class);

    int result = healthComponent.calculateDamageOf(damageType);

    assertEquals(0, result);
  }

  @Test
  void calculateDamageOf_noValidEntries_zeroReturned() {
    DamageType damageType = mock(DamageType.class);
    when(damage.damageType()).thenReturn(damageType);
    when(damage.damageAmount()).thenReturn(69);
    healthComponent.receiveHit(damage);
    DamageType damageType2 = mock(DamageType.class);

    int result = healthComponent.calculateDamageOf(damageType2);

    assertEquals(0, result);
  }

  @Test
  void calculateDamageOf_passedNull_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> healthComponent.calculateDamageOf(null));
  }
}
