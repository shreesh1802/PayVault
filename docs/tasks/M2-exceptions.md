# M2 — Exceptions & WalletService

**Phase 1 · Week 1 · ~1–2 days · Unit 6 (and 7)**

## Goal
Right now PayVault fails **silently**. `withdraw()` just returns `false` without saying why, and some invalid input isn't caught at all. Try `withdraw(new BigDecimal("-100"))` on a Savings account: `-100` is never more than the withdrawable amount, so it passes the check, the balance goes **up** by 100, and it's recorded as a WITHDRAWAL. In M2, every invalid operation must **fail loudly, with the right exception and a clear message**, and must leave the account unchanged.

You'll also create `WalletService`, the class that manages all accounts by account number. From M3 onwards, the rest of the app talks to `WalletService`, not to accounts directly.

## Step 0: finish the M1 leftovers first
These have been open since the M1 reviews:
- [ ] `Account.recordTransaction` → `private`
- [ ] `Transaction.toString()`: remove the double space after the note
- [ ] `Transaction.getCounterParty()` → `getCounterparty()` (Shift+F6)
- [ ] `SavingsAccount` constants → `MIN_BALANCE`, `ANNUAL_RATE`, `MONTHS` (UPPER_SNAKE_CASE, like `OVERDRAFT_LIMIT`)
- [ ] `CurrentAccount`: delete the unused `RoundingMode` import
- [ ] `docs/DESIGN.md`: write **D1** (opening balance as a transaction) and **E4** (`0.1 + 0.2` with `double`)

Commit these on their own: `Clean up M1 review leftovers`.

## 📚 Reference slides

| If you're stuck on… | Deck | Slides |
|---|---|---|
| Checked vs unchecked exceptions | U6 Exception Handling | 2–4 |
| The `Throwable` → `Exception` / `RuntimeException` hierarchy | U6 | 14–15 |
| Why the compiler forces you to handle checked exceptions | U6 | 16, 24 |
| `try` / `catch` | U6 | 5, 17 |
| Multiple catch blocks and their **order** | U6 | 7–9, 18 |
| `finally` | U6 | 6, 19–20 |
| How an exception travels up the call stack | U6 | 21–24 |
| `throws` (declare) vs `throw` (raise) | U6 | 25–28 |
| Writing your own exception classes, calling `super(message)` | U6 | 29–32 |
| A full custom-exception hierarchy example | U6 | 33–39 |
| `super(...)` in constructors | U4 Nested classes, Inheritance, Abstract Classes and Interfaces | 16–19 |
| `HashMap`: `put`, `get`, `containsKey` | U7 Collection Framework | 59–70 |
| Multi-catch (`catch (A \| B e)`) | Not in the slides. One `catch` handling two unrelated exception types, written `catch (AccountNotFoundException \| AccountFrozenException e)`. Java 7+. | — |

## Files

| File | Package | Kind |
|---|---|---|
| `WalletException.java` | `exception` | checked, `extends Exception` |
| `InsufficientBalanceException.java` | `exception` | `extends WalletException` |
| `AccountNotFoundException.java` | `exception` | `extends WalletException` |
| `AccountFrozenException.java` | `exception` | `extends WalletException` |
| `DailyLimitExceededException.java` | `exception` | `extends WalletException` (created now, used in M3) |
| `InvalidAmountException.java` | `exception` | **unchecked**, `extends RuntimeException` |
| `WalletService.java` | `service` | class |
| `Account.java` | `model` | modified |

## Requirements

### The exception hierarchy
```
Exception
 └── WalletException                      (checked: callers MUST handle it)
      ├── InsufficientBalanceException
      ├── AccountNotFoundException
      ├── AccountFrozenException
      └── DailyLimitExceededException
RuntimeException
 └── InvalidAmountException               (unchecked: on purpose, see the questions)
```
- `WalletException` has **two constructors**: `(String message)` and `(String message, Throwable cause)`. The second one is for M11, when you'll wrap `SQLException`s.
- Every subclass passes its message up with `super(message)`.
- `InsufficientBalanceException` also stores the **shortfall** (`BigDecimal`), with a getter. The shortfall is how much more money would have been needed.
- `AccountNotFoundException` stores the **account number** that wasn't found, with a getter.
- Messages must be readable on their own, e.g. `Insufficient balance in S001: short by ₹500.00`.

### `Account` changes
- New field `frozen` (`boolean`, starts `false`), with `freeze()`, `unfreeze()` and `isFrozen()`.
- A **private** helper `validateAmount(BigDecimal amount)` that throws `InvalidAmountException` if the amount is:
  - `null`
  - zero or negative
  - more than 2 decimal places (`10.555`). Careful: `10.50` and `10.500` are the **same value** and both must be accepted. Look at `stripTrailingZeros()` and `scale()`.
