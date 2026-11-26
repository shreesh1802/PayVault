# PayVault — Digital Wallet & Banking System

> A desktop banking application built in Java that lets customers manage accounts, move money safely between them, and keep a complete, auditable record of every transaction.

**Course:** UCS553 — Enterprise Web Applications (Java)
**Type:** Semester evaluation project
**Status:** 🚧 Phase 1 started

---

## 1. Project Description

PayVault is a desktop-based digital wallet and banking system that simulates the core operations of a real bank. Customers can open Savings or Current accounts, deposit and withdraw money, transfer funds to other accounts, review their transaction history, and export monthly statements. Administrators can manage accounts, freeze suspicious ones, import customers in bulk, apply monthly interest, and back up the system's data.

The project focuses on **correctness and reliability** rather than on the number of features. Money must never be created or lost. That means every operation is validated, every failure is handled, and a transfer either fully completes or leaves both accounts untouched. Every important action is recorded in log files so the system's behaviour can be audited later.

The application is built in four layers, each one adding on top of the last without rewriting it:

1. **Core logic**: an object-oriented model of accounts and transactions, driven from a console menu
2. **Persistence & logging**: data saved to disk with serialization and file streams; activity recorded with Java Util Logging
3. **Database**: storage moved to MySQL via JDBC behind a DAO interface, with transfers run as database transactions
4. **Graphical interface**: a JavaFX desktop UI with live validation, alerts and dialogs

## 2. Problem Statement

A banking system has to handle many failure cases that a simple CRUD application can ignore: insufficient funds, invalid input, frozen accounts, wrong PINs, daily limits, and a system crash halfway through moving money. A transfer that debits one account but fails before crediting the other destroys money. A system that keeps no record of failed logins can't detect misuse.

PayVault addresses these problems with:
- **Strict validation** of every operation before it changes any data
- **A meaningful exception hierarchy** so every failure has a clear cause and message
- **Atomic transfers**: database transactions with commit and rollback
- **Audit and security logs** for every important or suspicious event
- **A storage layer that can be swapped** (file ↔ database) without touching business logic

## 3. Objectives

- Apply object-oriented design (abstraction, inheritance, polymorphism, interfaces) to a real domain
- Build a robust error-handling strategy using checked and unchecked custom exceptions
- Choose the right Java collection for each job and justify the choice
- Persist application state using file I/O streams and object serialization
- Instrument the application with structured logging, levels, handlers, formatters and filters
- Connect to a relational database using JDBC, the DAO pattern and transaction management
- Build an event-driven JavaFX GUI with input validation and user feedback
- Organise the code as a professional Maven project with packages and naming conventions

## 4. Features

### Customer
- Register (Savings or Current account) and log in with a PIN
- Check balance, deposit, withdraw
- Transfer money to another account (with confirmation)
- View, search, sort and filter transaction history
- Export a monthly statement as a text file
- Change PIN

### Administrator
- View all accounts; freeze and unfreeze accounts
- Bulk-import accounts from a CSV file
- Apply monthly interest to all Savings accounts
- Back up and restore the data file
- View the security log

### Business rules
| Rule | Detail |
|---|---|
| Savings minimum balance | ₹1,000 must always remain |
| Current account overdraft | Up to ₹5,000 below zero |
| Daily transfer limit | ₹50,000 per account per day |
| PIN lock | Account locked after 3 consecutive wrong PINs |
| Amounts | Must be positive, at most 2 decimal places (`BigDecimal`) |
| Self-transfer | Not allowed |
| Frozen accounts | Cannot send or receive money |

## 5. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Eclipse Temurin JDK) |
| Build tool | Apache Maven |
| GUI | JavaFX 21 (+ CSS styling) |
| Database | MySQL 8 via JDBC (MySQL Connector/J) |
| Logging | Java Util Logging (`java.util.logging`) |
| Persistence (Phase 2) | Java Object Serialization, `java.io` streams |
| Testing | JUnit 5 |
| IDE | IntelliJ IDEA |
| Version control | Git |

