package contrib.systems;

import contrib.DefaultGameProvider;
import contrib.GameProvider;
import contrib.utils.level.ITickable;
import core.System;
import core.level.elements.ILevel;

/**
 * The LevelTickSystem is responsible for ticking the current level. It checks if the current level
 * has changed and calls the onTick method of the current level if it implements the ITickable
 * interface.
 *
 * @see ITickable
 * @see core.level.DungeonLevel DungeonLevel
 */
public class LevelTickSystem extends System {
  private final GameProvider game;

  /** Creates a new LevelTickSystem with a DefaultGameProvider. */
  public LevelTickSystem() {
    this(new DefaultGameProvider());
  }

  /**
   * Creates a new LevelTickSystem with the given GameProvider.
   *
   * @param game The game provider to be used.
   */
  public LevelTickSystem(GameProvider game) {
    super(AuthoritativeSide.BOTH);
    this.game = game;
  }

  /** The current level of the game. */
  private ILevel currentLevel = null;

  @Override
  public void execute() {
    if (game.currentLevel().orElse(null) instanceof ITickable tickable) {
      tickable.onTick(currentLevel != game.currentLevel().orElse(null));
    }
    if (currentLevel != game.currentLevel().orElse(null)) {
      this.currentLevel = game.currentLevel().orElse(null);
    }
  }

  @Override
  public void stop() {
    // Cant be stopped
  }
}
