package contrib;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.Component;
import core.Entity;
import core.Game;
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
 * Standard-Implementierung des GameProvider-Interfaces. Leitet alle Aufrufe direkt an die statische
 * Game-Klasse des Frameworks weiter.
 */
public class DefaultGameProvider implements GameProvider {

  // --- Lifecycle & Window ---
  @Override
  public void run() {
    Game.run();
  }

  @Override
  public void exit() {
    Game.exit();
  }

  @Override
  public void exit(String reason) {
    Game.exit(reason);
  }

  @Override
  public int windowWidth() {
    return Game.windowWidth();
  }

  @Override
  public void windowWidth(int windowWidth) {
    Game.windowWidth(windowWidth);
  }

  @Override
  public int windowHeight() {
    return Game.windowHeight();
  }

  @Override
  public void windowHeight(int windowHeight) {
    Game.windowHeight(windowHeight);
  }

  @Override
  public int frameRate() {
    return Game.frameRate();
  }

  @Override
  public void frameRate(int frameRate) {
    Game.frameRate(frameRate);
  }

  @Override
  public boolean resizeable() {
    return Game.resizeable();
  }

  @Override
  public void resizeable(boolean resizeable) {
    Game.resizeable(resizeable);
  }

  @Override
  public String windowTitle() {
    return Game.windowTitle();
  }

  @Override
  public void windowTitle(String newTitle) {
    Game.windowTitle(newTitle);
  }

  @Override
  public IPath logoPath() {
    return Game.logoPath();
  }

  @Override
  public void logoPath(IPath logoPath) {
    Game.logoPath(logoPath);
  }

  @Override
  public boolean isHeadless() {
    return Game.isHeadless();
  }

  @Override
  public int currentTick() {
    return Game.currentTick();
  }

  // --- Configuration & Settings ---
  @Override
  public void disableAudio(boolean disableAudio) {
    Game.disableAudio(disableAudio);
  }

  @Override
  public void enableCheckPattern(boolean enabled) {
    Game.enableCheckPattern(enabled);
  }

  @Override
  public boolean isCheckPatternEnabled() {
    return Game.isCheckPatternEnabled();
  }

  @Override
  public void loadConfig(IPath path, Class<?>... keyboardConfigClass) throws IOException {
    Game.loadConfig(path, keyboardConfigClass);
  }

  @Override
  public void userOnFrame(IVoidFunction userOnFrame) {
    Game.userOnFrame(userOnFrame);
  }

  @Override
  public void userOnSetup(IVoidFunction userOnSetup) {
    Game.userOnSetup(userOnSetup);
  }

  @Override
  public void userOnLevelLoad(Consumer<Boolean> userOnLevelLoad) {
    Game.userOnLevelLoad(userOnLevelLoad);
  }

  // --- Networking ---
  @Override
  public boolean isMultiplayerClient() {
    return Game.isMultiplayerClient();
  }

  @Override
  public void initializeNetwork() {
    Game.initializeNetwork();
  }

  @Override
  public INetworkHandler network() {
    return Game.network();
  }

  // --- UI ---
  @Override
  public Optional<Stage> stage() {
    return Game.stage();
  }

  // --- Entity & System Management (ECS) ---
  @Override
  public void add(Entity entity) {
    Game.add(entity);
  }

  @Override
  public void remove(Entity entity) {
    Game.remove(entity);
  }

  @Override
  public void removeAllEntities() {
    Game.removeAllEntities();
  }

  @Override
  public Optional<System> add(System system) {
    return Game.add(system);
  }

  @Override
  public void remove(Class<? extends System> system) {
    Game.remove(system);
  }

  @Override
  public void removeAllSystems() {
    Game.removeAllSystems();
  }

  @Override
  public Map<Class<? extends System>, System> systems() {
    return Game.systems();
  }

  @Override
  public <T extends System> void system(Class<T> s, Consumer<T> c) {
    Game.system(s, c);
  }

  @Override
  public Stream<Entity> allEntities() {
    return Game.allEntities();
  }

  @Override
  public Stream<Entity> levelEntities() {
    return Game.levelEntities();
  }

  @Override
  public Stream<Entity> levelEntities(System system) {
    return Game.levelEntities(system);
  }

