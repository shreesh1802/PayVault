# M1 — Account Model

**Phase 1 · Week 1 · ~1–2 days · Units 3, 4 (and 1)**

## Goal
Build the core domain model: an account hierarchy plus an immutable transaction record. By the end, **two account types should behave differently through the same method call**. That's polymorphism, which the rest of the project depends on.

## Revise before you start
- **Unit 3:** constructor overloading; `this(...)` chaining (why it must be the first statement)
- **Unit 4:** abstract classes, interfaces, static nested classes
- **`BigDecimal`:** `new BigDecimal("0.1")` vs `new BigDecimal(0.1)`, `add` / `subtract` / `multiply` / `divide`, `compareTo` (not `equals`), `setScale(2, RoundingMode.HALF_UP)`

## 📚 Reference slides (open these when you're stuck)

| If you're stuck on… | Deck | Slides |
|---|---|---|
| Writing a class, fields, methods | U2 Project Structure, Packages and Classes | 13–20 |
| Constructors, `this` | U2 | 21–27 |
| Overloaded constructors | U3 Method and Constructor Overloading | 6–8 |
| Overloaded methods (`deposit`) | U3 | 2–5 |
| `private` / `protected` / default / `public` | U3 | 17–24 (summary table: 24) |
| `static` counter, constants with `final` | U3 | 26–29 |
| Static nested class (`Builder`) | U4 Nested classes, Inheritance, Abstract Classes and Interfaces | 2–5 |
| `extends`, constructor call order | U4 | 9–13 |
| `super(...)` in subclass constructors | U4 | 16–19 |
| `List<Account>` holding subclasses | U4 | 14–15 |
| Overriding `withdrawableAmount()` / `toString()` | U4 | 22–25 |
| `abstract` class and method rules | U4 | 26–30 (Figure example: 29) |
| `interface` + `implements` | U4 | 31–38 |
| Naming constants, order of class members | U5 Coding Conventions and Build Tools | 3–6, 12 |
| `LocalDateTime.now()` | U7 Collection Framework | 161 |
| `BigDecimal`, `enum`, `unmodifiableList` | Not in the slides. See `docs/REFERENCES.md` → "Not in the slides". | — |

## Files to create (package `com.payvault.model`)

| File | Kind |
|---|---|
| `TransactionType.java` | enum |
| `Transaction.java` | class + static nested `Builder` |
| `Account.java` | abstract class |
| `SavingsAccount.java` | class |
| `CurrentAccount.java` | class |
| `InterestBearing.java` | interface |

Plus `com.payvault.Main` as your test driver.

## Requirements

### `TransactionType`
`DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`, `INTEREST`

### `Transaction` (immutable)
- Fields: `id` (long), `type`, `amount` (`BigDecimal`), `counterparty` (account no, may be null), `note` (may be null), `timestamp` (`LocalDateTime`)
- All fields `private final`, getters only, no setters
- **Private constructor**: only the `Builder` can create a `Transaction`
- `Builder` is a **static nested class**:
  - setter-style methods that return the `Builder` (so calls can be chained)
  - `build()` throws `IllegalStateException` if `type` or `amount` is missing
  - sets `timestamp` to now if it isn't given
- `id` comes from a **static counter**. Yes, static. Remember this, because it matters in M7.
- `toString()` gives one readable line, e.g. `#3 DEPOSIT ₹500.00 (salary) 2026-11-26T10:15`

### `Account` (abstract)
- Fields: `accountNo` (final), `holderName`, `balance`, `history` (`List<Transaction>`)
- Choose access modifiers deliberately: **nothing outside the class may set the balance directly**
- Two constructors:
  - `(accountNo, holderName)` → opening balance 0
  - `(accountNo, holderName, openingBalance)`
  - the first **must** call the second with `this(...)`
- `deposit(amount)` and `deposit(amount, note)`: one overload **must delegate** to the other. Both record a `DEPOSIT` transaction.
- `withdraw(amount)` returns `boolean`: allowed only if `amount ≤ withdrawableAmount()`. Records a `WITHDRAWAL`. (M2 replaces the `boolean` with exceptions.)
- `abstract BigDecimal withdrawableAmount()`
- `getHistory()` must **not** let the caller modify the internal list
- `toString()`

### `InterestBearing` (interface)
- `BigDecimal calculateInterest()`

### `SavingsAccount extends Account implements InterestBearing`
- Minimum balance **₹1,000** (a constant)
- `withdrawableAmount()` = balance − 1000, **never negative**
- `calculateInterest()` = balance × 4% ÷ 12 (monthly), 2 decimals, `HALF_UP`. It only calculates and doesn't deposit.

### `CurrentAccount extends Account`
- Overdraft limit **₹5,000** (a constant)
- `withdrawableAmount()` = balance + 5000. The balance may go down to −5000.

## Rules
- ❌ No `double` / `float` for money, anywhere
- ❌ No `instanceof` in `Account` or `Main` to decide behaviour
- ✅ Constants are `private static final` with `UPPER_SNAKE_CASE` names
- ✅ Don't validate negative or zero amounts yet. Leave a `// TODO M2` comment where it'll go.

## 🤔 A design decision for you to make
**Should an opening balance create a transaction in the history?** There's no single right answer. Pick one, then write it up as **D1** in `docs/DESIGN.md` with your reason. The expected output below assumes **yes** (`DEPOSIT`, note `"Opening balance"`, only if the opening balance is > 0). Adjust your counts if you choose differently.

## ✅ Done when: `Main` demonstrates all of these

| # | Action | Expected |
|---|---|---|
| 1 | Savings `S001`, opened with 5000 | withdrawable **4000.00** |
| 2 | Current `C001`, opened with 2000 | withdrawable **7000.00** |
| 3 | Withdraw 4500 from `S001` | refused (`false`), balance still **5000.00** |
| 4 | Withdraw 6500 from `C001` | allowed, balance **−4500.00**, withdrawable **500.00** |
| 5 | Deposit 1000 into `S001` with note `"salary"` | balance **6000.00**, history has **2** entries |
| 6 | `S001.calculateInterest()` | **20.00** |
| 7 | Put both in a `List<Account>` and loop, printing `withdrawableAmount()` | correct value for each, with no `instanceof` |
| 8 | `new Transaction.Builder()...build()` without an amount | `IllegalStateException` caught and printed |
| 9 | `s001.getHistory().add(...)` | `UnsupportedOperationException` |
| 10 | Uncomment `new Account(...)` | compile error, then comment it back out |

## 🎤 Be ready to answer (review + viva)
1. Why can't you write `new Account(...)`? What error do you get?
2. Why must `this(...)` be the first statement in a constructor?
3. Why is `Account` an abstract class but `InterestBearing` an interface? Why isn't `calculateInterest()` in `Account`?
4. Why is `Builder` **static**? What would change if it weren't?
5. Print `new BigDecimal(0.1)` and `new BigDecimal("0.1")`. Explain the difference.
6. Why does `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` return `false`? What should you use instead?
7. Name one example of **overloading** and one of **overriding** in your code.

## 📝 EST link
Output-tracing on constructor chaining · "will this compile?" on instantiating an abstract class · overloading resolution · abstract class vs interface (theory) · static nested vs inner class

## Suggested commits
1. `Add TransactionType and immutable Transaction with Builder`
2. `Add abstract Account with overloaded constructors and deposit`
3. `Add Savings and Current accounts with demo in Main`

Then push and say **"review M1"**.

## Stuck?
Ask for **"M1 hint 1"** (a nudge). Only go to hint 2 (pointer), 3 (pseudocode) or 4 (a small snippet you'll retype and explain) if you're still stuck.
