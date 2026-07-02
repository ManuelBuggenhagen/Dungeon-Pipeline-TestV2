package core;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/** for test purposes to be independent of the whole framework */
public class FakeGame {

  private final Set<Entity> entities = new HashSet<>();

  public Entity add(Entity entity) {
    entities.add(entity);
    return entity;
  }

  public Entity remove(Entity entity) {
    entities.remove(entity);
    return entity;
  }

  public Stream<Entity> allEntities() {
    return entities.stream();
  }

  public final Stream<Entity> filteredEntityStream(Class<? extends Component>... components) {

    return entities.stream()
        .filter(entity -> java.util.Arrays.stream(components).allMatch(entity::isPresent));
  }

  public void clear() {
    entities.clear();
  }
}
