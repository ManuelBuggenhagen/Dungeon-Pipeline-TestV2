package contrib.systems.DecoTestSystem_Folder;

import static org.junit.jupiter.api.Assertions.*;
import contrib.systems.DecoTestSystem;

import contrib.components.CollideComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModifySizeTest {

    private DecoTestSystem system;
    private CollideComponent collideComponent;

    @BeforeEach
    void setUp() {
        system = new DecoTestSystem();
        system.createTestEntity();

        collideComponent =
                system.getTestEntity()
                        .fetch(CollideComponent.class)
                        .orElseThrow();
    }

    @Test
    void shouldIncreaseWidth() {
        float oldWidth = collideComponent.collider().width();

        system.modifySize(true, 1);

        assertEquals(
                oldWidth + 0.05f,
                collideComponent.collider().width(),
                0.001f);
    }

    @Test
    void shouldDecreaseHeight() {
        float oldHeight = collideComponent.collider().height();

        system.modifySize(false, -1);

        assertEquals(
                oldHeight - 0.05f,
                collideComponent.collider().height(),
                0.001f);
    }

    @Test
    void zeroChangeShouldNotModifyWidth() {
        float oldWidth = collideComponent.collider().width();

        system.modifySize(true, 0);

        assertEquals(
                oldWidth,
                collideComponent.collider().width(),
                0.001f);
    }
}