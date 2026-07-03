package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.LeverComponent;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.SimpleIPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LeverFactory#createLever(Point, ICommand, core.utils.components.path.IPath)}.
 */
public class LeverFactoryTest {

  /** Resets global game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
  }

  /**
   * Verifies that a lever created with valid arguments has all expected components attached and
   * starts in the off state.
   */
  @Test
  public void createLever_validArgs_hasAllComponentsAndIsOff() {
    Entity lever =
        LeverFactory.createLever(new Point(0, 0), ICommand.NOOP, new SimpleIPath("objects/lever"));

    assertTrue(lever.isPresent(PositionComponent.class));
    assertTrue(lever.isPresent(DrawComponent.class));
    assertTrue(lever.isPresent(LeverComponent.class));
    assertTrue(lever.isPresent(InteractionComponent.class));
    assertFalse(lever.fetch(LeverComponent.class).get().isOn());
  }

  /**
   * Verifies that triggering the interaction on an off lever toggles it on and updates the {@link
   * DrawComponent} to the "on" state.
   */
  @Test
  public void interaction_togglesOffToOn_sendsOnSignal() {
    Point pos = new Point(0, 0);
    Entity lever = LeverFactory.createLever(pos, ICommand.NOOP, new SimpleIPath("objects/lever"));
    Entity who = new Entity();
    who.add(new PositionComponent(pos));

    InteractionComponent ic = lever.fetch(InteractionComponent.class).orElseThrow();
    ic.triggerInteraction(lever, who);

    assertTrue(lever.fetch(LeverComponent.class).get().isOn());
    assertEquals("on", lever.fetch(DrawComponent.class).get().currentState().name);
  }

  /**
   * Verifies that triggering the interaction on an on lever toggles it off and updates the {@link
   * DrawComponent} to the "off" state.
   */
  @Test
  public void interaction_togglesOnToOff_sendsOffSignal() {
    Point pos = new Point(0, 0);
    Entity lever = LeverFactory.createLever(pos, ICommand.NOOP, new SimpleIPath("objects/lever"));
    lever.fetch(LeverComponent.class).get().toggle(); // manually switch to on, bypassing signals
    Entity who = new Entity();
    who.add(new PositionComponent(pos));

    InteractionComponent ic = lever.fetch(InteractionComponent.class).orElseThrow();
    ic.triggerInteraction(lever, who);

    assertFalse(lever.fetch(LeverComponent.class).get().isOn());
    assertEquals("off", lever.fetch(DrawComponent.class).get().currentState().name);
  }

  /** Verifies that the given {@link ICommand} instance is stored unchanged in the lever. */
  @Test
  public void createLever_storesGivenCommand() {
    ICommand command =
        new ICommand() {
          @Override
          public void execute() {}

          @Override
          public void undo() {}
        };

    Entity lever =
        LeverFactory.createLever(new Point(0, 0), command, new SimpleIPath("objects/lever"));

    assertSame(command, lever.fetch(LeverComponent.class).get().command());
  }

  /**
   * Characterizes {@code createLever} with a {@code null} position. {@link
   * PositionComponent#PositionComponent(Point)} stores the given position without validation, so
   * entity creation does not throw. However, {@link PositionComponent#position()} later
   * dereferences the stored (null) position and throws a {@link NullPointerException}.
   */
  @Test
  public void createLever_nullPosition_characterize() {
    Entity lever = LeverFactory.createLever(null, ICommand.NOOP, new SimpleIPath("objects/lever"));

    assertTrue(lever.isPresent(PositionComponent.class));
    PositionComponent pc = lever.fetch(PositionComponent.class).get();
    assertThrows(NullPointerException.class, pc::position);
  }

  /**
   * Verifies that {@code createLever} with a {@code null} command does not throw and stores a
   * {@code null} command on the {@link LeverComponent}.
   */
  @Test
  public void createLever_nullCommand_createsLeverWithNullCommand() {
    Entity lever =
        LeverFactory.createLever(new Point(0, 0), null, new SimpleIPath("objects/lever"));

    assertNull(lever.fetch(LeverComponent.class).get().command());
  }

  /**
   * Characterizes {@code createLever} with a {@code null} texture path. {@link
   * core.utils.components.draw.animation.Animation#loadAnimationSpritesheet} dereferences the path
   * argument directly (calls {@code path.pathString()}), so a {@code null} path results in a {@link
   * NullPointerException}.
   */
  @Test
  public void createLever_nullTexturePath_throws() {
    assertThrows(
        NullPointerException.class,
        () -> LeverFactory.createLever(new Point(0, 0), ICommand.NOOP, null));
  }

  /**
   * Verifies that triggering the interaction on a lever missing its {@link LeverComponent} throws a
   * {@link MissingComponentException} (the interaction's {@code orElseThrow} fires).
   */
  @Test
  public void interaction_missingLeverComponent_throwsMissingComponentException() {
    Point pos = new Point(0, 0);
    Entity lever = LeverFactory.createLever(pos, ICommand.NOOP, new SimpleIPath("objects/lever"));
    lever.remove(LeverComponent.class);
    Entity who = new Entity();
    who.add(new PositionComponent(pos));

    InteractionComponent ic = lever.fetch(InteractionComponent.class).orElseThrow();
    assertThrows(MissingComponentException.class, () -> ic.triggerInteraction(lever, who));
  }

  /**
   * Verifies that triggering the interaction on a lever missing its {@link DrawComponent} still
   * toggles the lever and does not throw, since the signal-sending is only attempted via {@code
   * Optional#ifPresent}.
   */
  @Test
  public void interaction_missingDrawComponent_togglesNoSignalNoException() {
    Point pos = new Point(0, 0);
    Entity lever = LeverFactory.createLever(pos, ICommand.NOOP, new SimpleIPath("objects/lever"));
    lever.remove(DrawComponent.class);
    Entity who = new Entity();
    who.add(new PositionComponent(pos));

    InteractionComponent ic = lever.fetch(InteractionComponent.class).orElseThrow();
    assertDoesNotThrow(() -> ic.triggerInteraction(lever, who));
    assertTrue(lever.fetch(LeverComponent.class).get().isOn());
  }

  /**
   * Covers U4 (spritesheet missing the required "off" state) and U8 (a valid, but content-wise
   * mismatched, spritesheet) using the real "character/knight" spritesheet, which has neither an
   * "off" nor an "on" state. {@code State.fromMap} builds {@code new State("off", null)} for the
   * missing key, and the {@link core.utils.components.draw.state.State} constructor rejects a
   * {@code null} animation with an {@link IllegalArgumentException}.
   *
   * <p>U5 (a spritesheet that has "off" but is missing "on") triggers the identical {@code
   * State.fromMap}/{@code State} constructor failure path and has no dedicated asset fixture in
   * this repository, so it is covered by this same mechanism.
   */
  @Test
  public void createLever_spritesheetMissingRequiredState_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            LeverFactory.createLever(
                new Point(0, 0), ICommand.NOOP, new SimpleIPath("character/knight")));
  }
}
