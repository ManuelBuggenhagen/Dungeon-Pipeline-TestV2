---
title: "AIFactory Äquivalenzklassen"
---

Dieses Dokument beschreibt die Äquivalenzklassen für die Methoden der `AIFactory`.

## randomMonster()

### Kurzbeschreibung

Die Methode `randomMonster()` ermittelt aus allen Entities des aktuellen Levels diejenigen, die sowohl eine `HealthComponent` als auch eine `AIComponent` besitzen.
Aus dieser Menge wird zufällig genau eine Entity ausgewählt und als `Optional<Entity>` zurückgegeben.

Existiert keine passende Entity, wird ein leeres `Optional` zurückgegeben.

### Gültige Äquivalenzklassen

#### G1. Genau eine gültige, keine ungültigen Entities vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält genau eine Entity.
- Entity besitzt ein `HealthComponent` und `AIComponent`.

Erwartung:
- Die Methode liefert ein nicht-leeres `Optional`.
- Optional entspricht der einzig vorhandenen Entity.

#### G2. Genau eine gültige sowie ungültige Entities vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält mehrere Entities.
- Nur eine Entity besitzt ein `HealthComponent` und `AIComponent`.

Erwartung:
- Die Methode liefert ein nicht-leeres `Optional`.
- Optional entspricht der einzigen Entity mit `HealthComponent` sowie `AIComponent`..

#### G3. Mehrere gültige, keine ungültigen Entities vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält mehrere Entities.
- Alle Entities besitzt ein `HealthComponent` und `AIComponent`.

Erwartung:
- Die Methode liefert ein nicht-leeres `Optional`.
- Optional entspricht einem der vorhandenen Entities.

#### G4. Mehrere gültige sowie ungültigen Entities vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält mehrere Entities.
- Mehrere Entities besitzt ein `HealthComponent` und `AIComponent`.
- Mehrere Entities besitzt kein `HealthComponent` sowie `AIComponent`.

Erwartung:
- Die Methode liefert ein nicht-leeres `Optional`.
- Optional entspricht ausshcließlich einem der gültigen Monster.

### Ungültige Äquivalenzklassen

#### U1. Keine Entities im Level vorhanden

Vorbedingungen:
- `Game.levelEntities()` liefert einen leeren Stream.

Erwartung:
- Die Methode liefert ein leeres `Optional`.

#### U2. Entities vorhanden, aber keine Entity erfüllt die Monster-Kriterien

Vorbedingungen:
- `Game.levelEntities()` liefert mindestens eine Entity.
- Keine Entity besitzt gleichzeitig eine `HealthComponent` und eine `AIComponent`.

Erwartung:
- Die Methode liefert ein leeres `Optional`.

---

## randomIdleAI()

### Kurzbeschreibung

Die Methode `randomIdleAI()` erzeugt zufällig eine Instanz eines verfügbaren `IdleAI`-Verhaltens und gibt diese als `Consumer<Entity>` zurück.

Abhängig von einer zufälligen Auswahl wird eine der aktuell unterstützten Idle-AIs erzeugt. Dabei werden die erforderlichen Parameter zufällig innerhalb der definierten Wertebereiche generiert.

### Gültige Äquivalenzklassen

#### G1. Auswahl eines `PatrolWalk`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die Methode liefert eine Instanz von `PatrolWalk`.
- Der Patrol-Radius liegt im Bereich `PATROL_RADIUS_LOW` bis `PATROL_RADIUS_HIGH`.
- Die Anzahl der Checkpoints liegt im Bereich `CHECKPOINTS_LOW` bis `CHECKPOINTS_HIGH`.
- Die Pausenzeit liegt im Bereich `PAUSE_TIME_LOW` bis `PAUSE_TIME_HIGH`.
- Der verwendete Modus ist ein gültiger Wert aus `PatrolWalk.MODE.values()`.

#### G2. Auswahl eines `RadiusWalk`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Methode liefert eine Instanz von `RadiusWalk`.
- Der Radius liegt im Bereich `RADIUS_WALK_LOW` bis `RADIUS_WALK_HIGH`.
- Die Wartezeit liegt im Bereich `BREAK_TIME_LOW` bis `BREAK_TIME_HIGH`.

#### G3. Auswahl eines `StaticRadiusWalk`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Die Methode liefert eine Instanz von `StaticRadiusWalk`.
- Der Radius liegt im Bereich `STATIC_RADIUS_WALK_LOW` bis `STATIC_RADIUS_WALK_HIGH`.
- Die Wartezeit liegt im Bereich `STATIC_BREAK_TIME_LOW` bis `STATIC_BREAK_TIME_HIGH`.

