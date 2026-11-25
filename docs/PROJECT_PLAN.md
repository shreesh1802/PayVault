# PayVault — Detailed Project Plan

Each phase lists what to build, which syllabus concepts it uses, and an **📝 EST prep** list: the exam topics that phase drills. Tick items off as you finish them.

---

## Phase 1 — Core logic (Units 1–7) · console app

Build everything with a `Scanner` menu first. No files, no database, no GUI.

### 1.1 Object model (Units 3, 4)

```java
public abstract class Account {                        // abstract class
    private final String accountNo;
    private BigDecimal balance;                        // BigDecimal, not double: no rounding errors
    protected List<Transaction> history = new ArrayList<>();

    public Account(String no) { this(no, BigDecimal.ZERO); }        // constructor overloading
    public Account(String no, BigDecimal opening) { ... }           // + chaining with this(...)

    public abstract BigDecimal withdrawableAmount();   // each subclass decides
    public void deposit(BigDecimal amt) { ... }                     // method overloading
    public void deposit(BigDecimal amt, String note) { ... }
}

public class SavingsAccount extends Account implements InterestBearing { ... }  // min balance ₹1000
public class CurrentAccount extends Account { ... }                             // overdraft ₹5000
public interface InterestBearing { BigDecimal calculateInterest(); }
```

- [ ] `Account` (abstract), `SavingsAccount`, `CurrentAccount`
- [ ] `InterestBearing` interface
- [ ] `Transaction` with a `static class Builder` (static nested class)
- [ ] `TransactionType` enum (`DEPOSIT`, `WITHDRAW`, `TRANSFER_IN`, `TRANSFER_OUT`, `INTEREST`)
- [ ] `Customer` (name, phone, PIN hash, accounts)
- [ ] `equals()` / `hashCode()` on `Account`, based on `accountNo`
- [ ] One `Comparator` written as an **anonymous class**, then rewritten as a lambda
- [ ] `WalletService.withdraw(Account a, ...)` uses `a.withdrawableAmount()` with **no** `instanceof` (polymorphism)

### 1.2 Exceptions (Unit 6)

```
WalletException (checked, extends Exception)
 ├── InsufficientBalanceException   (carries the shortfall amount)
 ├── AccountNotFoundException
 ├── AccountFrozenException
 └── DailyLimitExceededException
InvalidAmountException (unchecked, extends RuntimeException)  ← contrast on purpose
```

- [ ] The exception classes above
- [ ] Use `throw` and `throws` correctly; know why one is checked and one isn't
- [ ] At least one multi-catch block
- [ ] A `finally` block (e.g. closing the `Scanner`)

### 1.3 Collections (Unit 7)

| Collection | Used for | Why this one |
|---|---|---|
| `HashMap<String, Account>` | accounts by account number | O(1) lookup |
| `ArrayList<Transaction>` | history | ordered, indexed |
| `TreeMap<YearMonth, List<Transaction>>` | monthly statement | keys come out sorted |
| `HashSet<String>` | frozen accounts | fast `contains`, no duplicates |
| `ArrayDeque<Transaction>` | "last 5 transactions" widget | add at front, trim at the back |
| `Comparator.comparing(...).thenComparing(...)` | sort history by amount, then date | |
| `Iterator.remove()` | purge old pending entries safely | avoids `ConcurrentModificationException` |

- [ ] All of the above used somewhere real

### 1.4 The core algorithm — `transfer(from, to, amount)`

Write this one entirely by yourself. Checks, in order:

1. amount > 0 and at most 2 decimal places
2. both accounts exist
3. not the same account
4. neither account frozen
5. within today's transfer limit
6. enough withdrawable funds
7. debit → credit → record two transactions

- [ ] `transfer()` implemented
- [ ] Each failed check throws the right exception with a clear message
- [ ] Console menu: register, login, balance, deposit, withdraw, transfer, history, exit

**📝 EST prep:** abstract class vs interface · overloading vs overriding · constructor chaining · `super` / `this` · static nested vs inner vs anonymous classes · checked vs unchecked exceptions · exception hierarchy · `finally` behaviour · choosing List/Set/Map · `HashMap` internals · `Comparable` vs `Comparator` · fail-fast iterators

---

## Phase 2 — Persistence & logging (Units 8, 9, 10)

### 2.1 Serialization (Unit 9) — saving the whole wallet

```java
class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient String sessionToken;   // not saved, comes back as null
}
```

