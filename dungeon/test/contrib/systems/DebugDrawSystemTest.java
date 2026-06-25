package contrib.systems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import contrib.components.CollideComponent;
import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.NetworkUtils;
import core.systems.CameraSystem;
import core.utils.FontHelper;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DebugDrawSystemTest {

  private DebugDrawSystem system;
  private MockedStatic<FontHelper> fontHelperMock;
  private MockedStatic<NetworkUtils> networkUtilsMock;
  private MockedStatic<CameraSystem> cameraSystemMock;
  private MockedConstruction<SpriteBatch> spriteBatchMock;
  private MockedConstruction<ShapeRenderer> shapeRendererMock;

  @BeforeEach
  void setUp() {
    Gdx.gl = Mockito.mock(GL20.class);
    Gdx.gl20 = Gdx.gl;

    // Mock-Konstruktion, um OpenGL-Fehler in einer kopflosen (headless) Umgebung zu vermeiden
    spriteBatchMock = Mockito.mockConstruction(SpriteBatch.class);
    shapeRendererMock = Mockito.mockConstruction(ShapeRenderer.class);

    // Statische Aufrufe von FontHelper mocken
    fontHelperMock = Mockito.mockStatic(FontHelper.class);
    BitmapFont mockFont = Mockito.mock(BitmapFont.class);
    try {
      java.lang.reflect.Field dataField = BitmapFont.class.getDeclaredField("data");
      dataField.setAccessible(true);
      dataField.set(mockFont, new BitmapFont.BitmapFontData());
    } catch (Exception ignored) {
    }
    Mockito.when(mockFont.getColor()).thenReturn(com.badlogic.gdx.graphics.Color.WHITE);
    fontHelperMock.when(FontHelper::getDefaultFont).thenReturn(mockFont);

    BitmapFont mockFont2 = Mockito.mock(BitmapFont.class);
    try {
      java.lang.reflect.Field dataField = BitmapFont.class.getDeclaredField("data");
      dataField.setAccessible(true);
      dataField.set(mockFont2, new BitmapFont.BitmapFontData());
    } catch (Exception ignored) {
    }
    Mockito.when(mockFont2.getColor()).thenReturn(com.badlogic.gdx.graphics.Color.WHITE);
    fontHelperMock
        .when(() -> FontHelper.getFont(Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(mockFont2);

    // Falls FONT bereits als Mock initialisiert ist, werden die Methoden mittels Reflection
    // gestubbt
    try {
      java.lang.reflect.Field fontField = DebugDrawSystem.class.getDeclaredField("FONT");
      fontField.setAccessible(true);
      BitmapFont font = (BitmapFont) fontField.get(null);
      if (font != null) {
        java.lang.reflect.Field dataField = BitmapFont.class.getDeclaredField("data");
        dataField.setAccessible(true);
        dataField.set(font, new BitmapFont.BitmapFontData());
        if (Mockito.mockingDetails(font).isMock()) {
          Mockito.when(font.getColor()).thenReturn(com.badlogic.gdx.graphics.Color.WHITE);
        }
      }
    } catch (Exception ignored) {
    }

    // NetworkUtils mocken, um false für isNetworkClient zurückzugeben und die Initialisierung von
    // Game.network zu verhindern
    networkUtilsMock = Mockito.mockStatic(NetworkUtils.class);
    networkUtilsMock.when(NetworkUtils::isNetworkClient).thenReturn(false);

    // Statische Aufrufe von CameraSystem mocken
    cameraSystemMock = Mockito.mockStatic(CameraSystem.class);
    cameraSystemMock.when(CameraSystem::camera).thenCallRealMethod();
    cameraSystemMock
        .when(() -> CameraSystem.isEntityHovered(Mockito.any(Entity.class)))
        .thenReturn(true);

    Game.removeAllEntities();
    Game.removeAllSystems();
    system = new DebugDrawSystem();
    Game.add(system);
  }

  @AfterEach
  void tearDown() {
    if (fontHelperMock != null) fontHelperMock.close();
    if (networkUtilsMock != null) networkUtilsMock.close();
    if (cameraSystemMock != null) cameraSystemMock.close();
    if (spriteBatchMock != null) spriteBatchMock.close();
    if (shapeRendererMock != null) shapeRendererMock.close();

    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  // Hilfsmethoden, die Reflection verwenden, um auf private Felder zuzugreifen

  // Holt den Wert des privaten render-Feldes.
  private boolean getRenderField() throws Exception {
    Field field = DebugDrawSystem.class.getDeclaredField("render");
    field.setAccessible(true);
    return field.getBoolean(system);
  }

  // Holt den Wert des privaten renderSystemList-Feldes.
  private boolean getRenderSystemListField() throws Exception {
    Field field = DebugDrawSystem.class.getDeclaredField("renderSystemList");
    field.setAccessible(true);
    return field.getBoolean(system);
  }

  // Holt das private quickInfoCache-Feld.
  private Map<Entity, String> getQuickInfoCache() throws Exception {
    Field field = DebugDrawSystem.class.getDeclaredField("quickInfoCache");
    field.setAccessible(true);
    return (Map<Entity, String>) field.get(null);
  }

  // Testet, ob toggleHUD() die HUD-Rendering-Option ein- und ausschaltet.
  @Test
  void testToggleHUD() throws Exception {
    boolean initial = getRenderField();
    system.toggleHUD();
    assertNotEquals(initial, getRenderField());
    system.toggleHUD();
    assertEquals(initial, getRenderField());
  }

  // Testet, ob toggleSystemList() die Systemliste-Rendering-Option ein- und ausschaltet.
  @Test
  void testToggleSystemList() throws Exception {
    boolean initial = getRenderSystemListField();
    system.toggleSystemList();
    assertNotEquals(initial, getRenderSystemListField());
    system.toggleSystemList();
    assertEquals(initial, getRenderSystemListField());
  }

  // Testet, ob setEntityQuickInfo() zusätzliche Informationen für eine Entity korrekt speichert.
  @Test
  void testSetEntityQuickInfo() throws Exception {
    Entity entity = new Entity();
    String info = "Test Info";
    DebugDrawSystem.setEntityQuickInfo(entity, info);
    Map<Entity, String> cache = getQuickInfoCache();
    assertEquals(info, cache.get(entity));
    cache.remove(entity); // Aufräumen
  }

  // Hilfsmethode, um die private statische Methode `isNearInteger` per Reflection aufzurufen.
  private boolean invokeIsNearInteger(float value) throws Exception {
    java.lang.reflect.Method method =
        DebugDrawSystem.class.getDeclaredMethod("isNearInteger", float.class);
    method.setAccessible(true);
    return (boolean) method.invoke(null, value);
  }

  @Test
  void testIsNearInteger() throws Exception {
    // Testen, ob Werte nahe an einer ganzen Zahl korrekt als true erkannt werden
    assertTrue(invokeIsNearInteger(3.0f));
    assertTrue(invokeIsNearInteger(3.005f));
    assertTrue(invokeIsNearInteger(2.995f));

    // Testen, ob Werte nicht nahe an einer ganzen Zahl korrekt als false erkannt werden
    org.junit.jupiter.api.Assertions.assertFalse(invokeIsNearInteger(3.02f));
    org.junit.jupiter.api.Assertions.assertFalse(invokeIsNearInteger(2.98f));
    org.junit.jupiter.api.Assertions.assertFalse(invokeIsNearInteger(3.5f));
  }

  @Test
  void testRenderDrawsRelevantDebugInfo() throws Exception {
    // Aktiviert die HUD-Zeichnung
    if (!getRenderField()) {
      system.toggleHUD();
    }

    // Entity erstellen
    Entity entity = new Entity();

    // 1. PositionComponent hinzufügen
    PositionComponent pc = new PositionComponent(new Point(2.5f, 3.5f));
    entity.add(pc);

    // 2. CollideComponent mocken und hinzufügen
    CollideComponent cc = Mockito.mock(CollideComponent.class);
    contrib.utils.components.collide.Collider collider =
        Mockito.mock(contrib.utils.components.collide.Collider.class);
    Mockito.when(collider.absoluteBottomLeft()).thenReturn(new Point(2f, 3f));
    Mockito.when(collider.absoluteTopRight()).thenReturn(new Point(3f, 4f));
    Mockito.when(collider.absoluteCenter()).thenReturn(new Point(2.5f, 3.5f));
    Mockito.when(cc.collider()).thenReturn(collider);
    Mockito.when(cc.isSolid()).thenReturn(true);
    entity.add(cc);

    // 3. DrawComponent mocken und hinzufügen
    DrawComponent dc = Mockito.mock(DrawComponent.class);
    Animation animation = Mockito.mock(Animation.class);
    AnimationConfig config = Mockito.mock(AnimationConfig.class);
    core.utils.components.draw.state.State mockState =
        Mockito.mock(core.utils.components.draw.state.State.class);
    Mockito.when(dc.currentAnimation()).thenReturn(animation);
    Mockito.when(animation.getWidth()).thenReturn(1f);
    Mockito.when(animation.getHeight()).thenReturn(1f);
    Mockito.when(animation.getConfig()).thenReturn(config);
    Mockito.when(config.centered()).thenReturn(true);
    Mockito.when(dc.currentStateName()).thenReturn("idle");
    Mockito.when(dc.currentState()).thenReturn(mockState);
    Mockito.when(mockState.getData()).thenReturn("mocked_data");
    entity.add(dc);

    // 4. InteractionComponent mocken und hinzufügen
    InteractionComponent ic = Mockito.mock(InteractionComponent.class);
    IInteractable interactable = Mockito.mock(IInteractable.class);
    Interaction interaction = Mockito.mock(Interaction.class);
    Mockito.when(ic.interactions()).thenReturn(interactable);
    Mockito.when(interactable.interact()).thenReturn(interaction);
    Mockito.when(interaction.range()).thenReturn(2.0f);
    entity.add(ic);

    // Entity zum globalen Game-State hinzufügen
    Game.add(entity);

    // Aufrufen des render-Durchlaufs. Darf unter keinen Umständen abstürzen.
    assertDoesNotThrow(() -> system.render(0.1f));

    // Entity aufräumen
    Game.remove(entity);
  }
}