### Ungültige Äquivalenzklassen

#### U1. Rückgabe eines nicht unterstützten IdleAI-Typs

Vorbedingungen:
- Beliebiger gültiger Aufruf der Methode.

Erwartung:
- Die Rückgabe entspricht ausschließlich einer der aktuell unterstützten Implementierungen:
  - `PatrolWalk`
  - `RadiusWalk`
  - `StaticRadiusWalk`

#### U2. `PatrolWalk` mit Radius außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Der erzeugte Radius liegt nicht außerhalb des Bereichs `PATROL_RADIUS_LOW` bis `PATROL_RADIUS_HIGH`.

#### U3. `PatrolWalk` mit ungültiger Checkpoint-Anzahl

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die Anzahl der Checkpoints liegt nicht außerhalb des Bereichs `CHECKPOINTS_LOW` bis `CHECKPOINTS_HIGH`.

#### U4. `PatrolWalk` mit ungültiger Pausenzeit

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die Pausenzeit liegt nicht außerhalb des Bereichs `PAUSE_TIME_LOW` bis `PAUSE_TIME_HIGH`.

#### U5. `PatrolWalk` mit ungültigem Modus

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Der verwendete Modus stammt aus `PatrolWalk.MODE.values()`.
- Es wird kein anderer Wert verwendet.

#### U6. `RadiusWalk` mit Radius außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Der erzeugte Radius liegt nicht außerhalb des Bereichs `RADIUS_WALK_LOW` bis `RADIUS_WALK_HIGH`.

#### U7. `RadiusWalk` mit Wartezeit außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Wartezeit liegt nicht außerhalb des Bereichs `BREAK_TIME_LOW` bis `BREAK_TIME_HIGH`.

#### U8. `StaticRadiusWalk` mit Radius außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Der erzeugte Radius liegt nicht außerhalb des Bereichs `STATIC_RADIUS_WALK_LOW` bis `STATIC_RADIUS_WALK_HIGH`.

#### U9. `StaticRadiusWalk` mit Wartezeit außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Die Wartezeit liegt nicht außerhalb des Bereichs `STATIC_BREAK_TIME_LOW` bis `STATIC_BREAK_TIME_HIGH`.

---

## randomMonsterOrMe(Entity me)

### Kurzbeschreibung

Die Methode `randomMonsterOrMe(Entity me)` liefert ein zufällig ausgewähltes Monster aus dem aktuellen Spiel oder das übergebene Entity zurück.

Als Monster gelten ausschließlich Entities, die sowohl eine `HealthComponent` als auch eine `AIComponent` besitzen.

Existiert mindestens ein Monster, wird eines dieser Monster zurückgegeben. Existiert kein Monster, wird die als Parameter übergebene Entity `me` zurückgegeben.

### Gültige Äquivalenzklassen

#### G1. Mindestens ein Monster vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält mindestens eine Entity mit `HealthComponent` und `AIComponent`.
- `me` ist eine beliebige Entity.

Erwartung:
- Die Methode liefert eine Entity zurück.
- Die zurückgegebene Entity besitzt sowohl eine `HealthComponent` als auch eine `AIComponent`.
- Die Entity entspricht nicht der übergebenen Entity `me`.

#### G2. Kein Monster vorhanden

Vorbedingungen:
- `Game.levelEntities()` enthält keine Entity mit gleichzeitig vorhandener `HealthComponent` und `AIComponent`.
- `me` ist eine beliebige Entity.

Erwartung:
- Die Methode liefert die übergebene Entity `me` zurück.

### Ungültige Äquivalenzklassen

#### U1. Rückgabe von `me` trotz vorhandenem Monster

Vorbedingungen:
- `Game.levelEntities()` enthält mindestens eine Entity mit `HealthComponent` und `AIComponent`.
- `me` ist eine beliebige Entity.

Erwartung:
- Die Methode darf nicht die übergebene Entity `me` zurückgeben.
- Die Rückgabe muss einer vorhandenen Monster-Entity entsprechen.

#### U2. Rückgabe einer Nicht-Monster-Entity bei vorhandenen Monstern

Vorbedingungen:
- `Game.levelEntities()` enthält mindestens eine Entity mit `HealthComponent` und `AIComponent`.
- Zusätzlich existieren Entities, die die Monster-Kriterien nicht erfüllen.

