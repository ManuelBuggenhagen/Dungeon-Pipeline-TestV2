package contrib.systems;

import contrib.DefaultGameProvider;
import contrib.GameProvider;
import contrib.components.StaminaComponent;
import core.System;

/**
 * A system that restores stamina to all entities with an {@link StaminaComponent}.
 *
 * <p>The restoration amount is calculated based on the stamina regeneration rate defined in each
 * {@code StaminaComponent}. To ensure frame-rate-independent behavior, the per-second restoration
 * value is divided by the current game frame rate.
 */
public class StaminaRestoreSystem extends System {
  private final GameProvider game;

  /** Creates a new {@code StaminaRestoreSystem} with a DefaultGameProvider. */
  public StaminaRestoreSystem() {
    this(new DefaultGameProvider());
  }

  /**
   * Creates a new {@code StaminaRestoreSystem} with the given GameProvider.
   *
   * <p>This system processes all entities that contain an {@link StaminaComponent}.
   *
   * @param game The game provider to be used.
   */
  public StaminaRestoreSystem(GameProvider game) {
    super(StaminaComponent.class);
    this.game = game;
  }

  /**
   * Executes the stamina restoration for all entities that contain an {@link StaminaComponent}.
   *
   * <p>For each entity, the system restores an amount of stamina equal to:
   *
   * <pre>
   * restorePerSecond / Game.frameRate()
   * </pre>
   *
   * <p>ensuring smooth, frame-rate-independent regeneration.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .flatMap(e -> e.fetch(StaminaComponent.class).stream())
        .forEach(c -> c.restore(c.restorePerSecond() / game.frameRate()));
  }
}
