package contrib.systems.DecoTestSystem_Folder;

import static org.junit.jupiter.api.Assertions.*;
import contrib.systems.DecoTestSystem;

import contrib.components.CollideComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModifyOffsetTest {

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
    void shouldIncreaseXOffset() {
        float oldX = collideComponent.collider().offset().x();

        system.modifyOffset(true, 1);

        assertEquals(
                oldX + 0.05f,
                collideComponent.collider().offset().x(),
                0.001f);
    }

    @Test
    void shouldDecreaseYOffset() {
        float oldY = collideComponent.collider().offset().y();

        system.modifyOffset(false, -1);

        assertEquals(
                oldY - 0.05f,
                collideComponent.collider().offset().y(),
                0.001f);
    }

    @Test
    void zeroChangeShouldNotModifyOffset() {
        float oldX = collideComponent.collider().offset().x();

        system.modifyOffset(true, 0);

        assertEquals(
                oldX,
                collideComponent.collider().offset().x(),
                0.001f);
    }
}