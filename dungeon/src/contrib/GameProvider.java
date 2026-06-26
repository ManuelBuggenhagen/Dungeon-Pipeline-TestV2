package contrib;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.Component;
import core.Entity;
import core.System;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.network.handler.INetworkHandler;
import core.sound.AudioApi;
import core.sound.player.ISoundPlayer;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Vollständige Schnittstelle für alle Kernfunktionen des Spiels. Entkoppelt Systeme und Komponenten
 * von der statischen Game-Klasse.
 */
public interface GameProvider {

  // --- Lifecycle & Window ---

  /** Starts the game loop and initializes necessary components. */
  void run();

  /** Exits the game cleanly without specifying a reason. */
  void exit();

  /**
   * Exits the game cleanly with a specific reason.
   *
   * @param reason The reason for exiting the game.
   */
  void exit(String reason);

  /**
   * Gets the current window width.
   *
   * @return The window width.
   */
  int windowWidth();

  /**
   * Sets the new window width.
   *
   * @param windowWidth The new window width.
   */
  void windowWidth(int windowWidth);

  /**
   * Gets the current window height.
   *
   * @return The window height.
   */
  int windowHeight();

  /**
   * Sets the new window height.
   *
   * @param windowHeight The new window height.
   */
  void windowHeight(int windowHeight);

  /**
   * Gets the target frame rate.
   *
   * @return The target frame rate.
   */
  int frameRate();

  /**
   * Sets the target frame rate.
   *
   * @param frameRate The target frame rate.
   */
  void frameRate(int frameRate);

  /**
   * Checks if the window is resizeable.
   *
   * @return True if the window is resizeable, false otherwise.
   */
  boolean resizeable();

  /**
   * Sets whether the window should be resizeable.
   *
   * @param resizeable True to make the window resizeable, false otherwise.
   */
  void resizeable(boolean resizeable);

  /**
   * Gets the current window title.
   *
   * @return The window title.
   */
  String windowTitle();

  /**
   * Sets the current window title.
   *
   * @param newTitle The new window title.
   */
  void windowTitle(final String newTitle);

  /**
   * Gets the path to the game logo.
   *
   * @return The path to the game logo.
   */
  IPath logoPath();

  /**
   * Sets the path to the game logo.
   *
   * @param logoPath The new path to the game logo.
   */
  void logoPath(IPath logoPath);

  /**
   * Checks if the game is running in headless mode (without graphical output).
   *
   * @return True if headless, false otherwise.
   */
  boolean isHeadless();

  /**
   * Gets the current game tick.
   *
   * @return The current game tick.
   */
  int currentTick();

  // --- Configuration & Settings ---

  /**
   * Enables or disables the game audio.
   *
   * @param disableAudio True to disable audio, false to enable.
   */
  void disableAudio(boolean disableAudio);

  /**
   * Enables or disables the check pattern on the floor.
   *
   * @param enabled True to enable the check pattern, false to disable.
   */
  void enableCheckPattern(boolean enabled);

  /**
   * Checks if the check pattern is currently enabled.
   *
   * @return True if enabled, false otherwise.
   */
  boolean isCheckPatternEnabled();

  /**
   * Loads the configuration file.
   *
   * @param path The path to the config file.
   * @param keyboardConfigClass The class array containing keyboard configuration.
   * @throws IOException If the config file cannot be read.
   */
  void loadConfig(final IPath path, final Class<?>... keyboardConfigClass) throws IOException;

  /**
   * Sets the custom callback that is executed every frame.
   *
   * @param userOnFrame The function to execute.
   */
  void userOnFrame(final IVoidFunction userOnFrame);

  /**
   * Sets the custom callback that is executed during game setup.
   *
   * @param userOnSetup The function to execute.
   */
  void userOnSetup(final IVoidFunction userOnSetup);

  /**
   * Sets the custom callback that is executed when a level loads.
   *
   * @param userOnLevelLoad The function to execute.
   */
  void userOnLevelLoad(final Consumer<Boolean> userOnLevelLoad);

  // --- Networking ---

  /**
   * Checks if the game is running as a multiplayer client.
   *
   * @return True if it is a multiplayer client, false otherwise.
   */
  boolean isMultiplayerClient();

  /** Initializes the network connections. */
  void initializeNetwork();

  /**
   * Gets the active network handler.
   *
   * @return The network handler.
   */
  INetworkHandler network();

  // --- UI ---

