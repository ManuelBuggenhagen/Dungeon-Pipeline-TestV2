package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.FakeGame;
import core.components.DrawComponent;
import org.junit.jupiter.api.Test;

/** test class */
public class HealthSystemTest {
  private final FakeGame game = new FakeGame();
  private final MockHealthSystem system = new MockHealthSystem(game);
  private final Entity entity = new Entity("test");

  @Test
  void calculateDamage_singleDamage_returnsDamage() {

    HealthComponent hc = new HealthComponent(100);
    hc.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

    DrawComponent dc = mock(DrawComponent.class);

    HealthSystem.HSData data = new HealthSystem.HSData(entity, hc, dc);

    assertEquals(10, system.calculateDamagePublic(data));
  }

  @Test
  void calculateDamage_multipleDamage_returnsSum() {

    HealthComponent hc = new HealthComponent(100);
    hc.receiveHit(new Damage(10, DamageType.PHYSICAL, null));
    hc.receiveHit(new Damage(15, DamageType.FIRE, null));

    DrawComponent dc = mock(DrawComponent.class);

    HealthSystem.HSData data = new HealthSystem.HSData(entity, hc, dc);

    assertEquals(25, system.calculateDamagePublic(data));
  }

  @Test
  void calculateDamage_noDamage_returnsZero() {

    HealthComponent hc = new HealthComponent(100);

    DrawComponent dc = mock(DrawComponent.class);

    HealthSystem.HSData data = new HealthSystem.HSData(entity, hc, dc);

    assertEquals(0, system.calculateDamagePublic(data));
  }

  @Test
  void calculateDamage_positiveAndNegativeDamage_returnsSum() {

    HealthComponent hc = new HealthComponent(100);

    hc.receiveHit(new Damage(20, DamageType.PHYSICAL, null));
    hc.receiveHit(new Damage(-5, DamageType.FIRE, null));

    DrawComponent dc = mock(DrawComponent.class);

    HealthSystem.HSData data = new HealthSystem.HSData(entity, hc, dc);

    assertEquals(15, system.calculateDamagePublic(data));
  }

  @Test
  void calculateDamage_nullHSData_throwsException() {

    assertThrows(NullPointerException.class, () -> system.calculateDamagePublic(null));
  }
}
