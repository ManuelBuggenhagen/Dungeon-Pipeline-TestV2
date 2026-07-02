package contrib.systems;

import contrib.components.HealthComponent;
import core.FakeGame;
import core.components.DrawComponent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Fake HealthSystemClass for test purposes */
public class MockHealthSystem extends HealthSystem {

  private final FakeGame game;

  public MockHealthSystem(FakeGame game) {
    this.game = game;
  }

  public HSData applyDamagePublic(HSData data) {
    return super.applyDamage(data);
  }

  public int calculateDamagePublic(HSData data) {
    return super.calculateDamage(data);
  }

  public void triggerOnDeathPublic(HSData data) {
    super.triggerOnDeath(data);
  }

  @Override
  public void execute() {

    Map<Boolean, List<HSData>> deadOrAlive =
        game.allEntities()
            .filter(e -> e.isPresent(FakeHealthComponent.class))
            .filter(e -> e.isPresent(FakeDrawComponent.class))
            .map(
                e ->
                    new HSData(
                        e,
                        e.fetch(HealthComponent.class).orElseThrow(),
                        e.fetch(DrawComponent.class).orElseThrow()))
            .collect(Collectors.partitioningBy(hsd -> hsd.hc().isDead()));

    deadOrAlive.get(false).forEach(this::applyDamagePublic);

    deadOrAlive.get(true).stream()
        .map(this::activateDeathAnimation)
        .filter(this::isDeathAnimationFinished)
        .filter(hsd -> !hsd.hc().alreadyDead())
        .forEach(this::triggerOnDeathPublic);
  }
}
