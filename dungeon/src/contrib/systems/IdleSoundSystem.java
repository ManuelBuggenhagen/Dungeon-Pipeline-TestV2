package contrib.systems;

import contrib.DefaultGameProvider;
import contrib.GameProvider;
import contrib.components.IdleSoundComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.sound.SoundSpec;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.Random;

/**
 * Works on Entities that contain the {@link IdleSoundComponent} and plays the stored sound effect
 * randomly.
 *
 * <p>Use this if you want to add some white noise monster sounds to your game.
 *
 * <p>Note: The chance that the sound is played is very low, so it shouldn't be too much noise.
 */
public final class IdleSoundSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(IdleSoundSystem.class);

  private static final Random RANDOM = new Random();
  private static final float DISTANCE_THRESHOLD = 10.0f;
  private static final float CHANCE_TO_PLAY_SOUND = 0.001f;

  private final GameProvider game;

  /** Creates a new IdleSoundSystem with the DefaultGameProvider. */
  public IdleSoundSystem() {
    this(new DefaultGameProvider());
  }

  /**
   * Creates a new IdleSoundSystem with the given GameProvider.
   *
   * @param gameProvider The game provider to be used.
   */
  public IdleSoundSystem(GameProvider gameProvider) {
    super(IdleSoundComponent.class);
    this.game = gameProvider;
  }

  private static boolean isEntityNearby(Point playerPos, Entity entity) {
    Point entityPosition =
        entity.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);

    if (playerPos == null || entityPosition == null) {
      return false;
    }

    double distance = playerPos.distance(entityPosition);

    return distance < DISTANCE_THRESHOLD;
  }

  @Override
  public void execute() {
    Point playerPos = game.player().flatMap(game::positionOf).orElse(null);
    if (playerPos == null) {
      LOGGER.debug("No player position found, skipping IdleSoundSystem execution.");
      return;
    }

    filteredEntityStream(IdleSoundComponent.class)
        .filter(e -> isEntityNearby(playerPos, e))
        .forEach(
            e ->
                playSound(
                    e,
                    e.fetch(IdleSoundComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(e, IdleSoundComponent.class))));
  }

  private void playSound(final Entity idlingEntity, final IdleSoundComponent component) {
    if (RANDOM.nextFloat(0f, 1f) < CHANCE_TO_PLAY_SOUND) {
      game.audio().playOnEntity(idlingEntity, SoundSpec.builder(component.soundEffectId()));
    }
  }
}
