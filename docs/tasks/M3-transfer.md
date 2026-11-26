# M3 — Transfers & the Daily Limit

**Phase 1 · Week 1 · ~2 days · Units 3 and 6 (and 7)**

## Goal
Move money **between two accounts**. This is the heart of PayVault and the method you'll be asked about most in the viva.

A transfer is harder than a deposit or a withdrawal because it changes **two** accounts. If the money leaves one account but never arrives in the other, it has simply vanished. So the rule for M3 is:

> **Money is never created or lost.** Either both accounts change, or neither does.

You'll also finally use `DailyLimitExceededException`: an account may send at most **₹50,000 per day** in transfers.

## Step 0: small leftovers from M2
- [ ] `InsufficientBalanceException` message → `Insufficient balance in S001: short by ₹489.50`
- [ ] `README.md` status line → `🚧 Phase 1 in progress (M2 done)`
- [ ] Put the `// 1. …` to `// 13. …` check-number comments back in `Main`

Commit: `Tidy M2 leftovers`.

## 📚 Reference slides

| If you're stuck on… | Deck | Slides |
|---|---|---|
| Passing objects to methods | U3 Method and Constructor Overloading | 9–11 |
| Pass by value vs pass by reference (viva Q5!) | U3 | 12–14 |
| The BankAccount programming problem (transfer between two objects) | U3 | 16 |
| `static` methods and members (`Validator`) | U3 | 26–28 |
| How an exception travels up through several methods | U6 Exception Handling | 21–24 |
| Writing a new custom exception | U6 | 29–32 |
| `LocalDate`, `LocalDateTime` | U7 Collection Framework | 158–162 |
| for-each loop over a `List` | U7 | 7–27 (ArrayList) |
| `String.equals()` vs `==` | Not in the slides. `==` checks whether two variables point to the **same object**; `equals()` checks whether two Strings have the **same characters**. Always compare Strings with `equals()`. | — |
| `LocalDateTime.toLocalDate()` | Not in the slides. Drops the time part: `2026-11-27T10:15` → `2026-11-27`. | — |

## Files

| File | Package | Kind |
|---|---|---|
| `Validator.java` | `util` | **new**, utility class (static methods only) |
| `InvalidTransferException.java` | `exception` | **new**, `extends WalletException` |
| `Account.java` | `model` | modified |
| `Transaction.java` | `model` | modified (`toString` only) |
| `WalletService.java` | `service` | modified |
| `Main.java` | — | modified |

## Requirements

### `Validator` (new, in `util`)
The amount rules are now needed in **two** places (`Account` and `WalletService`). Rather than copy them, move them into one shared class.
- `public final class Validator` with a **private constructor**, so nobody can create a `Validator` object. It's only a home for static methods (like Java's own `Math`).
- `public static BigDecimal validateAmount(BigDecimal amount)`: the same three checks as your current private `validateAmount` (null, ≤ 0, > 2 decimals). It **returns** the amount normalised with `setScale(2)`.
- In `Account`, **delete** the private `validateAmount`, and replace each pair of lines
  ```java
  validateAmount(amount);
  amount = amount.setScale(2);
  ```
  with a single `Validator.validateAmount(...)` call.
- **Also:** validate the opening balance in the constructor. A non-zero opening balance must pass `Validator.validateAmount`, so a negative or 3-decimal opening balance is rejected. Zero is still allowed (but records no transaction, as in D1).

### `InvalidTransferException` (new)
- `extends WalletException`, constructor `(String message)`.
- Used for a transfer to the **same** account.

### `Transaction.toString()`
- When `counterparty` isn't `null`, show it in square brackets, e.g.
  `#4 TRANSFER_OUT ₹10000.00 [C001] 2026-11-27T10:15`

### `Account` changes
- A constant `DAILY_TRANSFER_LIMIT` = `50000.00` (`private static final`).
- `public BigDecimal getTransferredToday()`: the total of this account's **`TRANSFER_OUT`** transactions whose date is **today**. Calculate it by looping over `history`; don't keep a separate counter. Deposits, withdrawals and incoming transfers **don't** count.
- `public void transferOut(BigDecimal amount, String toAccountNo)`: in this order:
  1. validate the amount
  2. frozen → `AccountFrozenException`
  3. `getTransferredToday() + amount` > limit → `DailyLimitExceededException`. The message must show the limit, how much was already sent today and the attempted amount.
  4. amount > `withdrawableAmount()` → `InsufficientBalanceException`
  5. subtract, and record a `TRANSFER_OUT` with `counterparty = toAccountNo`
- `public void transferIn(BigDecimal amount, String fromAccountNo)`: validate, frozen check, then add, and record a `TRANSFER_IN` with `counterparty = fromAccountNo`.
- `recordTransaction` needs a way to pass a counterparty. Add an **overload** with a fourth parameter, and make the old 3-parameter version delegate to it, the same pattern as `deposit`.
- **No copy-pasted checks.** `withdraw` and `transferOut` share the frozen and funds checks. Move each shared check into a small **private** helper (e.g. `checkNotFrozen()`, `checkSufficientFunds(amount)`) and call it from both.

### `WalletService.transfer(String fromNo, String toNo, BigDecimal amount)`
Checks, **in this order**:
1. validate the amount (`Validator`)
2. both accounts exist (`findAccount` twice)
3. not the same account → `InvalidTransferException`
4. the **destination** is not frozen → `AccountFrozenException`
5. `from.transferOut(amount, toNo)`: frozen source, daily limit and funds are checked inside it
6. `to.transferIn(amount, fromNo)`