## 6. Architecture

```
┌────────────────────────────────────────────┐
│  UI layer (JavaFX)          ui/            │  screens, event handlers, validation
├────────────────────────────────────────────┤
│  Service layer              service/       │  business rules: transfer, interest, auth
├────────────────────────────────────────────┤
│  DAO layer                  dao/           │  AccountDAO interface
│    ├── FileAccountDAO   (serialization)    │  ← Phase 2
│    └── JdbcAccountDAO   (MySQL)            │  ← Phase 3
├────────────────────────────────────────────┤
│  Model                      model/         │  Account, Transaction, Customer
└────────────────────────────────────────────┘
   Cross-cutting: exception/  util/ (logging, validation, DB connection)
```

The service layer depends only on the `AccountDAO` **interface**, so switching from file storage to the database is a one-line change.

### Package structure
```
com.payvault
├── model        Account, SavingsAccount, CurrentAccount, Transaction, TransactionType, Customer
├── exception    WalletException and its subclasses, InvalidAmountException
├── dao          AccountDAO, FileAccountDAO, JdbcAccountDAO, TransactionDAO
├── service      WalletService, AuthService, StatementService, BackupService
├── util         LogConfig, Validator, IdGenerator, DBConnection
├── ui           LoginView, DashboardView, TransferDialog, AdminView
└── Main
```

## 7. Development Phases

| Phase | Deliverable | Syllabus units |
|---|---|---|
| 1. Core logic | Console app: model, exceptions, transfer logic, collections | 1–7 |
| 2. Persistence & logging | Save/load wallet, statements, CSV import, backup, logs | 8, 9, 10 |
| 3. Database | MySQL schema, DAO switch, transactional transfers, stored procedure | 11 |
| 4. GUI | JavaFX screens, listeners, validation, alerts and dialogs | 12, 13 |

The full feature-by-feature plan is in [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md).
Design decisions and bugs found along the way are recorded in [docs/DESIGN.md](docs/DESIGN.md).
Course-slide references for every milestone and topic are in [docs/REFERENCES.md](docs/REFERENCES.md).
Milestone task cards live in [docs/tasks/](docs/tasks/).
Exam-ready concept notes are in [docs/NOTES.md](docs/NOTES.md).

## 8. Syllabus Coverage

| Unit | Topic | Where it appears in PayVault |
|---|---|---|
| 1 | Introduction to Java | Console menu, data types, `BigDecimal`, `String` handling |
| 2 | Project structure, packages, classes | 7-package layout, access modifiers |
| 3 | Method & constructor overloading | Overloaded `deposit()` and constructors, `this(...)` chaining |
| 4 | Nested classes, inheritance, abstract classes, interfaces | Abstract `Account`, `InterestBearing`, `Transaction.Builder`, anonymous `Comparator` |
| 5 | Coding conventions & build tools | Maven `pom.xml`, naming conventions, Javadoc |
| 6 | Exception handling | Custom checked/unchecked hierarchy, exception chaining |
| 7 | Collection framework | `HashMap`, `ArrayList`, `TreeMap`, `HashSet`, `ArrayDeque`, `Comparator`, `Iterator` |
| 8 | File handling | Statement export, CSV import, byte-stream backup, data-stream ID counter |
| 9 | Serialization | Saving the wallet, `transient`, `serialVersionUID`, object graphs |
| 10 | Logging | Audit + security logs, levels, handlers, formatters, filters |
| 11 | JDBC | DAO pattern, `PreparedStatement`, transactions, `CallableStatement`, scrollable `ResultSet` |
| 12 | JavaFX GUI | Lifecycle, Stage/Scene, all five layouts, modal windows |
| 13 | JavaFX events | Handlers, change listeners, input validation, alerts, dialogs |

## 9. How to Run

_To be completed once Phase 1 is in place._

```powershell
mvn clean compile
mvn exec:java          # console version (Phase 1–3)
mvn javafx:run         # GUI version (Phase 4)
```

## 10. Author

Shreesh — built as part of the UCS553 semester evaluation.
