# Dungeon-Breaker: Repository-Überblick & Teststrategie

Dieses Dokument bietet einen umfassenden Überblick über das **Dungeon-Breaker**-Projekt, analysiert die Struktur und Codegröße, definiert Testfokusbereiche, stellt Strategien für Deadlines und Code-Coverage vor und schlägt eine konkrete Ordnerstruktur sowie Konventionen für die Testentwicklung vor.

---

## 1. Überblick über den Dungeon

Der **Dungeon** ist ein 2D-Roguelike-Spiel-Framework, das in **Java** unter Verwendung des Game-Development-Frameworks **LibGDX** entwickelt wurde. Das Projekt ist als **Multi-Projekt-Gradle-Build** strukturiert und nutzt eine **Entity-Component-System (ECS)**-Architektur.

### Projektmodule (Subprojekte)
Das Repository ist in folgende Gradle-Subprojekte unterteilt:
* **`dungeon`**: Der Kern des Spiels und des Frameworks. Hier befinden sich die ECS-Engine, die Standard-Systeme und die spielweiten Gameplay-Komponenten.
* **`portal`**: Erweiterung, die Portal-Mechaniken (ähnlich wie das Spiel *Portal*) implementiert.
* **`escapeRoom`**: Ein Subprojekt zur Realisierung von Escape-Room-Rätseln.
* **`theLastHourEscapeRoom`**: Ein eigenständiges Escape-Room-Szenario.
* **`blockly`**: Integration von Blockly zur visuellen Programmierung oder Steuerung.
* **`advancedDungeon`**: Fortgeschrittene Dungeon-Komponenten und Maps.

---

## 2. Wo ist die Hauptlogik & was lohnt sich zu testen?

Da wir uns in der Testpyramide auf **Unit-** (Methodenebene) und **Component-Tests** (Klassenebene) sowie einfache **Integrationstests** (Zusammenspiel mehrerer Klassen) fokussieren, gibt es klare Prioritäten für die Testabdeckung:

### A. Core ECS Engine (`dungeon/src/core`)
Hier liegt die absolute Basis des gesamten Spiels. Fehler hier führen zum Absturz des gesamten Spiels.
* **`Game.java`**: Verwaltet den Game-Loop, Entities und geladene Systeme.
* **`Entity.java` & `Component.java`**: Basisklassen für das ECS-System.
* **`LevelSystem.java` & `core.level`**: Prozedurale Generierung und Laden von Leveln.
* **Core Systems**: `MoveSystem.java`, `VelocitySystem.java` (Bewegungsphysik).

### B. Gameplay-Logik (`dungeon/src/contrib`)
Hier befinden sich die eigentlichen Spielmechaniken, die am fehleranfälligsten bei Änderungen sind:
* **`CollisionSystem` & `CollideComponent`**: Physische Kollisionsberechnungen, Trigger und Hitboxen.
* **`HealthSystem` & `HealthComponent`**: Berechnung von Schaden, Lebenspunkten, God-Mode und Death-Events.
* **`InventoryComponent` & `Item`**: Stackgrößen, Hinzufügen, Entfernen und Transferieren von Items.
* **`SkillComponent` & Skills**: Abklingzeiten, Aktivierung von Fähigkeiten.
* **`AISystem` & `AIFactory`**: Zustandsautomaten der Monster, Wegfindung und KI-Transitionen.

### C. Netzwerkcode (`dungeon/src/core/network`)
* Da Multiplayer-Funktionen implementiert sind, ist die Logik zur Paketserialisierung (Protobuf) und Zustandssynchronisation kritisch für ein stabiles Spielerlebnis.

---

## 3. Codegröße (Wie viel ist das?)

Eine statistische Analyse der Java-Dateien im Repository zeigt folgende Verteilung:

