# PayVault — Slide Reference Index

When you're stuck, check the slides **before** asking for a hint. Slide numbers match the course decks in your `Desktop\java` folder.

- [By milestone](#by-milestone): which slides to open for each milestone
- [By deck](#by-deck): a topic → slide index for every deck
- [Not in the slides](#not-in-the-slides): things PayVault uses that the decks don't cover
- [Known slide errors](#known-slide-errors): examples that won't compile as written

---

## By milestone

| Milestone | Open these first |
|---|---|
| **M0** Project setup | U5: Maven structure 20, `pom.xml` 21, dependencies 22, plugins 23, lifecycle 24, commands 25 · U2: Maven project structure 5, packages 6–12 |
| **M1** Account model | U2: classes 13–20, constructors 21–23, `this` 24–27 · U3: method overloading 2–5, constructor overloading 6–8, access control 17–24, static members 26–28, `final` 29 · U4: nested classes 2–5, inheritance 9–13, superclass reference 14–15, `super` 16–19, overriding 22–25, abstract classes 26–30, interfaces 31–38 · U5: naming 3–6, class organisation 12 · U7: `LocalDateTime` 161 |
| **M2** Exceptions | U6: checked vs unchecked 2–4, hierarchy 14–15, try/catch 17–18, multiple catch + ordering 7–9, 18, `finally` 19–20, `throw` vs `throws` 25–28, custom exceptions 29–39 |
| **M3** `transfer()` | U3: objects as parameters 9–11, pass by value vs reference 12–14, BankAccount problem 16 · U6: propagation 21–24 |
| **M4** Collections | U7: hierarchy 5, ArrayList 7–27, LinkedList as queue/stack 37–40, ArrayList vs LinkedList 42, HashSet 43–58, HashMap 59–70, HashSet vs HashMap 71, Iterator + `remove()` 72–79, Comparator 80–90, generics 91–121, `Objects.hash`/`equals` 126, 129 |
| **M5** Console menu + PIN lock | U2: `Scanner` 10 · U7: `Optional` 134–144 |
| **M6** Serialization + `FileAccountDAO` | U9: why 2–4, steps 6, `Serializable` marker 7–8, `ObjectOutputStream` 9–15, `ObjectInputStream` / `readObject()` 16–19, object graph 27–28, shopping-cart example 30–32 · U11: DAO pattern 64–77 |
| **M7** Static-ID bug, statement export | U9: version problem + `serialVersionUID` 20–22, `transient` 23–25, **static fields 26** · U8: data streams 36–40, `File` object 42–43, `FileWriter` 48–49, `BufferedWriter` 53–54 |
| **M8** CSV import + backup | U8: `BufferedReader.readLine()` 50–52, byte-stream copy 26, buffered byte streams 30–35, try-with-resources 27–29 |
| **M9** Logging | U10: Logger 9–11, logging methods 12–13, levels + default INFO 14–17, handlers + duplicate output 18–21, formatters 22–24, Filter 25–30 |
| **M10** MySQL + connection | U11: architecture 4–7, components 8–15, connection URLs 16, `getConnection` 17, SQL 18–21, JDBC steps 22–23 |
| **M11** `JdbcAccountDAO` | U11: Statement 29–31, PreparedStatement 32–34, 38–40, comparison table 41, ResultSet 42–44, CRUD example 51–53 |
| **M12** Transactional transfer | U11: transactions 54, ACID 55, auto-commit 56, commit/rollback 57, transaction program 58–63 |
| **M13** Interest procedure + report | U11: CallableStatement 35–37, ResultSet types 45–47, navigation methods 48–50 |
| **M14** JavaFX setup + login | U12: architecture 3–4, lifecycle 5, 15–27 (comparison table 27), Stage/Scene graph 6–11, single-threaded 7, first program 12–13, GridPane 44–49 |
| **M15** Dashboard + history | U12: BorderPane 50–54, StackPane 55–59, HBox 36–39, VBox 40–43 · U13: listeners 17–22 |
| **M16** Transfer dialog | U12: secondary Stage 31–32, modality 33 · U13: handlers 6–16, validation 23–28, alerts 30–34, dialogs 35–38 |
| **M17** Admin + polish | U13: notifications 39–40, login example 41–43, summary 44 |
| **M18** Final review | All "EST prep" lists in `PROJECT_PLAN.md` |

---

## By deck

### Unit 2 — Project Structure, Packages and Classes
| Topic | Slides |
|---|---|
| Project structure, layers | 2–4 |
| Maven project structure | 5 |
| Built-in packages, `java.lang` | 6–9 |
| `Scanner` input | 10 |
| User-defined packages | 11–12 |
| Classes and objects | 13–20 |
| Object reference assignment | 17 |
| Constructors, parameterised | 21–23 |
| `this` keyword | 24–27 |
| Garbage collection, `finalize()` (deprecated) | 28–31 |

### Unit 3 — Method and Constructor Overloading
| Topic | Slides |
|---|---|
| Method overloading | 2–4 |
| Automatic conversion in overload resolution | 5 |
| Constructor overloading | 6–8 |
| Objects as parameters, copy constructor | 9–11 |
| Pass by value vs pass by reference | 12–14 |
| Returning objects | 15 |
| BankAccount programming problem | 16 |
| Access control: default, public, private, protected | 17–21 |
| Access modifier summary table | 24 |
| `static` members, static block | 26–28 |
| `final` | 29 |

### Unit 4 — Nested Classes, Inheritance, Abstract Classes, Interfaces
| Topic | Slides |
|---|---|
| Nested classes: static vs inner | 2–5 |
| Command-line arguments | 6–7 |
| Varargs | 8 |
| Inheritance, member access | 9–11 |
| Constructor calling order | 12 |
| Multilevel inheritance | 13 |
| Superclass reference to subclass object | 14–15 |
| `super` (variables, methods, constructors) | 16–19 |
| `final` class | 20 |
| Method overriding + rules | 22–25 |
| Abstract classes + rules | 26–30 |
| Interfaces, default/private methods | 31–34 |
| Multiple interfaces, conflicts | 35–37 |
| Partial implementation → abstract | 38 |
| Nested interfaces | 39–41 |
| Interface variables (constants) | 42–43 |
| Extending interfaces | 44 |
| Summary | 45 |

### Unit 5 — Coding Conventions and Build Tools
| Topic | Slides |
|---|---|
| Naming conventions | 3–6, 13 |
| Formatting, comments | 7–10 |
| Class organisation order | 12 |
| What a build tool does | 17–18 |
| Maven: structure, pom, dependencies, plugins | 19–23 |
| Maven lifecycle + commands | 24–25 |
| Gradle: files, tasks, wrapper | 26–30 |
| Maven vs Gradle, philosophies | 31–35 |
| Transitive dependencies | 36 |
| Local/remote repositories (`~/.m2`) | 37 |
| Build tool vs IDE | 38 |

### Unit 6 — Exception Handling
| Topic | Slides |
|---|---|
| Exceptions vs errors, checked vs unchecked | 2–4 |
| try/catch/finally examples | 5–6 |
| Multiple catch + ordering rules | 7–9, 18 |
| Exception class hierarchy (`Throwable`) | 14–15 |
| Compiler-enforced handling | 16 |
| `finally` clause | 19–20 |
| Exception propagation | 21–24 |
| `throws` vs `throw` | 25–28 |
| Creating custom exceptions | 29–32 |
| Full custom-exception example | 33–39 |

### Unit 7 — Collection Framework
| Topic | Slides |
|---|---|
| Hierarchy | 5 |
| List interface | 6 |
| ArrayList + methods | 7–27 |
| LinkedList + methods | 28–36 |
| LinkedList as queue (`offer`/`peek`/`poll`) | 37–38 |
| LinkedList as stack (`push`/`pop`) | 39–40 |
| ArrayList vs LinkedList | 42 |
| HashSet + set operations | 43–58 |
| HashMap + methods | 59–70 |
| HashSet vs HashMap | 71 |
| Iterator, safe `remove()` | 72–79 |
| Comparator, user-defined objects | 80–90 |
| Generics: classes, methods, interfaces | 91–121 |
| `Objects` utility class | 124–133 |
| `Optional` | 134–144 |
| `Math` | 145–157 |
| `LocalDate` / `LocalTime` / `LocalDateTime` | 158–162 |

### Unit 8 — File Handling
| Topic | Slides |
|---|---|
| Streams, input vs output | 2–4 |
| Byte vs character streams | 5 |
| Byte stream classes + methods | 6–10 |
| Character stream classes | 11–13 |
| `FileInputStream`, `read()` → −1 | 16–19 |
| Exceptions during file operations | 20 |
| Closing in `finally` | 21–22 |
| `FileOutputStream`, `write()` | 23–25 |
| Copying a file | 26 |
| try-with-resources | 27–29 |
| Buffered byte streams, `mark`/`skip`/`reset` | 30–35 |
| `DataInputStream` / `DataOutputStream` | 36–40 |
| `File` object, `createNewFile()` | 42–43 |
| `FileReader` / `FileWriter` | 46–49 |
| `BufferedReader` (`readLine()`) | 50–52 |
| `BufferedWriter` (`newLine()`, `flush()`) | 53–54 |
| `CharArrayReader` / `CharArrayWriter` | 55–59 |
| Stream pairs comparison | 60 |

### Unit 9 — Serialization
| Topic | Slides |
|---|---|
| Why serialization, definitions | 2–4 |
| Steps | 6 |
| `Serializable` marker interface | 7–8 |
| `ObjectOutputStream`, `writeObject()` | 9–13 |
| Serialization example | 14–15 |
| `ObjectInputStream`, `readObject()` | 16–19 |
| Version compatibility, `serialVersionUID` | 20–22 |
| `transient` | 23–25 |
| Static variables | 26 |
| Object graph | 27–28 |
| Shared references | 29 |
| Shopping-cart example | 30–32 |
| Circular references | 33–35 |

### Unit 10 — Logging
| Topic | Slides |
|---|---|
| What logging is, why | 2–4 |
| Frameworks (JUL, Log4j, SLF4J, Logback) | 5 |
| JUL components | 6–8 |
| Creating a Logger | 9–11 |
| Logging methods | 12–13 |
| Levels, default INFO, `setLevel` | 14–17 |
| Handlers, duplicate output | 18–21 |
| Formatters (Simple / XML) | 22–24 |
| Filter, `isLoggable`, lambda version | 25–30 |
| JUL limitations, Log4j | 31–32 |

### Unit 11 — JDBC
| Topic | Slides |
|---|---|
| What JDBC is, why | 2–3 |
| Architecture, 2-tier / 3-tier | 4–7 |
| Product components, driver types, ODBC bridge | 8–15 |
| MySQL vs SQLite, URLs | 16 |
| Establishing a connection | 17 |
| SQL basics | 18–21 |
| Workflow / steps | 22–23 |
| Insert / delete / select with Statement | 26–28 |
| Statement + execute methods | 29–31 |
| PreparedStatement | 32–34, 38–40 |
| CallableStatement | 35–37 |
| Statement vs Prepared vs Callable table | 41 |
| ResultSet basics | 42–44 |
| ResultSet types / concurrency / holdability | 45–47 |
| ResultSet methods (navigate / get / update) | 48–50 |
| CRUD menu example | 51–53 |
| Transactions, ACID, auto-commit, commit/rollback | 54–57 |
| Transaction example program | 58–63 |
| DAO pattern, components, example | 64–75 |
| DAO advantages / disadvantages | 76–77 |

### Unit 12 — JavaFX GUI
| Topic | Slides |
|---|---|
| Intro, architecture | 2–4 |
| Lifecycle overview | 5 |
| Stage, scene graph, nodes, coordinates | 6, 8–11 |
| Single-threaded UI | 7 |
| First program | 12–13 |
| Running from the command line | 14 |
| Application structure | 15–18 |
| `init()` | 19–20 |
| `start()`, Stage and Scene | 21–24 |
| `stop()` | 25–26 |
| init vs start vs stop table | 27 |
| Primary / secondary Stage | 29–32 |
| Modality | 33 |
| Why layout panes | 34–35 |
| HBox | 36–39 |
| VBox | 40–43 |
| GridPane, spanning | 44–49 |
| BorderPane | 50–54 |
| StackPane | 55–59 |

### Unit 13 — JavaFX Event-Driven Programming
| Topic | Slides |
|---|---|
| What events are, examples | 2–5 |
| Source, object, handler | 6–9 |
| `EventHandler<T>`, anonymous class | 10–12 |
| Lambda handlers, comparison | 13–14 |
| `setOnXxx` registration methods | 15 |
| Multiple buttons | 16 |
| Listeners, handler vs listener | 17–18 |
| `ChangeListener`, TextField example | 19–21 |
| Other observable properties | 22 |
| Input validation (4 types) | 23–25 |
| Full validation example | 26–28 |
| User feedback | 29 |
| Alerts + types table | 30–34 |
| Dialogs (TextInput, Choice, custom) | 35–38 |
| Notifications vs dialogs | 39–40 |
| Login application example | 41–43 |
| Summary table | 44 |

Unit 1 (Introduction to Java) is an old `.ppt` file and isn't indexed here. It covers basics you'll mostly use without needing to look up.

---

## Not in the slides

PayVault uses a few things the decks don't cover. Use the official Java 21 docs (or ask for a hint):

| Topic | Used in | Where to read |
|---|---|---|
| `BigDecimal`, `RoundingMode` | M1 onwards | [BigDecimal (Java 21 API)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html) |
| `enum` | M1 | [Enum types (Java tutorial)](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html) |
| Builder pattern | M1 | Concept from U4 static nested classes (2–5); ask for "M1 hint" |
| `Collections.unmodifiableList` | M1 | [Collections (Java 21 API)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html) |
| `TreeMap` | M4 | [TreeMap (Java 21 API)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html). Same methods as HashMap (U7 59–70), but keys stay sorted |
| `ArrayDeque` | M4 | Same idea as LinkedList as queue/stack (U7 37–40) |
| JUnit 5 | optional | [JUnit 5 user guide](https://junit.org/junit5/docs/current/user-guide/) |
| `TableView` | M15 | [JavaFX TableView](https://openjfx.io/javadoc/21/javafx.controls/javafx/scene/control/TableView.html) |
| JavaFX CSS | M17 | [JavaFX CSS reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html) |

---

## Known slide errors

These examples won't compile or misbehave exactly as written. Don't copy them blindly:

| Deck | Slide | Problem |
|---|---|---|
| U8 File Handling | 35 | Stray `}` puts `bufferOut` out of scope; `flush()` is called after `close()` |
| U8 File Handling | 49 | `writer` declared inside `try` but used in `finally`, so it won't compile. Declare it before `try`. |
| U8 File Handling | 59 | Missing `import java.io.CharArrayWriter` |
| U11 JDBC | 40 | Calls `executeQuery(String)` on a `PreparedStatement`, which throws `SQLException`. Use a separate `Statement`. |
| U11 JDBC | 26–28, 40 | `com.mysql.jdbc.Driver` is the old class name (now `com.mysql.cj.jdbc.Driver`); `Class.forName` isn't needed since JDBC 4 |
| U11 JDBC | 15 | The JDBC-ODBC bridge was removed in Java 8. Learn it for theory only. |
| U12 JavaFX GUI | 32 | Imports `javafx.scene.*`, but `Button`/`Label` are in `javafx.scene.control` |
