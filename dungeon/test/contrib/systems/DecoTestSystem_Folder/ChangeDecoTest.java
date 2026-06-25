package contrib.systems.DecoTestSystem_Folder;

import static org.junit.jupiter.api.Assertions.*;
import contrib.systems.DecoTestSystem;

import contrib.entities.deco.Deco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangeDecoTest {

    private DecoTestSystem system;

    @BeforeEach
    void setUp() {
        system = new DecoTestSystem();
        system.createTestEntity();
    }

    @Test
    void shouldSelectNextDeco() {
        Deco oldDeco = system.getCurrentDeco();

        system.changeDeco(1);

        assertNotEquals(oldDeco, system.getCurrentDeco());
    }

    @Test
    void shouldSelectPreviousDeco() {
        Deco oldDeco = system.getCurrentDeco();

        system.changeDeco(-1);

        assertNotEquals(oldDeco, system.getCurrentDeco());
    }

    @Test
    void changeByZeroShouldKeepCurrentDeco() {
        Deco oldDeco = system.getCurrentDeco();

        system.changeDeco(0);

        assertEquals(oldDeco, system.getCurrentDeco());
    }
}