- `deposit(amount, note)`: validate the amount → throw `AccountFrozenException` if frozen → then the existing logic.
- `withdraw(amount)`: **returns `void` now** (no more `boolean`). Validate → frozen check → if `amount` is more than `withdrawableAmount()`, throw `InsufficientBalanceException` with shortfall = `amount − withdrawableAmount()` → then the existing logic.
- Remove the `// TODO M2` comments once they're done.
- **A failed operation must change nothing**: no balance change and no transaction recorded.

### `WalletService`
- `private final Map<String, Account> accounts = new HashMap<>();`
- `addAccount(Account account)`: stores it under its account number. Adding a number that already exists throws `IllegalArgumentException` (a programming mistake, so unchecked).
- `findAccount(String accountNo) throws AccountNotFoundException`
- `deposit(String accountNo, BigDecimal amount, String note) throws WalletException`
- `withdraw(String accountNo, BigDecimal amount) throws WalletException`
- `freeze(String accountNo)` / `unfreeze(String accountNo)`, both `throws AccountNotFoundException`
- The service **finds** the account and **delegates** to it. The rules themselves stay in `Account`.

## Rules
- ❌ No `catch (Exception e)` in `WalletService` or `Account`. Catch only what you can actually handle, and let the rest propagate.
- ❌ Never catch an exception and ignore it (an empty `catch {}`)
- ❌ No `instanceof` to tell exceptions apart. That's what multiple `catch` blocks are for.
- ✅ Declare exactly what each method can throw, in its `throws` clause
- ✅ Catch order: subclasses **before** `WalletException`, or the code won't compile (U6 slide 9)

## 🤔 Design decision (D2)
**In what order should `withdraw()` check things?** Suppose a **frozen** account receives an **invalid amount** (−100). Should it report "account frozen" or "invalid amount"? Pick an order for the three checks (amount, frozen, balance), apply it in both `deposit()` and `withdraw()`, and write it up as **D2** in `DESIGN.md` with your reasoning. The expected output below assumes **amount → frozen → balance**.

## ✅ Done when: `Main` demonstrates all of these

Setup: a `WalletService` holding `S001` (Savings, opened with 5000) and `C001` (Current, opened with 2000). Run the checks **in this order**.

| # | Action | Expected |
|---|---|---|
| 1 | Deposit `0` into S001 | `InvalidAmountException` |
| 2 | Deposit `-500` into S001 | `InvalidAmountException` |
| 3 | Deposit `10.555` into S001 | `InvalidAmountException` |
| 4 | Deposit `10.500` into S001 | ✅ accepted, balance **5010.50** |
| 5 | Withdraw `4500` from S001 | `InsufficientBalanceException`, shortfall **489.50** |
| 6 | Withdraw `7000.01` from C001 | `InsufficientBalanceException`, shortfall **0.01** |
| 7 | Withdraw `7000` from C001 | ✅ allowed, balance **−5000.00** |
| 8 | Deposit into `X999` | `AccountNotFoundException`, message mentions **X999** |
| 9 | Freeze S001, then deposit `100` | `AccountFrozenException`, balance still **5010.50** |
| 10 | Unfreeze S001, deposit `100` | ✅ balance **5110.50** |
| 11 | Handle checks 8 and 9 with **one multi-catch** block | both caught by the same `catch` |
| 12 | S001's history | **3** entries: opening balance, 10.500, 100. **No failed attempts recorded.** |
| 13 | Wrap one attempt in `try / catch / finally`, with the `finally` printing the balance | the balance prints whether the attempt succeeded or failed |
| 14 | Temporarily remove `throws WalletException` from `main` while calling `service.withdraw(...)` outside any `try` | **compile error**: *unreported exception*. Then put it back. |

## 🎤 Be ready to answer (review + viva)
1. Why is `InvalidAmountException` **unchecked** while `InsufficientBalanceException` is **checked**? Who is "at fault" in each case: the programmer or the situation?
2. What's the difference between `throw` and `throws`? Point to one of each in your code.
3. Why must `catch (InsufficientBalanceException e)` come **before** `catch (WalletException e)`? What does the compiler say if you swap them?
4. Check 14 fails to compile, yet passing `-500` to `deposit()` compiles fine with no `try`. Why the difference?
5. When `withdraw()` throws halfway through, why is the balance unchanged? What would go wrong if you updated the balance **before** checking?
6. What does `finally` guarantee? Is there any case where it doesn't run?
7. Why does `WalletException` need a `(String, Throwable)` constructor? What's exception chaining?

## 📝 EST link
Checked vs unchecked (theory, with examples) · the exception class hierarchy (draw it) · output tracing with `try`/`catch`/`finally` and returns · catch-order compile errors · propagation through method calls · `throw` vs `throws` · writing a custom exception class

## Suggested commits
1. `Clean up M1 review leftovers` (Step 0)
2. `Add WalletException hierarchy and InvalidAmountException`
3. `Validate amounts and add frozen state to Account`
4. `Add WalletService with account lookup and delegation`
5. `Add M2 exception demo in Main`

Then push and say **"review M2"**.

## Stuck?
Check the 📚 slides first, especially **U6 29–39**, which walk through a complete custom-exception hierarchy. Then ask for **"M2 hint 1"**.