  @Override
  public Stream<Entity> levelEntities(Set<Class<? extends Component>> filter) {
    return Game.levelEntities(filter);
  }

  @Override
  public Optional<Entity> player() {
    return Game.player();
  }

  @Override
  public Stream<Entity> allPlayers() {
    return Game.allPlayers();
  }

  @Override
  public Optional<Entity> findEntityById(int entityId) {
    return Game.findEntityById(entityId);
  }

  @Override
  public Optional<Entity> findInAll(Component component) {
    return Game.findInAll(component);
  }

  @Override
  public Optional<Entity> findInLevel(Component component) {
    return Game.findInLevel(component);
  }

  @Override
  public boolean existInLevel(Entity entity) {
    return Game.existInLevel(entity);
  }

  @Override
  public boolean existInAll(Entity entity) {
    return Game.existInAll(entity);
  }

  // --- Level & Tiles ---
  @Override
  public Optional<ILevel> currentLevel() {
    return Game.currentLevel();
  }

  @Override
  public void currentLevel(ILevel level) {
    Game.currentLevel(level);
  }

  @Override
  public Optional<Tile> tileAt(Point point) {
    return Game.tileAt(point);
  }

  @Override
  public Optional<Tile> tileAt(Coordinate coordinate) {
    return Game.tileAt(coordinate);
  }

  @Override
  public Optional<Tile> tileAt(Coordinate coordinate, Direction direction) {
    return Game.tileAt(coordinate, direction);
  }

  @Override
  public Optional<Tile> tileAt(Point point, Direction direction) {
    return Game.tileAt(point, direction);
  }

  @Override
  public Optional<Tile> tileAtEntity(Entity entity) {
    return Game.tileAtEntity(entity);
  }

  @Override
  public Optional<Tile> randomTile() {
    return Game.randomTile();
  }

  @Override
  public Optional<Tile> randomTile(LevelElement elementType) {
    return Game.randomTile(elementType);
  }

  @Override
  public Optional<Point> randomTilePoint() {
    return Game.randomTilePoint();
  }

  @Override
  public Optional<Point> randomTilePoint(LevelElement elementTyp) {
    return Game.randomTilePoint(elementTyp);
  }

  @Override
  public Set<Tile> neighbours(Tile tile) {
    return Game.neighbours(tile);
  }

  @Override
  public Optional<Tile> startTile() {
    return Game.startTile();
  }

  @Deprecated
  @Override
  public Optional<Tile> endTile() {
    return Game.endTile();
  }

  @Override
  public Set<ExitTile> endTiles() {
    return Game.endTiles();
  }

  @Override
  public Set<Tile> allTiles() {
    return Game.allTiles();
  }

  @Override
  public Set<Tile> allTiles(Predicate<Tile> filterRule) {
    return Game.allTiles(filterRule);
  }

  @Override
  public Set<Tile> allTiles(LevelElement elementTyp) {
    return Game.allTiles(elementTyp);
  }

  @Override
  public Set<Tile> allFreeTiles() {
    return Game.allFreeTiles();
  }

  @Override
  public Optional<Tile> freeTile() {
    return Game.freeTile();
  }

  @Override
  public Optional<Point> freePosition() {
    return Game.freePosition();
  }

  @Override
  public boolean isFreeTile(Tile tile) {
    return Game.isFreeTile(tile);
  }

  @Override
  public List<Tile> accessibleTilesInRange(Point center, float radius) {
    return Game.accessibleTilesInRange(center, radius);
  }

  @Override
  public Optional<GraphPath<Tile>> findPath(Tile start, Tile end) {
    return Game.findPath(start, end);
  }

  @Override
  public Stream<Entity> entityAtTile(Tile check) {
    return Game.entityAtTile(check);
  }

  @Override
  public Stream<Entity> entityAtPoint(Point point) {
    return Game.entityAtPoint(point);
  }

  @Override
  public Optional<Point> positionOf(Entity entity) {
    return Game.positionOf(entity);
  }

  // --- Audio ---
  @Override
  public AudioApi audio() {
    return Game.audio();
  }

  @Override
  public ISoundPlayer soundPlayer() {
    return Game.soundPlayer();
  }
}