Erwartung:
- Die Methode darf keine Entity ohne vollständige Monster-Kriterien zurückgeben.
- Die Rückgabe muss einer gültigen Monster-Entity entsprechen.

#### U3. Rückgabe einer anderen Entity als `me`, obwohl kein Monster vorhanden ist

Vorbedingungen:
- `Game.levelEntities()` enthält keine Entity mit gleichzeitig vorhandener `HealthComponent` und `AIComponent`.

Erwartung:
- Die Methode muss exakt die übergebene Entity `me` zurückgeben.
- Es darf keine andere Entity zurückgegeben werden.

---

## randomTransition(Entity entity)

### Kurzbeschreibung

Die Methode `randomTransition(Entity entity)` erzeugt zufällig eine Instanz eines verfügbaren `TransitionAI`-Verhaltens und gibt diese als `Function<Entity, Boolean>` zurück.

Abhängig von einer zufälligen Auswahl wird eine der aktuell unterstützten Transition-AIs erzeugt. Dabei werden erforderliche Parameter zufällig innerhalb definierter Wertebereiche generiert. Für Verhaltensweisen, die eine Ziel-Entity benötigen, wird das Ergebnis von `randomMonsterOrMe(entity)` verwendet.

### Gültige Äquivalenzklassen

#### G1. Auswahl eines `RangeTransition`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die Methode liefert eine Instanz von `RangeTransition`.
- Der übergebene Distanzwert liegt im Bereich `RANGE_TRANSITION_LOW` bis `RANGE_TRANSITION_HIGH`.

#### G2. Auswahl eines `SelfDefendTransition`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Methode liefert eine Instanz von `SelfDefendTransition`.

#### G3. Auswahl eines `ProtectOnApproach`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.
- Der Parameter `entity` ist gesetzt.

Erwartung:
- Die Methode liefert eine Instanz von `ProtectOnApproach`.
- Der übergebene Distanzwert liegt im Bereich `PROTECT_RANGE_LOW` bis `PROTECT_RANGE_HIGH`.
- Als Ziel-Entity wird das Ergebnis von `randomMonsterOrMe(entity)` verwendet.

#### G4. Auswahl eines `ProtectOnAttack`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `3`.
- Der Parameter `entity` ist gesetzt.

Erwartung:
- Die Methode liefert eine Instanz von `ProtectOnAttack`.
- Als Ziel-Entity wird das Ergebnis von `randomMonsterOrMe(entity)` verwendet.

### Ungültige Äquivalenzklassen

#### U1. Rückgabe eines nicht unterstützten TransitionAI-Typs

Vorbedingungen:
- Beliebiger gültiger Aufruf der Methode.

Erwartung:
- Die Rückgabe entspricht ausschließlich einer der aktuell unterstützten Implementierungen:
  - `RangeTransition`
  - `SelfDefendTransition`
  - `ProtectOnApproach`
  - `ProtectOnAttack`

#### U2. `RangeTransition` mit Wert außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Der übergebene Distanzwert liegt nicht außerhalb des Bereichs `RANGE_TRANSITION_LOW` bis `RANGE_TRANSITION_HIGH`.

#### U3. `ProtectOnApproach` mit Reichweite außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- DDer übergebene Distanzwert liegt nicht außerhalb des Bereichs `PROTECT_RANGE_LOW` bis `PROTECT_RANGE_HIGH`.

---

## randomFightAI()

### Kurzbeschreibung

Die Methode `randomFightAI()` erzeugt zufällig eine Instanz eines verfügbaren FightAI-Verhaltens und gibt diese als `Consumer<Entity>` zurück.

Abhängig von einer zufälligen Auswahl wird eine der aktuell unterstützten FightAI-Implementierungen erzeugt. Dabei werden die erforderlichen Parameter zufällig innerhalb der definierten Wertebereiche generiert.

### Gültige Äquivalenzklassen

#### G1. Auswahl eines `AIChaseBehaviour`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die Methode liefert eine Instanz von `AIChaseBehaviour`.
- Die erzeugte Reichweite liegt im Bereich `RUSH_RANGE_LOW` bis `RUSH_RANGE_HIGH`.

#### G2. Auswahl eines `AIRangeBehaviour`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Methode liefert eine Instanz von `AIRangeBehaviour`.
- Die Angriffsreichweite liegt im Bereich `ATTACK_RANGE_LOW` bis `ATTACK_RANGE_HIGH`.
- Die Distanz liegt im Bereich `DISTANCE_LOW` bis `DISTANCE_HIGH`.
- Das Verhalten verwendet eine `FireballSkill`.
- Die `FireballSkill` verwendet den Cooldown `FIREBALL_COOL_DOWN`.

