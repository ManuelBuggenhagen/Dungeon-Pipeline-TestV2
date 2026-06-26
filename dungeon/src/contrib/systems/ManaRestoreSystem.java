package contrib.systems;

import contrib.DefaultGameProvider;
import contrib.GameProvider;
import contrib.components.ManaComponent;
import core.System;

/**
 * A system that restores mana to all entities with a {@link ManaComponent}.
 *
 * <p>The restoration is based on the mana regeneration rate defined in each {@code ManaComponent}.
 * The system ensures that restoration is applied frame-rate-independently by dividing the
 * per-second restoration rate by the current game frame rate.
 */
public class ManaRestoreSystem extends System {

  private final GameProvider game;

  /** Creates a new {@code ManaRestoreSystem} with a DefaultGameProvider. */
  public ManaRestoreSystem() {
    this(new DefaultGameProvider());
  }

  /**
   * Creates a new {@code ManaRestoreSystem} with the given GameProvider.
   *
   * @param game The game provider to be used.
   */
  public ManaRestoreSystem(GameProvider game) {
    super(ManaComponent.class);
    this.game = game;
  }

  /**
   * Executes the mana restoration for all entities that contain a {@link ManaComponent}.
   *
   * <p>For each entity, the system fetches the component and restores an amount of mana equal to:
   *
   * <pre>
   * restorePerSecond / Game.frameRate()
   * </pre>
   *
   * <p>This ensures a smooth, frame-rate-independent regeneration of mana.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .flatMap(e -> e.fetch(ManaComponent.class).stream())
        .forEach(m -> m.restore(m.restorePerSecond() / game.frameRate()));
  }
}
