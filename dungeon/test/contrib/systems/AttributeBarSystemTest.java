package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.BarDisplayable;
import contrib.components.HealthComponent;
import contrib.components.ManaComponent;
import contrib.components.StaminaComponent;
import contrib.components.UIComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.state.StateMachine;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for {@link AttributeBarSystem}. */
class AttributeBarSystemTest {

  private AttributeBarSystem system;
  private Entity entity;
  private PositionComponent positionComponent;
  private DrawComponent drawComponent;

  @BeforeEach
  void setUp() throws Exception {
    Game.removeAllEntities();
    Game.removeAllSystems();

    system = new AttributeBarSystem();
    Game.add(system);

    entity = new Entity();
    positionComponent = new PositionComponent(new Point(0, 0));
    drawComponent = new DrawComponent(Mockito.mock(StateMachine.class));

    entity.add(positionComponent);
    entity.add(drawComponent);

    Game.add(entity);

    // Set up mock Stage to prevent "No stage available" RuntimeException in tests
    com.badlogic.gdx.scenes.scene2d.Stage mockStage =
        Mockito.mock(com.badlogic.gdx.scenes.scene2d.Stage.class);
    com.badlogic.gdx.utils.viewport.Viewport mockViewport =
        Mockito.mock(com.badlogic.gdx.utils.viewport.Viewport.class);
    Mockito.when(mockStage.getViewport()).thenReturn(mockViewport);
    Mockito.when(mockViewport.getScreenWidth()).thenReturn(800);
    Mockito.when(mockViewport.getScreenHeight()).thenReturn(600);
    Mockito.when(mockStage.getWidth()).thenReturn(800f);
    Mockito.when(mockStage.getHeight()).thenReturn(600f);
    setStage(mockStage);
  }