#### G3. Auswahl eines `AIMeleeBehaviour`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Die Methode liefert eine Instanz von `AIMeleeBehaviour`.
- Die erzeugte Verfolgungsreichweite liegt im Bereich `RUSH_RANGE_LOW` bis `RUSH_RANGE_HIGH`.
- Die erzeugte Angriffsreichweite beträgt `1f`.
- Das Verhalten verwendet eine `FireballSkill`.
- Die `FireballSkill` verwendet den Cooldown `FIREBALL_COOL_DOWN`.

### Ungültige Äquivalenzklassen

#### U1. Rückgabe eines nicht unterstützten FightAI-Typs

Vorbedingungen:
- Beliebiger gültiger Aufruf der Methode.

Erwartung:
- Die Rückgabe entspricht ausschließlich einer der aktuell unterstützten Implementierungen:
  - `AIChaseBehaviour`
  - `AIRangeBehaviour`
  - `AIMeleeBehaviour`

#### U2. `AIChaseBehaviour` mit Reichweite außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `0`.

Erwartung:
- Die erzeugte Reichweite liegt nicht außerhalb des Bereichs `RUSH_RANGE_LOW` bis `RUSH_RANGE_HIGH`.

#### U3. `AIRangeBehaviour` mit Angriffsreichweite außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Angriffsreichweite liegt nicht außerhalb des Bereichs `ATTACK_RANGE_LOW` bis `ATTACK_RANGE_HIGH`.

#### U4. `AIRangeBehaviour` mit Distanz außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Die Distanz liegt nicht außerhalb des Bereichs `DISTANCE_LOW` bis `DISTANCE_HIGH`.

#### U5. `AIRangeBehaviour` ohne korrekte `FireballSkill`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `1`.

Erwartung:
- Das Verhalten verwendet eine `FireballSkill`.
- Der verwendete Cooldown entspricht `FIREBALL_COOL_DOWN`.

#### U6. `AIMeleeBehaviour` mit Verfolgungsreichweite außerhalb des definierten Wertebereichs

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Die erzeugte Verfolgungsreichweite liegt nicht außerhalb des Bereichs `RUSH_RANGE_LOW` bis `RUSH_RANGE_HIGH`.

#### U7. `AIMeleeBehaviour` mit abweichender Angriffsreichweite

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Die Angriffsreichweite entspricht exakt `1f`.

#### U8. `AIMeleeBehaviour` ohne korrekte `FireballSkill`

Vorbedingungen:
- Die zufällige Auswahl ergibt den Index `2`.

Erwartung:
- Das Verhalten verwendet eine `FireballSkill`.
- Der verwendete Cooldown entspricht `FIREBALL_COOL_DOWN`.

---

## randomAI(Entity entity)

### Kurzbeschreibung

Die Methode `randomAI(Entity entity)` erzeugt ein vollständig initialisiertes `AIComponent`, bestehend aus zufälligen Fight-, Idle- und Transition-Verhalten.

Dabei werden intern die Methoden `randomFightAI()`, `randomIdleAI()` und `randomTransition(entity)` verwendet. Das erzeugte `AIComponent` wird nicht automatisch einer Entity zugewiesen, sondern nur zurückgegeben.

### Gültige Äquivalenzklassen

#### G1. Erfolgreiche Erzeugung eines vollständigen AIComponents

Vorbedingungen:
- `randomFightAI()` liefert ein gültiges Fight-AI-Verhalten.
- `randomIdleAI()` liefert ein gültiges Idle-AI-Verhalten.
- `randomTransition(entity)` liefert ein gültiges Transition-Verhalten.

Erwartung:
- Die Methode gibt ein nicht-null `AIComponent` zurück.
- Das `AIComponent` enthält:
  - ein gültiges Fight-AI-Verhalten
  - ein gültiges Idle-AI-Verhalten
  - ein gültiges Transition-Verhalten
- Das zurückgegebene Component ist nicht automatisch an eine Entity gebunden.

### Ungültige Äquivalenzklassen

#### U1. Rückgabe eines unvollständigen AIComponents

Vorbedingungen:
- Beliebiger gültiger Aufruf.

Erwartung:
- Das `AIComponent` enthält immer alle drei Teile:
  - FightAI
  - IdleAI
  - TransitionAI
- Kein Teil darf `null` sein.