Declare exactly what it can throw, or `throws WalletException`, but be ready to justify your choice.

## Rules
- ❌ Don't implement a transfer as `withdraw()` + `deposit()`. Find out why in viva Q3.
- ❌ No `==` to compare Strings
- ❌ Don't store a "transferred today" counter field. Calculate it from `history` (see D5).
- ✅ **Every check that can fail must run before any balance changes.** Once `transferOut` has taken the money, nothing after it may throw.
- ✅ Keep the M2 demo working. Move it into `private static void runM2Checks(WalletService service) throws WalletException`, put the M3 checks in `runM3Checks()`, and have `main` call both.

## 🤔 Design decision (D5)
**Why must the destination's frozen check (step 4) happen in `WalletService`, before `transferOut`?** `transferIn` already checks whether the destination is frozen, so isn't step 4 redundant? Trace what happens to the money if you delete step 4 and transfer into a frozen account. Then write up **D5** in `DESIGN.md`: what the order protects, and one more failure that could still lose money halfway through a transfer, even with this order. Hint: think about what's outside your Java code (M12 fixes it).

## ✅ Done when: `runM3Checks()` demonstrates all of these

Setup: a **new** `WalletService` with `S001` (Savings, 60000), `C001` (Current, 2000) and `S002` (Savings, 5000). Run in this order:

| # | Action | Expected |
|---|---|---|
| 1 | Transfer 10000 S001 → C001 | ✅ S001 **50000.00**, C001 **12000.00**, S001 sent today **10000.00** |
| 2 | Transfer `-5` S001 → C001 | `InvalidAmountException` |
| 3 | Transfer 100 S001 → `X999` | `AccountNotFoundException` mentioning **X999**, S001 still **50000.00** |
| 4 | Transfer 100 S001 → S001 | `InvalidTransferException` |
| 5 | Freeze S002, transfer 100 S001 → S002, then unfreeze | `AccountFrozenException` mentioning **S002**. S001 still **50000.00**: **no money lost** |
| 6 | Transfer 40000.01 S001 → C001 | `DailyLimitExceededException` (10000 + 40000.01 > 50000) |
| 7 | Transfer 40000 S001 → C001 | ✅ exactly hits the limit. S001 **10000.00**, C001 **52000.00**, sent today **50000.00** |
| 8 | Transfer 1 S001 → C001 | `DailyLimitExceededException`, even though S001 still has 9000 withdrawable |
| 9 | Transfer 4000.01 S002 → C001 | `InsufficientBalanceException`, shortfall **0.01** |
| 10 | Transfer 4000 S002 → C001 | ✅ S002 **1000.00**, C001 **56000.00** |
| 11 | Add up all three balances | **67000.00**, the same as at the start (60000 + 2000 + 5000). Money was conserved. |
| 12 | Print every account's history | S001 **3** entries (opening + 2 `TRANSFER_OUT [C001]`), C001 **4** (opening + 3 `TRANSFER_IN`), S002 **2** (opening + 1 `TRANSFER_OUT`). No failed attempt recorded. |
| 13 | Withdraw 100 from C001, then print C001's sent-today total | **0.00**: withdrawals and incoming transfers don't count towards the limit |
| 14 | Create `new SavingsAccount("S003", "X", new BigDecimal("100.555"))` | `InvalidAmountException` (opening balances are validated now) |
| 15 | `runM2Checks()` still prints exactly what it did at the end of M2 | ✅ |

## 🎤 Be ready to answer (review + viva)
1. Why `fromNo.equals(toNo)` and not `fromNo == toNo`? Can you make up a case where `==` gives the wrong answer?
2. Walk through what happens to the money if step 4 (destination frozen) is removed and you transfer into a frozen account.
3. Why not implement `transfer` as `from.withdraw(amount)` then `to.deposit(amount)`? Give **two** reasons. Hint: look at the history entries, and at the daily limit.
4. The daily limit is **calculated from history**, not stored in a counter. Give one advantage and one disadvantage. What happens at midnight?
5. Inside `transfer`, you write `amount = Validator.validateAmount(amount);`. Does that change the caller's `BigDecimal` variable in `Main`? Why not? (U3 12–14: Java is always pass by value.)
6. Why does `Validator` have a private constructor and only static methods? Name a class in the Java library built the same way.
7. Is your transfer **atomic**? What if the program crashes between `transferOut` and `transferIn`? What will fix it in M12?
8. An `AccountFrozenException` is thrown inside `Account.transferOut`. List every method it passes through before it's caught in `runM3Checks`.

## 📝 EST link
Pass by value vs reference (output tracing with objects as parameters) · exception propagation through several methods · `String` `equals` vs `==` · `static` utility methods · method overloading (`recordTransaction`) · designing a custom exception · `LocalDate` / `LocalDateTime`

## Suggested commits
1. `Tidy M2 leftovers` (Step 0)
2. `Add Validator utility and validate opening balances`
3. `Add transferOut and transferIn with daily limit to Account`
4. `Add transfer to WalletService`
5. `Add M3 transfer demo in Main`

Then push and say **"review M3"**. In between, you can ask **"check M3.1"** … **"check M3.4"** after each commit.

## Stuck?
Check the 📚 slides first, especially **U3 16** (the BankAccount problem is almost this exact task). Then ask for **"M3 hint 1"**.
