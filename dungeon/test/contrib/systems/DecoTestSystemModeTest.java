package contrib.systems;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DecoTestSystemModeTest {

    @Test
    void nextShouldReturnNextMode() {
        assertEquals(
                DecoTestSystem.Mode.ModifyOffsetX,
                DecoTestSystem.Mode.ChangeDeco.next());

        assertEquals(
                DecoTestSystem.Mode.ModifyOffsetY,
                DecoTestSystem.Mode.ModifyOffsetX.next());

        assertEquals(
                DecoTestSystem.Mode.ModifySizeWidth,
                DecoTestSystem.Mode.ModifyOffsetY.next());

        assertEquals(
                DecoTestSystem.Mode.ModifySizeHeight,
                DecoTestSystem.Mode.ModifySizeWidth.next());
    }

    @Test
    void nextShouldWrapAround() {
        assertEquals(
                DecoTestSystem.Mode.ChangeDeco,
                DecoTestSystem.Mode.ModifySizeHeight.next());
    }
}