- [ ] Save the whole `HashMap<String, Account>` with **one** `writeObject()` call (object graph: accounts + their transaction lists)
- [ ] Load it back with `readObject()` + a cast; handle `ClassNotFoundException`
- [ ] **Experiment:** remove `serialVersionUID`, save, add a field, load → see `InvalidClassException`. Put it back → it works. Note the result in DESIGN.md.
- [ ] **Bug to discover:** `IdGenerator` uses `static int nextAccountNo`. After a restart new accounts get duplicate numbers, because **static fields are not serialized**. Fixed in 2.2.

### 2.2 File handling (Unit 8)

| Feature | Classes used | Concept |
|---|---|---|
| Store the ID counter between runs | `DataOutputStream.writeInt` / `DataInputStream.readInt` | Data streams for primitives (fixes the static bug) |
| Export a monthly statement `.txt` | `BufferedWriter` + `FileWriter`, `newLine()` | Character streams |
| Bulk-import accounts from CSV | `BufferedReader.readLine()` + `split(",")` | Read line by line; skip and log bad rows |
| Backup `wallet.dat` → `backup/wallet_<date>.dat` | `BufferedInputStream` / `BufferedOutputStream` | Byte-stream copy |
| Create `exports/` and `backup/` folders | `File.exists()`, `mkdirs()` | A `File` object is not the file itself |

- [ ] All five features
- [ ] Every stream in **try-with-resources**
- [ ] Write the backup once with `finally` first, then convert it, so you can explain the difference

### 2.3 Logging (Unit 10)

```java
private static final Logger log = Logger.getLogger(WalletService.class.getName());
```

- [ ] `logs/wallet.log`: everything `INFO` and above (`FileHandler` + `SimpleFormatter`)
- [ ] `logs/security.log`: only `WARNING` / `SEVERE` (wrong PINs, frozen-account attempts) using a **Filter** lambda
- [ ] `setUseParentHandlers(false)` — and be able to explain why messages would otherwise print twice
- [ ] `FINE` messages for debugging; change the level and watch them appear and disappear
- [ ] Bonus: `XMLFormatter` audit file for admins

**📝 EST prep:** byte vs character streams · which class for which job · `read()` returns `-1` · `readLine()` returns `null` · try-with-resources vs `finally` · `flush()` vs `close()` · `mark` / `reset` / `skip` output tracing · `Serializable` as a marker interface · `transient` and static output questions · `serialVersionUID` · `readObject()` exceptions and the cast · JUL components · default level INFO · duplicate ConsoleHandler output · `Filter.isLoggable` · JUL vs Log4j

---

## Phase 3 — Database (Unit 11)

### 3.1 Schema (MySQL)

Use **MySQL**, not SQLite: SQLite has no stored procedures.