  /**
   * Gets the active UI stage.
   *
   * @return An Optional containing the stage, if present.
   */
  Optional<Stage> stage();

  // --- Entity & System Management (ECS) ---

  /**
   * Adds an entity to the game.
   *
   * @param entity The entity to add.
   */
  void add(final Entity entity);

  /**
   * Removes an entity from the game.
   *
   * @param entity The entity to remove.
   */
  void remove(final Entity entity);

  /** Removes all entities from the game. */
  void removeAllEntities();

  /**
   * Adds a system to the game.
   *
   * @param system The system to add.
   * @return An Optional containing the previous system of the same class, if any.
   */
  Optional<System> add(final System system);

  /**
   * Removes a system of the given class.
   *
   * @param system The class of the system to remove.
   */
  void remove(final Class<? extends System> system);

  /** Removes all systems from the game. */
  void removeAllSystems();

  /**
   * Gets a map of all currently registered systems.
   *
   * @return The map of systems.
   */
  Map<Class<? extends System>, System> systems();

  /**
   * Executes a given action on a specific system, if it exists.
   *
   * @param s The class of the system.
   * @param c The action to execute.
   * @param <T> The type of the system.
   */
  <T extends System> void system(Class<T> s, Consumer<T> c);

  /**
   * Gets a stream of all active entities in the game.
   *
   * @return A stream of all entities.
   */
  Stream<Entity> allEntities();

  /**
   * Gets a stream of all entities currently present in the level.
   *
   * @return A stream of level entities.
   */
  Stream<Entity> levelEntities();

  /**
   * Gets a stream of level entities associated with a specific system.
   *
   * @param system The system to filter for.
   * @return A stream of entities for the system.
   */
  Stream<Entity> levelEntities(final System system);

  /**
   * Gets a stream of level entities matching a specific set of components.
   *
   * @param filter The component filter set.
   * @return A stream of filtered entities.
   */
  Stream<Entity> levelEntities(final Set<Class<? extends Component>> filter);

  /**
   * Gets the main player entity.
   *
   * @return An Optional containing the player entity, if present.
   */
  Optional<Entity> player();

  /**
   * Gets a stream of all player entities in the game.
   *
   * @return A stream of all player entities.
   */
  Stream<Entity> allPlayers();

  /**
   * Finds an entity by its unique ID.
   *
   * @param entityId The ID to search for.
   * @return An Optional containing the entity, if found.
   */
  Optional<Entity> findEntityById(int entityId);

  /**
   * Finds an entity across the entire game that contains a specific component instance.
   *
   * @param component The component instance.
   * @return An Optional containing the entity, if found.
   */
  Optional<Entity> findInAll(final Component component);

  /**
   * Finds an entity within the current level that contains a specific component instance.
   *
   * @param component The component instance.
   * @return An Optional containing the entity, if found.
   */
  Optional<Entity> findInLevel(final Component component);

  /**
   * Checks if an entity exists within the current level.
   *
   * @param entity The entity to check.
   * @return True if the entity exists in the level, false otherwise.
   */
  boolean existInLevel(Entity entity);

  /**
   * Checks if an entity exists anywhere in the game.
   *
   * @param entity The entity to check.
   * @return True if the entity exists, false otherwise.
   */
  boolean existInAll(final Entity entity);

  // --- Level & Tiles ---

  /**
   * Gets the current level.
   *
   * @return An Optional containing the current level, if loaded.
   */
  Optional<ILevel> currentLevel();

  /**
   * Sets the current level.
   *
   * @param level The new level to set.
   */
  void currentLevel(final ILevel level);

  /**
   * Gets the tile at the specified point.
   *
   * @param point The point to check.
   * @return An Optional containing the tile, if present.
   */
  Optional<Tile> tileAt(final Point point);

  /**
   * Gets the tile at the specified coordinate.
   *
   * @param coordinate The coordinate to check.
   * @return An Optional containing the tile, if present.
   */
  Optional<Tile> tileAt(final Coordinate coordinate);

  /**
   * Gets the tile in a specific direction from a coordinate.
   *
   * @param coordinate The starting coordinate.
   * @param direction The direction to look.
   * @return An Optional containing the neighboring tile, if present.
   */
  Optional<Tile> tileAt(final Coordinate coordinate, Direction direction);

  /**
   * Gets the tile in a specific direction from a point.
   *
   * @param point The starting point.
   * @param direction The direction to look.
   * @return An Optional containing the neighboring tile, if present.
   */
  Optional<Tile> tileAt(final Point point, Direction direction);