  @AfterEach
  void tearDown() throws Exception {
    setStage(null);
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  private static void setStage(com.badlogic.gdx.scenes.scene2d.Stage stage) throws Exception {
    java.lang.reflect.Field field = core.game.GameLoop.class.getDeclaredField("stage");
    field.setAccessible(true);
    field.set(null, stage);
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, Map<Class<? extends BarDisplayable>, ProgressBar>> getBarMapping()
      throws Exception {
    java.lang.reflect.Field field = AttributeBarSystem.class.getDeclaredField("barMapping");
    field.setAccessible(true);
    return (Map<Integer, Map<Class<? extends BarDisplayable>, ProgressBar>>) field.get(system);
  }

  private float getVerticalOffset(Entity barEntity) throws Exception {
    UIComponent uiComp = barEntity.fetch(UIComponent.class).orElseThrow();
    contrib.hud.dialogs.DialogContext ctx = uiComp.dialogContext();
    Object barContext =
        ctx.require(contrib.hud.dialogs.DialogContextKeys.PROGRESS_BAR, Object.class);
    java.lang.reflect.Method method = barContext.getClass().getMethod("verticalOffset");
    method.setAccessible(true);
    return (float) method.invoke(barContext);
  }

  /**
   * Verifies that the system executes without crashing when no BarDisplayable components are
   * present on the entity.
   */
  @Test
  void testExecuteWithoutBars() {
    assertDoesNotThrow(() -> system.execute());
  }

  /** Testen, ob für eine Entity mit HealthComponent eine Gesundheitsleiste erstellt wird. */
  @Test
  void testHealthBarCreated() {
    HealthComponent hc = new HealthComponent(100, e -> {});
    entity.add(hc);
    system.execute();

    boolean healthBarExists =
        Game.allEntities().anyMatch(e -> e.name().equals("healthbar_" + entity.id()));
    assertTrue(healthBarExists);
  }

  /** Testen, ob für eine Entity mit ManaComponent eine Mana-Leiste erstellt wird. */
  @Test
  void testManaBarCreated() {
    ManaComponent mc = new ManaComponent(100f, 50f, 1f);
    entity.add(mc);
    system.execute();

    boolean manaBarExists =
        Game.allEntities().anyMatch(e -> e.name().equals("manabar_" + entity.id()));
    assertTrue(manaBarExists);
  }

  /** Testen, ob für eine Entity mit StaminaComponent eine Ausdauerleiste erstellt wird. */
  @Test
  void testStaminaBarCreated() {
    StaminaComponent sc = new StaminaComponent(100f, 80f, 1f);
    entity.add(sc);
    system.execute();

    boolean staminaBarExists =
        Game.allEntities().anyMatch(e -> e.name().equals("staminabar_" + entity.id()));
    assertTrue(staminaBarExists);
  }

  /**
   * Testen, ob eine Leiste entfernt wird, wenn die zugehörige Komponente nicht mehr vorhanden ist.
   */
  @Test
  void testBarRemovedWhenComponentRemoved() throws Exception {
    HealthComponent hc = new HealthComponent(100, e -> {});
    entity.add(hc);
    system.execute();

    // Inject mock bar to mapping
    ProgressBar mockBar = Mockito.mock(ProgressBar.class);
    Map<Integer, Map<Class<? extends BarDisplayable>, ProgressBar>> barMapping = getBarMapping();
    barMapping
        .computeIfAbsent(entity.id(), k -> new HashMap<>())
        .put(HealthComponent.class, mockBar);

    // Remove component and execute update
    entity.remove(HealthComponent.class);
    system.execute();

    // Verify mockBar.remove() was called
    Mockito.verify(mockBar).remove();
  }

  /** Testen, ob mehrere Leisten entsprechend ihrer Priorität korrekt sortiert werden. */
  @Test
  void testPrioritySorting() throws Exception {
    HealthComponent hc = new HealthComponent(100, e -> {});
    ManaComponent mc = new ManaComponent(100f, 50f, 1f);
    StaminaComponent sc = new StaminaComponent(100f, 80f, 1f);

    // Add components in arbitrary order
    entity.add(sc);
    entity.add(hc);
    entity.add(mc);

    system.execute();

    Entity healthBarEntity =
        Game.allEntities()
            .filter(e -> e.name().equals("healthbar_" + entity.id()))
            .findFirst()
            .orElseThrow();
    Entity manaBarEntity =
        Game.allEntities()
            .filter(e -> e.name().equals("manabar_" + entity.id()))
            .findFirst()
            .orElseThrow();
    Entity staminaBarEntity =
        Game.allEntities()
            .filter(e -> e.name().equals("staminabar_" + entity.id()))
            .findFirst()
            .orElseThrow();

    float healthOffset = getVerticalOffset(healthBarEntity);
    float manaOffset = getVerticalOffset(manaBarEntity);
    float staminaOffset = getVerticalOffset(staminaBarEntity);

    // Health priority is 0 (offset 0), Mana priority is 1 (offset 15), Stamina priority is 2
    // (offset 30)
    assertEquals(0.0f, healthOffset);
    assertEquals(15.0f, manaOffset);
    assertEquals(30.0f, staminaOffset);
  }

  /** Testen, ob für neu hinzugefügte Komponenten automatisch eine neue Leiste erzeugt wird. */
  @Test
  void testNewComponentAutomaticallyAddsBar() {
    HealthComponent hc = new HealthComponent(100, e -> {});
    entity.add(hc);
    system.execute();

    assertTrue(Game.allEntities().anyMatch(e -> e.name().equals("healthbar_" + entity.id())));
    assertFalse(Game.allEntities().anyMatch(e -> e.name().equals("manabar_" + entity.id())));

    // Add new component
    ManaComponent mc = new ManaComponent(100f, 50f, 1f);
    entity.add(mc);
    system.execute();

    assertTrue(Game.allEntities().anyMatch(e -> e.name().equals("healthbar_" + entity.id())));
    assertTrue(Game.allEntities().anyMatch(e -> e.name().equals("manabar_" + entity.id())));
  }

  /**
   * Testen, ob vorhandene Leisten bei der Aktualisierung mit den aktuellen Attributwerten
   * synchronisiert werden.
   */
  @Test
  void testSynchronizeValues() throws Exception {
    HealthComponent hc = new HealthComponent(100, e -> {});
    entity.add(hc);
    system.execute();

    // Set up mock bar in mapping
    ProgressBar mockBar = Mockito.mock(ProgressBar.class);
    Map<Integer, Map<Class<? extends BarDisplayable>, ProgressBar>> barMapping = getBarMapping();
    barMapping
        .computeIfAbsent(entity.id(), k -> new HashMap<>())
        .put(HealthComponent.class, mockBar);

    // Change current health value to 50
    hc.currentHealthpoints(50);
    system.execute();

    // Verify mockBar value is updated to 50/100 = 0.5f
    Mockito.verify(mockBar).setValue(0.5f);
  }
}