```sql
CREATE TABLE accounts (
    account_no  VARCHAR(10) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    balance     DECIMAL(12,2) NOT NULL,
    pin_hash    VARCHAR(64)  NOT NULL,
    frozen      BOOLEAN DEFAULT FALSE
);

CREATE TABLE transactions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    from_acc    VARCHAR(10),
    to_acc      VARCHAR(10),
    amount      DECIMAL(12,2) NOT NULL,
    type        VARCHAR(15)  NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 DAO pattern — the most important design point

```java
public interface AccountDAO {
    Account findById(String no) throws AccountNotFoundException;
    void save(Account a);
    void updateBalance(Connection con, String no, BigDecimal newBal) throws SQLException;
}
// Phase 2:  AccountDAO dao = new FileAccountDAO();
// Phase 3:  AccountDAO dao = new JdbcAccountDAO();   ← one-line switch
```

| DAO component | PayVault class |
|---|---|
| Transfer Object | `Account`, `Transaction` |
| DAO interface | `AccountDAO` |
| DAO implementation | `JdbcAccountDAO`, `FileAccountDAO` |
| Data Source | `DBConnection` |
| Business Object | `WalletService` |

### 3.3 JDBC features

| Feature | JDBC concept |
|---|---|
| Login, find account, insert | `PreparedStatement` with `?`, never string concatenation |
| `executeUpdate()` returns 0 | Throw `AccountNotFoundException` (use the return value) |
| **Transfer** | `setAutoCommit(false)` → debit → credit → insert two transaction rows → `commit()`; any exception → `rollback()` |
| Monthly interest | `CallableStatement` → `{call apply_interest(?)}` stored procedure |
| Admin report: first/last 10 transactions | Scrollable `ResultSet` (`TYPE_SCROLL_INSENSITIVE`, `absolute()`, `last()`) |
| Connections | try-with-resources; `SQLException` wrapped in `WalletException` (exception chaining) |

- [ ] Schema created
- [ ] `JdbcAccountDAO` + `TransactionDAO`
- [ ] Transactional transfer with rollback
- [ ] `apply_interest` stored procedure + `CallableStatement`
- [ ] **Demo flag** `failMidTransfer` that throws after the debit — show both balances unchanged

**📝 EST prep:** JDBC architecture (2-tier / 3-tier) · 4 driver types · JDBC steps · `Statement` vs `PreparedStatement` vs `CallableStatement` · `execute` / `executeQuery` / `executeUpdate` return types · SQL injection · ResultSet types / concurrency / holdability · navigation methods · ACID · auto-commit · `commit` / `rollback` · DAO components, advantages and disadvantages

---

## Phase 4 — JavaFX GUI (Units 12, 13)

### 4.1 Lifecycle (Unit 12)

- [ ] `init()`: load config and open the DB connection — **no UI code** (launcher thread)
- [ ] `start(Stage)`: build the login scene
- [ ] `stop()`: close the DB connection, flush and close log handlers, auto-backup

### 4.2 Screens and layouts

| Screen | Layout | Controls |
|---|---|---|
| Login | `GridPane` (labels col 0, fields col 1, button colspan 2) | `TextField`, `PasswordField` |
| Dashboard | `BorderPane`: top `HBox` (name + logout), left `VBox` menu, center content, bottom status label | |
| Balance card | `StackPane` (rounded rectangle + label on top) | |
| Transfer | **Secondary Stage**, `Modality.APPLICATION_MODAL` | to-account, amount, note |
| History | `TableView` + search `TextField` | |

### 4.3 Events (Unit 13)

- [ ] `setOnAction` lambdas for every button; **one** written as an anonymous `EventHandler<ActionEvent>`
- [ ] `ChangeListener` on the amount field: shows "Max transferable: ₹X" live and disables Transfer while the input is invalid
- [ ] `ChangeListener` on the search field: filters the history table as you type
- [ ] All four validation types: empty, numeric (`NumberFormatException`), range, format (regex)
- [ ] `CONFIRMATION` alert before every transfer → `showAndWait()` → `Optional<ButtonType>`
- [ ] `ERROR` alert showing each custom exception's message
- [ ] `INFORMATION` alert on success
- [ ] `TextInputDialog` asking for the PIN again before large transfers
- [ ] `ChoiceDialog` to pick Savings / Current at registration
- [ ] Notification `Label` ("Statement exported ✓")
- [ ] `styles.css` for colours, rounded cards, hover effects

**📝 EST prep:** JavaFX architecture layers · lifecycle order + threads · Stage vs Scene vs scene graph · modality types · each layout's behaviour and methods · `GridPane.add(node, col, row, colspan, rowspan)` · event source / object / handler · `EventHandler.handle()` · handler vs listener · `ChangeListener` parameters · validation types · alert types · `showAndWait()` return values · dialog vs notification

---

## Timeline (about 4 weeks)

| Week | Build | Revise for EST alongside |
|---|---|---|
| 1 | Phase 1 — model, exceptions, transfer logic, console menu | Units 3, 4, 6, 7 |
| 2 | Phase 2 — serialization, files, logging | Units 8, 9, 10 |
| 3 | Phase 3 — MySQL, DAO switch, transactions | Unit 11 |
| 4 | Phase 4 — JavaFX screens, polish, DESIGN.md, demo rehearsal | Units 12, 13 |

## 5-minute demo script

1. Log in; show the wrong-PIN lock and `security.log` filling up
2. Transfer with the confirmation alert; show the live balance-hint listener
3. **Turn on `failMidTransfer`** → error alert → both balances unchanged (rollback)
4. Export a statement and run a backup; show the files
5. Change one line from `JdbcAccountDAO` to `FileAccountDAO` → the app still works (DAO + interfaces)

## Viva questions to prepare

- Why an abstract class here and not an interface?
- Why `BigDecimal` and not `double` for money?
- What happens if `commit()` is never called?
- Why is `sessionToken` marked `transient`? What value does it have after loading?
- Why did the account numbers repeat after a restart?
- `Statement` vs `PreparedStatement` — why did you choose this one?
- Why do log messages print twice without `setUseParentHandlers(false)`?
- Why can't you create UI components in `init()`?
- What happens to the UI if a database call takes 10 seconds?
