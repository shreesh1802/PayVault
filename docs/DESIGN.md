# PayVault — Design Log

A running record of design decisions, experiments and bugs, **written as you build**.
Add an entry whenever you make a choice, hit a bug, or learn something. These notes help you most in the viva.

---

## Decisions

<!-- Template for new decisions:
### D? — <short title>
- **Date:**
- **Decision:**
- **Alternatives considered:**
- **Why:**
-->

### D1 — Record the opening balance as a transaction
- **Date:** 26/11/2026
- **Decision:** When an account is opened with a balance greater than 0, the constructor records it as a `DEPOSIT` transaction with the note "Opening Balance". An account opened with 0 records nothing.
- **Alternatives considered:** Just set `balance` to the opening amount and leave the history empty. That's simpler and gives the history only "real" activity.
- **Why:** The history should fully explain the balance: opening deposit + later deposits − withdrawals = current balance. Without the opening entry, a customer who opened with ₹5000 would see a history that doesn't add up to their balance, and there'd be no record of where that money came from. A zero opening balance isn't recorded because a ₹0 "deposit" carries no information, and `Transaction` amounts should always be real money movements.

### D2 — Order of checks in deposit() and withdraw(): amount → frozen → balance
- **Date:** 26/11/2026
- **Decision:** Both methods run their checks in a fixed order, each one throwing straight away if it fails:
  1. **Amount** (`validateAmount`): null, ≤ 0, or more than 2 decimals → `InvalidAmountException`
  2. **Frozen**: the account is frozen → `AccountFrozenException`
  3. **Balance** (`withdraw` only): amount > `withdrawableAmount()` → `InsufficientBalanceException`

  Only after all checks pass do the balance change and the transaction get recorded. So a frozen account given −100 reports *"Amount must be positive"*, not *"Account is frozen"*.
- **Alternatives considered:**
  - **Frozen first:** a frozen account rejects everything with "frozen", whatever the amount. It's simple and reveals nothing about the account to someone it's locked away from. But it hides the caller's bug: −100 is wrong for *every* account, and the caller only finds out once the account is unfrozen.
  - **Balance before frozen:** this would tell a frozen account's user how much money it lacks, which they shouldn't learn while it's locked. And "insufficient balance" would mislead them: adding money wouldn't fix it, since deposits are blocked too.
  - **Collect all errors and report them together:** useful in forms (M15 validation), but overkill here. One clear reason per failure is enough.