| Subprojekt | Anzahl `.java`-Dateien | Beschreibung |
|---|---|---|
| **`dungeon`** | **567** | Kern-Engine und Framework (~73% des Codes) |
| * davon `core` | *250* | ECS-Core, Basis-Komponenten & Netzwerk |
| * davon `contrib` | *234* | Gameplay, HUD, Items, Systeme & Utilities |
| * davon `starter` | *3* | Einstiegspunkte (Basic, Multiplayer) |
| **`portal`** | **52** | Portal-Erweiterungen (Tractor Beam, Light Walls) |
| **`blockly`** | **52** | Blockly-Visual-Scripting-Anbindung |
| **`escapeRoom`** | **37** | Escape-Room-Rätsellogik |
| **`theLastHourEscapeRoom`** | **36** | Spezifisches Escape-Room-Szenario |
| **`advancedDungeon`** | **29** | Fortgeschrittene Dungeon-Mechaniken |
| **Gesamt (Code)** | **773** | Java-Dateien im gesamten Workspace |

### Aktueller Testbestand
Im Repository existieren derzeit **87 Testdateien** (davon **80 im `dungeon`-Modul**). Das bedeutet, dass ein Großteil der 567 Dungeon-Klassen noch keine dedizierten Tests besitzt – hier besteht das primäre Betätigungsfeld für uns als Test-Entwickler.

---

## 4. Deadline-Management (Wie setzen wir Deadlines?)

Deadlines sollten sich direkt an den **Milestones (Epics)** und den zugehörigen Issues orientieren. Da jedes Epic in einem eigenen Milestone-Branch bearbeitet wird, können wir Deadlines wie folgt planen:

1. **Komplexitätsgewichtung (Estimation)**:
   * **S (Einfache Unit-Tests)**: Reine Getter/Setter, simple mathematische Hilfsmethoden. 
     * *Richtwert*: ~1–2 Std. pro Methode.
   * **M (Komponente/Klasse)**: Logik mit Zuständen (z. B. `InventoryComponent.add(Item)` mit Stack-Logik). Benötigt Mocking von Nachbarklassen.
     * *Richtwert*: ~4–6 Std. pro Klasse/Komponente.
   * **L (Komplexes System / Integration)**: Zusammenwirken von Systemen (z. B. `CollisionSystem` interagiert mit `HealthSystem` bei Schaden).
     * *Richtwert*: 1–2 Tage pro Integrationsszenario.

2. **Sprint/Milestone-Dauer**:
   * Ein Milestone-Branch sollte eine feste Laufzeit haben (z. B. **2 Wochen**).
   * Die Summe der geschätzten Aufwände der Issues im Milestone bestimmt den Release-Termin des Branches in den `main`-Branch.

---

## 5. Code-Coverage-Ziele

Eine pauschale Code-Coverage von 100% ist meist ineffizient (z. B. bei reinem UI-Code oder LibGDX-Grafik-Rendering). Wir empfehlen stattdessen **differenzierte Abdeckungsraten** je nach Epic/Komponente:

* **Core Engine (`core/components`, `core/systems`)**: **85% – 90%**
  * *Begründung*: Fundamentale Funktionen. Fehler hier betreffen das gesamte Spiel.
* **Gameplay-Logik (`contrib/components`, `contrib/systems`)**: **75% – 85%**
  * *Begründung*: Logiklastige Teile wie `HealthComponent` und `InventoryComponent` müssen extrem stabil sein.
* **Netzwerk & Synchronisation (`core/network`)**: **60% – 70%**
  * *Begründung*: Testen der Serialisierung und Protokolle mittels Mocks.
* **UI/HUD & Visuals (`contrib/hud`, Blockly, `DrawSystem`)**: **30% – 50%**
  * *Begründung*: Sehr schwer automatisiert als Unit-Test zu prüfen (LibGDX-Render-Kontext). Hier sollte eher manuell verifiziert werden.

---

## 6. Testordner-Struktur & Konventionen

