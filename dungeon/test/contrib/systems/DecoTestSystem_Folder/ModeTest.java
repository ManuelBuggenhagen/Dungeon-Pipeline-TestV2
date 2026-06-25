package contrib.systems.DecoTestSystem_Folder;

import static org.junit.jupiter.api.Assertions.*;
import contrib.systems.DecoTestSystem;

import org.junit.jupiter.api.Test;

class ModeTest {

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