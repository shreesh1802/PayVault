# PayVault — Concept Notes (EST revision)

Short, exam-ready explanations of the concepts used in PayVault, each linked to the place in the code where you can see it in action. Add to this file whenever a concept finally "clicks".

---

## Exceptions (Unit 6)

### Checked vs unchecked

> **Checked exceptions** are subclasses of `Exception` (but not of `RuntimeException`). The compiler forces the programmer to either **catch** them (`try`/`catch`) or **declare** them (`throws`). They represent recoverable situations, such as a missing file or insufficient balance.
>
> **Unchecked exceptions** are subclasses of `RuntimeException`. The compiler does **not** force handling. They usually represent programming errors, such as a null reference, division by zero or an invalid argument. They *can* still be caught, but usually the fix is to correct the code.

**Common misconceptions**
- ❌ *"Checked exceptions happen at compile time."* **All** exceptions are thrown while the program **runs**. "Checked" means the compiler checks *at compile time* that you've **planned** for it.
- ❌ *"Unchecked means we handle it ourselves / custom exceptions are unchecked."* Checked or unchecked depends **only on which class the exception extends**. Being custom has nothing to do with it:

| | Built into Java | Custom (PayVault) |
|---|---|---|
| **Checked** (extends `Exception`) | `IOException`, `SQLException` | `WalletException`, `AccountFrozenException` |
| **Unchecked** (extends `RuntimeException`) | `NullPointerException`, `ArithmeticException` | `InvalidAmountException` |

**In PayVault** (see D3 in [DESIGN.md](DESIGN.md)):
```
Exception
 ├── WalletException                 ← checked: business situations
 │    ├── InsufficientBalanceException
 │    ├── AccountNotFoundException
 │    ├── AccountFrozenException
 │    ├── DailyLimitExceededException
 │    └── InvalidTransferException
 └── RuntimeException
      └── InvalidAmountException     ← unchecked: programming mistake
```
`InvalidAmountException` is **not** a `WalletException`. They're on separate branches.

### `throw` vs `throws`

| | `throw` | `throws` |
|---|---|---|
| What it is | an **action**: raises an exception now | a **declaration**: "this method might end with this exception" |
| Where | inside the method body | in the method header |
| Followed by | one exception **object** (`new …`) | one or more exception **class names** |
| Example | `throw new AccountFrozenException(accountNo);` | `public Account findAccount(String no) throws AccountNotFoundException` |

`throws` means: *"I'm not handling this one myself; whoever calls me must deal with it."*

A method needs `throws` whenever a checked exception can **leave** it, either because it throws one itself, or because it **calls** a method that throws one and doesn't catch it. Example: `WalletService.deposit` contains no `throw`, but needs `throws` because `findAccount` and `Account.deposit` can throw.

Unchecked exceptions never need `throws`. That's why `validateAmount` has none, though you *may* add it as documentation.

### Propagation
An uncaught exception travels **up the call stack**, method by method, until a matching `catch` is found. If even `main` doesn't catch it, the program stops and prints the stack trace.
```
Account.deposit  →  WalletService.deposit  →  Main.main  →  (crash + stack trace)
```

### Which `catch` catches what
A `catch (X e)` block catches **X and every subclass of X**, and nothing else.
- `catch (WalletException e)` catches `AccountFrozenException` ✅ but **not** `InvalidAmountException` ❌ (it's not a subclass).
- **Order matters:** subclasses must come **before** their parent. Otherwise the parent catches everything, and the compiler reports *"exception … has already been caught"*.
- **Multi-catch** `catch (A | B e)`: one block for two types. A and B must **not** be parent and child.

### `finally`
Runs whether the `try` finishes normally, throws (caught or not) or `return`s early. Used for cleanup (closing files, DB connections). It does **not** run after `System.exit()` or if the JVM crashes.
**EST trap:** if both `try` and `finally` contain a `return`, the `finally`'s value wins.

### Exception chaining
Wrapping a low-level exception in a higher-level one, keeping the original as the **cause**:
```java
catch (SQLException e) {
    throw new WalletException("Could not load account " + accountNo, e);
}
```
`e.getCause()` returns the original, and the stack trace shows it under *"Caused by:"*. This is why `WalletException` has a `(String, Throwable)` constructor (used in M11).