- **Why:** The checks go from the cheapest and most general to the most specific:
  - **Amount** depends only on the input. It needs no account state, and a bad amount is a *programming or input mistake* (that's why it's unchecked). That's a bug to report first, wherever it happens.
  - **Frozen** depends on the account's *state*, not the amount. If the account is frozen, no valid amount can succeed, so there's no point comparing balances.
  - **Balance** needs a *valid* amount (you can't compute a shortfall from null or −100) and an *active* account, so it has to come last.

  **Key rule:** every check runs *before* `balance` is changed or `recordTransaction` is called. A thrown exception stops the method on that line, so a failed operation changes nothing: no balance change and no history entry. Done-when check 12 (3 history entries) proves this.

### D3 — Which exceptions are checked and which are unchecked
- **Date:** 27/11/2026
- **Decision:**
  - **Checked** (`WalletException` and subclasses): `InsufficientBalanceException`, `AccountNotFoundException`, `AccountFrozenException`, `DailyLimitExceededException`
  - **Unchecked** (`RuntimeException`): `InvalidAmountException`, and Java's own `IllegalArgumentException` for adding a duplicate account
- **Alternatives considered:**
  - **Everything checked:** every `deposit` call would have to handle `InvalidAmountException`, even where the amount is a constant like `new BigDecimal("500")` that can never be invalid. The `try`/`catch` blocks would be noise, and people start writing empty `catch {}`s to silence the compiler.
  - **Everything unchecked** (the style of Spring and many modern libraries): less code, but the compiler would no longer remind anyone that a withdrawal can fail for lack of money, and the UI could forget to show that case.
- **Why:** The test is **"whose fault is it, and can the caller recover?"**
  - **Checked = a normal business situation** that a correct program will meet at runtime: a customer really can be short of money, type a wrong account number, or use a frozen account. The caller *must* plan for it (show a message, ask again), so the compiler enforces handling.
  - **Unchecked = a programming mistake** that a correct program never makes: passing `null` or −100 to `deposit`, or registering the same account twice. The fix is to correct the calling code (or validate input earlier, in the UI in M15), not to catch the exception.
  - All checked ones share the parent `WalletException`, so a caller that doesn't care about the details can catch everything with one `catch (WalletException e)`. One that does care can catch the specific subclass first.

### D4 — WalletService finds the account, Account enforces the rules
- **Date:** 27/11/2026
- **Decision:** `WalletService` keeps all accounts in a `HashMap<String, Account>` keyed by account number. Each of its methods only (1) looks up the account with `findAccount`, which throws `AccountNotFoundException` if it's missing, and (2) **delegates** to the account's own method. It contains no amount, frozen or balance checks and catches nothing; checked exceptions propagate to the caller through its `throws` clauses.
- **Alternatives considered:**
  - **A `List<Account>` searched with a loop:** works, but every lookup checks accounts one by one (O(n)), and duplicates aren't easy to detect. A `HashMap` gives O(1) lookup by key and `containsKey` for the duplicate check.
  - **Putting the rules in the service** (`if (account.isFrozen()) throw …` inside `WalletService`): then the rules exist in two places, and anyone calling `account.withdraw()` directly would bypass them.
  - **Catching and printing exceptions inside the service:** the service doesn't know who's calling. A console should print, a JavaFX screen should show an alert (M15). Catching here would hide the failure from the caller.
- **Why:** Each class has one job. `Account` owns its data **and** the rules that protect it (encapsulation), so they can never be skipped. `WalletService` is the single entry point the rest of the app uses: Main now, the UI later, transfers in M3. Keeping it thin means M3 (transfers) and M11 (database) only add new behaviour there, without touching the rules.

### D5 — A transfer checks everything that can fail before any money moves
- **Date:** 27/11/2026
- **Decision:** `WalletService.transfer(from, to, amount)` runs in this order:
  1. validate the amount (`Validator`)
  2. find both accounts
  3. reject a transfer to the same account (`fromNo.equals(toNo)`)
  4. reject a **frozen destination**
  5. `from.transferOut(...)`: source frozen → daily limit → funds → debit
  6. `to.transferIn(...)`: credit

  After step 5 takes the money, **nothing is allowed to fail**. Every check that could fail has already run.
- **Alternatives considered:**
  - **No step 4, relying on `transferIn`'s own frozen check:** the check would still happen, but too late. Transferring ₹100 from S001 into a frozen S002: `transferOut` succeeds and S001 drops by ₹100, then `transferIn` throws `AccountFrozenException`. The exception propagates out of `transfer`, S002 never receives the money, and **₹100 has vanished**. Done-when check 5 proves this doesn't happen: S001 is still 50000.00.
  - **Undo on failure** (catch the exception after the debit and put the money back with another `transferIn`): works for this case, but the undo itself could fail, and the history would show a TRANSFER_OUT that never really happened. Preventing the problem is simpler than repairing it.
  - **Implement transfer as `withdraw()` + `deposit()`:** the history would say WITHDRAWAL/DEPOSIT with no counterparty, so nobody could tell where the money went, and withdrawals don't count towards the daily limit, so the limit could be bypassed.
- **Why:** The rule for M3 is that **money is never created or lost: either both accounts change, or neither does**. Since the debit and the credit are two separate steps, the only safe design in plain Java is to make the second step unable to fail. Step 4 is the one check that belongs to the *destination* but has to run *before* the debit, which is why it lives in `WalletService` and not only in `Account`. Done-when check 11 confirms that the total of all balances (67000.00) is the same before and after.
- **What this still doesn't protect against:** a failure **outside** Java's exceptions, such as the program crashing, the power going off, or the process being killed between step 5 and step 6. S001 would already be debited in memory (and later in the database), and the credit would never happen. Ordering checks can't fix that. It needs a real **transaction**: in M12 the transfer runs inside a database transaction with `setAutoCommit(false)`, and either `commit()` saves both updates together or `rollback()` cancels both (ACID atomicity).

---

## Experiments

<!-- Template for new experiments:
### E? — <what you tried>
- **Date:**
- **What I expected:**
- **What actually happened:**
- **What I learned:**
-->

### E1 — Adding 0.1 + 0.2 with double vs BigDecimal
- **Date:** 26/11/2026
- **What I expected:** `0.3` from all three lines.
- **What actually happened:**
  ```
  0.1 + 0.2                                        → 0.30000000000000004
  new BigDecimal("0.1").add(new BigDecimal("0.2")) → 0.3
  new BigDecimal(0.1)                              → 0.1000000000000000055511151231257827021181583404541015625
  ```
- **What I learned:** `double` stores numbers in binary, and 0.1 has no exact binary form, so tiny errors appear even in simple sums. That's why PayVault uses `BigDecimal` for all money. The third line shows that `BigDecimal` only helps if it's built from a **String**: the `double` constructor copies the already-inaccurate binary value. So money values are always written as `new BigDecimal("...")`.

### E2 — Depositing 10.500 shows a balance of 5010.500
- **Date:** 27/11/2026
- **What I expected:** S001 (opened with 5000) would show `5010.50` after depositing `10.500`, since `10.500` and `10.50` are the same amount.
- **What actually happened:**
  ```
  balance after deposit 10.500        → 5010.500
  withdraw 4500 → shortfall message   → short by ₹489.500
  ```
- **What I learned:** A `BigDecimal` stores two things: the digits and a **scale** (the number of digits after the point). `10.500` has scale 3, and `add`/`subtract` keep the larger scale, so the 3 decimals spread into the balance and everything computed from it. It's the same reason `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` is `false` (different scales) while `compareTo` returns 0 (same value). Fix: after `validateAmount` has guaranteed ≤ 2 real decimals, normalise the amount once with `setScale(2)`. That can't lose digits, so no `RoundingMode` is needed, and every balance and transaction stays at exactly 2 decimals. **Fixed (27/11/2026):** `deposit` and `withdraw` call `amount = amount.setScale(2)` right after `validateAmount`, and the constructor stores `openingBalance.setScale(2)` as the balance (and records that same value in the opening transaction). Running `Main` now gives `5010.50`, `489.50` and `-5000.00`.

Planned experiments:
- [ ] Remove `serialVersionUID`, change the class, load old data → what happens?
- [ ] Add a `ConsoleHandler` without `setUseParentHandlers(false)` → what happens?
- [ ] Force a failure mid-transfer with and without `setAutoCommit(false)` → compare the balances
- [x] Use `double` instead of `BigDecimal` for `0.1 + 0.2` → what's printed? (E1)

---

## Bugs

<!-- Template for new bugs:
### B? — <symptom>
- **Date:**
- **Symptom:**
- **Cause:**
- **Fix:**
-->

### B1 — IntelliJ reports "cannot find symbol: class TransactionType"
- **Date:** 26/11/2026
- **Symptom:** After changing `withdraw()` to return `void`, IntelliJ's build failed with 18–20 errors, including *cannot find symbol: class TransactionType* in `Transaction.java` and `Account.java`. Those files hadn't changed, and `TransactionType.java` was still there.
- **Cause:** Not a code problem. The only real errors were in `Main` (it still used `withdraw()`'s old `boolean` result). IntelliJ builds incrementally and caches compiled classes; when part of a build fails, its cache can go stale and report unrelated classes as missing. `mvn clean compile` (which always starts from scratch) showed only the real `Main` errors.
- **Fix:** Fixed the real errors in `Main` (commented out the old `boolean` checks, added `throws WalletException` to `main`), then **Build → Rebuild Project** to throw away IntelliJ's cache. **Lesson:** when the IDE's errors don't make sense, compare with a clean Maven build: Maven's errors are the ones to trust. *(Happened again on 27/11 with 91 errors during M3; the same fix worked.)*

### B2 — Received transfers counted towards the receiver's daily limit
- **Date:** 27/11/2026
- **Symptom:** In `runM3Checks`, check 13 printed `C001 sent today: 54000.00` although C001 never sent anything; it only **received** three transfers. C001's history showed `TRANSFER_OUT [S001]` for money coming **in**.
- **Cause:** `Account.transferIn` was written by copying `transferOut`, and the transaction type was never changed: it recorded `TransactionType.TRANSFER_OUT`. `getTransferredToday()` adds up every `TRANSFER_OUT` from today, so it counted the received money as sent. Real impact: C001 would have been blocked from sending its own money by the limit.
- **Fix:** `transferIn` records `TransactionType.TRANSFER_IN`. Check 13 now prints `0.00`, and C001's history shows `TRANSFER_IN`. **Lesson:** copy-pasted code is where bugs hide. After copying a method, check every line that should differ. And a test that checks a value you *don't* expect to change (check 13) catches bugs that the obvious tests miss.

### B3 — The daily limit printed as ₹50000 instead of ₹50000.00
- **Date:** 27/11/2026
- **Symptom:** The `DailyLimitExceededException` message said `Daily transfer limit ₹50000 exceeded`, without the `.00` that every other amount has.
- **Cause:** The constant was created with the **`double`** constructor: `new BigDecimal(50000.00)`. To Java, `50000.00` is just the `double` `50000.0`, so the decimals written in the source are lost, and the BigDecimal becomes `50000` (scale 0). It happens to be exact for 50000, but with a value like `0.1` the `double` constructor gives the inaccurate `0.1000000000000000055511…`, exactly as E1 showed.
- **Fix:** `new BigDecimal("50000.00")`, using the **String** constructor, which keeps exactly the digits written. **Lesson:** the E1 rule applies to constants too: a `BigDecimal` is always created from a `String`.