Da die Testdesigners für **jede Methode ein eigenes Issue** anlegen (z. B. `#82 add(Item)`, `#83 remove(Item)`), müssen wir uns auf eine klare Struktur einigen.

### A. Wie sollte die Testordner-Struktur aussehen?
Die Ordnerstruktur unter `dungeon/test/` muss **exakt spiegelbildlich** zu `dungeon/src/` aufgebaut sein.

#### Option 1: Eine Testdatei pro Methode (Sehr granular)
Wenn wir das Prinzip *"Pro Methode eine Testdatei"* strikt umsetzen wollen, erstellen wir pro Klasse einen Unterordner (Package) und legen darin die Testdateien ab:

```text
dungeon/
└── test/
    └── contrib/
        └── components/
            └── inventorycomponent/               <-- Ordner für die Klasse
                ├── InventoryComponentTest.java   <-- Basisklasse (SetUp, Mocks)
                ├── AddTest.java                  <-- Testet nur add(Item) (Issue #82)
                ├── RemoveTest.java               <-- Testet nur remove(Item) (Issue #83)
                └── HasItemTest.java              <-- Testet nur hasItem(Item) (Issue #87)
```

> [!TIP]
> **Vorteil**: Da mehrere Tester-Entwickler gleichzeitig an verschiedenen Methoden derselben Klasse arbeiten, gibt es **keine Git-Merge-Conflicts** in den Testdateien. Jede Testdatei bleibt extrem klein und übersichtlich.
>
> **Umsetzung**: Die einzelnen Testklassen erben von einer gemeinsamen Basisklasse (z. B. `InventoryComponentTest`), die die Test-Instanz initialisiert (mit `@BeforeEach setUp()`).

#### Option 2: Eine Testdatei pro Klasse mit Inneren Klassen (Standard)
Alternativ nutzt man eine einzige Testdatei pro Klasse und strukturiert diese intern mit JUnit 5 `@Nested`-Klassen pro Methode:

```java
class InventoryComponentTest {
    private InventoryComponent component;

    @BeforeEach
    void setUp() { ... }

    @Nested
    class Add { // Repräsentiert Issue #82
        @Test
        void shouldAddItemWhenSlotAvailable() { ... }
    }

    @Nested
    class Remove { // Repräsentiert Issue #83
        @Test
        void shouldRemoveItemFromSlot() { ... }
    }
}
```

> [!WARNING]
> **Nachteil**: Wenn Tester A an `add()` (Branch A) und Tester B an `remove()` (Branch B) arbeitet, kommt es beim Zusammenführen in `InventoryComponentTest.java` mit hoher Wahrscheinlichkeit zu Git-Merge-Konflikten.

**Empfehlung**: 
Aufgrund der feingranularen Issue-Struktur der Testdesigner empfehlen wir **Option 1 (Subfolder pro Klasse, ein Testfile pro Methode)**, da dies die parallele Arbeit im Team massiv erleichtert.

---

### B. Branch Naming & Commit-Regeln

Um die Nachverfolgbarkeit zu gewährleisten, nutzen wir folgende Konventionen:

#### Branch Naming
1. **Milestone-Branches** (Epics): 
   * `milestone/<milestone-id>-<epic-name>`
   * Beispiel: `milestone/3-inventory-system`
2. **Feature-Branches** (Arbeitsbranches für Entwickler):
   * `feature/<issue-id>-<short-description>`
   * Beispiel: `feature/82-inventory-add-item`

#### Commit Message & Pull Requests
Jeder Commit und PR-Titel muss die führende Issue-Nummer tragen, um die automatische Verknüpfung in GitHub zu nutzen:

* **Commit-Format**: `#<issue-id> [Klasse - Methode] Beschreibung`
  * Beispiel: `#82 [InventoryComponent - add] test adding item to free slot`
* **Zusammenführen (PRs)**: 
  * Der PR-Titel lautet z. B. `Closes #82: Implement unit tests for InventoryComponent.add(Item)`