  /**
   * Gets the tile currently occupied by the specified entity.
   *
   * @param entity The entity to check.
   * @return An Optional containing the tile, if present.
   */
  Optional<Tile> tileAtEntity(final Entity entity);

  /**
   * Gets a random tile from the current level.
   *
   * @return An Optional containing the random tile, if present.
   */
  Optional<Tile> randomTile();

  /**
   * Gets a random tile of a specific type from the current level.
   *
   * @param elementType The required tile type.
   * @return An Optional containing the random tile, if present.
   */
  Optional<Tile> randomTile(final LevelElement elementType);

  /**
   * Gets the position of a random tile.
   *
   * @return An Optional containing the random tile point, if present.
   */
  Optional<Point> randomTilePoint();

  /**
   * Gets the position of a random tile of a specific type.
   *
   * @param elementTyp The required tile type.
   * @return An Optional containing the random tile point, if present.
   */
  Optional<Point> randomTilePoint(final LevelElement elementTyp);

  /**
   * Gets all adjacent neighbors of a given tile.
   *
   * @param tile The center tile.
   * @return A set of neighbor tiles.
   */
  Set<Tile> neighbours(final Tile tile);

  /**
   * Gets the designated start tile of the level.
   *
   * @return An Optional containing the start tile, if present.
   */
  Optional<Tile> startTile();

  /**
   * Gets the designated end tile of the level.
   *
   * @return An Optional containing the end tile, if present.
   * @deprecated Use {@link #endTiles()} instead, as levels may have multiple exits.
   */
  @Deprecated
  Optional<Tile> endTile();

  /**
   * Gets all designated exit tiles of the level.
   *
   * @return A set of all exit tiles.
   */
  Set<ExitTile> endTiles();

  /**
   * Gets all tiles present in the current level.
   *
   * @return A set of all tiles.
   */
  Set<Tile> allTiles();

  /**
   * Gets all tiles that match the given filter rule.
   *
   * @param filterRule The predicate used for filtering tiles.
   * @return A set of matching tiles.
   */
  Set<Tile> allTiles(Predicate<Tile> filterRule);

  /**
   * Gets all tiles of a specific element type.
   *
   * @param elementTyp The required tile type.
   * @return A set of matching tiles.
   */
  Set<Tile> allTiles(final LevelElement elementTyp);

  /**
   * Gets all currently free tiles (unoccupied and accessible).
   *
   * @return A set of all free tiles.
   */
  Set<Tile> allFreeTiles();

  /**
   * Gets a single random free tile.
   *
   * @return An Optional containing a free tile, if available.
   */
  Optional<Tile> freeTile();

  /**
   * Gets the position of a random free tile.
   *
   * @return An Optional containing the position, if available.
   */
  Optional<Point> freePosition();

  /**
   * Checks if a specific tile is free.
   *
   * @param tile The tile to check.
   * @return True if the tile is free, false otherwise.
   */
  boolean isFreeTile(Tile tile);

  /**
   * Gets a list of all accessible tiles within a given radius around a center point.
   *
   * @param center The center point.
   * @param radius The search radius.
   * @return A list of accessible tiles.
   */
  List<Tile> accessibleTilesInRange(final Point center, float radius);

  /**
   * Calculates a path from a start tile to an end tile.
   *
   * @param start The starting tile.
   * @param end The destination tile.
   * @return An Optional containing the calculated path, if a path exists.
   */
  Optional<GraphPath<Tile>> findPath(final Tile start, final Tile end);

  /**
   * Gets a stream of all entities occupying a specific tile.
   *
   * @param check The tile to check.
   * @return A stream of entities on the tile.
   */
  Stream<Entity> entityAtTile(final Tile check);

  /**
   * Gets a stream of all entities located at a specific point.
   *
   * @param point The point to check.
   * @return A stream of entities at the point.
   */
  Stream<Entity> entityAtPoint(Point point);

  /**
   * Gets the current position of an entity.
   *
   * @param entity The entity to check.
   * @return An Optional containing the entity's position, if present.
   */
  Optional<Point> positionOf(final Entity entity);

  // --- Audio ---

  /**
   * Gets the Audio API responsible for playing and managing sound.
   *
   * @return The Audio API instance.
   */
  AudioApi audio();

  /**
   * Gets the raw sound player used by the game engine.
   *
   * @return The SoundPlayer instance.
   */
  ISoundPlayer soundPlayer();
}
