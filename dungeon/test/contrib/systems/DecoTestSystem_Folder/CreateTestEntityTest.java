package contrib.systems.DecoTestSystem_Folder;

import static org.junit.jupiter.api.Assertions.*;
import contrib.systems.DecoTestSystem;

import contrib.components.CollideComponent;
import core.components.PositionComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateTestEntityTest {

    private DecoTestSystem system;

    @BeforeEach
    void setUp() {
        system = new DecoTestSystem();
    }

    @Test
    void shouldCreateEntity() {
        system.createTestEntity();

        assertNotNull(system.getTestEntity());
    }

    @Test
    void createdEntityShouldContainCollider() {
        system.createTestEntity();

        assertTrue(
                system.getTestEntity()
                        .fetch(CollideComponent.class)
                        .isPresent());
    }

    @Test
    void createdEntityShouldContainPosition() {
        system.createTestEntity();

        assertTrue(
                system.getTestEntity()
                        .fetch(PositionComponent.class)
                        .isPresent());
    }
}